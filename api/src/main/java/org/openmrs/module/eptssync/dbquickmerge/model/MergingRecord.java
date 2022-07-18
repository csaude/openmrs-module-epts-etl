package org.openmrs.module.eptssync.dbquickmerge.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.MissingParentException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class MergingRecord {
	private OpenMRSObject record;
	private SyncTableConfiguration config;
	private List<ParentInfo>  parentsWithDefaultValues;
	
	public MergingRecord(OpenMRSObject record, SyncTableConfiguration config) {
		this.record = record;
		this.config = config;	
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
	}
	
	public void merge(Connection srcConn, Connection destConn) throws DBException {
		consolidateAndSaveData(srcConn, destConn);
	}
	
	private void consolidateAndSaveData(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException{
		if (!config.isFullLoaded()) config.fullLoad(); 
		
		try {
			MergingRecord.loadDestParentInfo(this,  srcConn, destConn);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		record.save(config, destConn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	private void reloadParentsWithDefaultValues(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException {
		for (ParentInfo parentInfo: this.parentsWithDefaultValues) {
			
			RefInfo refInfo = parentInfo.getRefInfo();
			
			OpenMRSObject parent= parentInfo.getParent();
			
			MergingRecord parentData = new MergingRecord(parent, refInfo.getRefTableConfiguration());
			parentData.merge(srcConn, destConn);
				
			parent = OpenMRSObjectDAO.getByUuid(refInfo.getRefTableConfiguration().getSyncRecordClass(), parent.getUuid(), destConn);
			
			record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
		}		
	}
	
	private static void loadDestParentInfo(MergingRecord mergingRecord, Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, SQLException {
		OpenMRSObject record = mergingRecord.record;
		SyncImportInfoVO stageInfo = record.getRelatedSyncInfo();
		SyncTableConfiguration config = mergingRecord.config;
		
		for (RefInfo refInfo: config.getParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Integer parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentIdInOrigin != null) {
				OpenMRSObject parent = record.retrieveParentInDestination(parentIdInOrigin, stageInfo.getRecordOriginLocationCode(), refInfo.getRefTableConfiguration(),  true, destConn);
		
				if (parent == null) {
					OpenMRSObject parentInSrc = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(), parentIdInOrigin, srcConn);
					
					if (parentInSrc != null) {
						mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInSrc));
					}
					else throw new MissingParentException("Missing parent "+ refInfo + " with value [" + parentIdInOrigin + "] from [" + srcConn.getSchema() + "]");
					
					parent = OpenMRSObjectDAO.getDefaultRecord(refInfo.getRefTableConfiguration(), destConn);
				}
				
				record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
			}
		}
	}
	
}
