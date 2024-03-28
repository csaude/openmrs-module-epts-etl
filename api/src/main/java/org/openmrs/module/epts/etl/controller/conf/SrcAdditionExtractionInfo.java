package org.openmrs.module.epts.etl.controller.conf;

import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * Represent the additional extraction information, specially additional conditions. This is
 * information is usefully when there is a need to add extraction condition from other related
 * tables rather than the main src table
 */
public class SrcAdditionExtractionInfo extends BaseConfiguration {
	
	/*
	 * The join type for all the additional src tables. It could be INNER or LEFT. This will be applied to all additional tables.
	 */
	private JoinType joinType;
	
	/*
	 * Additional src tables 
	 */
	private List<AdditionlExtractionSrcTable> additionalExtractionTables;
	
	private SrcConf parent;
	
	public JoinType getJoinType() {
		return joinType;
	}
	
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
	public SrcConf getParent() {
		return parent;
	}
	
	public void setParent(SrcConf parent) {
		this.parent = parent;
	}
	
	public List<AdditionlExtractionSrcTable> getAdditionalExtractionTables() {
		return additionalExtractionTables;
	}
	
	public void setAdditionalExtractionTables(List<AdditionlExtractionSrcTable> additionalExtractionTables) {
		this.additionalExtractionTables = additionalExtractionTables;
	}
	
	public synchronized void fullLoad() throws DBException {
		OpenConnection mainConn = getRelatedSyncConfiguration().getMainApp().openConnection();
		
		OpenConnection dstConn = null;
		
		try {
			for (AdditionlExtractionSrcTable t : this.additionalExtractionTables) {
				t.fullLoad(mainConn);
				
				if (!utilities.arrayHasElement(t.getJoinFields())) {
					//Try to autoload join fields
					
					FieldsMapping fm = null;
					
					//Assuming that the aux src is parent
					RefInfo pInfo = this.parent.getMainSrcTableConf().findParent(RefInfo.init(t.getTableName()));
					
					if (pInfo != null) {
						fm = new FieldsMapping(pInfo.getRefColumnName(), "", pInfo.getRefColumnName());
					} else {
						
						//Assuning that the aux src is child
						pInfo = t.findParent(RefInfo.init(this.parent.getMainSrcTableConf().getTableName()));
						
						if (pInfo != null) {
							fm = new FieldsMapping(pInfo.getRefColumnName(), "", pInfo.getRefColumnName());
						}
					}
					
					if (fm != null) {
						t.addJoinField(fm);
					}
				}
				
				if (utilities.arrayHasNoElement(t.getJoinFields())) {
					throw new ForbiddenOperationException("No join fields were difined between "
					        + this.parent.getMainSrcTableConf().getTableName() + " And " + t.getTableName());
				}
			}
			
		}
		finally {
			mainConn.finalizeConnection();
			
			if (dstConn != null) {
				dstConn.finalizeConnection();
			}
		}
	}
	
	public SyncConfiguration getRelatedSyncConfiguration() {
		return this.parent.getRelatedSyncConfiguration();
	}
	
}
