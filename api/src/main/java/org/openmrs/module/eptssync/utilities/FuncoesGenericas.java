package org.openmrs.module.eptssync.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;


public class FuncoesGenericas {
	public static Logger logger = Logger.getAnonymousLogger();

	public static String garantirXCaracterOnNumber(long number, int x){
		String formatedNumber = "";
		int numberOfCharacterToIncrise = 0;
		
		formatedNumber = number + "";
		
		numberOfCharacterToIncrise = x - formatedNumber.length();
		
		for(int i = 0; i < numberOfCharacterToIncrise; i++) formatedNumber = "0" + formatedNumber;
	
		return formatedNumber; 
	}
	
	public static List<?> parseObjectToList(Object obj){
		if (obj == null) return null;
		
		if (!(obj instanceof List)) throw new ClassCastException(obj.getClass() + " CANNOT CAST TO " + List.class);
		
		return  (List<?>) obj;
	}
	
	/**
	 * Concatena duas strings com String de separacao
	 * 
	 * @param currentString
	 * @param field
	 * @scapeStr Caracteres de escape, por exemplo "\n" "," "<br>" 
	 * @return
	 */
	public static String concatStrings(String currentString, String toConcant, String scapeStr){
		if (!stringHasValue(toConcant)) return currentString;
		
		if (!stringHasValue(currentString)) return toConcant;
		
		return currentString + scapeStr+ toConcant; 
	}
	
	public static String concatStrings(String value, String... inValues){
		if (inValues == null) return value;
		
		for (String str : inValues){
			value = concatStrings(value, str, "");
		}
		
		return value;
	}
	
	
	/**
	 * Verifica se a String 'value' encontra se na lista especificada em 'inValue'.
	 * Nao ignora o a caixa ('case'), Case sensitive
	 * @param value
	 * @param inValue
	 * @return
	 */
	public static boolean isStringIn(String value, String... inValues){
		if (inValues == null || value == null) return false;
		
		for (String str : inValues){
			if (value.equals(str)) return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param qtdObjects
	 * @param params 
	 * @param p
	 * @return Insere um novo objecto num array de Objectos e retorna este ultimo
	 */
	public static Object[] setParam(int qtdObjects, Object[] paramsCorrentes, Object paramAAdicionar){
		Object[] objects = new Object[qtdObjects+1];
		
		for (int i = 0; i < qtdObjects; i++){
			objects[i] = paramsCorrentes[i];
		}
		objects[qtdObjects] = paramAAdicionar;
		paramsCorrentes = objects;
		return objects;
	}
	
	public static Object[] setParam(Object[] paramsCorrentes, Object [] paramAAdicionar){
		Object[] objects = new Object[paramsCorrentes.length + paramAAdicionar.length];

		int i=0;
		for (i = 0; i < paramsCorrentes.length; i++) {
				objects[i] = paramsCorrentes[i];
		}

		for (int j=0; j < paramAAdicionar.length; j++, i++) {
				objects[i] = paramAAdicionar[j];
		}
		
		
		
		return objects;
			
	}
	
	
	public static void printParams(Object[] params){
		if (params == null) return;
		
		for (int i = 0; i < params.length; i++){
			logger.info("Param ["+i+"]="+ params[i]);
		}
	}
	
	
	public static String[] trimAll(String[] params){
		for (int i = 0; i < params.length; i++){
			params[i] = params[i].trim();
		}
		
		return params;
	}
	
	public static void printParams(List<?> params){
		for (int i = 0; i < params.size(); i++){
			logger.info("Param ["+i+"]="+ params.get(i));
		}
	}
	
	/**
	 * Remove a primeira ocorrencia do caracter espa�o
	 * @param text
	 * @return
	 */
	public static String removeFirstEmptyCharacter(String text){
		
		int i;
		for (i =0; i < text.length() && text.charAt(i) == ' ';i++);
		
		return text.substring(i, text.length());
	}

	
	/**
	 * Remove todas as ocorrencias de espa�o superiores a 1
	 * @param text
	 * @return
	 */
	public static String removeDuplicatedEmptySpace(String text){
		if (!stringHasValue(text)) return "";
		
		String monoSpacedText = "";
		
		
		int i;
		for (i =0; i < text.length()-1;i++){
			if (text.charAt(i) == ' ' && text.charAt(i+1) == ' '){
			}
			else 
			if (text.charAt(i) == '\t' && text.charAt(i+1) == '\t') {
			}
			else
			if (text.charAt(i) == '\t'){
				monoSpacedText = monoSpacedText + ' ';
			}
			else
			if (text.charAt(i) == '\n' && text.charAt(i+1) == '\n') {
			}
			else
			if (text.charAt(i) == '\n'){
				monoSpacedText = monoSpacedText + ' ';
			}
			else monoSpacedText = monoSpacedText + text.charAt(i);
		}
		
		monoSpacedText = monoSpacedText + text.charAt(text.length()-1);
		
		return monoSpacedText;
	}	
	
	/**
	 * Remove todas as ocorrencias de espa�o
	 * @param text
	 * @return
	 */
	public static String removeAllEmptySpace(String text){
		if (!stringHasValue(text)) return "";
		
		String notSpaced = "";
		
		int i;
		for (i =0; i < text.length();i++){
			if (text.charAt(i) != ' '){
				notSpaced += text.charAt(i);
			}
		}
		
		return notSpaced;
	}	
	
	
	/**
	 * Forca a aproximacao por excesso do numero recebido pelo parametro
	 * @param numero: Valor numerico (inteiro ou decimal)
	 * @return
	 */
	public static long forcarAproximacaoPorExcesso(String numero){
		
		try {
	        long[] tokens = splitDoubleNumber(numero);
	        
	        if (tokens == null) return 0;
	        
	        long parteInteira=tokens[0];
	        long parteFracionaria=tokens[1];
	        
	        return parteFracionaria != 0 ? parteInteira+1 : parteInteira;
        } catch (Exception e) {
	        return 0;
        }
	}
	
	
	public static String getNumberInXPrecision(double number, int precision){
		String s =(""+number);
		
		BigDecimal b = new BigDecimal(s.replace(",", "."));
		
		return b.setScale(precision, RoundingMode.HALF_UP).toPlainString();
	}

	
	/**
	 * Faz o Split de um double, dividindo-o em duas partes atraves do ponto ou virgula
	 * @param numero
	 * @return
	 */
	public static long[] splitDoubleNumber(String numero){
		if (numero == null || numero.equals("")) return null;
		String parteInteira="";
		String parteDecimal="";
		int i = 0;
		/*
		 * Identificacao da parte inteira
		 */
		
		while(i < numero.length() && (numero.charAt(i) != '.' && numero.charAt(i) != ',')  ){
				parteInteira = parteInteira+numero.charAt(i);
				i++;
		}
		
		for (int j=i+1;j<numero.length();j++) parteDecimal = parteDecimal+numero.charAt(j);
		
		int pInt=0;
		try {
			pInt = Integer.parseInt(parteInteira);
		} catch (NumberFormatException e) {
		}
		
		long pDec=0;
		try {
			pDec = Long.parseLong(parteDecimal);
		} catch (NumberFormatException e) {
		}
		
		long[] tokens = {pInt, pDec};
		return tokens;
	}
	
	/**
	 * Gera o selfId concatenando o id da reparticao (prefixo) com o numero sequencial.
	 * @param numSequencial
	 * @param idReparticao
	 * @return
	 */
	public static long generateSelfId(long numSequencial, long idReparticao){
		 /* A separacao dos elementos podera ser identificada identificando a ultima parte do selfID a qual tem exactamente o numero
		 * de digitos na variavel qtdDigitosSufixo definida abaixo
		 */
		int qtdDigitosSufixo = 9;
		String selfId = ""+idReparticao + "" + garantirXCaracterOnNumber(numSequencial, qtdDigitosSufixo);
		
		return Long.parseLong(selfId);
	}
	
	/**
	 * Verifica se o selfId tem a quantidade permitida de digitos e possui o mesmo prefixo que o reparicaoId
	 * 
	 * @param selfId
	 * @param reparicaoId: reparticao onde foi gerado o id
	 * @param qtdDigitos: Quantidade de digitos permitidos
	 * @return
	 */
	public static boolean isValidSelfId(long selfId, int qtdDigitos, long reparicaoId){
		String str = ""+selfId;
		
		if (str.length() != qtdDigitos) return false;
		
		try {
			String pref = str.substring(0, (""+reparicaoId).length());
			
			if (Long.parseLong(pref) != reparicaoId) return false;
		} catch (NumberFormatException e) {
		}
		
		return true;
	}
	
	
	/**
	 * Verifica se o selfId tem a quantidade permitida de digitos
	 * 
	 * @param selfId
	 * @param reparicaoId: reparticao onde foi gerado o id
	 * @param qtdDigitos: Quantidade de digitos permitidos
	 * @return
	 */
	public static boolean isValidSelfId(long selfId, int qtdDigitos){
		String str = ""+selfId;
		
		if (str.length() != qtdDigitos) return false;
		
		return true;
	}
	
	/**
	 * Transforma uma String para o formato de elemento de um array multi-dimensional.
	 * Exemplo: String s = "name.parent";
	 *                 s = parseStringToArgArray(s);//s=[name][parent]
	 *                 
	 * @param strToParse
	 * @return 
	 */
	public static String parseStringToArgArray(String strToParse){
		String[] tokens = strToParse.split("\\.");
		
		if (!(tokens != null && tokens.length > 0)) return "["+strToParse+"]";
		
		String parsed = "";
		
		for (int i =0; i < tokens.length; i++){
			parsed = parsed + "["+tokens[i]+"]";
		}
		
		return parsed;
	}
	
	/**
	 * Transforma uma String para um array de double.
	 * Exemplo: String s = "14.3,11.4";
	 *          double[] d = parseStringToArgArray(s, ",");//d=[14.3][11.4]
	 *                 
	 * @param strToParse
	 * @param separator
	 * @return 
	 */
	public static double[] parseStringToArrayOfDouble(String strToParse, String separator){
		String[] tokens = strToParse.split(separator);
		
		double[] parsed = new double[tokens.length];
		
		if ((tokens != null && tokens.length > 0)) {
			for (int i =0; i < tokens.length; i++){
				parsed[i] = Double.valueOf(tokens[i]);
			}
		}
		
		return parsed;
	}
	
	public static String parseArrayToString(String[] array){
		String parsed = "";
		
		for (int i =0; i < array.length; i ++){
			parsed = addAtributeToValidationString(parsed, "'" + array[i] + "'");
		}
		
		return parsed;
		
	}
	
	public static String addAtributeToValidationString(String currentString, String field) {
		if (currentString == null || currentString.isEmpty())
			return field;

		return currentString + ", " + field;
	}
	
	/**
	 * Convert uma lista de objectos em lista
	 * @param obj
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> parseToList(T... obj){
		if (obj == null || obj.length == 0) return null;
		
		List<T> list = new ArrayList<T>();
		
		for (T o : obj) list.add(o);
		
		return list;
	}
	
	public static <T> List<T> parseArrayToList(T[] array){
		List<T> parsed = new ArrayList<T>();
		
		for (T o : array){
			parsed.add(o);
		}
		
		return parsed;
	}
	
	/**
	 * Localiza um objecto de uma {@link List}
	 * 
	 * @param list
	 *            da qual se pretende recuperar o registo
	 * @param toFind
	 *            registo a recuperar
	 * @return registo recuperado
	 */
	public static <T> T findOnArray(List<T> list, Object toFind) {
		if (list == null || toFind == null)
			return null;

		for (T current : list)
			if (toFind.equals(current))
				return current;

		return null;
	}

	public static <T> int countQtdOcorrenciaDoRegistoOnList(List<T> list, T obj){
		if (!arrayHasElement(list)) return 0;
		
		int qtdOcorrencias=0;
		
		for (int i=0; i < list.size(); i++){
			if (obj.equals(list.get(i))) qtdOcorrencias++;
		}
		
		return qtdOcorrencias;
	}
	
	public static int arraySize(List<?> list){
		if (list != null) return  list.size();
		
		return 0;
	}
	
	public static boolean arrayHasExactlyOneElement(List<?> list){
		return arraySize(list) == 1;
	}
	
	
	public static <T> T getFirstRecordOnArray(List<T> list){
		return  list != null && !list.isEmpty() ? list.get(0) : null;
	}

	/**
	 * Fun��o auxiliar usada para gerar o id do checkbox na tag listcheckbox.tag
	 * @return 
	 */					  	
	public static String generateIdForCheckBox(String name, int pos){
		return name + "_"+pos;
	}
	
	/*
	public String getValueOnArray(Object array, String index){
		String[] tokens = index.split("\\.");
		
		
		
		//if (!(tokens != null && tokens.length > 0)) return "["+strToParse+"]";
		
		int indiceLevel = tokens.length;
		
		switch (indiceLevel) {
		case 1: {
			Object[] array1 = (Object[])array;
			return array1[tokens[0]];
		}
			
			break;

		default:
			break;
		}
		
	
	}
		*/
	public static void main(String[] args){
		System.out.println(getAlphabeticalCharAt(2));
		System.out.println(getPosAlphabeticalChar('a'));
	}
	
	/**
	 * Concatena duas condicoes sql
	 * @param condition
	 * @param otherCondition
	 * @return Condicao SQL composta pela condi��o inicial concatenada a nova condi��o
	 */
	public static String concatCondition(String condition, String otherCondition){
		if (stringHasValue(condition)){
			if (stringHasValue(otherCondition)) return condition + " AND " + otherCondition;
			else return condition;
		}
		else return otherCondition;		
	}
	
	
	/**
	 * Concatena duas condicoes sql
	 * @param condition
	 * @param otherCondition
	 * @return Condicao SQL composta pela condi��o inicial concatenada a nova condi��o
	 */
	public static String concatCondition(String condition, String otherCondition, String connector){
		
		if (stringHasValue(condition)){
			if (stringHasValue(otherCondition)) return condition + " " + connector + " " + otherCondition;
			else return condition;
		}
		else return otherCondition;		
	}
	
	/**
	
	/**
	 * Verifica se uma determinada String � num�rica ou n�o
	 * @param str
	 * @return
	 */ 
	public static boolean isNumeric(String str){
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		catch (NullPointerException e) {
			return false;
		}
	}
	
	/** 
	 * Permite formatar um numero para a visualizacao
	 * @return
	 */
	public static String displayNumberOnStringFormat(double number){
		if (number == 0) return "";
		else return ""+number;
	}
	
	/** 
	 * Permite formatar um numero para a visualizacao
	 * @return
	 */
	public static String displayDoubleOnIntegerFormat(double number){
		return ""+(long)number;
	}
	
	public static double dateDiff(String dataMaior, String dataMenor, String format) throws ParseException{
		System.out.println("dataMaior "+dataMaior);
		System.out.println("dataMaior "+dataMenor);
		System.out.println("format "+format);
		
		try {
			Date dataMenorAux = DateAndTimeUtilities.createDate(dataMenor, "dd-MM-yyyy");
			Date dataMaiorAux = DateAndTimeUtilities.createDate(dataMaior, "dd-MM-yyyy");
			
			return DateAndTimeUtilities.dateDiff(dataMaiorAux, dataMenorAux, format);
		} catch (Exception e) {
			e.printStackTrace(); 
			return 0;
		}
	}
	
	public static double calculaIdade(String dataMenor, String format) throws ParseException{
		
		String dataMaior = DateAndTimeUtilities.formatToDDMMYYYY(DateAndTimeUtilities.getCurrentDate());
			 
		try {
			Date dataMenorAux = DateAndTimeUtilities.createDate(dataMenor, "dd-MM-yyyy");
			Date dataMaiorAux = DateAndTimeUtilities.createDate(dataMaior, "dd-MM-yyyy");
			
			return DateAndTimeUtilities.dateDiff(dataMaiorAux, dataMenorAux, format);
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static boolean stringHasValue(String string){
		return string != null && !string.isEmpty();
	}
	
	public static boolean arrayHasElement(List<?> list){
		return list != null && !list.isEmpty();
	}
	
	public static boolean arrayHasElement(ArrayList<?> list){
		return list != null && !list.isEmpty();
	}
	
	public static boolean arrayHasMoreThanOneElements(List<?> list){
		return list != null && list.size() > 1;
	}
	
	public static <T> int getPosOnArray(T[] array, T toFind){
		for (int i =0; i < array.length; i++){
			if (array[i].equals(toFind)) return i;
		}
		
		throw new RuntimeException("Not found object ["+toFind+"]");
	}
	
	public static String valorPorExtenso(double num) {
		long inteiro = 0;
		long parteDec = 0;
		long decomp = 0;
		double numAux = 0;
		String extenso = "";
		String dinheiro = "";
		String b1 = "";
		
		// Para aceitar somente numeros com duas casas decimais.
		numAux = (double) Math.round(num * 100) / 100;

		// Para pegar a parte inteira do numero.
		inteiro = (long) Math.floor(numAux);

		// Para pegar a parte decima do numero.
		parteDec = (long)Math.round(((numAux - inteiro) * 100));

		if (inteiro == 0) {
			extenso = (parteDec > 0 ? cents((int) parteDec) : "Zero Meticais");
		} else {
			dinheiro = (inteiro == 1 ? " Metical " : " Meticais ");

			decomp = (long) Math.floor(inteiro / 1000000);
			if (decomp > 0) {
				b1 = buildSpell(decomp);
				extenso = (decomp > 1 ? b1 + " Milhoes " : b1 + "Milhao");
				inteiro -= decomp * 1000000;
				if (inteiro == 0)
					dinheiro = " de Meticais";
			}
			decomp = (long) Math.floor(inteiro / 1000);
			if (decomp > 0) {				
				b1 = buildSpell(decomp);
				extenso += b1 + " Mil";
				inteiro -= decomp * 1000;
				
				if (inteiro > 0) {
					if (inteiro <= 900)
						extenso += " e";
				}
			}
			b1 = buildSpell(inteiro);
			extenso += b1 + dinheiro;

			if (parteDec > 0) {
				extenso += " e " + cents((int)parteDec);				
			}
		}
		return extenso;
	}
	
	public static ArrayList<String> parseToList(String[] array){
		ArrayList<String> list = new ArrayList<String>();
		
		for (String str : array){
			if (stringHasValue(str)) list.add(str);
		}
		
		return list;
	}
	
	/**
	 * @param inteiro
	 * @return
	 */
	private static String buildSpell(long inteiro) {
		long parte = 0;
		String a1 = "";
		String a2 = "";
		String a3 = "";
		String a4 = "";
		String a5 = "";

		parte = (long) Math.floor(inteiro / 100);
		inteiro = inteiro - (parte * 100);

		if (parte > 0) {
			switch ((int) parte) {
			case 1:
				a1 = (inteiro == 0 ? " Cem " : " Cento ");
				break;
			case 2:
				a1 = " Duzentos ";
				break;
			case 3:
				a1 = " Trezentos ";
				break;
			case 4:
				a1 = " Quatrocentos ";
				break;
			case 5:
				a1 = " Quinhentos ";
				break;
			case 6:
				a1 = " Seiscentos ";
				break;
			case 7:
				a1 = " Setecentos ";
				break;
			case 8:
				a1 = " Oitocentos ";
				break;
			case 9:
				a1 = " Novecentos ";
				break;
			}
			if (inteiro > 0)
				a2 = " e ";
		}

		if (inteiro > 10 && inteiro < 20) {
			switch ((int) inteiro) {
			case 11:
				a3 = " Onze ";
				break;
			case 12:
				a3 = " Doze ";
				break;
			case 13:
				a3 = " Treze ";
				break;
			case 14:
				a3 = " Catorze ";
				break;
			case 15:
				a3 = " Quinze ";
				break;
			case 16:
				a3 = " Dezasseis ";
				break;
			case 17:
				a3 = " Dezassete ";
				break;
			case 18:
				a3 = " Dezoito ";
				break;
			case 19:
				a3 = " Dezanove ";
				break;
			}
		} else if (inteiro > 0) {
			parte = (long) Math.floor(inteiro / 10);
			inteiro = inteiro - (parte * 10);
			if (parte > 0) {
				switch ((int) parte) {
				case 1:
					a3 = " Dez ";
					break;
				case 2:
					a3 = " Vinte ";
					break;
				case 3:
					a3 = " Trinta ";
					break;
				case 4:
					a3 = " Quarenta ";
					break;
				case 5:
					a3 = " Cinqueta ";
					break;
				case 6:
					a3 = " Sessenta ";
					break;
				case 7:
					a3 = " Setenta ";
					break;
				case 8:
					a3 = " Oitenta ";
					break;
				case 9:
					a3 = " Noventa ";
					break;
				}
				if (inteiro > 0)
					a4 = " e ";
			}
			if (inteiro > 0) {
				switch ((int) inteiro) {
				case 1:
					a5 = " Um ";
					break;
				case 2:
					a5 = " Dois ";
					break;
				case 3:
					a5 = " Tr�s ";
					break;
				case 4:
					a5 = " Quatro ";
					break;
				case 5:
					a5 = " Cinco ";
					break;
				case 6:
					a5 = " Seis ";
					break;
				case 7:
					a5 = " Sete ";
					break;
				case 8:
					a5 = " Oito ";
					break;
				case 9:
					a5 = " Nove ";
					break;
				}
			}
		}
		return a1 + a2 + a3 + a4 + a5;
	}
	
	/**
	 * @param num
	 * @return
	 */
	private static String cents(int num) {
		String extenso = null;
		
		int dezenas = (int) Math.floor(num / 10);	
		int unidades = (int) num - dezenas*10;

		switch (unidades) {
		case 0:
			extenso = "";
			break;
		case 1:
			extenso = "Um";
			break;
		case 2:
			extenso = "Dois";
			break;
		case 3:
			extenso = "Tr�s";
			break;
		case 4:
			extenso = "Quatro";
			break;
		case 5:
			extenso = "Cinco";
			break;
		case 6:
			extenso = "Seis";
			break;
		case 7:
			extenso = "Sete";
			break;
		case 8:
			extenso = "Oito";
			break;
		case 9:
			extenso = "Nove";
			break;
		}
		if (dezenas == 0)
			if (unidades != 1)
				extenso += " Centavos";
			else
				extenso += " Centavo";

		if (dezenas > 0) {
			if (unidades > 0)
				extenso = "e " + extenso;
			switch (dezenas) {
			case 1: {
				switch (unidades) {
				case 0:
					extenso = "Dez Centavos";
					break;
				case 1:
					extenso = "Onze Centavos";
					break;
				case 2:
					extenso = "Doze Centavos";
					break;
				case 3:
					extenso = "Treze Centavos";
					break;
				case 4:
					extenso = "Catorze Centavos";
					break;
				case 5:
					extenso = "Quinze Centavos";
					break;
				case 6:
					extenso = "Dezasseis Centavos";
					break;
				case 7:
					extenso = "Dezassete Centavos";
					break;
				case 8:
					extenso = "Dezoito Centavos";
					break;
				case 9:
					extenso = "Dezanove Centavos";
					break;
				}
			}
				break;
			case 2:
				extenso = "Vinte " + extenso + " Centavos";
				break;
			case 3:
				extenso = "Trinta " + extenso + " Centavos";
				break;
			case 4:
				extenso = "Quarenta " + extenso + " Centavos";
				break;
			case 5:
				extenso = "Cinqueta " + extenso + " Centavos";
				break;
			case 6:
				extenso = "Sessenta " + extenso + " Centavos";
				break;
			case 7:
				extenso = "Setenta " + extenso + " Centavos";
				break;
			case 8:
				extenso = "Oitenta " + extenso + " Centavos";
				break;
			case 9:
				extenso = "Noventa " + extenso + " Centavos";
				break;
			}
		}
		return extenso;
	}

	/**
	 * Converte valores clobs para String
	 * 
	 * @param data
	 * @return
	 * @throws RuntimeException: se ocorrer uma SQLException ou IOException
	 */
	public static String convertClobToString(java.sql.Clob data) throws RuntimeException{
	    final StringBuilder sb = new StringBuilder();

	    try {
			final Reader         reader = data.getCharacterStream();
			final BufferedReader br     = new BufferedReader(reader);

			int b;
			while(-1 != (b = br.read())){
			    sb.append((char)b);
			}

			br.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	    
	    return sb.toString();
	}

	/**
	 * 
	 * @param data
	 * @param conn
	 * @return
	 * @throws RuntimeException: se ocorrer um SQLException
	 */
	public static Clob convertStringToClob(String data, Connection conn) throws RuntimeException, SQLException{
		Clob c = conn.createNClob();
		c.setString(1, data);
			
		return c;
	}
	
	/**
	 * Retorna o caracter na pos-esima posicao no alfabeto
	 * A primeira posicao � a posi��o 1
	 * @param pos
	 * @return
	 */
	public static char getAlphabeticalCharAt(int pos){
		char[] chars = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'}; // With Tonny,'0','1','2','3','4','5','6','7','8','9'};
		
		return chars[pos-1];
	}
	
	
	public static boolean isStringStartWithAlphabeticalChar(String str){
		if (!stringHasValue(str)) return false;
		
		return isBetween(str.toUpperCase().charAt(0), 65, 90);  
	}
	
	public static List<Integer> fillListByInts(int start, int end){
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		for (int i = start; i <= end; i++){
			list.add(i);
		}
		
		return  list;
	}
	
	/**
	 * Retorna a posicao do caracter no alfabeto
	 * A primeira posicao � a posi��o 1
	 * @param pos
	 * @return
	 */
	public static int getPosAlphabeticalChar(char chr){
		String[] array = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"}; 

		List<?> chars = parseToList(array);
		
	
		try {
			return getPosOnArray(array, (""+chr).toUpperCase()) + 1;
			
			} catch (Exception e) {
				throw new  NoSuchElementException("A letra indicada nao faz parte do alfabeto ["+chars+"]");
			}
		
	}
	
	public static boolean isBetween(double number, double interval_1, double interval_N){
		return number >= interval_1 && number <=interval_N;
	}
	
	public static double max(double ...num){
		double max = num[0];
		
		for (double n : num){
			if (max < n) max = n;
		}
		
		return max;
	}
	
	public static double min(double ...num){
		double min = num[0];
		
		for (double n : num){
			if (min > n) min = n;
		}
		
		return min;
	}
}
