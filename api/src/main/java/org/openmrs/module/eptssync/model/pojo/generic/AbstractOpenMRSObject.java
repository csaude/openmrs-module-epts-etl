package org.openmrs.module.eptssync.model.pojo.generic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.exceptions.SyncExeption;
import org.openmrs.module.eptssync.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractOpenMRSObject extends BaseVO implements OpenMRSObject{
	protected boolean metadata;
	/*
	 * Indicate if there where parents which have been ingored
	 */
	protected boolean hasIgnoredParent;
	protected String uuid;
	
	protected SyncImportInfoVO relatedSyncInfo;
	
	
	public void load(ResultSet rs) throws SQLException{ 
		try {
			super.load(rs);
			
			this.uuid = rs.getString("uuid");
			
		} catch (SQLException e) {}
	}
	
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
	public OpenMRSObject retrieveParentInDestination(Integer parentId, SyncTableConfiguration parentTableConfiguration, boolean ignorable, Connection conn) throws ParentNotYetMigratedException, DBException {
		if (parentId == null) return null;
		
		OpenMRSObject parentOnDestination;
		
		try {
			parentOnDestination = OpenMRSObjectDAO.thinGetByRecordOrigin(parentId, parentTableConfiguration, conn);
		} catch (DBException e) {
			logger.info("NEW ERROR PERFORMING LOAD OF " + parentTableConfiguration.getSyncRecordClass().getName());
			
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
			
		throw new ParentNotYetMigratedException(parentId, parentTableConfiguration.getTableName(), this.relatedSyncInfo.getRecordOriginLocationCode());
	}
	
	@Override
	public SyncImportInfoVO getRelatedSyncInfo() {
		return relatedSyncInfo;
	}
	
	@Override
	public void setRelatedSyncInfo(SyncImportInfoVO relatedSyncInfo) {
		this.relatedSyncInfo = relatedSyncInfo;
	}
	
	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String getUuid() {
		return this.uuid;
	}
	
	@JsonIgnore
	public boolean hasIgnoredParent() {
		return hasIgnoredParent;
	}
	
	public void setHasIgnoredParent(boolean hasIgnoredParent) {
		this.hasIgnoredParent = hasIgnoredParent;
	}
	
	@Override
	public void save(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException{ 
		if (tableConfiguration.isMetadata()) {
			OpenMRSObject recordOnDBByUuid = OpenMRSObjectDAO.thinGetByUuid(this.getClass(), this.getUuid(), conn);
			
			if (recordOnDBByUuid == null) {
				//Check if ID is free 
				OpenMRSObject recOnDBById = OpenMRSObjectDAO.getById(this.getClass(), this.getObjectId(), conn);
				
				if (recOnDBById == null) {
					OpenMRSObjectDAO.insert(this, conn);
				}
				else {
					this.resolveMetadataCollision(recOnDBById, tableConfiguration, conn);
				}
			}
			else {
				if (recordOnDBByUuid.getObjectId() != this.getObjectId()) {
					resolveMetadataCollision(recordOnDBByUuid, tableConfiguration, conn);
				}
				else {
					getRelatedSyncInfo().markAsConsistent(tableConfiguration, conn);
				}
			}
		}
		else {
			OpenMRSObject recordOnDB = null;
			
			if (tableConfiguration.getRelatedSynconfiguration().isDestinationSyncProcess()) {
				recordOnDB = OpenMRSObjectDAO.thinGetByUuid(this.getClass(), this.getRelatedSyncInfo().getRecordUuid(), conn);
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
	
	/**
	 * Resolve collision between existing metadata (in destination) and newly coming metadata (from any source).
	 * The collision resolution consist on changind existing children to point the newly coming metadata 
	 *  
	 * @param syncTableInfo
	 * @param recordInConflict
	 * @param conn
	 * @throws DBException
	 */
	private void resolveMetadataCollision(OpenMRSObject recordInConflict, SyncTableConfiguration syncTableInfo, Connection conn) throws DBException {
		//Object Id Collision
		if (this.getObjectId() == recordInConflict.getObjectId()) {
			recordInConflict.changeObjectId(syncTableInfo, conn);
	
			OpenMRSObjectDAO.insert(this, conn);
		}
		else 
		if (this.getUuid() != null && this.getUuid().equals(recordInConflict.getUuid())){
			//In case of uuid collision it is assumed that the records are same then the old record must be changed to the new one
			
			//1. Change existing record Uuid
			recordInConflict.setUuid(recordInConflict.getUuid() + "_");
			
			OpenMRSObjectDAO.update(recordInConflict, conn);
			
			//2. Check if the new object id is avaliable
			OpenMRSObject recOnDBById = OpenMRSObjectDAO.getById(this.getClass(), this.getObjectId(), conn);
			
			if (recOnDBById == null) {
				//3. Save the new record
				OpenMRSObjectDAO.insert(this, conn);
			}
			else {
				recOnDBById.changeObjectId(syncTableInfo, conn);
				
				OpenMRSObjectDAO.insert(this, conn);
			}
			
			recordInConflict.changeParentForAllChildren(this, syncTableInfo, conn);
			
			recordInConflict.remove(conn);
		}
	}
	
	@Override
	public void changeObjectId(SyncTableConfiguration syncTableInfo, Connection conn) throws DBException {
		//1. backup the old record
		GenericOpenMRSObject oldRecod = GenericOpenMRSObject.fastCreate(getRelatedSyncInfo(), syncTableInfo);
		
		//2. Retrieve any avaliable id for old record
		Integer avaliableId = OpenMRSObjectDAO.getAvaliableObjectId(syncTableInfo, 999999999, conn);
		
		this.setObjectId(avaliableId);
		this.setUuid("tmp" + avaliableId);
		this.setRelatedSyncInfo(null);
		
		//3. Save the new recod
		OpenMRSObjectDAO.insert(this, conn);
		
		//4. Change existing record's children to point to new parent
		oldRecod.changeParentForAllChildren(this, syncTableInfo, conn);
		
		//5. Remove old record
		oldRecod.remove(conn);
		
		//6. Reset record info
		this.setUuid(oldRecod.getUuid());
		this.setRelatedSyncInfo(oldRecod.getRelatedSyncInfo());
		
		OpenMRSObjectDAO.update(this, conn);
	}
	
	@Override
	public void changeParentForAllChildren(OpenMRSObject newParent, SyncTableConfiguration syncTableInfo, Connection conn) throws DBException {
		for (RefInfo refInfo: syncTableInfo.getChildred()) {
			List<OpenMRSObject> children =  OpenMRSObjectDAO.getByParentId (refInfo.getRefTableConfiguration(), refInfo.getRefColumnName(), this.getObjectId(), conn);
			
			for (OpenMRSObject child : children) {
				child.changeParentValue(refInfo.getRefColumnAsClassAttName(), newParent);	
				OpenMRSObjectDAO.update(child, conn);
			}
		}
	}
	
	@Override
	public void refreshLastSyncDateOnOrigin(SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn){ 
		try{
			OpenMRSObjectDAO.refreshLastSyncDateOnOrigin(this, tableConfiguration, recordOriginLocationCode, conn); 
		}catch(DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void refreshLastSyncDateOnDestination(SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn){ 
		try{
			OpenMRSObjectDAO.refreshLastSyncDateOnDestination(this, tableConfiguration, recordOriginLocationCode, conn); 
		}catch(DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void generateRelatedSyncInfo(SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException {
		this.relatedSyncInfo = SyncImportInfoVO.generateFromSyncRecord(this, recordOriginLocationCode, true);
	}
	
	@Override
	public void resolveInconsistence(SyncTableConfiguration tableConfiguration, Connection conn) throws InconsistentStateException, DBException {
		if (!tableConfiguration.isFullLoaded()) tableConfiguration.fullLoad();
		
		this.generateRelatedSyncInfo(tableConfiguration, tableConfiguration.getOriginAppLocationCode(), conn);
		
		Map<RefInfo, Integer> missingParents = loadMissingParents(tableConfiguration, conn);
		
		int qtyInconsistence = missingParents.size();
		
		if (qtyInconsistence == 0) {
			getRelatedSyncInfo().setConsistent(OpenMRSObject.CONSISTENCE_STATUS);
		}
		else {
			boolean solvedCurrentInconsistency = true;
			
			for (Entry<RefInfo, Integer> entry : missingParents.entrySet()) {
				//try to load the default parent
				
				if (entry.getKey().isSetNullDueInconsistency()) {
					 this.setParentToNull(entry.getKey().getRefColumnAsClassAttName());
					 this.save(tableConfiguration, conn);
					 
					 qtyInconsistence--;	
				}
				else
				if (entry.getKey().getDefaultValueDueInconsistency() != null) {
					 OpenMRSObject parent = OpenMRSObjectDAO.getById(entry.getKey().getRefObjectClass(), entry.getKey().getDefaultValueDueInconsistency(), conn);
					 
					 if (parent == null) {
						 solvedCurrentInconsistency = false;
					 }
					 else {
						 this.changeParentValue(entry.getKey().getRefColumnAsClassAttName(), parent);
						 this.save(tableConfiguration, conn);
						 
						 qtyInconsistence--;
					 }
				 }
				 else {
					 solvedCurrentInconsistency = false;
				 }
				 
				 saveInconsistence(tableConfiguration, entry, solvedCurrentInconsistency, getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
			}
			
			if (qtyInconsistence == 0) {
				getRelatedSyncInfo().setConsistent(OpenMRSObject.CONSISTENCE_STATUS);
			}
			else {
				getRelatedSyncInfo().setLastSyncTryErr(generateMissingInfo(missingParents));
				this.remove(conn);
				resolveChildrenInconsistences(tableConfiguration, missingParents, conn);
			}
		}
		
		getRelatedSyncInfo().save(tableConfiguration, conn);
	}
	
	private void saveInconsistence(SyncTableConfiguration tableConfiguration, Entry<RefInfo, Integer> inconsistenceInfoSource, boolean inconsistenceResoloved,  String recordOriginLocationCode, Connection conn) throws DBException {
		Integer defaultParent = inconsistenceInfoSource.getKey().getDefaultValueDueInconsistency();
		
		InconsistenceInfo info = InconsistenceInfo.generate(tableConfiguration.getTableName(), this.getObjectId(), inconsistenceInfoSource.getKey().getTableName(), inconsistenceInfoSource.getValue(), defaultParent, recordOriginLocationCode);
		info.save(tableConfiguration, conn);
	}

	public void resolveChildrenInconsistences(SyncTableConfiguration syncTableInfo, Map<RefInfo, Integer> missingParents, Connection conn) throws DBException{
		if (!syncTableInfo.getRelatedSynconfiguration().isSourceSyncProcess())  throw new SyncExeption("You cannot move record to stage area in a installation different to source") {private static final long serialVersionUID = 1L;};
		
		if ( (syncTableInfo.isMetadata() || syncTableInfo.isRemoveForbidden()) && !syncTableInfo.isRemovableMetadata() ) throw new SyncExeption("This metadata metadata [" + syncTableInfo.getTableName() + " = " + this.getObjectId() + ". is missing its some parents [" + generateMissingInfo(missingParents) +"] You must resolve this inconsistence manual") {private static final long serialVersionUID = 1L;};
		
		
		for (RefInfo refInfo: syncTableInfo.getChildred()) {
			if (!refInfo.getRefTableConfiguration().isConfigured()) continue;
			
			int qtyChildren = OpenMRSObjectDAO.countAllOfParentId(refInfo.getRefTableConfiguration().getSyncRecordClass(), refInfo.getRefColumnName(), this.getObjectId(), conn);
			
			if (qtyChildren == 0) {
				continue;
			}
			
			/*if (qtyChildren > 999) {
				throw new ForbiddenOperationException("The operation is trying to remove this record [" + syncTableInfo.getTableName() + " = " + this.getUuid() + ", from " + this.getRelatedSyncInfo().getRecordOriginLocationCode() + " but it has " + qtyChildren + " " + refInfo.getTableName() + " related to. Please check this inconsistence before continue");
			}*/
			
			List<OpenMRSObject> children =  OpenMRSObjectDAO.getByParentId(refInfo.getRefTableConfiguration().getSyncRecordClass(), refInfo.getRefColumnName(), this.getObjectId(), conn);
			
			for (OpenMRSObject child : children) {
				child.resolveInconsistence(refInfo.getRefTableConfiguration(), conn);
			}
		}
	}
	
	@Override
	public void consolidateData(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException{
		if (!tableConfiguration.isFullLoaded()) tableConfiguration.fullLoad();
		
		Map<RefInfo, Integer> missingParents = loadMissingParents(tableConfiguration, conn);
	
		int qtyInconsistence = missingParents.size();
			
		if (!missingParents.isEmpty()) {
			for (Entry<RefInfo, Integer> entry : missingParents.entrySet()) {
				boolean solvedCurrentInconsistency = true;
				
				//try to load the default parent
				 if (entry.getKey().getDefaultValueDueInconsistency() != null) {
					 OpenMRSObject parent = OpenMRSObjectDAO.getById(entry.getKey().getRefObjectClass(), entry.getKey().getDefaultValueDueInconsistency(), conn);
					 
					 if (parent == null) {
						 solvedCurrentInconsistency = false;
					 }
					 else {
						 this.changeParentValue(entry.getKey().getRefColumnAsClassAttName(), parent);
						 qtyInconsistence--;
					 }
				 }
				 else {
					 solvedCurrentInconsistency = false;
				 }
				 
				 saveInconsistence(tableConfiguration, entry, solvedCurrentInconsistency, getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
			}
		}
			
		if (qtyInconsistence == 0) {
			loadDestParentInfo(tableConfiguration, getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
			
			save(tableConfiguration, conn);
			
			this.getRelatedSyncInfo().markAsConsistent(tableConfiguration, conn);
		}
		else {
			removeDueInconsistency(tableConfiguration, missingParents, conn);
			getRelatedSyncInfo().markAsFailedToMigrate(tableConfiguration, generateMissingInfo(missingParents), conn);
		}
	}

	
	@Override
	public void loadDestParentInfo(SyncTableConfiguration tableInfo, String recordOriginLocationCode, Connection conn) throws ParentNotYetMigratedException, DBException {
		if (!tableInfo.getRelatedSynconfiguration().isDestinationSyncProcess()) throw new ForbiddenOperationException("You can only load destination parent in a destination installation");
		
		for (RefInfo refInfo: tableInfo.getParents()) {
			if (tableInfo.getSharePkWith() != null && tableInfo.getSharePkWith().equals(refInfo.getRefTableConfiguration().getTableName())) {
				continue;
			}
			
			Integer parentId = getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentId != null) {
				OpenMRSObject parent;
				
				if (refInfo.getRefTableConfiguration().isMetadata()) {
					parent = OpenMRSObjectDAO.getById(refInfo.getRefTableConfiguration().getTableName(), refInfo.getRefTableConfiguration().getPrimaryKey(), parentId , conn);
				}
				else {
					parent = retrieveParentInDestination(parentId, refInfo.getRefTableConfiguration(),  refInfo.isIgnorable() || refInfo.getDefaultValueDueInconsistency() > 0, conn);
				}
				
				if (parent == null) {
					//Try to recover the parent from stage_area and check if this record doesnt exist on destination with same uuid
					
					OpenMRSObject parentFromSource = new GenericOpenMRSObject(refInfo.getRefTableConfiguration());
					parentFromSource.setObjectId(parentId);
					
					parentFromSource.setRelatedSyncInfo(SyncImportInfoVO.generateFromSyncRecord(parentFromSource, recordOriginLocationCode, true));
					
					SyncImportInfoVO sourceInfo = SyncImportInfoDAO.retrieveFromOpenMRSObject(refInfo.getRefTableConfiguration(), parentFromSource, recordOriginLocationCode, conn);
					
					parentFromSource = sourceInfo.convertToOpenMRSObject(refInfo.getRefTableConfiguration(), conn);
					
					OpenMRSObject parentFromDestionationSharingSameObjectId = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(), parentId, conn);
					
					boolean sameUuid = true;
					
					sameUuid = sameUuid && parentFromDestionationSharingSameObjectId  != null;
					sameUuid = sameUuid && parentFromDestionationSharingSameObjectId.getUuid() != null && parentFromSource.getUuid() != null;
					sameUuid = sameUuid && parentFromSource.getUuid().equals(parentFromDestionationSharingSameObjectId.getUuid());
										
					if (sameUuid) {
						parent = parentFromDestionationSharingSameObjectId;
					}
				}
				
				 if (parent == null && refInfo.getDefaultValueDueInconsistency() > 0) {
					 parent = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(), refInfo.getDefaultValueDueInconsistency(), conn);
				 }
				
				changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
			}
		}
	}
	
	@Override
	public  SyncImportInfoVO retrieveRelatedSyncInfo(SyncTableConfiguration tableInfo, String recordOriginLocationCode, Connection conn) throws DBException {
		return SyncImportInfoDAO.retrieveFromOpenMRSObject(tableInfo, this, recordOriginLocationCode, conn);
	}
	
	public void removeDueInconsistency(SyncTableConfiguration syncTableInfo, Map<RefInfo, Integer> missingParents, Connection conn) throws DBException{
		if (syncTableInfo.isMetadata() || syncTableInfo.isRemoveForbidden()) throw new SyncExeption("This metadata [" + syncTableInfo.getTableName() + " = " + this.getObjectId() + ". is missing its some parents [" + generateMissingInfo(missingParents) +"] You must resolve this inconsistence manual") {private static final long serialVersionUID = 1L;};
		
		this.remove(conn);
	
		for (RefInfo refInfo: syncTableInfo.getChildred()) {
			if (!refInfo.getRefTableConfiguration().isConfigured()) continue;
			
			int qtyChildren = OpenMRSObjectDAO.countAllOfOriginParentId(refInfo.getRefColumnName(), getRelatedSyncInfo().getRecordOriginId(), getRelatedSyncInfo().getRecordOriginLocationCode(), refInfo.getRefTableConfiguration(), conn);
				
			if (qtyChildren == 0) {
				continue;
			}
			else {
				List<OpenMRSObject> children =  OpenMRSObjectDAO.getByOriginParentId(refInfo.getRefColumnName(), getRelatedSyncInfo().getRecordOriginId(), getRelatedSyncInfo().getRecordOriginLocationCode(), refInfo.getRefTableConfiguration(), conn);
						
				
				for (OpenMRSObject child : children) {
					child.consolidateData(refInfo.getRefTableConfiguration(), conn);
				}
			}
		}
	}
	
	public void  remove(Connection conn) throws DBException {
		OpenMRSObjectDAO.remove(this, conn);
	}

	Logger logger = Logger.getLogger(AbstractOpenMRSObject.class);
	
	public Map<RefInfo, Integer>  loadMissingParents(SyncTableConfiguration tableInfo, Connection conn) throws DBException{
		Map<RefInfo, Integer> missingParents = new HashMap<RefInfo, Integer>();
		
		for (RefInfo refInfo: tableInfo.getParents()) {
			Integer parentId = null;
			
			try {
				parentId = getParentValue(refInfo.getRefColumnAsClassAttName());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
				 
			try {
				if (parentId != null) {
					OpenMRSObject parent;
					
					if (refInfo.getRefTableConfiguration().isMetadata()) {
						parent = OpenMRSObjectDAO.getById(refInfo.getRefTableConfiguration().getTableName(), refInfo.getRefTableConfiguration().getPrimaryKey(), parentId , conn);
					}
					else {
						
						if (tableInfo.getRelatedSynconfiguration().isDestinationSyncProcess()) {
							parent = retrieveParentInDestination(parentId, refInfo.getRefTableConfiguration(),  refInfo.isIgnorable() || refInfo.getDefaultValueDueInconsistency() > 0, conn);
							
							if (parent == null) {
								//Try to recover the parent from stage_area and check if this record doesnt exist on destination with same uuid
								
								OpenMRSObject parentFromSource = new GenericOpenMRSObject(refInfo.getRefTableConfiguration());
								parentFromSource.setObjectId(parentId);
								
								parentFromSource.setRelatedSyncInfo(SyncImportInfoVO.generateFromSyncRecord(parentFromSource, getRelatedSyncInfo().getRecordOriginLocationCode(), true));
								
								SyncImportInfoVO sourceInfo = SyncImportInfoDAO.retrieveFromOpenMRSObject(refInfo.getRefTableConfiguration(), parentFromSource, getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
								
								parentFromSource = sourceInfo.convertToOpenMRSObject(refInfo.getRefTableConfiguration(), conn);
								
								OpenMRSObject parentFromDestionationSharingSameObjectId = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(), parentId, conn);
								
								boolean sameUuid = true;
								
								sameUuid = sameUuid && parentFromDestionationSharingSameObjectId  != null;
								sameUuid = sameUuid && parentFromDestionationSharingSameObjectId.getUuid() != null && parentFromSource.getUuid() != null;
								sameUuid = sameUuid && parentFromSource.getUuid().equals(parentFromDestionationSharingSameObjectId.getUuid());
													
								if (sameUuid) {
									parent = parentFromDestionationSharingSameObjectId;
								}
							}
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
				parent.setRelatedSyncInfo(SyncImportInfoVO.generateFromSyncRecord(parent, getRelatedSyncInfo().getRecordOriginLocationCode(), true));
				
				try {
					SyncImportInfoDAO.retrieveFromOpenMRSObject(refInfo.getRefTableConfiguration(), parent, getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
				} catch (DBException e1) {
					e1.printStackTrace();
				} catch (ForbiddenOperationException e1) {
					throw new ForbiddenOperationException("The parent '" + refInfo.getRefTableConfiguration().getTableName() + " = " + parentId + "' from '"+ this.getRelatedSyncInfo().getRecordOriginLocationCode() + "' was not found in the main database nor in the stagging area. You must resolve this inconsistence manual!!!!!!"); 
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
