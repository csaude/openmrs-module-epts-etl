package org.openmrs.module.epts.etl.merge.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class MergingRecord {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private EtlDatabaseObject record;
	
	private TableConfiguration config;
	
	private SyncImportInfoVO stageInfo;
	
	private List<ParentInfo> parentsWithDefaultValues;
	
	private DBConnectionInfo srcApp;
	
	private DBConnectionInfo destApp;
	
	public MergingRecord(SyncImportInfoVO stageInfo, TableConfiguration config, DBConnectionInfo srcApp, DBConnectionInfo destApp) {
		this.srcApp = srcApp;
		this.destApp = destApp;
		this.stageInfo = stageInfo;
		this.config = config;
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
	}
	
	public void merge(Connection conn) throws DBException {
		this.record = DatabaseObjectDAO.getByOid(config, Oid.fastCreate("", stageInfo.getRecordOriginId()), conn);
		this.record.setRelatedSyncInfo(stageInfo);
		
		consolidateAndSaveData(conn);
	}
	
	private void consolidateAndSaveData(Connection conn) throws DBException {
		if (!config.isFullLoaded())
			config.fullLoad();
		
		MergingRecord.loadDestParentInfo(this, conn);
		
		record.save(config, conn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(conn);
		}
	}
	
	private void reloadParentsWithDefaultValues(Connection conn) throws ParentNotYetMigratedException, DBException {
		for (ParentInfo parentInfo : this.parentsWithDefaultValues) {
			
			ParentTable refInfo = parentInfo.getRefInfo();
			
			SyncImportInfoVO parentStageInfo = parentInfo.getParentStageInfo();
			
			MergingRecord parentData = new MergingRecord(parentStageInfo, refInfo, this.srcApp, this.destApp);
			parentData.record = DatabaseObjectDAO.getByOid(refInfo, parentStageInfo.getRecordOriginIdAsOid(), conn);
			parentData.merge(conn);
			
			EtlDatabaseObject parent = DatabaseObjectDAO.getByUniqueKeys(parentData.record, conn);
			
			record.changeParentValue(refInfo, parent);
		}
	}
	
	private static void loadDestParentInfo(MergingRecord mergingRecord, Connection conn)
	        throws ParentNotYetMigratedException, DBException {
		TableConfiguration config = mergingRecord.config;
		
		if (!utilities.arrayHasElement(config.getParents()))
			return;
		
		EtlDatabaseObject record = mergingRecord.record;
		SyncImportInfoVO stageInfo = record.getRelatedSyncInfo();
		
		for (ParentTable refInfo : config.getParentRefInfo()) {
			if (refInfo.isMetadata())
				continue;
			
			Object parentIdInOrigin = record.getParentValue(refInfo);
			
			if (parentIdInOrigin != null) {
				EtlDatabaseObject parent = record.retrieveParentInDestination(Integer.parseInt(parentIdInOrigin.toString()),
				    stageInfo.getRecordOriginLocationCode(), refInfo, true, conn);
				
				if (parent == null) {
					SyncImportInfoVO parentStageInfo = SyncImportInfoDAO.getByOriginIdAndLocation(refInfo,
					    Integer.parseInt(parentIdInOrigin.toString()), stageInfo.getRecordOriginLocationCode(), conn);
					
					if (parentStageInfo != null) {
						mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentStageInfo));
					} else
						throw new MissingParentException("Missing parent " + refInfo + " with value [" + parentIdInOrigin
						        + "] from [" + stageInfo.getRecordOriginLocationCode() + "]", null);
					
					parent = DatabaseObjectDAO.getDefaultRecord(refInfo, conn);
				}
				
				record.changeParentValue(refInfo, parent);
			}
		}
	}
	
}
