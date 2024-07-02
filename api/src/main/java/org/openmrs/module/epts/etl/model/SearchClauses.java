package org.openmrs.module.epts.etl.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.VO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.FuncoesGenericas;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilitie;

/**
 * Classe auxiliar para representar as clausulas de uma pesquisa. Uma clausula de pesquisa e
 * composta por uma String, representando uma sequencia de clausulas SQL e uma lista de parametros
 * associada a essas clausula bem como uma sequencia de clausulas para a clausula FROM
 * 
 * @author JPBOANE
 * @version 1.0 18/07/2013 Quelimane
 */
public class SearchClauses<T extends VO> {
	
	/**
	 * Sequencia de clausulas
	 */
	private String clauses;
	
	/**
	 * Sequencia de clausulas para o havin
	 */
	private String havingClauses;
	
	/**
	 * Valores dos Paramentros para as clausulas
	 */
	private Object[] parameters;
	
	/**
	 * Contem as colunas a seleccionar
	 */
	private String columnsToSelect;
	
	/**
	 * Clausula from e todas as tabelas associadas
	 */
	private String clauseFrom;
	
	private String groupingFields;
	
	private String orderByFields;
	
	private String orderByType;
	
	/**
	 * Indica se o select possui a clausula distinct
	 */
	private boolean distinctSelect;
	
	/**
	 * Default column to select on distinct selections
	 */
	private String defaultColumnToSelect;
	
	private AbstractSearchParams<T> searchParameters;
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public SearchClauses(AbstractSearchParams<T> searchParameters) {
		clauses = "";
		clauseFrom = "";
		columnsToSelect = "";
		groupingFields = "";
		orderByFields = "";
		havingClauses = "";
		orderByType = "ASC";
		
		parameters = new Object[0];
		
		this.searchParameters = searchParameters;
	}
	
	/**
	 * Adiciona uma coluna ou varias (separadas por virgula) as colunas de selecao
	 * 
	 * @param columnToSelect
	 */
	public void addColumnToSelect(String columnToSelect) {
		this.columnsToSelect += FuncoesGenericas.stringHasValue(this.columnsToSelect) ? "\n" : "";
		this.columnsToSelect = FuncoesGenericas.concatStringsWithSeparator(this.columnsToSelect, columnToSelect, ",");
	}
	
	public void addToGroupingFields(String field) {
		this.groupingFields += FuncoesGenericas.stringHasValue(this.groupingFields) ? "\n" : "";
		this.groupingFields = FuncoesGenericas.concatStringsWithSeparator(this.groupingFields, field, ",");
	}
	
	/**
	 * Adiciona todas as colunas actualmente no "columnsToSelect" ao groupingFields
	 */
	public void addAllColumnsToGroupFields() {
		this.groupingFields = columnsToSelect;
	}
	
	public void addToOrderByFields(String... field) {
		for (String str : field) {
			addToOrderByFields(str);
		}
	}
	
	public void addToOrderByFields(String field) {
		String[] elements = field.toUpperCase().split(" ");
		
		if (elements.length > 1) {
			if (utilities.isStringIn(elements[elements.length - 1], "DESC", "ASC")) {
				String newStr = "";
				
				for (int i = 0; i < elements.length - 1; i++) {
					newStr += elements[i] + " ";
				}
				
				field = newStr;
				
				this.orderByType = elements[elements.length - 1];
			}
		}
		
		this.orderByFields += FuncoesGenericas.stringHasValue(this.orderByFields) ? "\n" : "";
		this.orderByFields = FuncoesGenericas.concatStringsWithSeparator(this.orderByFields, field, ",");
	}
	
	public String getDefaultColumnToSelect() {
		return defaultColumnToSelect;
	}
	
	public void setDefaultColumnToSelect(String defaultColumnToSelect) {
		this.defaultColumnToSelect = defaultColumnToSelect;
	}
	
	public boolean isDistinctSelect() {
		return distinctSelect;
	}
	
	public void setDistinctSelect(boolean distinctSelect) {
		this.distinctSelect = distinctSelect;
	}
	
	/**
	 * Adiciona novo (s) elemento (s) a clausula forma
	 * 
	 * @param anotherFromClause Ex: "INNER JOIN PESSOA ON PESSOA.SELF_ID = TABELA_BASE.PESSOA_ID ".
	 *            Onde TABELA_BASE e uma tabela previamente adicionada�a clausula FROM
	 */
	public void addToClauseFrom(String anotherFromClause) {
		if (!utilities.stringHasValue(anotherFromClause))
			return;
		
		this.clauseFrom += (FuncoesGenericas.stringHasValue(this.clauseFrom) ? "\n" : "");
		this.clauseFrom = FuncoesGenericas.concatStrings(this.clauseFrom, anotherFromClause);
	}
	
	public void addToClauses(String condition) {
		if (!utilities.stringHasValue(condition))
			return;
		
		this.clauses += (FuncoesGenericas.stringHasValue(this.clauses) ? "\n" : "");
		this.clauses = FuncoesGenericas.concatCondition(clauses, "(" + condition + ")");
	}
	
	public void addToClauses(String condition, String operator) {
		if (!utilities.stringHasValue(condition))
			return;
		
		this.clauses += (FuncoesGenericas.stringHasValue(this.clauses) ? "\n" : "");
		this.clauses = FuncoesGenericas.concatCondition(clauses, condition, operator);
	}
	
	public void addToHavingClauses(String condition) {
		this.havingClauses += (FuncoesGenericas.stringHasValue(this.havingClauses) ? "\n" : "");
		this.havingClauses = FuncoesGenericas.concatCondition(havingClauses, "(" + condition + ")");
	}
	
	public void addToHavingClauses(String condition, String operator) {
		if (!utilities.stringHasValue(condition))
			return;
		
		this.havingClauses += (FuncoesGenericas.stringHasValue(this.havingClauses) ? "\n" : "");
		this.havingClauses = FuncoesGenericas.concatCondition(havingClauses, condition, operator);
	}
	
	public void addToParameters(Object parameter) {
		parameters = FuncoesGenericas.setParam(parameters.length, parameters, parameter);
	}
	
	public void addToParameters(Object[] parameters) {
		this.parameters = FuncoesGenericas.setParam(this.parameters, parameters);
	}
	
	public String getClauses() {
		return clauses;
	}
	
	public void setClauses(String clauses) {
		this.clauses = clauses;
	}
	
	public Object[] getParameters() {
		return parameters;
	}
	
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
	
	public String getColumnsToSelect() {
		return columnsToSelect;
	}
	
	public void setColumnsToSelect(String columnsToSelect) {
		this.columnsToSelect = columnsToSelect;
	}
	
	public String getClauseFrom() {
		return clauseFrom;
	}
	
	public void setClauseFrom(String clauseFrom) {
		this.clauseFrom = clauseFrom;
	}
	
	public String getGroupingFields() {
		return groupingFields;
	}
	
	public String getOrderByFields() {
		return orderByFields;
	}
	
	/**
	 * @return o valor do atributo {@link #havingClauses}
	 */
	public String getHavingClauses() {
		return havingClauses;
	}
	
	/**
	 * Modifica o valor do atributo {@link #havingClauses} para o valor fornecido pelo par�metro
	 * <code>havingClauses</code>
	 * 
	 * @param havingClauses novo valor para o atributo {@link #havingClauses}
	 */
	public void setHavingClauses(String havingClauses) {
		this.havingClauses = havingClauses;
	}
	
	public String generateSQL(Connection conn) throws DBException {
		String sql = " SELECT " + (isDistinctSelect() ? " DISTINCT " : "") + columnsToSelect + " \n";
		sql += " FROM   " + clauseFrom + " \n";
		sql += " WHERE  1 = 1 \n";
		sql += (FuncoesGenericas.stringHasValue(clauses) ? " AND " + clauses : "");
		
		sql += "			  	 \n" + (FuncoesGenericas.stringHasValue(groupingFields) ? "GROUP BY " + groupingFields : "");
		sql += "			  	 \n" + (FuncoesGenericas.stringHasValue(havingClauses) ? "HAVING " + havingClauses : "");
		sql += "			  	 \n"
		        + (FuncoesGenericas.stringHasValue(orderByFields) ? "ORDER BY " + orderByFields + " " + this.orderByType
		                : "");
		
		if (this.searchParameters.isPhaseSelected() && !utilities.stringHasValue(groupingFields)) {
			
			if (conn != null)
				sql = SQLUtilitie.createPhasedSelect(sql, this.searchParameters.getStartAt(),
				    this.searchParameters.getQtdRecordPerSelected(), conn);
		}
		
		return sql;
	}
	
	public AbstractSearchParams<T> getSearchParameters() {
		return searchParameters;
	}
	
	public void setSearchParameters(AbstractSearchParams<T> searchParameters) {
		this.searchParameters = searchParameters;
	}
	
	/**
	 * Faz replace de uma coluna na lista de colunas de selecção. Este método é útil, por exemplo
	 * quando uma subclasse de uma determinada classe pretende usar o SearchClause da super classe
	 * Quando há campos sobrepostos.
	 * 
	 * @param column
	 * @param newColumn
	 */
	public void replaceColumn(String column, String newColumn) {
		this.columnsToSelect = this.columnsToSelect.replaceAll(column, newColumn);
	}
	
	/**
	 * Faz replace de um campo na lista de campos na clausula.
	 * 
	 * @param field
	 * @param newField
	 */
	public void replaceColumnOnClause(String field, String newField) {
		this.clauses = this.clauses.replaceAll(field, newField);
	}
	
	/**
	 * Remove uma linha da cláusula FROM dada a alinha
	 * 
	 * @param column
	 * @param newColumn
	 */
	public void removeFromClauseFrom(String clauseFromLine) {
		String auxStr[] = this.clauseFrom.split(clauseFromLine);
		
		if (auxStr.length <= 0)
			return;
		
		if (auxStr.length == 1) {
			clauseFrom = auxStr[0];
		} else if (auxStr.length == 2) {
			clauseFrom = auxStr[0] + auxStr[1];
		} else
			throw new ForbiddenOperationException("A clausula ocorre mais de uma vez");
	}
	
	/**
	 * Remove uma linha da cláusula das condicoes dada a alinha
	 * 
	 * @param column
	 * @param newColumn
	 */
	public void removeFromConditionClause(String conditionClauseLine) {
		String auxStr[] = this.clauses.split(conditionClauseLine);
		
		if (auxStr.length < 0)
			return;
		
		if (auxStr.length == 1) {
			clauses = auxStr[0];
		} else if (auxStr.length == 2) {
			clauses = auxStr[0] + auxStr[1];
		} else
			throw new ForbiddenOperationException("A clausula ocorre mais de uma vez");
	}
	
	/**
	 * Remove uma linha da cláusula dada a alinha
	 * 
	 * @param column
	 * @param newColumn
	 */
	public void removeFromColumnsToSelect(String columnToSelect) {
		String auxStr[] = this.columnsToSelect.split(",");
		
		if (auxStr.length < 0)
			return;
		
		columnsToSelect = "";
		
		for (String column : auxStr) {
			column = column.trim();
			if (!column.equalsIgnoreCase(columnToSelect))
				addColumnToSelect(column);
		}
	}
	
	/**
	 * Remove um campo da clausula Group by
	 * 
	 * @param column
	 * @param newColumn
	 */
	public void removeFromGroupingBy(String columnToRemove) {
		String auxStr[] = this.groupingFields.split(",");
		
		if (auxStr.length < 0)
			return;
		
		this.groupingFields = "";
		
		for (String column : auxStr) {
			column = column.trim();
			if (!column.equalsIgnoreCase(columnToRemove))
				addToGroupingFields(column);
		}
	}
	
	public void clone(SearchClauses<?> toCloneFrom) {
		this.clauses = toCloneFrom.clauses;
		this.parameters = toCloneFrom.parameters;
		this.columnsToSelect = toCloneFrom.columnsToSelect;
		this.clauseFrom = toCloneFrom.clauseFrom;
		this.groupingFields = toCloneFrom.groupingFields;
		this.orderByFields = toCloneFrom.orderByFields;
		this.distinctSelect = toCloneFrom.distinctSelect;
		this.defaultColumnToSelect = toCloneFrom.defaultColumnToSelect;
	}
	
	/**
	 * Add to this 'clausulasDePesquisa' all the clauses from 'anotherClauses'
	 * 
	 * @param anotherClauses
	 * @param joinTable: the table to join the two clauses
	 * @param joinCondition: the condition to join de to clauses
	 */
	public void addToAll(SearchClauses<?> anotherClauses, String joinTable, String joinCondition) {
		this.addToClauses(anotherClauses.clauses);
		this.addToParameters(anotherClauses.parameters);
		this.addColumnToSelect(anotherClauses.columnsToSelect);
		
		List<ClauseFromElements> l = new ArrayList<ClauseFromElements>();
		
		ClauseFromElements.generateAllFromClauseFromString(anotherClauses.clauseFrom, l);
		
		if (utilities.arrayHasElement(l)) {
			anotherClauses.clauseFrom = "";
			
			for (ClauseFromElements c : l) {
				if (utilities.isStringIn(joinTable, c.getFirstTab(), c.getSecondTab())) {
					if (c.getFirstTab().equals(joinTable)) {
						this.addToClauseFrom(
						    "INNER JOIN " + joinTable + " " + c.getFirstTabAlias() + " ON " + joinCondition);
						c.setFirstTab("");
						c.setFirstTabAlias("");
					} else {
						if (true)
							throw new ForbiddenOperationException(
							        "Actualmente o metodo nao suporta a juncao de clausulas cuja tabela de juncao da 2a clausula nao esta na primeira posicao!");
						/*
						if (c.getSecondTab().equals(joinTable)){
							this.addToClauseFrom("INNER JOIN " + joinTable + " " + c.getSecondTabAlias() +   " ON " + joinCondition); 
							c.setSecondTab("");
							c.setSecondTabAlias("");
						}
						*/
					}
				}
				
				if (utilities.stringHasValue(c.getFirstTab()) || utilities.stringHasValue(c.getSecondTab()))
					this.addToClauseFrom(c.toString());
			}
		}
		
		this.addToGroupingFields(anotherClauses.groupingFields);
		this.addToOrderByFields(anotherClauses.orderByFields);
	}
	
	public static final int READ_TYPE = 0;
	
	public void changeOrdeyByTypeToDesc() {
		this.orderByType = "DESC";
	}
	
	public void changeOrdeyByTypeToAsc() {
		this.orderByType = "ASC";
	}
	
	public void generateAndAddInClause(String field, Object[] inValues) {
		generateAndAddInClause(field, utilities.parseArrayToList(inValues));
		
	}
	
	public void generateAndAddInClause(String field, List<?> inValues) {
		if (!utilities.arrayHasElement(inValues))
			return;
		
		String auxCondition = field + " IN (?";
		
		this.addToParameters(inValues.get(0));
		
		for (int i = 1; i < inValues.size(); i++) {
			auxCondition += ", ?";
			
			this.addToParameters(inValues.get(i));
		}
		
		auxCondition += ")";
		
		this.addToClauses(auxCondition);
	}
	
	public boolean isToSelectColumn(String name) {
		return this.columnsToSelect.contains(name);
	}
	
	@Override
	public String toString() {
		try {
			return generateSQL(null);
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
	}
}
