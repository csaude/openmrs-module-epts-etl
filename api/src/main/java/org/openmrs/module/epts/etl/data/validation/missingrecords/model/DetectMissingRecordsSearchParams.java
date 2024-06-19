package org.openmrs.module.epts.etl.data.validation.missingrecords.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.data.validation.missingrecords.controller.DetectMissingRecordsController;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DetectMissingRecordsSearchParams extends EtlDatabaseObjectSearchParams {
	
	DstConf relatedDstConf;
	
	public DetectMissingRecordsSearchParams(Engine<EtlDatabaseObject> relatedEngine,
	    ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		
		super(relatedEngine, limits);
		
		this.excludedRecords = null;
		
		this.relatedDstConf = new DstConf();
		this.relatedDstConf.setTableName(getSrcConf().getTableName());
		this.relatedDstConf.setParentConf(relatedEngine.getEtlItemConfiguration());
		this.relatedDstConf
		        .setRelatedSyncConfiguration(relatedEngine.getEtlItemConfiguration().getRelatedSyncConfiguration());
		
		OpenConnection dstConn = null;
		
		try {
			dstConn = getRelatedController().tryToOpenDstConn();
			
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
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord intervalExtremeRecord,
	        Connection srcConn, Connection dstConn) throws DBException {
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
			
			tryToAddLimits(intervalExtremeRecord, searchClauses);
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
