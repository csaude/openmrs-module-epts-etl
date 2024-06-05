package org.openmrs.module.epts.etl.dbsync.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncModel {
	
	private String tableToSyncModelClass;
	
	private BaseModel model;
	
	private SyncMetadata metadata;
	
	/**
	 * Gets the tableToSyncModelClass
	 *
	 * @return the tableToSyncModelClass
	 */
	public String getTableToSyncModelClass() {
		return tableToSyncModelClass;
	}
	
	/**
	 * Sets the tableToSyncModelClass
	 *
	 * @param tableToSyncModelClass the tableToSyncModelClass to set
	 */
	public void setTableToSyncModelClass(String tableToSyncModelClass) {
		this.tableToSyncModelClass = tableToSyncModelClass;
	}
	
	/**
	 * Gets the model
	 *
	 * @return the model
	 */
	public BaseModel getModel() {
		return model;
	}
	
	/**
	 * Sets the model
	 *
	 * @param model the model to set
	 */
	public void setModel(BaseModel model) {
		this.model = model;
	}
	
	/**
	 * Gets the metadata
	 *
	 * @return the metadata
	 */
	public SyncMetadata getMetadata() {
		return metadata;
	}
	
	/**
	 * Sets the metadata
	 *
	 * @param metadata the metadata to set
	 */
	public void setMetadata(SyncMetadata metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public String toString() {
		return "{tableToSyncModelClass=" + tableToSyncModelClass + ", model=" + model + ", metadata=" + metadata + "}";
	}
}
