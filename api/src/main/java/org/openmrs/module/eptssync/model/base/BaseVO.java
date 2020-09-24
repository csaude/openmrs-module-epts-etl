package org.openmrs.module.eptssync.model.base;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

/**
 * Base class for all value objects. Provides utility function load(...) to fill
 * fields.
 *
 */
public abstract class BaseVO  implements VO{
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	/**
	 * Data da criacao
	 */
	protected Date creationDate;
	
	protected boolean excluded;
	/**
	 * Cria uma instancia de {@link BaseVO} com os atributos iniciados com
	 * valores <code>default</code>
	 */
	public BaseVO() {
	}

	/**
	 * Constroi uma nova instancia de {@link BaseVO} com os valores dos
	 * atributos recuperados a partir de um {@link ResultSet}
	 * 
	 * @param resultSet
	 *            contendo os valores para a inicializa��o dos atributos do
	 *            registo
	 * 
	 * @see #load(List)
	 */
	public BaseVO(ResultSet resultSet) {
		try {
			load(resultSet);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return O valor do atributo {@link #creationDate}
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Modifica o valor do atributo {@link #creationDate} para o valor fornecido
	 * pelo parametro <code>creationDate</code>
	 * 
	 * @param creationDate
	 *            Novo valor para o atributo {@link #creationDate}
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Converte o atributo para uma coluna na BD Por exemplo: imageFile =>
	 * image_file
	 * 
	 * @param fieldName
	 *            nome do atributo
	 * 
	 * @return nome da coluna correspondente ao atributo
	 */
	private static String toColumnName(String fieldName) {
		return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}

	/**
	 * COnverte o nome de uma classe java (classe VO) para nome de uma
	 * correspondente tabela na BD. Por exemplo: RigthTypeVO => rigth_type
	 * 
	 * O nome gerado n�o ir� incluir a sufixo VO (caso exista)
	 * 
	 * @param classeName
	 *            nome da classe
	 * 
	 * @return nome da tabela gerado
	 * 
	 * @throws OperacaoProibidaException
	 *             se o nome da classe n�o tiver o sufixo <code>"VO"</code>
	 */
	protected static String toTableName(String classeName) {
		// Verificar se tem sufixo VO

		if (classeName.length() < 3 || !classeName.substring(classeName.length() - 2, classeName.length()).equals("VO"))
			throw new ForbiddenOperationException(
					"Nome da classe nao suportado. Deve passar uma classe com terminacao VO");

		String classeNameWithout_VO = classeName.substring(0, classeName.length() - 2);

		return classeNameWithout_VO.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}

	/**
	 * Retorna o nome da tabela correspodente a esta classe. Por exemplo: Se o
	 * nome da classe For PedidoPareceVO retornará pedido_parecer
	 * 
	 * Note que se a classe VO tiver um nome cujo nome da tabela nao concide com
	 * o nome gerado por este m�todo, ent�o a classe em causa dever� reescrever
	 * o seu pr�prio m�todo
	 * 
	 * @author JPBOANE
	 */
	public String generateTableName() {
		return toTableName(getClass().getSimpleName());
	}

	/**
	 * Retorna todos os atributos de inst�ncia desta classe independentemento do
	 * modificador de acesso
	 * 
	 * @return todos os atributos de inst�ncia desta classe independentemento do
	 *         modificador de acesso
	 */
	private Object[] getFields() {
		return getFields(this);
	}
	
	public boolean isExcluded() {
		return excluded;
	}
	
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}

	/**
	 * Retorna todos os atributos de inst�ncia de da classe de um objecto
	 * independentemento do modificador de acesso
	 * 
	 * @return todos os atributos de inst�ncia de da classe de um objecto
	 *         independentemento do modificador de acesso
	 */
	public static Object[] getFields(Object obj) {
		List<Object> fields = new ArrayList<Object>();
		Class<?> cl = obj.getClass();
		while (cl != null) {
			Field[] in = cl.getDeclaredFields();
			for (int i = 0; i < in.length; i++) {
				Field field = in[i];
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				field.setAccessible(true);
				fields.add(field);
			}
			cl = cl.getSuperclass();
		}
		return fields.toArray();
	}
	
	/**
	 * Carrega para os atributos deste objecto valores a partir dos
	 * correspondentes campos em um {@link ResultSet}
	 * 
	 * @param resultSet
	 *            contendo os valores a copiar
	 */
	public void load(ResultSet resultSet) throws SQLException{
		Object[] fields = getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = (Field) fields[i];
			String name = toColumnName(field.getName());

			try {
				Object value = resultSet.getObject(name);

				if (value != null) {
					String c = field.getType().getName();

					if (c.equals("int"))
						field.setInt(this, resultSet.getInt(name));
					else if (c.equals("boolean")) {
						int number = resultSet.getInt(name);

						field.setBoolean(this, number > 0);
					} else if (c.equals("double"))
						field.setDouble(this, resultSet.getDouble(name));
					else if (c.equals("float"))
						field.setFloat(this, resultSet.getFloat(name));
					else if (c.equals("long"))
						field.setLong(this, resultSet.getLong(name));
					else if (c.equals("java.lang.String")) {
						/*
						 * JP: 2015.02.18
						 * 
						 * Este codigo foi acrescentado para tratar dos casos em
						 * que o campo que se pretende carregar eh uma String
						 * guardada num campo Clob
						 *
						 */

						field.set(this, resultSet.getString(name).trim());
					} else if (value instanceof Timestamp || value instanceof Date) {

						java.util.Date data = new java.util.Date(resultSet.getTimestamp(name).getTime());
						// System.out.println("TimeStamp =
						// "+rs.getTimestamp(name).getTime());
						field.set(this, data);

					} else if (c.equals("java.io.InputStream")) {

						Blob blob = resultSet.getBlob(name);

						// InputStream input = blob.getBinaryStream();
						// System.out.println("Is blob " + input.available());

						field.set(this, blob.getBinaryStream());

					} else if (c.equals("[B")) {// byte[]
						field.set(this, resultSet.getBytes(name));
					}

					else {
						field.set(this, value);
						/* System.out.println("Unkown type"); */}
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}

	/**
	 * Transforma uma lista de VO's e lista de String
	 * 
	 * @param voToParse
	 * @return
	 */
	public static List<String> toString(List<?> voToParse) {
		List<String> parsed = new ArrayList<String>();

		if (voToParse == null)
			return null;

		for (Object o : voToParse) {
			if (o instanceof BaseVO)
				throw new ClassCastException();

			parsed.add(o.toString());
		}

		return parsed;
	}

	public static <T extends BaseVO> T createInstance(Class<T> classe) {
		try {
			return classe.getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <S extends Object> S generateInstancia(Class<? extends S> classe) {
		S obj;

		try {
			obj = classe.getConstructor().newInstance();
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}

		return obj;
	}

	public boolean checkIfAttExists(String attName) {
		for (Object obj : getFields()) {
			Field field = (Field) obj;

			if (attName.equals(field.getName())) {
				return true;
			}
			;
		}

		for (Method method : this.getClass().getMethods()) {
			if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
				String name = utilities.deCapitalize(method.getName().substring(3));

				if (name.equals(attName))
					return true;
			}
		}

		return false;
	}
}
