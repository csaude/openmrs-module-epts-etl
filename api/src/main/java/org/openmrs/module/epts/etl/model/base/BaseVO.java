package org.openmrs.module.epts.etl.model.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.parseToCSV;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for all value objects. Provides utility function load(...) to fill fields.
 */
public abstract class BaseVO implements VO {
	
	public static parseToCSV utilities = parseToCSV.getInstance();
	
	protected Date dateCreated;
	
	protected Date dateChanged;
	
	protected Date dateVoided;
	
	protected boolean excluded;
	
	protected List<Field> fields;
	
	/**
	 * Cria uma instancia de {@link BaseVO} com os atributos iniciados com valores
	 * <code>default</code>
	 */
	public BaseVO() {
	}
	
	/**
	 * Constroi uma nova instancia de {@link BaseVO} com os valores dos atributos recuperados a
	 * partir de um {@link ResultSet}
	 * 
	 * @param resultSet contendo os valores para a inicializa��o dos atributos do registo
	 * @see #load(List)
	 */
	public BaseVO(ResultSet resultSet) {
		try {
			load(resultSet);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		try {
			this.setDateVoided(resultSet.getDate("date_retired"));
		}
		catch (SQLException e) {}
	}
	
	public Date getDateChanged() {
		return dateChanged;
	}
	
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public Date getDateVoided() {
		return dateVoided;
	}
	
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	/**
	 * Converte o atributo para uma coluna na BD Por exemplo: imageFile => image_file
	 * 
	 * @param fieldName nome do atributo
	 * @return nome da coluna correspondente ao atributo
	 */
	private static String toColumnName(String fieldName) {
		return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}
	
	/**
	 * COnverte o nome de uma classe java (classe VO) para nome de uma correspondente tabela na BD.
	 * Por exemplo: RigthTypeVO => rigth_type O nome gerado n�o ir� incluir a sufixo VO (caso
	 * exista)
	 * 
	 * @param classeName nome da classe
	 * @return nome da tabela gerado
	 * @throws OperacaoProibidaException se o nome da classe n�o tiver o sufixo <code>"VO"</code>
	 */
	protected static String toTableName(String classeName) {
		// Verificar se tem sufixo VO
		
		if (classeName.length() < 3 || !classeName.substring(classeName.length() - 2, classeName.length()).equals("VO"))
			throw new ForbiddenOperationException("Nome da classe nao suportado. Deve passar uma classe com terminacao VO");
		
		String classeNameWithout_VO = classeName.substring(0, classeName.length() - 2);
		
		return classeNameWithout_VO.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}
	
	/**
	 * Retorna o nome da tabela correspodente a esta classe. Por exemplo: Se o nome da classe For
	 * PedidoPareceVO retornará pedido_parecer Note que se a classe VO tiver um nome cujo nome da
	 * tabela nao concide com o nome gerado por este metodo, entao a classe em causa devera
	 * reescrever o seu proprio metodo
	 * 
	 * @author JPBOANE
	 */
	@JsonIgnore
	public String generateTableName() {
		return toTableName(getClass().getSimpleName());
	}
	
	/**
	 * Retorna todos os atributos de instancia desta classe independentemento do modificador de
	 * acesso
	 * 
	 * @return todos os atributos de instancia desta classe independentemento do modificador de
	 *         acesso
	 */
	@JsonIgnore
	public List<java.lang.reflect.Field> getInstanceFields() {
		return utilities.getInstanceFields(this);
	}
	
	@Override
	public List<Field> getFields() {
		if (fields == null)
			generateFields();
		
		return fields;
	}
	
	@Override
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	
	@JsonIgnore
	public boolean isExcluded() {
		return excluded;
	}
	
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}
	
	/**
	 * Carrega para os atributos deste objecto valores a partir dos correspondentes campos em um
	 * {@link ResultSet}
	 * 
	 * @param resultSet contendo os valores a copiar
	 */
	public void load(ResultSet resultSet) throws SQLException {
		for (java.lang.reflect.Field field : this.getInstanceFields()) {
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
					else if (c.equals("Integer"))
						field.setInt(this, resultSet.getInt(name));
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
					} else if (value instanceof Timestamp || value instanceof Date || value instanceof LocalDateTime) {
						
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
			}
			catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}
	
	public static Object retrieveFieldValue(String fieldName, String type, ResultSet resultSet) throws SQLException {
		
		if (utilities.isStringIn(type.toUpperCase(), "INT", "MEDIUMINT", "INT8", "BIGINT", "SERIAL", "SERIAL4"))
			type = "java.lang.Integer";
		else if (utilities.isStringIn(type.toUpperCase(), "TINYINT", "BIT"))
			type = "java.lang.Byte";
		else if (utilities.isStringIn(type.toUpperCase(), "YEAR", "SMALLINT"))
			type = "java.lang.Short";
		else if (utilities.isStringIn(type.toUpperCase(), "BIGINT", "INT8", "SERIAL"))
			type = "java.lang.Long";
		else if (utilities.isStringIn(type.toUpperCase(), "DECIMAL", "NUMERIC", "SMALLINT", "REAL", "DOUBLE"))
			type = "java.lang.Double";
		else if (utilities.isStringIn(type.toUpperCase(), "FLOAT", "NUMERIC", "SMALLINT"))
			type = "java.lang.Float";
		else if (utilities.isStringIn(type.toUpperCase(), "VARCHAR", "CHAR", "TEXT", "MEDIUMTEXT"))
			type = "java.lang.String";
		else if (utilities.isStringIn(type.toUpperCase(), "VARBINARY", "BLOB", "LONGBLOB"))
			type = "[B";
		else if (utilities.isStringIn(type.toUpperCase(), "DATE", "DATETIME", "TIME", "TIMESTAMP"))
			type = "java.util.Date";
		else if (utilities.isStringIn(type.toUpperCase(), "BOOLEAN"))
			type = "java.util.Boolean";
		
		Object value = resultSet.getObject(fieldName);
		
		if (value != null) {
			if (type.equals("java.util.Boolean")) {
				int number = resultSet.getInt(fieldName);
				
				return number > 0;
			} else if (type.equals("java.lang.Double"))
				return resultSet.getDouble(fieldName);
			else if (type.equals("java.lang.Float"))
				return resultSet.getFloat(fieldName);
			else if (type.equals("java.lang.Integer"))
				return resultSet.getInt(fieldName);
			else if (type.equals("java.lang.String")) {
				return resultSet.getString(fieldName).trim();
			} else if (value instanceof Timestamp || value instanceof Date || value instanceof LocalDateTime) {
				
				return new java.util.Date(resultSet.getTimestamp(fieldName).getTime());
				
			} else if (type.equals("java.io.InputStream")) {
				
				Blob blob = resultSet.getBlob(fieldName);
				
				return blob.getBinaryStream();
				
			} else if (type.equals("[B")) {
				return resultSet.getBytes(fieldName);
			}
			
		}
		
		return value;
		
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
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <S extends Object> S generateInstancia(Class<? extends S> classe) {
		S obj;
		
		try {
			obj = classe.getConstructor().newInstance();
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		
		return obj;
	}
	
	public boolean checkIfAttExists(String attName) {
		for (Object obj : getInstanceFields()) {
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
