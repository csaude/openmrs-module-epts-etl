package org.openmrs.module.eptssync.model.pojo.generic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.exceptions.MetadataInconsistentException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.exceptions.SyncExeption;
import org.openmrs.module.eptssync.load.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractOpenMRSObject extends BaseVO implements OpenMRSObject{
	protected boolean metadata;
	/*
	 * Indicate if there where parents which have been ingored
	 */
	private boolean hasIgnoredParent;
	
	/**
	 * Retrieve a specific parent of this record. The parent is loaded using the origin (source) identification key
	 * 
	 * @param <T> 
	 * @param parentClass parent class
	 * @param parentId in origin (source database)
	 * @param ignorable
	 * @param conn
	 * @return
	 * @throws ParentNotYetMigratedException if the parent is not ignorable and is not found on database
	 * @throws DBException
	 */
	public <T extends OpenMRSObject> T retrieveParentInDestination(Class<T> parentClass, int parentId, boolean ignorable, Connection conn) throws ParentNotYetMigratedException, DBException {
		if (parentId == 0) return null;
		
		T parentOnDestination;
		try {
			parentOnDestination = OpenMRSObjectDAO.thinGetByOriginRecordId(parentClass, parentId, this.getOriginAppLocationCode(), conn);
		} catch (DBException e) {
			logger.info("NEW ERROR PERFORMING LOAD OF " + parentClass.getName());
			
			e.printStackTrace();

			TimeCountDown.sleep(2000);
			
			throw new RuntimeException(e);
		}
 
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
	
	/*
	@Override
	public boolean isMetadata() {
		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
	}*/
	
	@JsonIgnore
	public boolean hasIgnoredParent() {
		return hasIgnoredParent;
	}
	
	public void setHasIgnoredParent(boolean hasIgnoredParent) {
		this.hasIgnoredParent = hasIgnoredParent;
	}

	@Override
	public void save(SyncTableConfiguration syncTableInfo, Connection conn) throws DBException{ 
		if (syncTableInfo.isMetadata()) {
			OpenMRSObject recordOnDB = OpenMRSObjectDAO.thinGetByUuid(this.getClass(), this.getUuid(), conn);
			
			if (recordOnDB == null) {
				//Check if ID is free 
				OpenMRSObject recOnDBById = OpenMRSObjectDAO.getById(this.getClass(), this.getObjectId(), conn);
				
				if (recOnDBById == null) {
					this.setOriginRecordId(this.getObjectId());
					OpenMRSObjectDAO.insert(this, conn);
				}
				else {
					String msg = "This record " + this.generateTableName() + " [object_id  = " + this.getObjectId() + ", uuid= " + this.getUuid() +"] share the same ID with record [uuid= " + recOnDBById.getUuid() + "] on the central database. Please ajust data if is needed!";

					throw new MetadataInconsistentException(msg);
				}
			}
			else {
				if (recordOnDB.getObjectId() != this.getObjectId()) {
					String msg = "This record " + this.generateTableName() + " [object_id  = " + this.getObjectId() + "] share the same UUID [" + this.getUuid() + "] with record [" + recordOnDB.getObjectId() + "] on the central database. Please ajust data if is needed!";
					
					throw new MetadataInconsistentException(msg);
				}
			}
		}
		else {
			OpenMRSObject recordOnDB = null;
			
			if (syncTableInfo.getRelatedSynconfiguration().isDestinationInstallationType()) {
				recordOnDB = OpenMRSObjectDAO.thinGetByOriginRecordId(this.getClass(), this.getOriginRecordId(), this.getOriginAppLocationCode(), conn);
			}
			else {
				recordOnDB = OpenMRSObjectDAO.getById(this.getClass(), this.getObjectId(), conn);
			}
				
			if (recordOnDB != null) {
				this.setObjectId(recordOnDB.getObjectId());
				OpenMRSObjectDAO.update(this, conn);
			}
			else {
				OpenMRSObjectDAO.insert(this, conn);
			}
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
	public void resolveInconsistence(SyncTableConfiguration syncTableInfo, Connection conn) throws InconsistentStateException, DBException {
		Map<RefInfo, Integer> missingParents = loadMissingParents(syncTableInfo, conn);
		
		if (missingParents.isEmpty()) {
			markAsConsistent(conn);
		}
		else {
			boolean inconsistencySolved = true;
			
			for (Entry<RefInfo, Integer> entry : missingParents.entrySet()) {
				//try to load the default parent
				 if (entry.getKey().getDefaultValueDueInconsistency() > 0) {
					 OpenMRSObject parent = OpenMRSObjectDAO.getById(entry.getKey().getRefObjectClass(), entry.getKey().getDefaultValueDueInconsistency(), conn);
					 
					 if (parent == null) {
						 inconsistencySolved = false;
						 break;
					 }
					 
					 this.changeParentValue(entry.getKey().getRefColumnAsClassAttName(), parent);
				 }
				 else {
					 inconsistencySolved = false;
					 break; 
				 }
			}
			
			if (inconsistencySolved) {
				this.save(syncTableInfo, conn);
				markAsConsistent(conn);
				
				copyToStageAreaDueInconsistencySolvedByDefaultParents(syncTableInfo, missingParents, conn);
			}
			else {
				moveToStageAreaDueInconsistency(syncTableInfo, missingParents, conn);
			}
		}
	}
	
	public void moveToStageAreaDueInconsistency(SyncTableConfiguration syncTableInfo, Map<RefInfo, Integer> missingParents, Connection conn) throws DBException{
		if (syncTableInfo.isMetadata() || syncTableInfo.isRemoveForbidden()) throw new SyncExeption("This metadata metadata [" + syncTableInfo.getTableName() + " = " + this.getObjectId() + ". is missing its some parents [" + generateMissingInfo(missingParents) +"] You must resolve this inconsistence manual") {private static final long serialVersionUID = 1L;};
		
		SyncImportInfoVO syncInfo = this.generateSyncInfo();
		
		syncInfo.setOriginAppLocationCode(syncTableInfo.getOriginAppLocationCode());
		
		syncInfo.setLastMigrationTryErr(generateMissingInfo(missingParents));
		
		SyncImportInfoDAO.insert(syncInfo, syncTableInfo, conn);
		
		this.remove(conn);
		
		for (RefInfo refInfo: syncTableInfo.getChildred()) {
			if (!refInfo.getRefTableConfiguration().isConfigured()) continue;
			
			List<OpenMRSObject> children =  OpenMRSObjectDAO.getByOriginParentId(refInfo.getRefTableConfiguration().getSyncRecordClass(), refInfo.getRefColumnName(), this.getOriginRecordId(), this.getOriginAppLocationCode(), conn);
			
			for (OpenMRSObject child : children) {
				child.resolveInconsistence(refInfo.getRefTableConfiguration(), conn);
			}
		}
	}
	
	public void copyToStageAreaDueInconsistencySolvedByDefaultParents(SyncTableConfiguration syncTableInfo, Map<RefInfo, Integer> missingParents, Connection conn) throws DBException{
		SyncImportInfoVO syncInfo = this.generateSyncInfo();
		
		if (utilities.stringHasValue(this.getOriginAppLocationCode())) {
			syncInfo.setOriginAppLocationCode(this.getOriginAppLocationCode());
		}
		else {
			syncInfo.setOriginAppLocationCode(syncTableInfo.getOriginAppLocationCode());
		}
		
		SyncImportInfoDAO.insert(syncInfo, syncTableInfo, conn);
		
		syncInfo = SyncImportInfoDAO.retrieveFromOpenMRSObject(syncTableInfo, this, conn);
		
		syncInfo.markAsPartialMigrated(syncTableInfo, generateMissingInfoForSolvedInconsistency(missingParents), conn);
	}
	
	private SyncImportInfoVO generateSyncInfo() {
		return SyncImportInfoVO.generateFromSyncRecord(this);
	}

	@Override
	public void consolidateData(SyncTableConfiguration tableInfo, Connection conn) throws DBException{
		
		if (this.getObjectId() == 4807) {
			System.out.println("STOP..");
		}
		
		Map<RefInfo, Integer> missingParents = loadMissingParents(tableInfo, conn);
	
		boolean inconsistencySolved = true;
		
		if (!missingParents.isEmpty()) {
			for (Entry<RefInfo, Integer> entry : missingParents.entrySet()) {
				//try to load the default parent
				 if (entry.getKey().getDefaultValueDueInconsistency() > 0) {
					 OpenMRSObject parent = OpenMRSObjectDAO.getById(entry.getKey().getRefObjectClass(), entry.getKey().getDefaultValueDueInconsistency(), conn);
					 
					 if (parent == null) {
						 inconsistencySolved = false;
						 break;
					 }
					 
					 this.changeParentValue(entry.getKey().getRefColumnAsClassAttName(), parent);
				 }
				 else {
					 inconsistencySolved = false;
					 break; 
				 }
			}
			
			if (inconsistencySolved) {
				copyToStageAreaDueInconsistencySolvedByDefaultParents(tableInfo, missingParents, conn);
			}
			else {
				removeDueInconsistency(tableInfo, missingParents, conn);
			}
		}
		
		if (inconsistencySolved) {
			loadDestParentInfo(tableInfo, conn);
			
			save(tableInfo, conn);
			
			this.markAsConsistent(conn);
		}
	}

	
	@Override
	public void loadDestParentInfo(SyncTableConfiguration tableInfo, Connection conn) throws ParentNotYetMigratedException, DBException {
		if (!tableInfo.getRelatedSynconfiguration().isDestinationInstallationType()) throw new ForbiddenOperationException("You can only load destination parent in a destination installation");
		
		for (RefInfo refInfo: tableInfo.getParents()) {
			if (tableInfo.getSharePkWith() != null && tableInfo.getSharePkWith().equals(refInfo.getRefTableConfiguration().getTableName())) {
				continue;
			}
			
			int parentId = getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentId != 0) {
				OpenMRSObject parent;
				
				if (refInfo.getRefTableConfiguration().isMetadata()) {
					parent = OpenMRSObjectDAO.getById(refInfo.getRefTableConfiguration().getTableName(), refInfo.getRefTableConfiguration().getPrimaryKey(), parentId , conn);
				}
				else {
					parent = retrieveParentInDestination(refInfo.getRefObjectClass(), parentId, refInfo.isIgnorable() || refInfo.getDefaultValueDueInconsistency() > 0, conn);
				}
				
				 if (parent == null && refInfo.getDefaultValueDueInconsistency() > 0) {
					 parent = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(), refInfo.getDefaultValueDueInconsistency(), conn);
				 }
				
				changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
			}
		}
	}
	
	public  SyncImportInfoVO retrieveRelatedSyncInfo(SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		return SyncImportInfoDAO.retrieveFromOpenMRSObject(tableInfo, this, conn);
	}
	
	public void removeDueInconsistency(SyncTableConfiguration syncTableInfo, Map<RefInfo, Integer> missingParents, Connection conn) throws DBException{
		if (syncTableInfo.isMetadata() || syncTableInfo.isRemoveForbidden()) throw new SyncExeption("This metadata metadata [" + syncTableInfo.getTableName() + " = " + this.getObjectId() + ". is missing its some parents [" + generateMissingInfo(missingParents) +"] You must resolve this inconsistence manual") {private static final long serialVersionUID = 1L;};
		
		SyncImportInfoVO syncInfo = this.retrieveRelatedSyncInfo(syncTableInfo, conn);
		
		syncInfo.markAsFailedToMigrate(syncTableInfo, generateMissingInfo(missingParents), conn);
		
		this.remove(conn);
	
		for (RefInfo refInfo: syncTableInfo.getChildred()) {
			if (!refInfo.getRefTableConfiguration().isConfigured()) continue;
				
			List<OpenMRSObject> children =  OpenMRSObjectDAO.getByOriginParentId(refInfo.getRefTableConfiguration().getSyncRecordClass(), refInfo.getRefColumnName(), this.getOriginRecordId(), this.getOriginAppLocationCode(), conn);
			
			for (OpenMRSObject child : children) {
				child.consolidateData(refInfo.getRefTableConfiguration(), conn);
			}
		}
		
	}
	
	public void  remove(Connection conn) throws DBException {
		OpenMRSObjectDAO.remove(this, conn);
	}

	public void markAsConsistent(Connection conn) throws DBException{
		markAsConsistent();
		
		OpenMRSObjectDAO.markAsConsistent(this, conn);
	}

	Logger logger = Logger.getLogger(AbstractOpenMRSObject.class);
	
	public Map<RefInfo, Integer>  loadMissingParents(SyncTableConfiguration tableInfo, Connection conn) throws DBException{
		Map<RefInfo, Integer> missingParents = new HashMap<RefInfo, Integer>();
		
		for (RefInfo refInfo: tableInfo.getParents()) {
			if (tableInfo.getSharePkWith() != null && tableInfo.getSharePkWith().equals(refInfo.getRefTableConfiguration().getTableName())) {
				continue;
			}
			
			int parentId = getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			try {
				if (parentId != 0) {
					OpenMRSObject parent;
					
					if (refInfo.getRefTableConfiguration().isMetadata()) {
						parent = OpenMRSObjectDAO.getById(refInfo.getRefTableConfiguration().getTableName(), refInfo.getRefTableConfiguration().getPrimaryKey(), parentId , conn);
					}
					else {
						
						if (tableInfo.getRelatedSynconfiguration().isDestinationInstallationType()) {
							parent = retrieveParentInDestination(refInfo.getRefObjectClass(), parentId, refInfo.isIgnorable() || refInfo.getDefaultValueDueInconsistency() > 0, conn);
						}
						else {
							parent = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(), parentId, conn);
						}
					}
				
					 if (parent == null) {
						 missingParents.put(refInfo, parentId);
					 }
				}
				 
			} catch (ParentNotYetMigratedException e) {
				OpenMRSObject parent = utilities.createInstance(refInfo.getRefObjectClass());
				parent.setOriginRecordId(parentId);
				parent.setOriginAppLocationCode(this.getOriginAppLocationCode());
				
				try {
					SyncImportInfoDAO.retrieveFromOpenMRSObject(refInfo.getRefTableConfiguration(), parent, conn);
				} catch (DBException e1) {
					e1.printStackTrace();
				} catch (ForbiddenOperationException e1) {
					throw new ForbiddenOperationException("The parent '" + refInfo.getRefTableConfiguration().getTableName() + " = " + parentId + "' from '"+ this.getOriginAppLocationCode() + "' was not found in the main database nor in the stagging area. You must resolve this inconsistence manual!!!!!!"); 
				}
				
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
		
		if (this.getObjectId() > 0 && objAsOpenMrs.getObjectId() > 0) return this.getObjectId() == objAsOpenMrs.getObjectId();
		
		if (utilities.stringHasValue(this.getUuid()) && utilities.stringHasValue(objAsOpenMrs.getUuid())) {
			return this.getUuid().equals(objAsOpenMrs.getUuid());
		}
		
		return super.equals(obj);
	}
	
	public String generateMissingInfo(Map<RefInfo, Integer> missingParents) {
		String missingInfo = "";
		
		for (Entry<RefInfo, Integer> missing : missingParents.entrySet()) {
			missingInfo = utilities.concatStrings(missingInfo, "[" +missing.getKey().getRefTableConfiguration().getTableName() + ": " + missing.getValue() + "]", ";");
		}
		
		return "The record [" + this.generateTableName() + " = " + this.getObjectId() + "] is in inconsistent state. There are missing these parents: " + missingInfo;
	}	
	
	public String generateMissingInfoForSolvedInconsistency(Map<RefInfo, Integer> missingParents) {
		String missingInfo = "";
		
		for (Entry<RefInfo, Integer> missing : missingParents.entrySet()) {
			missingInfo = utilities.concatStrings(missingInfo, "[" +missing.getKey().getRefTableConfiguration().getTableName() + ": " + missing.getValue() + "]", ";");
		}
		
		return "The record [" + this.generateTableName() + " = " + this.getObjectId() + "] is was in inconsistent state solved using some default parents.  These are missing parents: " + missingInfo;
	}	
	
	@SuppressWarnings("unchecked")
	public  Class<OpenMRSObject> tryToGetExistingCLass(File targetDirectory, String fullClassName) {
		try {
			URLClassLoader loader = URLClassLoader.newInstance(new URL[] {targetDirectory.toURI().toURL()});
	        
	        Class<OpenMRSObject> c = (Class<OpenMRSObject>) loader.loadClass(fullClassName);
	        
	        loader.close();
	        
	        return c;
		} 
		catch (ClassNotFoundException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			
			return null;
		}
	}

}
