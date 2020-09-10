package org.openmrs.module.eptssync.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openmrs.module.eptssync.model.base.VO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public abstract class AbstractSearchParams<T extends VO> {
	private static final long serialVersionUID = 1L;
	
	public static final int STATUS_NO=-1;
	public static final int STATUS_INDIFFERENT=0;
	public static final int STATUS_YES=1;
	
	protected SearchResult<T> searchResult;
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	/**
	 * Indica o inicio da seleccao de registos na BD no caso de pesquisa faseada.
	 * O valor desta variável deve ser actualizado (deve avançar) a cada nova pesquisa
	 */
	protected int startAt;
	
	/**
	 * Indica a quantidade de registos a seleccionar a cada pesquisa
	 */
	protected int qtdRecordPerSelected;
	
	protected List<Field> parameters;
	
	protected List<T> excludedRecords;
	
	protected List<T> includedRecords;
	
	protected String extraCondition;
		
	public String getExtraCondition() {
		return extraCondition;
	}

	public void setExtraCondition(String extraCondition) {
		this.extraCondition = extraCondition;
	}

	public AbstractSearchParams(){
		this.startAt = 1;
		searchResult = new SearchResult<T>();
	}
	
	public AbstractSearchParams(int qtdRecordPerSelected){
		this.qtdRecordPerSelected = qtdRecordPerSelected;
		this.startAt = 1;
		searchResult = new SearchResult<T>();
	}
	
	public boolean hasResult(){
		return this.searchResult != null;
	}
	
	public boolean hasAtLeatOnSearchedRecord(){
		return this.hasResult() && utilities.arrayHasElement(this.searchResult.getCurrentSearchedRecords());
	}
	
	public SearchResult<T> getSearchResult() {
		return searchResult;
	}

	public void setSearchResult(SearchResult<T> searchResult) {
		this.searchResult = searchResult;
	}

	public List<Field> getParameters() {
		return parameters;
	}

	public void setParameters(List<Field> parameters) {
		this.parameters = parameters;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * Indica se o selected é "faseado"
	 */
	public boolean isPhaseSelected(){
		return this.qtdRecordPerSelected > 0;
	}

	public int getStartAt() {
		return startAt;
	}

	public void setStartAt(int startAt) {
		this.startAt = startAt;
	}

	public int getQtdRecordPerSelected() {
		return qtdRecordPerSelected;
	}

	public void setQtdRecordPerSelected(int qtdRecordPerSelected) {
		this.qtdRecordPerSelected = qtdRecordPerSelected;
	}

	public void addAllExcludedRecords(List<T> excludeds) {
		if (this.excludedRecords == null) this.excludedRecords = new ArrayList<T>();
		
		this.excludedRecords.addAll(excludeds);
	}
	
	public void addExcludedRecords(T excluded) {
		if (this.excludedRecords == null) this.excludedRecords = new ArrayList<T>();
		
		this.excludedRecords.add(excluded);
	}
	
	public void setExcludedRecords(List<T> excluded) {
		this.excludedRecords = excluded;
	}

	public List<T> getExcludedRecords() {
		return this.excludedRecords;
	}
	
	public void addIncludedRecords(T included) {
		if (this.includedRecords == null) this.includedRecords = new ArrayList<T>();
		
		this.includedRecords.add(included);
	}
	
	public void addAllIncludedRecords(Collection<T> excludeds) {
		if (this.includedRecords == null) this.includedRecords = new ArrayList<T>();
		
		this.includedRecords.addAll(excludeds);
	}

	public List<T> getIncludedRecords() {
		return this.includedRecords;
	}
	
	public void setIncludedRecords(List<T> includedRecords) {
		this.includedRecords = includedRecords;
	}

	public int countAllSearchebleRecords(Connection conn) throws DBException{
		return SearchParamsDAO.countAll(this, conn);
	}
	
	/**
	 * Gera os parametros para a exlusao dos 'excludedRecord' e osadiciona a 'searchClauses'
	 * 
	 * @param field: field to use for exclusion 
	 * @param searchClauses: searchClauses to modify
	 */
	/*public void generateParameterToExcludeRecords(String field, SearchClauses<T> searchClauses){
		if ( utilities.arrayHasElement(excludedRecords)){
			double totalRecordPerIteration = 999.0;
			
			int qtdIteracoes = Integer.parseInt(""+utilities.forcarAproximacaoPorExcesso(""+ utilities.arraySize(excludedRecords)/totalRecordPerIteration)); 
			
			for (int i = 0; i < qtdIteracoes; i++){
				String auxClause = field + " NOT IN (0 ";
				
				for (int j=i*(int)totalRecordPerIteration; j < (i+1)*(int)totalRecordPerIteration && j < excludedRecords.size() ; j++){
					searchClauses.addToParameters(((T)(excludedRecords.get(j))).getSelfId());
					auxClause += ",?"; 
				}
				auxClause += ")";
				
				searchClauses.addToClauses(auxClause);
			}
		}
	}
	*/
	
	/*
	public void generateParameterForIncludedRecords(String field, SearchClauses<T> searchClauses){
		
		if (arrayHasElement(includedRecords)){
			double totalRecordPerIteration = 999.0;
			int qtdIteracoes = Integer.parseInt(""+utilities.forcarAproximacaoPorExcesso(""+ utilities.arraySize(includedRecords)/totalRecordPerIteration)); 
			String auxCondtions = "";
			
			for (int i = 0; i < qtdIteracoes; i++){
				String auxClause = field + " IN (0 ";
				
				for (int j=i*(int)totalRecordPerIteration; j < (i+1)*(int)totalRecordPerIteration && j < includedRecords.size() ; j++){
					searchClauses.addToParameters(((BaseVO)includedRecords.get(j)).getSelfId());
					auxClause += ",?"; 
				}
				auxClause += ")";
				
				auxCondtions = utilities.concatCondition(auxCondtions, auxClause, "OR");
			}
			
			searchClauses.addToClauses("(" + auxCondtions + ")");
		}
	}
	*/
	
	private String parseParamToString (Object param){
		if (param instanceof Date){
			return "TO_DATE('"+ DateAndTimeUtilities.formatToDDMMYYYY_HHMISS((Date) param) + "', '" + DateAndTimeUtilities.DATE_TIME_FORMAT + "')"; 
		}
		
		if (param instanceof String) return "'" + param.toString() + "'";
		
		return param.toString();
	}
	
	public abstract SearchClauses<T> generateSearchClauses(Connection conn) throws DBException;
	
	public String generateFulfilledQueryClause(Connection conn) throws DBException{
		SearchClauses<T> searchClauses = generateSearchClauses(conn);
		
		String fulfiledQuery = "";
		String clauses = searchClauses.getClauses();
		
		int currParam=0;
		
		for (int i = 0; i < clauses.length(); i++){
			if (clauses.charAt(i) == '?'){
				fulfiledQuery += parseParamToString(searchClauses.getParameters()[currParam]);
				currParam++;
			}
			else fulfiledQuery += clauses.charAt(i);
		}
		
		return utilities.stringHasValue(fulfiledQuery) ? fulfiledQuery : "1=1";
	}
	
	/**
	 * Gera os parametros para a inclusao de uma lista de registos
	 * 
	 * @param field: field to use for exclusion 
	 * @param searchClauses: searchClauses to modify
	 */
	/*public static <T extends BaseVO> SearchClauses<T> generateParameterForIncludedRecords(String field, List<? extends Object> toIncludeRecords){
		Utilitarios utilities = Utilitarios.getInstance();
		
		SearchClauses<T> searchClauses = new SearchClauses<T>();
		
		if (arrayHasElement(toIncludeRecords)){
			double totalRecordPerIteration = 999.0;
			
			int qtdIteracoes = Integer.parseInt(""+utilities.forcarAproximacaoPorExcesso(""+ utilities.arraySize(toIncludeRecords)/totalRecordPerIteration)); 
			String auxCondtions="";
			
			for (int i = 0; i < qtdIteracoes; i++){
				String auxClause = field + " IN (0 ";
				
				for (int j=i*(int)totalRecordPerIteration; j < (i+1)*(int)totalRecordPerIteration && j < toIncludeRecords.size() ; j++){
					searchClauses.addToParameters(((BaseVO)(toIncludeRecords.get(j))).getSelfId());
					auxClause += ",?"; 
				}
				auxClause += ")";
				
				auxCondtions = utilities.concatCondition(auxCondtions, auxClause, "OR");
			}
			
			searchClauses.addToClauses("(" + auxCondtions + ")");
		}
		
		return searchClauses;
	}
	*/
	
	public void copy(AbstractSearchParams<T> copyFrom){
		this.startAt = copyFrom.startAt;
		this.qtdRecordPerSelected = copyFrom.qtdRecordPerSelected;
		this.extraCondition = copyFrom.extraCondition;
		this.parameters = copyFrom.parameters;
		this.includedRecords = copyFrom.includedRecords;
	}
	
	public abstract  Class<T> getRecordClass();
}
 