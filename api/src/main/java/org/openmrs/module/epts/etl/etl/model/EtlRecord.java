package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.controller.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class EtlRecord {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private DatabaseObject record;
	
	private SyncTableConfiguration config;
	
	private boolean writeOperationHistory;
	
	private long destinationRecordId;
	
	public EtlRecord(DatabaseObject record, SyncTableConfiguration config, boolean writeOperationHistory) {
		this.record = record;
		this.config = config;
		this.writeOperationHistory = writeOperationHistory;
	}
	
	public void load(Connection srcConn, Connection destConn) throws DBException {
		this.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAll(this.config.getUniqueKeys()));
		
		doSave(srcConn, destConn);
		
		if (writeOperationHistory) {
			save(srcConn);
		}
	}
	
	public SyncTableConfiguration getConfig() {
		return config;
	}
	
	private void doSave(Connection srcConn, Connection destConn) throws DBException {
		if (!config.isFullLoaded())
			config.fullLoad();
		
		try {
			this.destinationRecordId = record.save(config, destConn);
		}
		catch (DBException e) {
			if (e.isDuplicatePrimaryOrUniqueKeyException()) {
				
				boolean existWinningRecInfo = utilities.arrayHasElement(config.getWinningRecordFieldsInfo());
				boolean existObservationDateFields = utilities.arrayHasElement(config.getObservationDateFields());
				
				if (existObservationDateFields || existWinningRecInfo) {
					List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
					
					DatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
					
					this.destinationRecordId = ((AbstractDatabaseObject) record)
					        .resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
				}
			} else
				throw e;
		}
	}
	
	public void resolveConflict(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException {
		if (!config.isFullLoaded())
			config.fullLoad();
		
		List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
		
		DatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
		
		((AbstractDatabaseObject) record).resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
	}
	
	public void save(Connection conn) throws DBException {
		SyncImportInfoVO syncInfo = SyncImportInfoVO.generateFromSyncRecord(getRecord(),
		    getConfig().getOriginAppLocationCode(), false);
		
		syncInfo.setDestinationId((int) this.destinationRecordId);
		
		syncInfo.save(getConfig(), conn);
	}
	
	public DatabaseObject getRecord() {
		return record;
	}
	
	public static void loadAll(List<EtlRecord> mergingRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(mergingRecs)) {
			return;
		}
		
		SyncTableConfiguration config = mergingRecs.get(0).config;
		
		if (!config.isFullLoaded()) {
			config.fullLoad();
		}
		
		List<DatabaseObject> objects = new ArrayList<DatabaseObject>(mergingRecs.size());
		
		for (EtlRecord etlRecord : mergingRecs) {
			etlRecord.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAll(etlRecord.config.getUniqueKeys()));
			
			objects.add(etlRecord.record);
		}
		
		DatabaseObjectDAO.insertAll(objects, config, config.getOriginAppLocationCode(), dstConn);
	}
	
	public static void loadAll(Map<String, List<EtlRecord>> mergingRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		for (String key : mergingRecs.keySet()) {
			loadAll(mergingRecs.get(key), srcConn, dstConn);
		}
	}
}
