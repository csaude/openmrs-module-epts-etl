package org.openmrs.module.epts.etl.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SearchResult<T> implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * Algumas pesquisas nao retorna de uma unica vez a quantidade de registos que cumprem com as condicoes de pesquisa.
	 * Esta variavel devera guardar a quantidade total de registos considerando que a pesquisa nao sera faseada. 
	 */
	protected int qtdAllRecords;
	
	protected double totalValue;
	
	protected List<T> currentSearchedRecords;
		
	public SearchResult(){
		currentSearchedRecords = new ArrayList<T>();
	}
	
	public SearchResult(List<T>  currentSearchedRecords){
		setCurrentSearchedRecords(currentSearchedRecords);
	}
	
	public double getTotalValue() {
		return totalValue;
	}
	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}
	public int getQtdAllRecords() {
		return qtdAllRecords;
	}
	public void setQtdAllRecords(int qtdAllRecords) {
		this.qtdAllRecords = qtdAllRecords;
	}
	public List<T> getCurrentSearchedRecords() {
		return currentSearchedRecords;
	}
	
	public void setCurrentSearchedRecords(List<T> currentSearchedRecords) {
		this.currentSearchedRecords = currentSearchedRecords;
	}
}
