package org.openmrs.module.epts.etl.data.validation.missingrecords.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.data.validation.missingrecords.controller.DetectMissingRecordsController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DetectMissingRecordsSearchParams extends EtlDatabaseObjectSearchParams {
	
	DstConf relatedDstConf;
	
	public DetectMissingRecordsSearchParams(EtlItemConfiguration config, RecordLimits limits,
	    DetectMissingRecordsController relatedController) {
		super(config, limits, relatedController);
		
		this.excludedRecords = null;
		
		this.relatedDstConf = new DstConf();
		this.relatedDstConf.setTableName(getSrcConf().getTableName());
		this.relatedDstConf.setParentConf(config);
		this.relatedDstConf.setRelatedSyncConfiguration(config.getRelatedSyncConfiguration());
		
		OpenConnection dstConn = null;
		
		try {
			dstConn = relatedController.openDstConnection();
			
			this.relatedDstConf.fullLoad(dstConn);
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (dstConn != null)
				dstConn.finalizeConnection();
		}
		
	}
	
	@Override
	public DetectMissingRecordsController getRelatedController() {
		return (DetectMissingRecordsController) super.getRelatedController();
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SrcConf srcConfig = getSrcTableConf();
		
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addColumnToSelect("distinct " + srcConfig.generateFullAliasedSelectColumns() + "\n");
		
		String clauseFrom = srcConfig.generateSelectFromClauseContent();
		
		//To avoid slowness, don't exclude if there is no limit
		if (hasLimits()) {
			clauseFrom += " LEFT JOIN " + relatedDstConf.generateSelectFromClauseContent() + " ON "
			        + relatedDstConf.generateJoinConditionWithSrc();
			
			searchClauses.addToClauseFrom(clauseFrom);
			
			String extraCondition = "";
			
			for (FieldsMapping uk : relatedDstConf.getJoinFields()) {
				if (!extraCondition.isEmpty()) {
					extraCondition += " OR ";
				}
				
				extraCondition += this.relatedDstConf.getAlias() + "." + uk.getDstField() + " is null";
			}
			
			searchClauses.addToClauses(extraCondition);
			
			tryToAddLimits(searchClauses);
		} else {
			searchClauses.addToClauseFrom(clauseFrom);
		}
		
		tryToAddExtraConditionForExport(searchClauses);
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
	}
}
