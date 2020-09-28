package org.openmrs.module.eptssync.model.openmrs.generic;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmrs.module.eptssync.controller.conf.ParentRefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.exceptions.MetadataInconsistentException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.load.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractOpenMRSObject extends BaseVO implements OpenMRSObject{
	protected boolean metadata;
	/*
	 * Indicate if there where parents which have been ingored
	 */
	private boolean hasIgnoredParent;
	
	public <T extends OpenMRSObject> T loadParent(Class<T> parentClass, int parentId, boolean ignorable, Connection conn) throws ParentNotYetMigratedException, DBException {
		if (parentId == 0) return null;
		
		T parentOnDestination = OpenMRSObjectDAO.thinGetByOriginRecordId(parentClass, parentId, this.getOriginAppLocationCode(), conn);
 
		if (parentOnDestination != null){
			return parentOnDestination;
		}
		
		if (ignorable) {
			this.hasIgnoredParent = true;
			return null;
		}
			
		throw new ParentNotYetMigratedException(parentId, utilities.createInstance(parentClass).generateTableName(), this.getOriginAppLocationCode());
	}
	
	@Override
	public boolean isConsistent() {
		return this.getConsistent() > 0;
	}
	
	@Override
	public void markAsConsistent() {
		this.setConsistent(1);
	}
	
	@Override
	public void markAsInconsistent() {
		this.setConsistent(-1);
	}
	
	@Override
	public boolean isMetadata() {
		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}
	
	@JsonIgnore
	public boolean hasIgnoredParent() {
		return hasIgnoredParent;
	}
	
	public void setHasIgnoredParent(boolean hasIgnoredParent) {
		this.hasIgnoredParent = hasIgnoredParent;
	}

	@Override
	public void save(Connection conn) throws DBException{ 
		OpenMRSObject recordOnDB = OpenMRSObjectDAO.thinGetByOriginRecordId(this.getClass(), this.getOriginRecordId(), this.getOriginAppLocationCode(), conn);
 
		if (recordOnDB != null) {
			this.setObjectId(recordOnDB.getObjectId());
			OpenMRSObjectDAO.update(this, conn);
		}
		else {
			OpenMRSObjectDAO.insert(this, conn);
		}
	} 
	
	@Override
	public void refreshLastSyncDate(OpenConnection conn){ 
		try{
			OpenMRSObjectDAO.refreshLastSyncDate(this, conn); 
		}catch(DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void consolidateMetadata(Connection conn) throws DBException {
		OpenMRSObject recordOnDB = OpenMRSObjectDAO.thinGetByUuid(this.getClass(), this.getUuid(), conn);
		
		if (recordOnDB == null) {
			//Check if ID is free 
			OpenMRSObject recOnDBById = OpenMRSObjectDAO.getById(this.getClass(), this.getObjectId(), conn);
			
			if (recOnDBById == null) {
				OpenMRSObjectDAO.insert(this, conn);
			}
			else {
				throw new MetadataInconsistentException(recOnDBById);
			}
		}
		else {
			if (recordOnDB.getObjectId() != this.getObjectId()) {
				throw new MetadataInconsistentException(recordOnDB);
			}
		}
	}
	
	@Override
	public void consolidateData(SyncTableInfo tableInfo, Connection conn) throws DBException{
		Map<ParentRefInfo, Integer> missingParents = loadMissingParents(tableInfo, conn);
		
		boolean missingNotIgnorableParent = false;
		
		for (Entry<ParentRefInfo, Integer> missingParent : missingParents.entrySet()) {
			if (!missingParent.getKey().isIgnorable()) {
				missingNotIgnorableParent = true;
				break;
			}
		}
		
		if (missingNotIgnorableParent) {
			removeDueInconsistency(tableInfo, missingParents, conn);
		}
		else {
			loadDestParentInfo(conn);
			
			save(conn);
			
			SyncImportInfoVO syncInfo = this.retrieveRelatedSyncInfo(tableInfo, conn);
			
			if (hasIgnoredParent()) {
				syncInfo.markAsPartialMigrated(tableInfo, generateMissingInfo(missingParents), conn);
			}
			else syncInfo.delete(tableInfo, conn);
			
			this.markAsConsistent(conn);
			
			BaseDAO.commit(conn);
		}
	}
	
	protected  SyncImportInfoVO retrieveRelatedSyncInfo(SyncTableInfo tableInfo, Connection conn) throws DBException {
		return SyncImportInfoDAO.retrieveFromOpenMRSObject(tableInfo, this, conn);
	}

	public void removeDueInconsistency(SyncTableInfo syncTableInfo, Map<ParentRefInfo, Integer> missingParents, Connection conn) throws DBException{
		SyncImportInfoVO syncInfo = this.retrieveRelatedSyncInfo(syncTableInfo, conn);
		
		syncInfo.markAsFailedToMigrate(syncTableInfo, generateMissingInfo(missingParents), conn);
		
		this.remove(conn);
		
		BaseDAO.commit(conn);
		
		for (ParentRefInfo refInfo: syncTableInfo.getChildRefInfo()) {
			
			if (!refInfo.isMetadata()) {
				List<OpenMRSObject> children =  OpenMRSObjectDAO.getByOriginParentId(refInfo.determineRelatedReferenceClass(), refInfo.getReferenceColumnName(), this.getOriginRecordId(), this.getOriginAppLocationCode(), conn);
				
				for (OpenMRSObject child : children) {
					child.consolidateData(refInfo.getReferenceTableInfo(), conn);
				}
			}
		}
	}
	
	protected void  remove(Connection conn) throws DBException {
		OpenMRSObjectDAO.remove(this, conn);
	}

	public void markAsConsistent(Connection conn) throws DBException{
		markAsConsistent();
		
		OpenMRSObjectDAO.markAsConsistent(this, conn);
	}

	private Map<ParentRefInfo, Integer>  loadMissingParents(SyncTableInfo tableInfo, Connection conn) throws DBException{
		Map<ParentRefInfo, Integer> missingParents = new HashMap<ParentRefInfo, Integer>();
		
		for (ParentRefInfo refInfo: tableInfo.getParentRefInfo()) {
			 int parentId = getParentValue(refInfo.getReferenceColumnAsClassAttName());
				 
			try {
				if (parentId != 0) {
					OpenMRSObject parent = loadParent(refInfo.determineRelatedReferencedClass(), parentId, refInfo.isIgnorable(), conn);
					 
					 if (parent == null) {
						missingParents.put(refInfo, parentId);
					 }
				}
				 
			} catch (ParentNotYetMigratedException e) {
				missingParents.put(refInfo, parentId);
			} 
		}
		
		return missingParents;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (!obj.getClass().equals(this.getClass())) return false;
		
		AbstractOpenMRSObject objAsOpenMrs = (AbstractOpenMRSObject)obj;
		
		if (utilities.stringHasValue(this.getUuid()) && utilities.stringHasValue(objAsOpenMrs.getUuid())) {
			return this.getUuid().equals(objAsOpenMrs.getUuid());
		}
		
		return super.equals(obj);
	}
	
	private String generateMissingInfo(Map<ParentRefInfo, Integer> missingParents) {
		String missingInfo = "";
		
		for (Entry<ParentRefInfo, Integer> missing : missingParents.entrySet()) {
			missingInfo = utilities.concatStrings(missingInfo, "[" +missing.getKey().getReferencedTableInfo().getTableName() + ": " + missing.getValue() + "]", ";");
		}
		
		return "The record [" + this.generateTableName() + " = " + this.getObjectId() + "] is in inconsistent state. There are missing these parents: " + missingInfo;
	}	

}
