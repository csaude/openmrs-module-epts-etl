package org.openmrs.module.eptssync.model;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.utilities.CommonUtilities;

public class ClauseFromElements {
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static final String LEFT_JOIN_CLAUSE = "LEFT";
	public static final String RIGHT_JOIN_CLAUSE = "RIGHT";
	public static final String INNER_JOIN_CLAUSE = "INNER";
	
	
	private int firstTabPosition;
	private int firstTabAliasPosition;
	
	private int secondTabPosition;
	private int secondTabAliasPosition;
	
	private int onClausePosition;
	
	private String firstTab;
	private String firstTabAlias;
	
	private String joinClause;
	
	private String secondTab;
	private String secondTabAlias;
	
	private String joinCondition;
	
	public ClauseFromElements(){
		firstTabPosition = -1;
		firstTabAliasPosition = -1;
		
		secondTabPosition = -1;
		secondTabAliasPosition = -1;
	
		firstTab="";
		firstTabAlias="";
		
		secondTab="";
		secondTabAlias="";
	}
	
	/**
	 * Indica se a string passada pelo parametro é uma clausula de juncao "LEFT", "INNER", "RIGHT"
	 * @param sql
	 * @return
	 */
	public static boolean isJoinClause(String sql){
		return utilities.isStringIn(sql, LEFT_JOIN_CLAUSE, INNER_JOIN_CLAUSE, RIGHT_JOIN_CLAUSE);
	}
	
	
	/**
	 * @param sql
	 * @return
	 */
	public static boolean isConditionConcatenation(String sql){
		return utilities.isStringIn(sql, "AND", "OR");
	}
	
	private static boolean isReservedWord(String sql){
		return utilities.isStringIn(sql, LEFT_JOIN_CLAUSE, INNER_JOIN_CLAUSE, RIGHT_JOIN_CLAUSE, "ON", "AND", "OR");
	}
	
	
	/**
	 * Gera 
	 * @param clauseFlormString
	 * @return
	 */
	public static void generateAllFromClauseFromString(String clauseFromString,  List<ClauseFromElements> allreadyGeneratedClause){
		if (!utilities.stringHasValue(clauseFromString)) return;
		
		int i =1;
		
		String nextClause = "";
		String newClausulaFromString = clauseFromString;
		
		String[] nextElements = newClausulaFromString.split("[\\s*]+");
			
		nextClause = utilities.concatStrings(nextClause, nextElements[0], " ");
		
		
		for (i=1; i<nextElements.length; i++){
			boolean isToCut = isNextEndClause(nextElements[i]) && i > 2;//Indica se nao é pra cortar daqui (Se true, significa que nao é pra cortar)
			boolean isAtTheEnd =  i+1 == nextElements.length;//Indica se esta no fim da clausula
			
			if (!isToCut && !isAtTheEnd) {
				nextClause = utilities.concatStrings(nextClause, nextElements[i], " ");
			}
			else{
				
				if (isAtTheEnd) {
					nextClause = utilities.concatStrings(nextClause, nextElements[i], " ");
					i++;
				}
				
				allreadyGeneratedClause.add(generateFromFullClauseFromString(nextClause));
				break;
			}
		}
		
		if (i==1) {//A clausula so é composta de uma unica tabela
			allreadyGeneratedClause.add(generateFromFullClauseFromString(nextClause));
		}
		
		nextClause = "";
		
		for (int j=i; j<nextElements.length; j++){
			nextClause = utilities.concatStrings(nextClause, nextElements[j], " ");
		}
		
		generateAllFromClauseFromString(nextClause, allreadyGeneratedClause);
	}
	
	public static boolean isNextEndClause(String str){
		return isJoinClause(str);
	}
	
	/**
	 * Gera os elementos da clausula from de uma unica tabela ou de duas tabelas unidas por um "JOIN"
	 *  
	 * @param clauseFlormString
	 * @return
	 */
	public static ClauseFromElements generateFromFullClauseFromString(String clauseFlormString){
		/*
		 * possible structures
		 * 1. 
		 * 		table1
		 *  
		 * 2. 
		 * 		table1 JOIN_CLAUSE JOIN table2 ON FIED
		 * 3.
		 * 		table1 aliastab1 JOIN_CLAUSE JOIN table2 ON condition
		 * 4.     
		 * 		table1 JOIN_CLAUSE JOIN table2 aliastab2 ON condition
		 * 5.		
		 * 		table1 JOIN_CLAUSE JOIN table2 aliastab2 ON condition
		 */
		
		ClauseFromElements c = new ClauseFromElements();
		
		String[] elements = clauseFlormString.split("[\\s*]+");
		
		if (elements.length == 1){
			c.firstTab = elements[0];
			c.firstTabPosition = 0;
			c.firstTabAliasPosition = 0;
		}
		else{
			c.firstTabPosition = isJoinClause(elements[0]) ? -1 : 0;//If isJoinClause, assume there is no firstTab
			
			c.firstTab = c.firstTabPosition>=0 ? elements[c.firstTabPosition] : "";
			
			c.firstTabAliasPosition = c.firstTabPosition>=0 ? (!isReservedWord(elements[c.firstTabPosition+1]) ? c.firstTabPosition+1 : c.firstTabPosition) : -1;//If the next word is not a reserved word, assume it is a ALIAS
			
			c.firstTabAlias =  c.firstTabPosition != c.firstTabAliasPosition ? elements[c.firstTabAliasPosition] : "";
			
			c.joinClause = elements[c.firstTabAliasPosition+1];
			
			c.secondTabPosition = c.firstTabAliasPosition + 3;
			
			c.secondTab =  elements[c.secondTabPosition];
			
			c.secondTabAliasPosition  = !isReservedWord(elements[c.secondTabPosition+1]) ? c.secondTabPosition+1 : c.secondTabPosition;
			
			c.secondTabAlias =  c.secondTabAliasPosition != c.secondTabPosition ? elements[c.secondTabAliasPosition] : "";
			
			c.onClausePosition = c.secondTabAliasPosition + 1;
			
			c.joinCondition = "";
			
			for (int i=c.onClausePosition+1; i < elements.length; i++){
				c.joinCondition = utilities.concatStrings(c.joinCondition, elements[i], " ");
			}
		}
		return c;
	}
	
	
	public static void main(String[] args) {
		List<ClauseFromElements> list = new ArrayList<ClauseFromElements>();
		
		ClauseFromElements.generateAllFromClauseFromString("PESSOA P INNER JOIN CLIENTE C ON CLIENTE.PESSOA_ID = PESSOA.SELF_ID " +
																	"INNER JOIN DOCUMENTO_IDENTIFICACAO DOC ON DOCUMENTO_IDENTIFICACAO.PESSOA_ID = PESSOA.SELF_ID " +
																	"LEFT JOIN PESSOA_SINGULAR ON PESSOA_SINGULAR.PESSOA_ID = P.SELF_ID", list);
		
		for (ClauseFromElements c : list)
		System.out.println(c);
		//e.changeTablePositions();
		//System.out.println(e);
	}
	
	public String getFirstTab() {
		return firstTab;
	}
	public void setFirstTab(String firstTab) {
		this.firstTab = firstTab;
	}
	public String getSecondTab() {
		return secondTab;
	}
	public void setSecondTab(String secondTab) {
		this.secondTab = secondTab;
	}
	public String getJoinClause() {
		return joinClause;
	}
	public void setJoinClause(String joinClause) {
		this.joinClause = joinClause;
	}
	public String getJoinCondition() {
		return joinCondition;
	}
	public void setJoinCondition(String joinCondition) {
		this.joinCondition = joinCondition;
	}
	
	public void changeTablePositions(){
		String bkTable1 = firstTab;
		String bkTable1Alias = firstTabAlias;
		
		this.firstTab = this.secondTab;
		this.firstTabAlias = this.secondTabAlias;
		
		this.secondTab = bkTable1;
		this.secondTabAlias = bkTable1Alias;
	}
	
	public int getSecondTabAliasPosition() {
		return secondTabAliasPosition;
	}

	public void setSecondTabAliasPosition(int secondTabAliasPosition) {
		this.secondTabAliasPosition = secondTabAliasPosition;
	}


	public int getOnClausePosition() {
		return onClausePosition;
	}


	public void setOnClausePosition(int onClausePosition) {
		this.onClausePosition = onClausePosition;
	}


	public String getFirstTabAlias() {
		return firstTabAlias;
	}


	public void setFirstTabAlias(String firstTabAlias) {
		this.firstTabAlias = firstTabAlias;
	}


	public String getSecondTabAlias() {
		return secondTabAlias;
	}


	public void setSecondTabAlias(String secondTabAlias) {
		this.secondTabAlias = secondTabAlias;
	}


	@Override
	public String toString() {
		return (firstTab + " " + firstTabAlias).trim() + " " + joinClause + " JOIN " + (secondTab + " " + secondTabAlias).trim() + " ON " + joinCondition;
	}
}
