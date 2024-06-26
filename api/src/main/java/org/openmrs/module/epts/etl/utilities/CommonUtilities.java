package org.openmrs.module.epts.etl.utilities;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.base.VO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CommonUtilities implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected static CommonUtilities utilities;
	
	protected CommonUtilities() {
		//if (utilities != null) throw new OperacaoProibidaException("Ja foi criada uma instancia Utilitarios! Use o metodo Utilitarios.getInstance()!!!!");
	}
	
	public static synchronized CommonUtilities getInstance() {
		if (utilities == null)
			utilities = new CommonUtilities();
		
		return utilities;
	}
	
	public String removeNewline(String str) {
		return str.replaceAll("[\\r\\n]+$", "");
	}
	
	public String garantirXCaracterOnNumber(long number, int x) {
		return FuncoesGenericas.garantirXCaracterOnNumber(number, x);
	}
	
	public String garantirXCaracteres(String str, int x) {
		if (!stringHasValue(str) || str.length() <= x)
			return str;
		
		return str.substring(0, x) + "...";
	}
	
	public String addAtributeToValidationString(String currentString, long field, String scapeStr) {
		return addAtributeToValidationString(currentString, "" + field, scapeStr);
	}
	
	public String addAtributeToValidationString(String currentString, char field, String scapeStr) {
		return addAtributeToValidationString(currentString, "" + field, scapeStr);
	}
	
	public String addAtributeToValidationString(String currentString, Object field, String scapeStr) {
		return addAtributeToValidationString(currentString, "" + field, scapeStr);
	}
	
	/**
	 * Concatena duas strings com String de separacao
	 * 
	 * @param currentString
	 * @param field
	 * @scapeStr Caracteres de escape, por exemplo "\n" "," "<br>
	 *           "
	 * @return
	 */
	public String concatStringsWithSeparator(String currentString, String toConcant, String scapeStr) {
		return FuncoesGenericas.concatStringsWithSeparator(currentString, toConcant, scapeStr);
	}
	
	public boolean isStringStartWithAlphabeticalChar(String str) {
		return FuncoesGenericas.isStringStartWithAlphabeticalChar(str);
	}
	
	public List<?> parseObjectToList(Object obj) {
		return FuncoesGenericas.parseObjectToList(obj);
	}
	
	public <T> List<T> parseObjectToList_(Object obj, Class<T> objClass_) {
		if (obj == null)
			return null;
		
		List<Object> list = new ArrayList<Object>();
		
		list.add(obj);
		
		return parseList(list, objClass_);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T[] parseObjectToArray(T... objts) {
		T[] array = (T[]) Array.newInstance(objts[0].getClass(), objts.length);
		
		for (int i = 0; i < objts.length; i++) {
			array[i] = objts[i];
		}
		
		return array;
	}
	
	/**
	 * Verifica se a String 'value' encontra se na lista especificada em 'inValue'. Nao ignora o a
	 * caixa ('case'), Case sensitive
	 * 
	 * @param value
	 * @param inValue
	 * @return
	 */
	public boolean isStringIn(String value, String... inValues) {
		return FuncoesGenericas.isStringIn(value, inValues);
	}
	
	public Character parseToCharacter(char chr) {
		return new Character(chr);
	}
	
	public Character[] parseToCharacter(char... toParse) {
		Character[] parsed = new Character[toParse.length];
		
		int i = 0;
		
		for (char chr : toParse) {
			parsed[i] = parseToCharacter(chr);
			i++;
		}
		
		return parsed;
	}
	
	/**
	 * Verifica se uma determinada {@link List} contem todos os elementos passados pelo parametro
	 * 
	 * @param list lista cujo elementos se pretende verificar
	 * @param o elementos a verificar
	 * @return true se a lista contem todos os elementos ou false no caso contrario
	 */
	public <T> boolean containsAll(List<T> list, @SuppressWarnings("unchecked") T... o) {
		for (T t : o) {
			if (!list.contains(t))
				return false;
		}
		
		return true;
	}
	
	public String concatStrings(String value, String... inValues) {
		return FuncoesGenericas.concatStrings(value, inValues);
	}
	
	/**
	 * @param qtdObjects
	 * @param params
	 * @param p
	 * @return Insere um novo objecto num array de Objectos e retorna este ultimo
	 */
	public Object[] addToParams(int qtdObjects, Object[] paramsCorrentes, Object paramAAdicionar) {
		return FuncoesGenericas.setParam(qtdObjects, paramsCorrentes, paramAAdicionar);
	}
	
	public Object[] setParam(Object[] paramsCorrentes, Object[] paramAAdicionar) {
		return FuncoesGenericas.setParam(paramsCorrentes, paramAAdicionar);
	}
	
	public void printParams(Object[] params) {
		FuncoesGenericas.printParams(params);
	}
	
	public String[] trimAll(String[] params) {
		return FuncoesGenericas.trimAll(params);
	}
	
	public void printParams(List<?> params) {
		FuncoesGenericas.printParams(params);
	}
	
	/**
	 * Remove a primeira ocorrencia do caracter espa�o
	 * 
	 * @param text
	 * @return
	 */
	public String removeFirstEmptyCharacter(String text) {
		return FuncoesGenericas.removeFirstEmptyCharacter(text);
	}
	
	public String removeAllEmptySpace(String text) {
		return FuncoesGenericas.removeAllEmptySpace(text);
	}
	
	public String replaceAllEmptySpace(String text, char replacement) {
		return FuncoesGenericas.replaceAllEmptySpace(text, replacement);
	}
	
	/**
	 * Remove todas as ocorrencias de espa�o superiores a 1
	 * 
	 * @param text
	 * @return
	 */
	public String removeDuplicatedEmptySpace(String text) {
		return FuncoesGenericas.removeDuplicatedEmptySpace(text);
	}
	
	/**
	 * Forca a aproximacao por excesso do numero recebido pelo parametro
	 * 
	 * @param numero: Valor numerico (inteiro ou decimal)
	 * @return
	 */
	public long forcarAproximacaoPorExcesso(String numero) {
		return FuncoesGenericas.forcarAproximacaoPorExcesso(numero);
	}
	
	/**
	 * Forca a aproximacao por excesso do numero recebido pelo parametro
	 * 
	 * @param numero: Valor numerico (inteiro ou decimal)
	 * @return
	 */
	public long forcarAproximacaoPorExcesso(double numero) {
		return FuncoesGenericas.forcarAproximacaoPorExcesso("" + numero);
	}
	
	public String getNumberInXPrecision(double number, int precision) {
		return FuncoesGenericas.getNumberInXPrecision(number, precision);
	}
	
	public String formatDateToDDMMYYYY(Date date) {
		return DateAndTimeUtilities.formatToDDMMYYYY(date);
	}
	
	public String formatDateToDDMMYYYY_HHMISS(Date date) {
		return DateAndTimeUtilities.formatToDDMMYYYY_HHMISS(date);
	}
	
	/**
	 * Faz o Split de um double, dividindo-o em duas partes atraves do ponto ou virgula
	 * 
	 * @param numero
	 * @return
	 */
	public long[] splitDoubleNumber(String numero) {
		return FuncoesGenericas.splitDoubleNumber(numero);
	}
	
	/**
	 * Gera o selfId concatenando o id da reparticao (prefixo) com o numero sequencial.
	 * 
	 * @param numSequencial
	 * @param idReparticao
	 * @return
	 */
	public long generateSelfId(long numSequencial, long idReparticao) {
		return FuncoesGenericas.generateSelfId(numSequencial, idReparticao);
	}
	
	public UUID generateUUID() {
		return UUID.randomUUID();
	}
	
	/**
	 * Verifica se o selfId tem a quantidade permitida de digitos e possui o mesmo prefixo que o
	 * reparicaoId
	 * 
	 * @param selfId
	 * @param reparicaoId: reparticao onde foi gerado o id
	 * @param qtdDigitos: Quantidade de digitos permitidos
	 * @return
	 */
	public boolean isValidSelfId(long selfId, int qtdDigitos, long reparicaoId) {
		return FuncoesGenericas.isValidSelfId(selfId, qtdDigitos, reparicaoId);
	}
	
	/**
	 * Verifica se o selfId tem a quantidade permitida de digitos
	 * 
	 * @param selfId
	 * @param reparicaoId: reparticao onde foi gerado o id
	 * @param qtdDigitos: Quantidade de digitos permitidos
	 * @return
	 */
	public boolean isValidSelfId(long selfId, int qtdDigitos) {
		return FuncoesGenericas.isValidSelfId(selfId, qtdDigitos);
	}
	
	/**
	 * Transforma uma String para o formato de elemento de um array multi-dimensional. Exemplo:
	 * String s = "name.parent"; s = parseStringToArgArray(s);//s=[name][parent]
	 * 
	 * @param strToParse
	 * @return
	 */
	public String parseStringToArgArray(String strToParse) {
		return FuncoesGenericas.parseStringToArgArray(strToParse);
	}
	
	/**
	 * Transforma uma String para um array de double. Exemplo: String s = "14.3,11.4"; double[] d =
	 * parseStringToArgArray(s, ",");//d=[14.3][11.4]
	 * 
	 * @param strToParse
	 * @param separator
	 * @return
	 */
	public double[] parseStringToArrayOfDouble(String strToParse, String separator) {
		return FuncoesGenericas.parseStringToArrayOfDouble(strToParse, separator);
	}
	
	public String parseArrayToString(String[] array) {
		return FuncoesGenericas.parseArrayToString(array);
	}
	
	/**
	 * Convert uma lista de objectos em lista
	 * 
	 * @param obj
	 * @return
	 */
	public <T> List<T> parseToList(@SuppressWarnings("unchecked") T... obj) {
		return FuncoesGenericas.parseToList(obj);
	}
	
	public <T> List<T> parseArrayToList(T[] array) {
		return FuncoesGenericas.parseArrayToList(array);
	}
	
	public List<Integer> fillListByInts(int start, int end) {
		return FuncoesGenericas.fillListByInts(start, end);
	}
	
	public <T> boolean existOnArray(List<T> list, T obj) {
		return FuncoesGenericas.findOnArray(list, obj) != null;
	}
	
	public <T> boolean existOnArray(T[] array, T obj) {
		return FuncoesGenericas.findOnArray(array, obj) != null;
	}
	
	public <T> int countQtdOcorrenciaDoRegistoOnList(List<T> list, T obj) {
		return FuncoesGenericas.countQtdOcorrenciaDoRegistoOnList(list, obj);
	}
	
	public int arraySize(List<?> list) {
		return FuncoesGenericas.arraySize(list);
	}
	
	public boolean arrayHasExactlyOneElement(List<?> list) {
		return FuncoesGenericas.arrayHasExactlyOneElement(list);
	}
	
	public boolean arrayHasMoreThanOneElements(List<?> list) {
		return FuncoesGenericas.arrayHasMoreThanOneElements(list);
	}
	
	public <T> T getFirstRecordOnArray(List<T> list) {
		return FuncoesGenericas.getFirstRecordOnArray(list);
	}
	
	public <T> T getLastRecordOnArray(List<T> list) {
		return FuncoesGenericas.getLastRecordOnArray(list);
	}
	
	/**
	 * Fun��o auxiliar usada para gerar o id do checkbox na tag listcheckbox.tag
	 * 
	 * @return
	 */
	public String generateIdForCheckBox(String name, int pos) {
		return FuncoesGenericas.generateIdForCheckBox(name, pos);
	}
	
	/**
	 * Concatena duas condicoes dump
	 * 
	 * @param condition
	 * @param otherCondition
	 * @return Condicao SQL composta pela condi��o inicial concatenada a nova condi��o
	 */
	public String concatCondition(String condition, String otherCondition) {
		return FuncoesGenericas.concatCondition(condition, otherCondition);
	}
	
	/**
	 * Concatena duas condicoes dump
	 * 
	 * @param condition
	 * @param otherCondition
	 * @return Condicao SQL composta pela condi��o inicial concatenada a nova condi��o
	 */
	public String concatCondition(String condition, String otherCondition, String connector) {
		return FuncoesGenericas.concatCondition(condition, otherCondition, connector);
	}
	
	public String generateCommaSeparetedNumber(double number) {
		return FuncoesGenericas.generateCommaSeparetedNumber(number);
	}
	
	/**
	 * /** Verifica se uma determinada String � num�rica ou n�o
	 * 
	 * @param str
	 * @return
	 */
	public boolean isNumeric(String str) {
		return FuncoesGenericas.isNumeric(str);
	}
	
	/**
	 * Permite formatar um numero para a visualizacao
	 * 
	 * @return
	 */
	public String displayNumberOnStringFormat(double number) {
		return FuncoesGenericas.displayNumberOnStringFormat(number);
	}
	
	/**
	 * Permite formatar um numero para a visualizacao
	 * 
	 * @return
	 */
	public String displayDoubleOnIntegerFormat(double number) {
		return FuncoesGenericas.displayDoubleOnIntegerFormat(number);
	}
	
	public double dateDiff(String dataMaior, String dataMenor, String format) throws ParseException {
		return FuncoesGenericas.dateDiff(dataMaior, dataMenor, format);
	}
	
	public double calculaIdade(String dataMenor, String format) throws ParseException {
		return FuncoesGenericas.calculaIdade(dataMenor, format);
	}
	
	public double calculaSomaNPrimeirosTermosSerieHarmonica(int n) {
		double soma = 0;
		
		for (int i = 1; i <= n; i++) {
			double j = i;
			
			soma += 1 / j;
		}
		
		return soma;
	}
	
	public boolean stringHasValue(String string) {
		return FuncoesGenericas.stringHasValue(string);
	}
	
	public boolean arrayHasElement(List<?> list) {
		return FuncoesGenericas.arrayHasElement(list);
	}
	
	public boolean arrayHasNoElement(List<?> list) {
		return !arrayHasElement(list);
	}
	
	public boolean arrayHasElement(ArrayList<?> list) {
		return FuncoesGenericas.arrayHasElement(list);
	}
	
	public boolean arrayHasElement(Object[] array) {
		return array != null && array.length > 0;
	}
	
	public int getPosOfElementOnList(List<?> list, Object toFind) {
		if (!arrayHasElement(list))
			return -1;
		
		return list.indexOf(toFind);
	}
	
	public <T> T findOnList(List<T> list, Object toFind) {
		if (!arrayHasElement(list))
			return null;
		
		int pos = getPosOfElementOnList(list, toFind);
		
		return pos >= 0 ? list.get(pos) : null;
	}
	
	public <T> int getPosOnArray(T[] array, T toFind) {
		return FuncoesGenericas.getPosOnArray(array, toFind);
	}
	
	public String valorPorExtenso(double num) {
		return FuncoesGenericas.valorPorExtenso(num);
	}
	
	public ArrayList<String> parseToList(String[] array) {
		return FuncoesGenericas.parseToList(array);
	}
	
	/**
	 * Procura um elemento na lista passada pelo parametro e retorna a posicao deste na lista. Se o
	 * elemento nao existir na lista, retorna -1;
	 * 
	 * @param list [Lista contendo todos os elementos]
	 * @param toFind
	 * @return posicao do elemento 'toFind' na lista 'list' ou -1 se o elemento nao estiver na lista
	 */
	public int getPosOnArray(List<?> list, Object toFind) throws NoSuchElementException {
		if (list == null)
			return -1;
		
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).equals(toFind))
				return i;
			
		throw new NoSuchElementException("O registo não foi encontrado");
	}
	
	public void removeOnArray(List<?> list, Object toRemove) throws NoSuchElementException {
		if (!FuncoesGenericas.arrayHasElement(list))
			throw new NoSuchElementException();
		int pos = getPosOnArray(list, toRemove);
		
		list.remove(pos);
	}
	
	public void removeOnArray(List<?> list, List<?> toRemove) throws NoSuchElementException {
		if (toRemove == null)
			return;
		
		for (Object obj : toRemove) {
			int pos = getPosOnArray(list, obj);
			list.remove(pos);
		}
	}
	
	public <E> void updateOnArray(List<E> list, E toUpdate, E newReg) throws NoSuchElementException {
		int pos = getPosOnArray(list, toUpdate);
		
		list.set(pos, newReg);
	}
	
	/**
	 * Localiza um objecto de uma {@link List}
	 * 
	 * @param list da qual se pretende recuperar o registo
	 * @param toFind registo a recuperar
	 * @return registo recuperado
	 */
	public <T> T findOnArray(List<T> list, Object toFind) {
		if (list == null || toFind == null)
			return null;
		
		for (T current : list)
			if (toFind.equals(current))
				return current;
			
		return null;
	}
	
	public <T extends Comparable<? super T>> T getMaxOnList(List<T> list) {
		if (!arrayHasElement(list))
			return null;
		
		Collections.sort(list);
		
		return list.get(list.size() - 1);
	}
	
	/**
	 * Converte valores clobs para String
	 * 
	 * @param data
	 * @return
	 * @throws RuntimeException: se ocorrer uma SQLException ou IOException
	 */
	public String convertClobToString(java.sql.Clob data) throws RuntimeException {
		return FuncoesGenericas.convertClobToString(data);
	}
	
	/**
	 * @param data
	 * @param conn
	 * @return
	 * @throws RuntimeException: se ocorrer um SQLException
	 */
	public Clob convertStringToClob(String data, Connection conn) throws RuntimeException, SQLException {
		return FuncoesGenericas.convertStringToClob(data, conn);
	}
	
	/**
	 * Retorna o caracter na pos-esima posicao no alfabeto portugues A primeira posicao � a posi��o
	 * 1
	 * 
	 * @param pos
	 * @return
	 */
	public char getAlphabeticalCharAt(int pos) {
		return FuncoesGenericas.getAlphabeticalCharAt(pos);
	}
	
	/**
	 * Retorna o caracter na pos-esima posicao no alfabeto portugues A primeira posicao � a posi��o
	 * 1
	 * 
	 * @param pos
	 * @return
	 */
	public int getPosAlphabeticalChar(char chr) {
		return FuncoesGenericas.getPosAlphabeticalChar(chr);
	}
	
	public String getFirstCharactersOfString(String originString) {
		originString = originString.toUpperCase();
		
		String destinationString = "" + originString.charAt(0);
		
		for (int i = 1; i < originString.length() - 1; i++) {
			if (originString.charAt(i) == ' ') {
				destinationString += originString.charAt(i + 1);
			}
		}
		
		return destinationString;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Object, S extends Object> List<S> parseList(List<T> list, Class<S> classe) {
		if (list == null)
			return null;
		
		List<S> parsedList = new ArrayList<S>();
		
		for (T t : list) {
			parsedList.add((S) t);
		}
		
		return parsedList;
	}
	
	public <T extends Object> T[] parseListToArray(List<T> list) {
		if (!arrayHasElement(list))
			return null;
		
		@SuppressWarnings("unchecked")
		T[] parsedList = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
		
		for (int i = 0; i < list.size(); i++) {
			parsedList[i] = list.get(i);
		}
		
		return parsedList;
	}
	
	public <T> List<T> cloneList(List<T> list) {
		if (list == null)
			return null;
		
		List<T> parsedList = null;
		
		if (list instanceof ArrayList)
			parsedList = new ArrayList<T>();
		else if (list instanceof LinkedList)
			parsedList = new LinkedList<T>();
		else if (list instanceof CopyOnWriteArrayList)
			parsedList = new CopyOnWriteArrayList<T>();
		else if (list instanceof Stack)
			parsedList = new Stack<T>();
		else if (list instanceof Vector)
			parsedList = new Vector<T>();
		else if (list instanceof CopyOnWriteArrayList)
			parsedList = new CopyOnWriteArrayList<T>();
		else
			throw new IllegalArgumentException("List not suppotted");
		
		for (T t : list) {
			parsedList.add((T) t);
		}
		
		return parsedList;
	}
	
	public double max(double... num) {
		return FuncoesGenericas.max(num);
	}
	
	public double min(double... num) {
		return FuncoesGenericas.min(num);
	}
	
	public <T> List<T> copyList(List<T> list, int start, int end) {
		if (list == null)
			return null;
		
		List<T> parsedList = null;
		
		if (list instanceof ArrayList)
			parsedList = new ArrayList<T>();
		else if (list instanceof LinkedList)
			parsedList = new LinkedList<T>();
		else if (list instanceof CopyOnWriteArrayList)
			parsedList = new CopyOnWriteArrayList<T>();
		else if (list instanceof Stack)
			parsedList = new Stack<T>();
		else if (list instanceof Vector)
			parsedList = new Vector<T>();
		else if (list instanceof CopyOnWriteArrayList)
			parsedList = new CopyOnWriteArrayList<T>();
		else
			throw new IllegalArgumentException("List not suppotted");
		
		for (int i = start; i <= end; i++) {
			parsedList.add((T) list.get(i));
		}
		
		return parsedList;
	}
	
	public boolean isBetween(double number, double interval_1, double interval_N) {
		return FuncoesGenericas.isBetween(number, interval_1, interval_N);
	}
	
	public boolean isValidAno(int ano) {
		return DateAndTimeUtilities.isValidAno(ano);
	}
	
	public boolean isValidAno(int ano, int minAno, int maxAno) {
		return DateAndTimeUtilities.isValidAno(ano, minAno, maxAno);
	}
	
	public String formatDateToYYYY(Date date) {
		return "" + DateAndTimeUtilities.getYear(date);
	}
	
	public int extractYear(Date date) {
		return DateAndTimeUtilities.getYear(date);
	}
	
	public int extractHours(Date date) {
		return DateAndTimeUtilities.getHours(date);
	}
	
	public int extractMinutes(Date date) {
		return DateAndTimeUtilities.getMinutes(date);
	}
	
	public Date getCurrentDate() {
		return DateAndTimeUtilities.getCurrentDate();
	}
	
	public int getCurrentYear() {
		return extractYear(DateAndTimeUtilities.getCurrentDate());
	}
	
	public String capitalize(String str) {
		if (str.startsWith("unknown")) {
			System.out.println("Stop");
		}
		
		if (!stringHasValue(str))
			return str;
		
		return str.toUpperCase().charAt(0) + str.toLowerCase().substring(1);
	}
	
	public String deCapitalize(String str) {
		if (!stringHasValue(str))
			return str;
		
		return str.toLowerCase().charAt(0) + str.substring(1);
	}
	
	/**
	 * Converte um Json em objecto
	 * 
	 * @param clazz Classe do objecto
	 * @param json a converter
	 * @return objecto convertido
	 */
	public <T> T loadObjectFormJSON(Class<T> clazz, String json) {
		try {
			return new ObjectMapperProvider().getContext(clazz).readValue(json, clazz);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Converte um objecto em Json
	 * 
	 * @param objecto a converter
	 * @return O JSON correspondente a este objecto
	 */
	public String parseToJSON(Object objecto) {
		try {
			return new ObjectMapperProvider().getContext(objecto.getClass()).writeValueAsString(objecto);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Converte um objecto em Json
	 * 
	 * @param objecto a converter
	 * @return O JSON correspondente a este objecto
	 */
	public String parseListToJSON(List<? extends Object> objecto) {
		try {
			return new ObjectMapperProvider().getContext(List.class).writeValueAsString(objecto);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String parseToJSON(Map<String, Object> map) {
		List<Map<String, Object>> mapers = new ArrayList<Map<String, Object>>();
		mapers.add(map);
		
		return parseToJSON(mapers);
	}
	
	private JsonNode parseToArrayNode(List<Map<String, Object>> maps) {
		ObjectMapper mapper = new ObjectMapper();
		
		if (maps.size() == 1) {
			return createJSONNode(mapper, maps.get(0));
		} else {
			ArrayNode arrayNode = mapper.createArrayNode();
			for (Map<String, Object> map : maps) {
				
				ObjectNode objectNode = createJSONNode(mapper, map);
				arrayNode.add(objectNode);
			}
			return arrayNode;
		}
	}
	
	public <T extends Object> T createInstance(Class<T> instanceClass) {
		try {
			return instanceClass.newInstance();
		}
		catch (InstantiationException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private ObjectNode createJSONNode(ObjectMapper mapper, Map<String, Object> map) {
		ObjectNode objectNode = mapper.createObjectNode();
		
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() instanceof List) {
				objectNode.putPOJO(entry.getKey(), parseToArrayNode((List<Map<String, Object>>) entry.getValue()));
			} else if (entry.getValue() instanceof Map) {
				objectNode.putPOJO(entry.getKey(), parseToArrayNode((Map<String, Object>) entry.getValue()));
			} else
				putObjectOnNode(objectNode, entry);
		}
		return objectNode;
	}
	
	private JsonNode parseToArrayNode(Map<String, Object> map) {
		List<Map<String, Object>> mapers = new ArrayList<Map<String, Object>>();
		mapers.add(map);
		return parseToArrayNode(mapers);
	}
	
	public String parseToJSON(List<Map<String, Object>> maps) {
		return parseToArrayNode(maps).toString();
		/*ObjectMapper mapper = new ObjectMapper();
		try {
			
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parseToArrayNode(maps));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}*/
	}
	
	private void putObjectOnNode(ObjectNode objectNode, Map.Entry<String, Object> entry) {
		if (entry.getValue() instanceof String) {
			objectNode.put(entry.getKey(), entry.getValue().toString());
		} else if (entry.getValue() instanceof Long) {
			objectNode.put(entry.getKey(), Long.parseLong(entry.getValue().toString()));
		} else if (entry.getValue() instanceof Integer) {
			objectNode.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
		} else {
			throw new ForbiddenOperationException("Tipo de dado nao suportado");
		}
	}
	
	public JsonNode parseToJSONObject(String json) {
		try {
			return new ObjectMapper().readTree(json);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String parseToCSV(List<? extends VO> objs) {
		if (objs == null || objs.isEmpty()) {
			return "";
		}
		
		StringBuilder csvBuilder = new StringBuilder();
		
		for (VO obj : objs) {
			List<String> values = obj.getFieldValuesAsString();
			
			csvBuilder.append(String.join(",", values)).append("\n");
		}
		
		return csvBuilder.toString();
	}
	
	public String toUpperCase(String str) {
		if (stringHasValue(str))
			return str.toUpperCase();
		
		return str;
	}
	
	public String toLowerCase(String str) {
		if (stringHasValue(str))
			return str.toLowerCase();
		
		return str;
	}
	
	public String convertTableAttNameToClassAttName(String tableAttName) {
		String[] attParts = tableAttName.split("_");
		
		String attName = attParts[0];
		
		for (int i = 1; i < attParts.length; i++) {
			attName += utilities.capitalize(attParts[i]);
		}
		
		return attName;
	}
	
	public boolean isValidUUID(String str) {
		try {
			UUID.fromString(str);
			return true;
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}
	
	public String scapeQuotationMarks(String str) {
		if (str == null)
			return null;
		
		str = new String(str.replaceAll("\"", "\\\\\""));
		
		str = str.replaceAll("\\\\\\\\", "\\\\\\\\\\\\");
		
		return str;
	}
	
	public String delemeterWithCotationMarks(String str) {
		return quote(str);
	}
	
	public String removeLastChar(String str) {
		return str.substring(0, str.length() - 1);
	}
	
	public String removeCharactersOnString(String str, String... characters) {
		if (!stringHasValue(str) || characters == null)
			return str;
		
		for (String c : characters) {
			str = str.replaceAll(c, "");
		}
		
		return str;
	}
	
	public String resolveScapeCharacter(String str) {
		if (!stringHasValue(str))
			return str;
		
		char scapeCaracter = '\\';
		
		String resolved = "";
		
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == scapeCaracter) {
				if (i + 1 < str.length()) {
					if (!isSpecialCharacter(str.charAt(i + 1))) {
						//Force to be scaped
						resolved += str.charAt(i) + "\\";
					} else {
						resolved += str.charAt(i);
					}
				} else {
					//Force to be scaped
					resolved += str.charAt(i) + "\\";
				}
			} else
				resolved += str.charAt(i);
		}
		
		return resolved;
	}
	
	private boolean isSpecialCharacter(char charAt) {
		char[] spectials = { '\\', '\"' };
		
		return getPosOnArray(spectials, charAt) >= 0;
	}
	
	private int getPosOnArray(char[] array, char toFind) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == toFind)
				return i;
		}
		
		return -1;
	}
	
	public String quote(String strToQuote) {
		if (strToQuote == null)
			return strToQuote;
		
		return "\"" + strToQuote + "\"";
	}
	
	/**
	 * Create a map populated with an initial entries passed by parameter
	 * 
	 * @param params the entries which will populate the map. It's an array which emulate a map
	 *            entries in this format [key1, val1, key2, val2, key3, val3, ..]
	 * @return the generated map
	 * @throws ForbiddenOperationException when the params array length is not odd
	 */
	public Map<String, Object> fastCreateMap(Object... params) throws ForbiddenOperationException {
		if (params.length % 2 != 0)
			throw new ForbiddenOperationException("The parameters for fastCreatMap must be pars <K1, V1>, <K2, V2>");
		
		Map<String, Object> map = new HashMap<>();
		
		int paramsSize = params.length / 2;
		
		for (int set = 1; set <= paramsSize; set++) {
			int pos = set * 2 - 1;
			
			map.put(((String) params[pos - 1]), params[pos]);
		}
		
		return map;
	}
	
	public Object getFieldValueOnFieldList(List<org.openmrs.module.epts.etl.model.Field> fields, String fieldName)
	        throws ForbiddenOperationException {
		
		if (!utilities.arrayHasElement(fields))
			return null;
		
		for (int i = 0; i < fields.size(); i++) {
			org.openmrs.module.epts.etl.model.Field field = fields.get(i);
			
			if (field.getName().equals(fieldName)) {
				return field.getValue();
			}
		}
		
		throw new ForbiddenOperationException("The field '" + fieldName + "' was not found on list of fields");
		
	}
	
	public Object getFieldValue(EtlObject obj, String fieldName) throws ForbiddenOperationException {
		return getFieldValue(obj.getObjectName(), obj, fieldName);
	}
	
	public Object getFieldValue(Object obj, String fieldName) throws ForbiddenOperationException {
		return getFieldValue(obj.getClass().getName(), obj, fieldName);
	}
	
	private Object getFieldValue(String objectName, Object obj, String fieldName) throws ForbiddenOperationException {
		
		for (Field field : getInstanceFields(obj)) {
			
			if (field.getName().equals(fieldName)) {
				
				try {
					if (field.get(obj) != null) {
						return field.get(obj);
					}
				}
				catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
				catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		throw new ForbiddenOperationException("The field '" + fieldName + "' was not found on object '" + objectName + "'");
	}
	
	public Field getField(Object obj, String fieldName) throws ForbiddenOperationException {
		for (Field field : getInstanceFields(obj)) {
			
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		
		throw new ForbiddenOperationException(
		        "The field '" + fieldName + "' was not found on object '" + obj.getClass().getName() + "'");
		
	}
	
	public Class<?> getFieldType(Object obj, String fieldName) {
		return getField(obj, fieldName).getType();
	}
	
	public Object parseValue(String value, Class<?> destinationType) {
		if (destinationType.equals(String.class) && value instanceof String) {
			return value;
		} else if (destinationType.equals(Date.class) && value instanceof String) {
			return DateAndTimeUtilities.createDate(value);
			
		} else if (destinationType.equals(Double.class) && value instanceof String) {
			return Double.parseDouble(value);
		} else if (destinationType.equals(Integer.class) && value instanceof String) {
			return Integer.parseInt(value);
			
		} else if (destinationType.equals(Long.class) && value instanceof String) {
			return Long.parseLong(value);
		} else if (destinationType.equals(Boolean.class) && value instanceof String) {
			return Boolean.parseBoolean(value);
		} else if (destinationType.equals(Short.class) && value instanceof String) {
			return Short.parseShort(value);
		} else if (destinationType.equals(Float.class) && value instanceof String) {
			return Float.parseFloat(value);
		}
		return value;
	}
	
	/**
	 * Retorna todos os atributos de instancia de da classe de um objecto independentemento do
	 * modificador de acesso
	 * 
	 * @return todos os atributos de instancia de da classe de um objecto independentemento do
	 *         modificador de acesso
	 */
	public List<Field> getInstanceFields(Object obj) {
		List<Field> fields = new ArrayList<Field>();
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
		return fields;
	}
	
	public void throwReviewMethodException() {
		throw new ForbiddenOperationException("Review the method calling me");
	}
	
	public void throwForbiddenMethodException() {
		throw new ForbiddenOperationException("This method is forbidden");
	}
	
	public int getAvailableProcessors() {
		Runtime runtime = Runtime.getRuntime();
		
		return runtime.availableProcessors();
	}
	
	/**
	 * Converte o atributo para uma coluna na BD Por exemplo: imageFile => image_file
	 * 
	 * @param fieldName nome do atributo
	 * @return nome da coluna correspondente ao atributo
	 */
	public String parsetoSnakeCase(String fieldName) {
		return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}
	
	public String parsetoCamelCase(String snakeCase) {
		StringBuilder camelCase = new StringBuilder();
		
		// Split the string on underscores
		String[] parts = snakeCase.split("_");
		
		// Append the first word as is (in lowercase)
		camelCase.append(parts[0].toLowerCase());
		
		// Capitalize the first letter of each subsequent word and append
		for (int i = 1; i < parts.length; i++) {
			camelCase.append(parts[i].substring(0, 1).toUpperCase());
			camelCase.append(parts[i].substring(1).toLowerCase());
		}
		
		return camelCase.toString();
	}
	
}
