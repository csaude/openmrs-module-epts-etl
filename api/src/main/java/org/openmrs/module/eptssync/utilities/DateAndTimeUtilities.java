/**
 * @author Jorge Boane
 */
package org.openmrs.module.eptssync.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;


public class DateAndTimeUtilities {
	
	
	public static final String YEAR_FORMAT="YYYY";
	public static final String DAY_FORMAT="DD";
	public static final String MONTH_FORMAT="MM";
	public static final String HOUR_FORMAT="HH";
	public static final String SECOND_FORMAT="ss";
	public static final String MINUTE_FORMAT="mm";
	public static final String MILLISECOND_FORMAT="SSS";
	public static final String DATE_FORMAT="dd-MM-yyyy";
	
	public static final String ORACLE_DATE_TIME_FORMAT="dd-MM-yyyy HH24:MI:SS";
	public static final String DATE_TIME_FORMAT="dd-MM-yyyy HH:mm:ss";
	
	public static final String ORACLE_MINUTE_FORMAT="MI";
	public static final String ORACLE_HOUR_FORMAT="HH24";
	
	public static final String LANGUAGE_PT="PT";
	public static final String LANGUAGE_EN="EN";
	
	
	/**
	 * Converty um mes em String para int
	 * @return
	 */
	public static int convertStringMonthToInt(String month, String language){
		month = month.toUpperCase();
		int pos = -1;
		
		if (language.equalsIgnoreCase(LANGUAGE_PT)){
			String[] months = {"JANEIRO", "FEVEREIRO", "MARCO", "ABRIL", "MAIO", "JUNHO", "JULHO","AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"};
			pos = FuncoesGenericas.getPosOnArray(months, month);
		}
		else
		if (language.equals(LANGUAGE_EN)){
			String[] months = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY","AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
			pos = FuncoesGenericas.getPosOnArray(months, month);
		}
		else throw new ForbiddenOperationException("Languague not supported");
		
		if (pos < 0) throw new ForbiddenOperationException("Mes invalido ["+month+"]");
		
		return pos+1;
	}
	
	
	/**
	 * Converty um mes em int para String
	 * @return
	 */
	public static String convertMonthToDesignation(int month, String language){
		if (month < 1 || month > 12) throw new ForbiddenOperationException("Mes invalido ["+month+"]");
		
		String[] months = null;
		
		if (language.equalsIgnoreCase(LANGUAGE_PT)){
			String[] months_pt = {"JANEIRO", "FEVEREIRO", "MARCO", "ABRIL", "MAIO", "JUNHO", "JULHO","AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"};
		
			months = months_pt;
		}
		else
		if (language.equals(LANGUAGE_EN)){
			String[] months_en = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY","AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
			
			months = months_en;
		}
		else throw new ForbiddenOperationException("Languague not supported");
		
		return months[month - 1];
	}
	
	/**
	 * 
	 * @param data
	 * @return recebe uma String no formato dd-mm-yyyy e retorna a o ano. 
	 */
	public static int getYear(String data){
		String[] partes= data.split("-");
		
		String dia, ano;
		
		dia = partes[0];
		ano = partes[2];
		
		/**
		 * Se a data estiver no formato yyyy/mm/dd
		 */
		if (Integer.parseInt(dia) > 31) {
			String anoAux = ano;
			ano = dia;
			dia = anoAux;
		}
		
		return Integer.parseInt(ano);
	}

	/**
	 * 
	 * @param data
	 * @return recebe uma String no formato dd-mm-yyyy e retorna a o ames. 
	 */
	public static int getMes(String data){
		String[] partes= data.split("-");

		return Integer.parseInt(partes[1]);
	}

	/**
	 * 
	 * @param data
	 * @return recebe uma String no formato dd-mm-yyyy e retorna a o dia. 
	 */
	public static int getDia(String data){
		String[] partes= data.split("-");
		
		String dia, ano;
		
		dia = partes[0];
		ano = partes[2];
		
		/**
		 * Se a data estiver no formato yyyy/mm/dd
		 */
		if (Integer.parseInt(dia) > 31) {
			String anoAux = ano;
			ano = dia;
			dia = anoAux;
		}
		return Integer.parseInt(dia);
	}

	/**
	 * @author Jorge Boane
	 * @return Transforma uma data no formato dd/mm/yyyy ou yyyy/mm/dd para yyyymmdd do tipo long  
	 * @param  date
	 */
	
	public static long parseDateToLong(String date) throws IllegalArgumentException{
		String[] partes = null;
		
		String dia = "",
		   	   mes = "",
		       ano = "";
		
		partes = date.split("/");
		if (partes.length > 1){
			dia = partes[0];
		   	mes = partes[1];
		    ano = partes[2];
		}
		else{
			partes = date.split("-");
			if (partes.length > 1){		
				dia = partes[0];
				mes = partes[1];
				ano = partes[2];
			}
			else {
					throw new IllegalArgumentException("O argumento indicado para o parametro Data E inválido!");
			}
			
		}
		
		/**
		 * Se a data estiver no formato yyyy/mm/dd
		 */
		if (Integer.parseInt(dia) > 31) {
			String anoAux = ano;
			ano = dia;
			dia = anoAux;
		}
		
		/**
		 * Se por ventura a data estiver no formato dd/mm, isto E, se o dia vier antes do mes
		 * Nota: esta situacao será detectada no caso em que o dia for maior que 12, caso contrario passará tudo despercebido. 
		 * 		 Isso E PERIGOSO, mas por enquanto nAo há alternativa
		 */
		
		if (Integer.parseInt(mes) > 12){
			String mesAux = mes;
			
			mes = dia;
			dia = mesAux;
		}
		
		return Long.parseLong(ano+ "" +mes+""+dia);
	}
	
	/**
	 * 
	 * @param data1
	 * @param data2
	 * @return retorna 0 se data1=data2, 1 se data1>tada2,-1 se data1<data2
	 */
	public static int compareTo(Date data1, Date data2){
		
		long d1 = parseDateToLong(data1),
			 d2 = parseDateToLong(data2);
	  
	  
		if (d1 == d2) return 0;
		if (d1 > d2) return 1;
		
		return -1;
		
	  
		/*
		 * parseDateToLong(validade) >= parseDateToLong(hoje): Esta comparaçAo retornará sempre
		 * um valor correcto, tendo em conta que a data foi convertida para o formato 'yyyymmdd'. 
		 * Prova: Consulte o autor.
		 *  
		 * 
		 * */ 
		
	
	}


	/**
	 * 
	 * @param data1
	 * @param data2
	 * @return retorna 0 se data1=data2, 1 se data1>tada2,-1 se data1<data2
	 * 		   Nota: A data deve estar no formato dd-mm-yyyy
	 */
	public static int compareTo(String data1, String data2){
		
		long d1 = parseDateToLong(data1),
			 d2 = parseDateToLong(data2);
	  
	  
		
		if (d1 == d2) return 0;
		if (d1 > d2) return 1;
		
		return -1;
		
	  
		/*
		 * parseDateToLong(validade) >= parseDateToLong(hoje): Esta comparaçAo retornará sempre
		 * um valor correcto, tendo em conta que a data foi convertida para o formato 'yyyymmdd'. 
		 * Prova: Consulte o autor.
		 *  
		 * 
		 * */ 
		
	
	}

	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data no formato (no dd/mm/yyyy ou yyyy/mm/dd) para o formato dd/mm/yyyy
	 * @param  date
	 */
	
	public static String formatToDDMMYYYY(String date) throws IllegalArgumentException{
		if (date == null) return null;
		
		String[] partes = null;
		
		String dia = "",
		   	   mes = "",
		       ano = "";
		
		partes = date.split("/");
		if (partes.length > 1){
			dia = partes[0];
		   	mes = partes[1];
		    ano = partes[2];
		}
		else{
			partes = date.split("-");
			if (partes.length > 1){		
				dia = partes[0];
				mes = partes[1];
				ano = partes[2];
			}
			else {
					throw new IllegalArgumentException("O argumento indicado para o parametro Data E invalido!");
			}
			
		}
		
		/**
		 * Se a data estiver no formato yyyy/mm/dd
		 */
		if (Integer.parseInt(dia) > 31) {
			String anoAux = ano;
			ano = dia;
			dia = anoAux;
		}
		
		/**
		 * Se por ventura a data estiver no formato dd/mm, isto E, se o dia vier antes do mes
		 * Nota: esta situacao será detectada no caso em que o dia for maior que 12, caso contrario passará tudo despercebido. 
		 * 		 Isso E PERIGOSO, mas por enquanto nAo há alternativa
		 */
		
		if (Integer.parseInt(mes) > 12){
			String mesAux = mes;
			
			mes = dia;
			dia = mesAux;
		}
		
		return dia + "-" + mes + "-" + ano;
	}
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data no formato (no dd/mm/yyyy ou yyyy/mm/dd) para o formato yyyy/mm/dd
	 * @param  date
	 */
	
	public static String formatToYYYYMMDD(String date) throws IllegalArgumentException{
		if (date == null) return null;
		
		String[] partes = null;
		
		String dia = "",
		   	   mes = "",
		       ano = "";
		
		partes = date.split("/");
		if (partes.length > 1){
			dia = partes[0];
		   	mes = partes[1];
		    ano = partes[2];
		}
		else{
			partes = date.split("-");
			if (partes.length > 1){		
				dia = partes[0];
				mes = partes[1];
				ano = partes[2];
			}
			else {
					throw new IllegalArgumentException("O argumento indicado para o parametro Data E inválido!");
			}
			
		}
		
		/**
		 * Se a data estiver no formato yyyy/mm/dd
		 */
		if (Integer.parseInt(dia) > 31) {
			String anoAux = ano;
			ano = dia;
			dia = anoAux;
		}
		
		/**
		 * Se por ventura a data estiver no formato dd/mm, isto E, se o dia vier antes do mes
		 * Nota: esta situacao será detectada no caso em que o dia for maior que 12, caso contrario passará tudo despercebido. 
		 * 		 Isso E PERIGOSO, mas por enquanto nao há alternativa
		 */
		
		if (Integer.parseInt(mes) > 12){
			String mesAux = mes;
			
			mes = dia;
			dia = mesAux;
		}
		
		return ano + "-" + mes + "-" + dia;
	}
	
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data para uma sitring no formato dd/mm/yyyy
	 * @param  date
	 * 
	 */
	
	public static String formatToDDMMYYYY(Date date){	
		if (date == null) return null;
		
		return formatToDDMMYYYY(date, "-");
	}
	
	public static String formatToDDMMYYYY(Date date, String separator){	
		if (date == null){
			return "";
		}
		return FuncoesGenericas.garantirXCaracterOnNumber(getDayOfMonth(date), 2) + separator + FuncoesGenericas.garantirXCaracterOnNumber(getMonth(date), 2) + separator + getYear(date);
	}
	
	public static String formatToDDMM(Date date){	
		if (date == null){
			return "";
		}
		return formatToDDMM(date, "-");
	}
	
	public static String formatToMMYYYY(Date date){	
		if (date == null){
			return "";
		}
		return formatToMMYYYY(date, "-");
	}
	
	public static String formatToMMYYYY(Date date, String separator){	
		if (date == null){
			return "";
		}
		return FuncoesGenericas.garantirXCaracterOnNumber(getMonth(date), 2) + separator + getYear(date);
	}
	
	public static String formatToDDMM(Date date, String separator){	
		if (date == null){
			return "";
		}
		return FuncoesGenericas.garantirXCaracterOnNumber(getDayOfMonth(date), 2) + separator + FuncoesGenericas.garantirXCaracterOnNumber(getMonth(date), 2);
	}
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data para uma sitring no formato dd-mm-yyyy hh:mi:ss
	 * @param  date
	 * 
	 */
	
	public static String formatToDDMMYYYY_HHMISS(Date date){	
		
		//DD-Mon-YY HH:MI:SS
		if (date == null){
			return "";
		}
		
		return FuncoesGenericas.garantirXCaracterOnNumber(getDayOfMonth(date), 2) + "-" + FuncoesGenericas.garantirXCaracterOnNumber(getMonth(date), 2) + "-" + getYear(date) + " " + formatToHHMISS(date);
	}
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data para uma sitring no formato yyyy-mm-dd hh:mi:ss
	 * @param  date
	 * 
	 */
	
	public static String formatToYYYYMMDD_HHMISS(Date date){	
		
		//DD-Mon-YY HH:MI:SS
		if (date == null){
			return "";
		}
		
		return formatToYYYYMMDD(date) + " " + formatToHHMISS(date);
	}
	
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data para uma sitring no formato dd/mm/yyyy
	 * @param  date
	 * 
	 */
	
	public static String formatToMilissegundos(Date date){	
		
		//DD-Mon-YY HH:MI:SS
		if (date == null){
			return "";
		}
		
		return getYear(date) + FuncoesGenericas.garantirXCaracterOnNumber(getMonth(date), 2) + FuncoesGenericas.garantirXCaracterOnNumber(getDayOfMonth(date), 2) + FuncoesGenericas.garantirXCaracterOnNumber(getHours(date), 2) + FuncoesGenericas.garantirXCaracterOnNumber(getMinutes(date), 2) + FuncoesGenericas.garantirXCaracterOnNumber(getSeconds(date), 2);
	}	
	
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data para uma sitring no formato dd/mm/yyyy
	 * @param  date
	 * 
	 */
	
	public static String formatToTime(Date date){	
		if (date == null){
			return "";
		}
		return FuncoesGenericas.garantirXCaracterOnNumber(getHours(date), 2) + ":" + FuncoesGenericas.garantirXCaracterOnNumber(getMinutes(date), 2);
	}
	
	
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data para uma sitring no formato dd/mm/yyyy
	 * @param  date
	 * 
	 */
	
	public static String formatToHHMISS(Date date){	
		if (date == null){
			return "";
		}
		
		return FuncoesGenericas.garantirXCaracterOnNumber(getHours(date), 2) + ":" + FuncoesGenericas.garantirXCaracterOnNumber(getMinutes(date), 2) + ":" + FuncoesGenericas.garantirXCaracterOnNumber(getSeconds(date), 2);
	}
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data para uma string no formato yyyy/mm/dd
	 * @param  date
	 */
	
	public static String formatToYYYYMMDD(Date date){
		if (date == null) return null;
		
		return getYear(date) + "-" +  FuncoesGenericas.garantirXCaracterOnNumber(getMonth(date), 2) + "-" + FuncoesGenericas.garantirXCaracterOnNumber(getDayOfMonth(date), 2);
	}
	
	public static String formatToDDMESYYYY(Date date){
		return  ""+ getDayOfMonth(date) + " de " + getLongMonth(date, LANGUAGE_PT) + " de " + getYear(date);
	}
	
	/**
	 * 
	 * @param date
	 * @param lang
	 * @return Busca o mes no formato longo na lingua indicada
	 */
	public static String getLongMonth(Date date, String lang){
		
		if (!lang.equalsIgnoreCase(LANGUAGE_PT) && lang.equalsIgnoreCase(LANGUAGE_EN)) throw new RuntimeException("Language '"+lang+"' not supported");
		
		switch(getMonth(date)){
			case 1: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Janeiro" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "January" : ""));
			case 2: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Fevereiro" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "February" : ""));
			case 3: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Março" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "March" : ""));
			case 4: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Abril" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "April" : ""));
			case 5: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Maio" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "May" : ""));
			case 6: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Junho" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "June" : ""));
			case 7: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Julho" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "July" : ""));
			case 8: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Agosto" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "August" : ""));
			case 9: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Setembro" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "September" : ""));
			case 10: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Outubro" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "October" : ""));
			case 11: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Novembro" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "November" : ""));
			case 12: return (lang.equalsIgnoreCase(LANGUAGE_PT) ? "Dezembro" : (lang.equalsIgnoreCase(LANGUAGE_EN) ? "December" : ""));
		}
		
		throw new RuntimeException("Ipossible to determine the long month of specified date");
	}
	
	
	
	public static Date getCurrentDate(){
		return new Date();
    }
	
	public static int getDayOfMonth(Date date){
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		
		return d.get(Calendar.DAY_OF_MONTH);
	}
	
	public static int getDayOfWeek(Date date){
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		
		return d.get(Calendar.DAY_OF_WEEK);
	}

	public static int getMonth(Date date){
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		
		return d.get(Calendar.MONTH) + 1;
	}

	public static int getYear(Date date){
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		
		return d.get(Calendar.YEAR);
	}

	public static int getHours(Date date){
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		
		return d.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getMinutes(Date date){
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		
		return d.get(Calendar.MINUTE);
	}
	
	public static int getSeconds(Date date){
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		
		return d.get(Calendar.SECOND);
	}
	
	/**
	 * Transforma uma data java.util.Date para uma data no formato String yyyy/mm/dd
	 * @param date
	 * @return
	 */
	public static String parseDateToString(Date date){
		if (date == null) return null;
		
		return getYear(date) + "-" +  FuncoesGenericas.garantirXCaracterOnNumber(getMonth(date), 2) + "-" + FuncoesGenericas.garantirXCaracterOnNumber(getDayOfMonth(date), 2);
	}
	
	/**
	 * Transforma uma data java.util.Date para uma data no formato String yyyy/mm/dd
	 * @param date
	 * @return
	 */
	public static Long parseDateToLong(Date date){
		return Long.parseLong(getYear(date) + "" +  FuncoesGenericas.garantirXCaracterOnNumber(getMonth(date), 2) + "" + FuncoesGenericas.garantirXCaracterOnNumber(getDayOfMonth(date), 2));
	}
	
	public static String parseTimeToString(Date date){
		return  FuncoesGenericas.garantirXCaracterOnNumber(getHours(date),2) + ":" + FuncoesGenericas.garantirXCaracterOnNumber(getMinutes(date),2);
	}
	
	/**
	 * Transforma uma data para o formato HHMM
	 * @param date
	 * @return
	 */
	public static String parseTimeToLong(Date date){
		return  FuncoesGenericas.garantirXCaracterOnNumber(getHours(date),2) + "" + FuncoesGenericas.garantirXCaracterOnNumber(getMinutes(date),2);
	}

	/**
	 * Transforma uma data para o formato HHMMSS
	 * @param date
	 * @return
	 */
	public static String parseFullTimeToLong(Date date){
		return  FuncoesGenericas.garantirXCaracterOnNumber(getHours(date),2) + "" + FuncoesGenericas.garantirXCaracterOnNumber(getMinutes(date),2)+ "" + FuncoesGenericas.garantirXCaracterOnNumber(getSeconds(date),2);
	}
	
	/**
	 * Transforma uma data para o formato yyyymmddhhmm
	 * @param date
	 * @return
	 */
	public static Long parseFullDateToTimeLong(Date date){
		return   Long.parseLong(""+parseDateToLong(date) + "" + parseTimeToLong(date)); //  FuncoesGenericas.garantirXCaracterOnNumber(getHours(date),2) + ":" + FuncoesGenericas.garantirXCaracterOnNumber(getMinutes(date),2);
	}
	
	/**
	 * Transforma uma data para o formato yyyymmddhhmmss
	 * @param date
	 * @return
	 */
	public static Long parseFullDateToTimeLongIncludeSeconds(Date date){
		return   Long.parseLong(""+parseDateToLong(date) + "" + parseFullTimeToLong(date)); 
	}
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma data no formato yyyymmdd para dd/mm/yyyy do tipo long  
	 * @param  date
	 */
	
	public static String parseLongToDate(String date) throws IllegalArgumentException{
		return date.substring(6,8) + "-" + date.substring(4, 6) + "-" + date.substring(0, 4);  
	}
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma String em uma data do tipo java.util.Date 
	 * @param  stringDate = data a transformar
	 * @param  dateFormat = formato da data recebida em stringDate: Nota use sempre MM para mês
	 */	
	public static Date createDate(String stringDate, String dateFormat) {
		 try {
			SimpleDateFormat sDate = new SimpleDateFormat(dateFormat); 
			Date date = sDate.parse(stringDate);
			 
			 return date;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @author Jorge Boane
	 * @return Transforma uma String em uma data do tipo java.util.Date, considera-se que a data estara no formato 'DD-MM-YYYY'
	 * @param  dateFormat = formato da data recebida em stringDate: Nota use sempre MM para mês
	 */	
	public static Date createDate(String stringDate) {
		String dateFormat = determineDateFormat(stringDate);
		
		return createDate(stringDate, dateFormat);
	}
	
	private static String determineDateFormat(String stringDate) {
		String[] dateElements = stringDate.split("-");
		char separador = '-';
		
		if (dateElements.length == 1){
			dateElements = stringDate.split("/");
			separador='/';
		}
		
		if (dateElements.length != 3){
			throw new ForbiddenOperationException("Formato de data nao suportado");
		}
		
		int firstElements = Integer.parseInt(dateElements[0]);
		//int secondElements = Integer.parseInt(dateElements[1]);
		//int thirdElements = Integer.parseInt(dateElements[2]);
		
		//Algoritimo simplificado. Assume-se que o formato ou � "dd-MM-yyyy" ou "yyyy-MM-dd"
		//Para algoritimo mais complecto, rever o codigo mais abaixo
		
		if (firstElements > 31) return "yyyy" + separador + "MM" + separador + "dd";
		else return "dd"+ separador + "MM" + separador + "yyyy";
		
		/*
		String formatElement01="";
		String formatElement02="";
		String formatElement03="";
		
		if (firstElements > 31){
			formatElement01 = "yyyy";
		}
		else
		if (firstElements > 12){
			formatElement01 = "dd";
		}
		
		if (secondElements > 31){
			formatElement02 = "yyyy";
		}
		else
		if (secondElements > 12){
			formatElement02 = "dd";
		}
		
		if (thirdElements > 31){
			formatElement03 = "yyyy";
		}
		else
		if (thirdElements > 12){
			formatElement03 = "dd";
		}
		
		if (formatElement01.isEmpty()){
			if (firstElements <= 12){
				
			}
		}
		*/
		
		//return null;
	}


	public static Date setHour(Date date, int hour){
		Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(date);
	    
	    calendar.set(Calendar.HOUR_OF_DAY, hour);
	    //calendar.set(Calendar.MINUTE, 59);
	    //calendar.set(Calendar.SECOND, 59);
		 
		return calendar.getTime();
	} 
	
	public static Date setMinute(Date date, int minute){
		Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(date);
	    
	    calendar.set(Calendar.MINUTE, minute);
	    
		return calendar.getTime();
	} 
	
	public static Date setSecond(Date date, int second){
		Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(date);
	    
	    calendar.set(Calendar.SECOND, second);
		 
		return calendar.getTime();
	} 
	
	
	public static Date setTime(Date date, int hour, int minute){
		Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(date);
	    
	    calendar.set(Calendar.MINUTE, minute);
	    calendar.set(Calendar.HOUR_OF_DAY, hour);
		    
		return calendar.getTime();
	} 
	
	/**
	 * COloca a hora na data:
	 * 
	 * @param date
	 * @param time: horas no formato hh:mm
	 * @return
	 */
	public static Date setTime(Date date, String time){
		String[] timeElements = time.split(":");
		
		Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(date);
	    
	    calendar.set(Calendar.MINUTE, Integer.parseInt(timeElements[1]));
	    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeElements[0]));
	    
	    if (timeElements.length > 2) calendar.set(Calendar.SECOND, Integer.parseInt(timeElements[2]));
	    
		return calendar.getTime();
	} 
	
	public static boolean isValidHour(int hour){
		return hour >= 0 && hour <=23;
	}
	
	public static boolean isValidMinute(int minute){
		return minute>= 0 && minute <=59;
	}
	
	/**
	 * @author Jorge Boane
	 * @return Transforma um long no formato (yyyymmddhhmm) em uma data do tipo java.util.Date 
	 */	
	public static Date createDate(long longDate) throws RuntimeException{
		if ((""+longDate).length() != 12) throw new RuntimeException("Parametro inválido");
		
		String data = ""+longDate;
			
		int year = Integer.parseInt(data.substring(0, 4)) ;
		int month = Integer.parseInt(data.substring(4, 6))-1 ;
		int date = Integer.parseInt(data.substring(6, 8)) ;
		int hourOfDay = Integer.parseInt(data.substring(8, 10)) ;
		int minute = Integer.parseInt(data.substring(10, 12)) ;
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.set(year, month, date, hourOfDay, minute);
		 
		return calendar.getTime();
	}
	
	/**
	 */	
	public static Date createDateFromMillesecondsTime(long millesecondsTime) throws RuntimeException{
		SimpleDateFormat sDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); 
		
		String S = sDate.format(millesecondsTime);
		
		return createDate(S, "MM/dd/yyyy HH:mm:ss");
	}
	
	
	
	
	/**
	 * Transforma uma data para o ultimo segundo dessa data.
	 * Por exemplo: se a data recebida for 12-01-2012 11:40:34 retorna 12-01-2012 23:59:59
	 * 		
	 * @author Jorge Boane
	 */	
	public static Date changeToUltimoSegundoDoDia(Date date) {
		Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(date);
	    
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
		 
		return calendar.getTime();
	}	
	
	/**
	 * Transforma uma data para o primeiro segundo dessa data.
	 * Por exemplo: se a data recebida for 12-01-2012 11:40:34 retorna 12-01-2012 00:00:00
	 * 		
	 * @author Jorge Boane
	 */	
	public static Date changeToPrimeiroSegundoDoDia(Date date) {
		Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(date);
	    
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
		 
		return calendar.getTime();
	}	
	
	/**
	 * Calcula a direrença entre duas datas; a diferença E em dias
	 * @param dataMaior 
	 * @param dataMenor
	 * @return
	 */
	
	public static double dateDiff(Date dataMaior, Date dataMenor){
	    double differenceMilliSeconds =  dataMaior.getTime() - dataMenor.getTime();   
		
		return differenceMilliSeconds/1000/60/60/24;       
	}
	
	/**
	 * Verifica se a 'dataObservacao' esta entre a 'dataInicio' e 'dataFim' ou nao.
	 * @param dataObservacao
	 * @param dataInicio
	 * @param dataFim
	 * @return 'true' se a 'dataObservacao' estiver entre 'dataInicio' e 'dataFim' ou se for igual a uma destas datas;
	 */
	public static boolean isBetween (Date dataObservacao, Date dataInicio, Date dataFim) {
		 if (dateDiff(dataFim, dataInicio) < 0) return false;
		 
		 double diffExtremoEsquerdo = dateDiff(dataObservacao, dataInicio);
		 
		 double diffExtremoDireito = dateDiff(dataObservacao, dataFim);
		 
		 return diffExtremoEsquerdo*diffExtremoDireito <= 0;
	}
	
	
	/**
	 * Check if date1 is same to date2. "same" meaning the significative parte of date (dd-mm-yyyy) is equals
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean isTheSameDate(Date date1, Date date2){
		double dif = DateAndTimeUtilities.dateDiff(date1, date2);
		
		return !(dif>1 || dif < -1);
	}
	
	/**
	 * Calcula a direrença entre duas datas; a diferença E em dias
	 * @param dataMaior 
	 * @param dataMenor
	 * @return
	 */
	
	public static double dateDiff(Date dataMaior, Date dataMenor, String dateFormat){
	    
		double differenceMilliSeconds =  dataMaior.getTime() - dataMenor.getTime();   
		     
		
		 if (dateFormat.equals(DateAndTimeUtilities.MILLISECOND_FORMAT)) return differenceMilliSeconds;
			
		 double diferenceSeconds = differenceMilliSeconds/1000;
			
		 if (dateFormat.equals(DateAndTimeUtilities.SECOND_FORMAT)) return diferenceSeconds;
			
		double diferenceMinutes = diferenceSeconds/60;
		
		if (dateFormat.equals(DateAndTimeUtilities.MINUTE_FORMAT)) return diferenceMinutes;
			
		double diferenceHours = diferenceMinutes/60;
		
		if (dateFormat.equals(DateAndTimeUtilities.HOUR_FORMAT)) return diferenceHours;
		
		double diferenceDays = diferenceHours/24;
		
		if (dateFormat.equals(DateAndTimeUtilities.DAY_FORMAT)) return diferenceDays;
			
		double diferenceMonts = diferenceDays/30;
		
		if (dateFormat.equals(DateAndTimeUtilities.MONTH_FORMAT)) return diferenceMonts;
			
		double diferenceYears = diferenceMonts/12;
				
		if (dateFormat.equals(DateAndTimeUtilities.YEAR_FORMAT)) return diferenceYears;
		 
		throw new ForbiddenOperationException("UNKOWN DATE FORMAT [" + dateFormat + "]");       
	}
	
	/**
	 * 
	 * @param dataMaior
	 * @param dataMenor
	 * @param excludedDates
	 * @return
	 */
	public static int countPassedDays(Date dataMaior, Date dataMenor, List<Date> excludedDates){
		List<Date> dates = generateDateListBetween(dataMenor, dataMaior);
	
		int qtdPaassedDays = 0;
		
		for (int i=0; i < dates.size();i++){
			if (!existOnList(dates.get(i), excludedDates)) qtdPaassedDays++;
		}
		
		/*
		 * Remover o primeiro dia do periodo
		 */
		qtdPaassedDays = qtdPaassedDays-1;
		
		return qtdPaassedDays;
	}
	
	/**
	 * Gera uma lista de datas 
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<Date> generateDateListBetween(Date start, Date end){
		int qtdDays = (int)dateDiff(end, start);
		
		ArrayList<Date> dates = new ArrayList<Date>();
		
		dates.add(start);
		
		for (int i=1; i < qtdDays; i++){
			dates.add(addDaysDate(dates.get(i-1), 1));
		}
		
		dates.add(end);
		
		return dates;
	}

	/**
	 * Gera uma lista de datas a partir da data inidicada no campo 'start'.
	 * A lista gerada ter 'qtdDaysToGenerate' datas e a diferenca entre as datas seguidas sera 'qtdDaysDiff'
	 * @param start: inicio
	 * @param qtdDaysToGenerate: qtd de datas a gerar 
	 * @param qtdDaysDiff: diferenca entre as datas seguidas
	 * @return
	 */
	public static List<Date> generateDateListStartingFrom(Date start, int qtdDaysToGenerate, int qtdDaysDiff){
		ArrayList<Date> dates = new ArrayList<Date>();
		
		dates.add(start);
		
		for (int i=1; i < qtdDaysToGenerate; i++){
			dates.add(addDaysDate(dates.get(i-1), qtdDaysDiff));
		}
				
		return dates;
	}	
	
	/**
	 * Adiciona uma um perido de tempo (em horas) a uma data passada pelo parametro
	 * @param dataInicial
	 * @param qtdHours
	 * @return Retorna a dataInicial adicionada ao tempo passado pelo parametro qtdHours
	 */
	public static Date addHoursToDate(Date dataInicial, int qtdHours){     
	    Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(dataInicial);
	       	   
	    calendar.add(Calendar.HOUR_OF_DAY, qtdHours);
	    
		return calendar.getTime();
	}
	
	
	/**
	 * Adiciona uma um perido de tempo (em minutos) a uma data passada pelo parametro
	 * @param dataInicial
	 * @param qtdMinutes
	 * @return Retorna a dataInicial adicionada ao tempo passado pelo parametro qtdMinutes
	 */
	public static Date addMinutesToDate(Date dataInicial, int qtdMinutes){     
	    Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(dataInicial);
	       	   
	    calendar.add(Calendar.MINUTE, qtdMinutes);
	    
		return calendar.getTime();
	}
	
	/**
	 * Adiciona uma um perido de tempo (em dias) a uma data passada pelo parametro
	 * @param dataInicial
	 * @param qtdDias
	 * @return Retorna a dataInicial adicionada ao tempo passado pelo parametro qtdDias
	 */
	public static Date addDaysDate(Date dataInicial, int qtdDias){     
	    Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(dataInicial);
	       	   
	    calendar.add(Calendar.DAY_OF_YEAR, qtdDias);
	    
		return calendar.getTime();
	}
	
	/**
	 * Adiciona uma um perido de tempo (em anos) a uma data passada pelo parametro
	 * @param dataInicial
	 * @param qtdDias
	 * @return Retorna a dataInicial adicionada ao tempo passado pelo parametro qtdAnos
	 */
	public static Date addYearsDate(Date dataInicial, int qtdAnos){     
	     
	    int ano = DateAndTimeUtilities.getYear(dataInicial)+qtdAnos;
	    int mes = DateAndTimeUtilities.getMonth(dataInicial);
	    int dia = DateAndTimeUtilities.getDayOfMonth(dataInicial);
	    
		String newDate = dia + "-" + mes + "-" + ano;
		
	    return createDate(newDate, DATE_FORMAT);
	    
	    /*
	    Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(dataInicial);
	 
	    calendar.add(Calendar.DAY_OF_YEAR, qtdAnos*365);
	    
		return calendar.getTime();
		*/
	}	
	
	public static void main(String[] args) {
		//Connection conn = BaseDAO.openConnection("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@192.1:1521:lims", "lims", "exi2k12");
		
		
		System.out.println(determineDateFormat("14-03-2019"));
		
		
		/*List<Date> holidays = generatePublicHolidays(2019, 2019, "mz");
		
		Date oldDate = createDate("26-04-2019 12:44:35", DATE_TIME_FORMAT);
		
		Date datePlus = addDaysDate(oldDate, 40);
		
		System.out.println(datePlus);
		System.out.println(getNextBusinessDate(addDaysDate(datePlus, -1), holidays));*/
		
	}
	
	
	/**
	 * @author Jorge Boane
	 * @return Retorna a segunda feira da semana cuja data esta especificada no parametro Hoje 
	 * @param  date
	 */
	
	public static Date getPrimeiroDiaDaSemana(Date hoje){
		int diaDaSemana;
	    int diasDecorridos;
	        
	    Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(hoje);
	    
	    diaDaSemana = calendar.get(Calendar.DAY_OF_WEEK) ; // 1==DOM, 2==SEG, 3==TER, 4==QUA, 5==QUI, 6==SEXTA, 7==SAB
	    
	    diasDecorridos = diaDaSemana - 2;// dias decorridos desde a primeira 2a-feira imediatamente antes da data Hoje
	    
	    diasDecorridos = (diasDecorridos + 7) % 7; //PARA ANULAR O SINAL NEGATIVO NO CASO DE diaDeSemana==1==DOMINGO
	    	    
	    calendar.add(Calendar.DATE, -diasDecorridos);//Subrair o numero de dias decorridos desde a 2a feira ultima
	    
	    return calendar.getTime();
	}
	
	/**
	 * @author Jorge Boane
	 * @param  date
	 */
	
	public static Date getPrimeiroSegundoDoDia(Date hoje){
		hoje = setHour(hoje, 0);
		hoje = setMinute(hoje,0);
		hoje = setSecond(hoje, 1);
		
		return hoje;
	}
	
	/**
	 * @author Jorge Boane
	 * @return Retorna o primeiro dia do mes cuja data esta especificada no parametro Hoje 
	 * @param  date
	 * @throws ParseException 
	 */
	
	public static Date getPrimeiroDiaDoMes(Date hoje, String format){
		Calendar calendar = Calendar.getInstance();
	    
	    calendar.setTime(hoje);
	   
	    
	    return createDate("01-"+ (calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.YEAR) , format);
	}
	
	/**
	 * @author Jorge Boane
	 * @return Retorna o primeiro dia do mes cuja data esta especificada no parametro Hoje 
	 * @param  date
	 * @throws ParseException 
	 */
	
	public static Date getPrimeiroDiaDoMes(Date hoje) {
		return getPrimeiroDiaDoMes(hoje, DateAndTimeUtilities.DATE_FORMAT);
	}
	
	
	
	/**
	 * @author Jorge Boane
	 * @return Retorna o ultimo dia do mes cuja data esta especificada no parametro Hoje 
	 * @param  date
	 * @throws ParseException 
	 */
	
	public static Date getUltimoDiaDoMes(Date hoje, String format){
		 return createDate(getDiasDoMes(getMonth(hoje), getYear(hoje))+"-"+ getMonth(hoje) + "-" + getYear(hoje), format);  
	}
	
	public static int getDiasDoMes(int mes, int ano){
		switch (mes) {
			case 1: return 31;
			case 2: return isBissextoAno(ano) ? 29 : 28;
			case 3: return 31;
			case 4: return 30;
			case 5: return 31;
			case 6: return 30;
			case 7: return 31;
			case 8: return 31;
			case 9: return 30;
			case 10: return 31;
			case 11: return 30;
			case 12: return 31;
			
			default: throw new ForbiddenOperationException("Mes invalido");
		}
	}
	public static boolean isBissextoAno(int ano){
		if (ano%400==0) return true;
		
		if (ano%4 == 0 && ano%100 != 0) return true;
		
		return false;
	}
	
	/**
	 * @author Jorge Boane
	 * @return Retorna o primeiro dia do mes cuja data esta especificada no parametro Hoje 
	 * @param  date
	 * @throws ParseException 
	 */
	
	public static Date getUltimoDiaDoMes(Date hoje) {
		return getUltimoDiaDoMes(hoje, DateAndTimeUtilities.DATE_FORMAT);
	}	
	
	/**
	 * Considera-se que o ano E válido se estiver no intervalo [minAno, maxAno]
	 * 
	 * @param ano
	 * @param minAno
	 * @param maxAno
	 * @return
	 */
	public static boolean isValidAno(int ano, int minAno, int maxAno){
		return ano >= minAno && ano <= maxAno;
	}
	
	/**
	 * Considera-se que o ano E válido se tiver 4 dígito e for maior que 0;
	 * @param ano
	 * @return
	 */
	public static boolean isValidAno(int ano){
		String strAno = ""+ano;
		
		return strAno.length() == 4 && ano > 0;
	}
	
	/**
	 * Verifica se a data recebida pelo parametro E maior que a data corrente
	 * @param data
	 * @return true se a data recebida for maior que a data corrente
	 */
	public static boolean chechDateMaiorQueDataCorrente(Date data){
		return DateAndTimeUtilities.dateDiff(DateAndTimeUtilities.getCurrentDate(), data, DateAndTimeUtilities.DAY_FORMAT) < 0;
	}
	
	/**
	 * Verifica se a data recebida pelo parametro E maior que a data corrente
	 * @param data
	 * @return true se a data recebida for maior que a data corrente
	 */
	public static boolean chechDateMenorQueDataCorrente(Date data){
		return DateAndTimeUtilities.dateDiff(DateAndTimeUtilities.getCurrentDate(), data, DateAndTimeUtilities.DAY_FORMAT) > 0;
	}
	
	static public Date getCurrentSystemDate(Connection conn) {
		try {
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.MYSQL_DATABASE)){
				return tryToGetMySQLDate(conn);
			}
			
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.ORACLE_DATABASE)) {
				return tryToGetOracleDate(conn);
			}
			
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.POSTGRES_DATABASE)) {
				return tryToGetPostgresDate(conn);	
			}
			
			throw new ForbiddenOperationException("Base de Dados nao suportada!");
		} catch (SQLException e) {
			throw new RuntimeException(e);
			
		}
	}
	
	
	/**
	 * @Tenta obter a data do sistema assumindo que o motor de base de dados E
	 *        MySQL Caso o motor nAo seja o que se presume, rebenta uma excepçAo
	 * @param context
	 * @return
	 */
	@SuppressWarnings("resource")
	private static Date tryToGetMySQLDate(Connection conn) {

		Date data = null;

		Statement st = null;
		ResultSet rs = null;
		String sql = "";
		ArrayList<String> logs = new ArrayList<String>();


		try {

			st = conn.createStatement();
			logs.add("Opened st");
			
			sql = "SELECT CURRENT_TIMESTAMP as data";

			rs = st.executeQuery(sql);
			logs.add("Opened rs");
			
			while (rs.next()) {
				data =  DateAndTimeUtilities.createDate(DateAndTimeUtilities.formatToDDMMYYYY_HHMISS(rs.getTimestamp("data")), DateAndTimeUtilities.DATE_TIME_FORMAT);
			}
		} catch (SQLException e) {
			
		} finally {
			try {
				st.close();
				logs.add("Closed st");
				rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				//e.printStackTrace();
			}
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
		}

		return data;
	}
	


	/**
	 * @Tenta obter a data do sistema assumindo que o motor de base de dados E
	 *        Postgres Caso o motor nAo seja o que se presume, rebenta uma excepçAo
	 * @param context
	 * @return
	 */
	@SuppressWarnings("resource")
	private static Date tryToGetPostgresDate(Connection conn) {
		ArrayList<String> logs = new ArrayList<String>();
		Date data = null;

		ResultSet rs = null;
		String sql = "";
		
		PreparedStatement st = null;
		
	
		try {
				
			sql = "SELECT current_timestamp as data";
			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.executeQuery();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			while (rs.next()) {
				data = rs.getTimestamp("data");
			}
		} catch (SQLException e) {
			//e.printStackTrace();
			
		} finally {
			try {
				st.close();
				logs.add("Closed st");
				rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				//e.printStackTrace();
			}
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
		}

		return data;
	}
	
/**
	 * @Tenta obter a data do sistema assumindo que o motor de base de dados E
	 *        ORACLE Caso o motor nAo seja o que se presume, rebenta uma
	 *        excepçAo
	 * @param context
	 * @return
	 */
	private static Date tryToGetOracleDate(Connection conn) {
		Date data = null;
		
		Statement st = null;
		ResultSet rs = null;
		String sql = "";
		ArrayList<String> logs = new ArrayList<String>();
		

		try {

			st = conn.createStatement();
			logs.add("Opened st");
			
			sql = "SELECT sysdate as data FROM dual";

			rs = st.executeQuery(sql);
			logs.add("Opened rs");

			while (rs.next()) {
				data =  DateAndTimeUtilities.createDate(DateAndTimeUtilities.formatToDDMMYYYY_HHMISS(rs.getTimestamp("data")), DateAndTimeUtilities.DATE_TIME_FORMAT);
			}

		} catch (Exception e) {
			
		} finally {
			try {
				st.close();
				logs.add("closed st");
				rs.close();
				logs.add("closed rs");
			} catch (Exception e) {
				//e.printStackTrace();
			}	
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
		}

		return data;
	}
		
	/**
	 * Retorna a proxiam data util a partir da data indicada pelo parametro.
	 * Note que este mEtodo considera dias nAo uteis SABADOS e DOMINGO se qtdWorkDays=5 ou APENAS DOMINGOS se qtdWorkDays=6
	 * 
	 * @param currDate
	 * @param qtdWorkDays
	 * @return
	 * @throws ForbiddenOperationException Se qtdWorkDays > 6 || qtdWorkDays < 5
	 */
	
	public static Date getNextBusinessDate(Date currDate, int qtdWorkDays) throws ForbiddenOperationException{
		currDate = addDaysDate(currDate, 1);
		
		if (isWeekend(currDate, qtdWorkDays)) return getNextBusinessDate(currDate, qtdWorkDays);
		
		return currDate;
	}
	
	public static List<Date> generatePublicHolidays(int startYear, int lastYear, String countryCode){
		
		List<Date> holidays = new ArrayList<Date>();
		
		String[] holidaysDates = { 	 "25-06",                            
									 "25-09",                            
									 "04-10",                            
									 "01-05",                            
									 "15-10",                            
									 "03-02",                            
									 "07-04",                            
									 "07-09",                            
									 "01-01",                            
									 "25-12"};
							
		
		if (countryCode.equalsIgnoreCase("MZ")){
			for (int i = startYear; i <= lastYear; i++){
				
				for (String holiday : holidaysDates){
					
					Date hd = createDate(holiday+ "-" +i);
					
					if (getDayOfWeek(hd) == Calendar.SUNDAY){
						hd = addDaysDate(hd, 1);
					}
					
					holidays.add(hd);
				}
			}
			
			return holidays;
		}
		
		throw new ForbiddenOperationException("Country Code Not supported [" + countryCode + "]");
		
	}
	

	/**
	 * Retorna a proxima data util a partir da data indicada pelo parâmetro.
	 * Note que este mEtodo considera dias nAo uteis SABADOS e DOMINGO se qtdWorkDays=5 ou APENAS DOMINGOS se qtdWorkDays=6 + os feriados em holidays
	 * 
	 * @param currDate
	 * @param qtdWorkDays
	 * @return
	 * @throws ForbiddenOperationException Se qtdWorkDays > 6 || qtdWorkDays < 5
	 */
	
	public static Date getNextBusinessDate(Date currDate, int qtdWorkDays, List<Date> holidays) throws ForbiddenOperationException{
		currDate = addDaysDate(currDate, 1);
		
		if (isWeekend(currDate, qtdWorkDays) || existOnList(currDate, holidays)) return getNextBusinessDate(currDate, qtdWorkDays, holidays);
		
		return currDate;
	}
	
	/**
	 * Retorna a proxima segunda feira a partir da data indicada pelo parametro. Se a data indicada for segunda-feira, será retornada a mesma data
	 * 
	 * @param date
	 * @return
	 */
	public static Date getNextMonday(Date today){
		if (getDayOfWeek(today) == Calendar.MONDAY) return today;
		
		return getNextMonday(addDaysDate(today, 1));
	}
	
	
	
	public static List<Date> generateWeekendsBetween(Date startDay, Date endDay, int qtdWorkDays){
		List<Date> listDaysBetween = generateDateListBetween(startDay, endDay);
		
		List<Date> weekends = new ArrayList<Date>();
		
		for (int i=0; i < listDaysBetween.size(); i++){
			if (isWeekend(listDaysBetween.get(i), qtdWorkDays)){
				weekends.add(listDaysBetween.get(i));
			}
		}
		
		return weekends;
	}
	
	
	/**
	 * Retorna a proxima data util a partir da data indicada pelo parâmetro.
	 * Note que este mEtodo considera dias nAo uteis SABADOS e DOMINGO + os feriados em holidays
	 * 
	 * @param currDate
	 * @param qtdWorkDays
	 * @return
	 */
	
	public static Date getNextBusinessDate(Date currDate, List<Date> holidays) {
		currDate = addDaysDate(currDate, 1);
		
		int qtdWorkDays=5;
		
		if (isWeekend(currDate, qtdWorkDays) || existOnList(currDate, holidays)) 
			return getNextBusinessDate(currDate, holidays);
		
		return currDate;
	}
	
	private static boolean existOnList(Date date, List<Date> dayList){
		if (!FuncoesGenericas.arrayHasElement(dayList)) return false;
		
		for (Date d : dayList){
			if (compareTo(formatToDDMMYYYY(date), formatToDDMMYYYY(d)) == 0) return true;
		}
		
		return false;
	}
	
	/**
	 * Retorna a proxima data util a partir de hoje.
	 * Note que este mEtodo considera dias nAo uteis SABADOS e DOMINGO
	 * @param currDate
	 * @return
	 * @throws ForbiddenOperationException Se qtdWorkDays > 6 || qtdWorkDays < 5
	 */
	
	public static Date getNextBusinessDate(Date currDate){
		currDate = addDaysDate(currDate, 1);
		
		int qtdWorkDays = 5;
		
		if (isWeekend(currDate, qtdWorkDays)) return getNextBusinessDate(currDate, qtdWorkDays);
		
		return currDate;
	}
	
	private static boolean isWeekend(Date date, int qtdWorkDays){
		
		switch (qtdWorkDays) {
		case 5:{//Se a semana de trabalho tiver 5 dias entao SABADO E DOMINGO NAO SAO DIAS UTEIS 
				return getDayOfWeek(date) == Calendar.SUNDAY || getDayOfWeek(date) == Calendar.SATURDAY;
			  }
		case 6:{//Se a semana de trabalho tiver 6 dias entao DOMINGO NAO EH DIA UTIL 
			return getDayOfWeek(date) == Calendar.SUNDAY ;
		  }

		default: throw new ForbiddenOperationException("A quantidade de dias de trabalho nao pode ser inferior a 5 nem superior a 6");
		}
	}
	
	public static List<Date> getAllDatesOnTable(String table, String field, String condition, Connection conn){
		Statement st = null;
		ResultSet rs = null;
		
		List<Date> listOfDates= new ArrayList<Date>();
		
		try {
			st = conn.createStatement();
			
			String sql = " SELECT " + field + " FROM " +table + " WHERE 1 = 1 " + (FuncoesGenericas.stringHasValue(condition) ? " AND "+ condition : "");

			rs = st.executeQuery(sql);
			
			while (rs.next()) {
				listOfDates.add(rs.getDate(field));
			}
		} catch (SQLException e) {
			
		} finally {
			try {
				st.close();
				rs.close();
			} catch (Exception e) {
			}
			finally{
			}
		}
		
		return listOfDates;
	}
}
