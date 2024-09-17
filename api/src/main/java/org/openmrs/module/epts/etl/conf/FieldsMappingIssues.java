package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;

public class FieldsMappingIssues {
	
	private List<FieldsMapping> avaliableInMultiDataSources;
	
	private List<FieldsMapping> notAvaliableInSpecifiedDataSource;
	
	private List<FieldsMapping> notAvaliableInAnyDataSource;
	
	public FieldsMappingIssues() {
		this.avaliableInMultiDataSources = new ArrayList<>();
		this.notAvaliableInSpecifiedDataSource = new ArrayList<>();
		this.notAvaliableInAnyDataSource = new ArrayList<>();
	}
	
	public List<String> extractDstFieldInAvaliableInMultiDataSources() {
		List<String> extracted = new ArrayList<>();
		
		for (FieldsMapping map : this.getAvaliableInMultiDataSources()) {
			extracted.add(map.getDstField() + ", srcs: [" + map.getPossibleSrc().toString() + "]");
		}
		
		return extracted;
	}
	
	public List<String> extractDstFieldInNotAvaliableInSpecifiedDataSource() {
		List<String> extracted = new ArrayList<>();
		
		for (FieldsMapping map : this.getNotAvaliableInSpecifiedDataSource()) {
			extracted.add(map.getDstField());
		}
		
		return extracted;
	}
	
	public List<String> extractDstFieldInNotAvaliableInAnyDataSource() {
		List<String> extracted = new ArrayList<>();
		
		for (FieldsMapping map : this.getNotAvaliableInAnyDataSource()) {
			extracted.add(map.getDstField());
		}
		
		return extracted;
	}
	
	public List<FieldsMapping> getAvaliableInMultiDataSources() {
		return avaliableInMultiDataSources;
	}
	
	public void setAvaliableInMultiDataSources(List<FieldsMapping> avaliableInMultiDataSources) {
		this.avaliableInMultiDataSources = avaliableInMultiDataSources;
	}
	
	public List<FieldsMapping> getNotAvaliableInSpecifiedDataSource() {
		return notAvaliableInSpecifiedDataSource;
	}
	
	public void setNotAvaliableInSpecifiedDataSource(List<FieldsMapping> notAvaliableInSpecifiedDataSource) {
		this.notAvaliableInSpecifiedDataSource = notAvaliableInSpecifiedDataSource;
	}
	
	public List<FieldsMapping> getNotAvaliableInAnyDataSource() {
		return notAvaliableInAnyDataSource;
	}
	
	public void setNotAvaliableInAnyDataSource(List<FieldsMapping> notAvaliableInAnyDataSource) {
		this.notAvaliableInAnyDataSource = notAvaliableInAnyDataSource;
	}
	
	public boolean contains(FieldsMapping fm) {
		if (this.getAvaliableInMultiDataSources().contains(fm)) {
			return true;
		}
		if (this.getNotAvaliableInAnyDataSource().contains(fm)) {
			return true;
		}
		if (this.getNotAvaliableInSpecifiedDataSource().contains(fm)) {
			return true;
		}
		return false;
	}
	
	public boolean hasIssue() {
		if (!this.getAvaliableInMultiDataSources().isEmpty()) {
			return true;
		}
		if (!this.getNotAvaliableInAnyDataSource().isEmpty()) {
			return true;
		}
		if (!this.getNotAvaliableInSpecifiedDataSource().isEmpty()) {
			return true;
		}
		
		return false;
	}
	
}
