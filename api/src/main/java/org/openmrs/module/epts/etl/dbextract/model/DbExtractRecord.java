package org.openmrs.module.epts.etl.dbextract.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.model.ParentInfo;
import org.openmrs.module.epts.etl.dbquickmerge.model.QuickMergeRecord;
import org.openmrs.module.epts.etl.exceptions.EtlException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DbExtractRecord extends QuickMergeRecord {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public DbExtractRecord(EtlDatabaseObject record, SrcConf srcConf, DstConf config, AppInfo srcApp, AppInfo destApp,
	    boolean writeOperationHistory) {
		
		super(record, srcConf, config, srcApp, destApp, writeOperationHistory);
		
	}
	
	private void tryToLoadDefaultParents(Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		TableConfiguration config = this.config;
		
		if (!utilities.arrayHasElement(config.getParentRefInfo()))
			return;
		
		EtlDatabaseObject record = this.record;
		
		for (ParentTable refInfo : config.getParentRefInfo()) {
			if (refInfo.isMetadata())
				continue;
			
			if (!refInfo.isFullLoaded()) {
				refInfo.tryToGenerateTableAlias(this.getEtlConfiguration());
				
				refInfo.fullLoad(dstConn);
			}
			
			Oid key = refInfo.generateParentOidFromChild(record);
			
			if (key.hasNullFields()) {
				continue;
			}
			
			if (refInfo.useSharedPKKey()) {
				if (!refInfo.getSharedKeyRefInfo().isFullLoaded()) {
					refInfo.getSharedKeyRefInfo().tryToGenerateTableAlias(this.getEtlConfiguration());
					
					refInfo.getSharedKeyRefInfo().fullLoad(dstConn);
				}
			}
			
			EtlDatabaseObject parent = DatabaseObjectDAO.getByOid(refInfo, key, dstConn);
			
			if (parent == null) {
				EtlDatabaseObject parentInOrigin = DatabaseObjectDAO
				        .getByOid(getConfig().findCorrespondentSrcParentConf(refInfo), key, srcConn);
				
				if (parentInOrigin == null) {
					throw new MissingParentException(record, key, refInfo.getTableName(),
					        this.getConfig().getOriginAppLocationCode(), refInfo);
				}
				
				EtlDatabaseObject defaultParentInDst = refInfo.getDefaultObject(dstConn);
				
				if (defaultParentInDst == null) {
					defaultParentInDst = config.generateAndSaveDefaultObject(dstConn);
				}
				
				record.changeParentValue(refInfo, defaultParentInDst);
				
				this.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
			}
		}
	}
	
	EtlDatabaseObject extractParent(ParentInfo parentInfo, Connection srcConn, Connection destConn)
	        throws ForbiddenOperationException, DBException {
		
		EtlDatabaseObject parentInOrigin = parentInfo.getParentRecordInOrigin();
		ParentTable parentConfInDst = parentInfo.getParentTableConfInDst();
		
		EtlDatabaseObject parentInDst = parentInOrigin.findOnDB(parentConfInDst, destConn);
		
		if (parentInDst != null) {
			return parentInDst;
		}
		
		//All the available dstConf for the parent
		List<DstConf> allAvaliableDstConfForParent = parentConfInDst.findRelatedDstConf();
		
		if (!utilities.arrayHasElement(allAvaliableDstConfForParent)) {
			throw new ForbiddenOperationException(
			        "There are relashioship that cannot be auto resolved as there is no configured etl for "
			                + parentConfInDst.getTableName() + " as destination!");
		}
		
		EtlDatabaseObject parentFromSrc = null;
		
		//Retrieves the record in all available src and tries to extract it 
		for (DstConf dst : allAvaliableDstConfForParent) {
			if (!dst.getParentConf().isFullLoaded()) {
				dst.getParentConf().fullLoad();
			}
			
			if (!dst.isFullLoaded()) {
				dst.fullLoad(destConn);
			}
			
			parentFromSrc = dst.getParentConf().retrieveRecordInSrc(parentInOrigin, srcConn);
			
			try {
				if (parentFromSrc != null) {
					parentInDst = dst.transform(parentFromSrc, srcConn, srcApp, destApp);
					
					if (parentInDst != null) {
						DbExtractRecord parentData = new DbExtractRecord(parentInDst, dst.getSrcConf(), dst, srcApp, destApp,
						        this.writeOperationHistory);
						
						parentData.extract(srcConn, destConn);
						
						break;
					}
				}
			}
			catch (NullPointerException e) {
				e.printStackTrace();
				
				throw new EtlException("Error extracting parent " + parentFromSrc + " For record: " + this.getRecord());
			}
		}
		
		if (parentInDst == null) {
			throw new ForbiddenOperationException("The record " + parentInOrigin + " is needed for extraction of "
			        + this.record + " but this cannot be extracted");
		}
		
		return parentInDst;
	}
	
	public void extract(Connection srcConn, Connection dstConn) throws DBException {
		
		if (!config.isFullLoaded())
			config.fullLoad();
		
		try {
			
			if (this.getConfig().useSharedPKKey()) {
				//Force the extraction of shared pk record
				Oid key = this.getConfig().getSharedKeyRefInfo().generateParentOidFromChild(getRecord());
				
				TableConfiguration parentConf = getEtlConfiguration().findTableInSrc(this.getConfig().getSharedKeyRefInfo(),
				    srcConn);
				
				EtlDatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(parentConf, key, srcConn);
				
				ParentInfo pInfo = new ParentInfo(this.getConfig().getSharedKeyRefInfo(), parentInOrigin);
				
				extractParent(pInfo, srcConn, dstConn);
			}
			
			this.tryToLoadDefaultParents(srcConn, dstConn);
			
			DatabaseObjectDAO.insertWithObjectId(record, dstConn);
			
			if (config.useSimpleNumericPk()) {
				this.destinationRecordId = record.getObjectId().getSimpleValueAsInt();
			}
			
		}
		catch (DBException e) {
			if (e.isDuplicatePrimaryOrUniqueKeyException()) {} else if (e.isIntegrityConstraintViolationException()) {
				determineMissingMetadataParent(this, srcConn, dstConn);
				
				//If there is no missing metadata parent, throw exception
				
				throw e;
			} else
				throw e;
		}
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, dstConn);
		}
		
		if (writeOperationHistory) {
			save(srcConn);
		}
	}
	
	public void reloadParentsWithDefaultValues(Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		
		for (ParentInfo parentInfo : this.parentsWithDefaultValues) {
			
			EtlDatabaseObject parent = extractParent(parentInfo, srcConn, destConn);
			
			record.changeParentValue(parentInfo.getParentTableConfInDst(), parent);
		}
		
		record.update(this.getConfig(), destConn);
	}
	
	public static void extractAll(List<DbExtractRecord> extractRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(extractRecs)) {
			return;
		}
		
		TableConfiguration config = extractRecs.get(0).config;
		
		if (!config.isFullLoaded()) {
			config.fullLoad();
		}
		
		List<EtlDatabaseObject> objects = new ArrayList<EtlDatabaseObject>(extractRecs.size());
		
		for (DbExtractRecord quickMergeRecord : extractRecs) {
			
			if (quickMergeRecord.getConfig().useSharedPKKey()) {
				//Force the extraction of shared pk record
				Oid key = quickMergeRecord.getConfig().getSharedKeyRefInfo()
				        .generateParentOidFromChild(quickMergeRecord.getRecord());
				
				TableConfiguration parentConf = quickMergeRecord.getEtlConfiguration()
				        .findTableInSrc(quickMergeRecord.getConfig().getSharedKeyRefInfo(), srcConn);
				
				EtlDatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(parentConf, key, srcConn);
				
				ParentInfo pInfo = new ParentInfo(quickMergeRecord.getConfig().getSharedKeyRefInfo(), parentInOrigin);
				
				quickMergeRecord.extractParent(pInfo, srcConn, dstConn);
			}
			
			quickMergeRecord.tryToLoadDefaultParents(srcConn, dstConn);
			
			objects.add(quickMergeRecord.getRecord());
		}
		
		DatabaseObjectDAO.insertAllDataWithId(objects, dstConn);
		
		for (DbExtractRecord quickMergeRecord : extractRecs) {
			if (!quickMergeRecord.getParentsWithDefaultValues().isEmpty()) {
				quickMergeRecord.reloadParentsWithDefaultValues(srcConn, dstConn);
			}
		}
	}
	
	public static void extractAll(List<String> mapOrder, Map<String, List<DbExtractRecord>> mergingRecs, Connection srcConn,
	        OpenConnection dstConn) throws ParentNotYetMigratedException, DBException {
		for (String key : mapOrder) {
			extractAll(utilities.parseList(mergingRecs.get(key), DbExtractRecord.class), srcConn, dstConn);
		}
	}
	
	@Override
	public String toString() {
		return "[" + this.record.toString() + ", from " + this.srcConf.getTableName() + " to "
		        + this.getRecord().getRelatedConfiguration().getObjectName();
	}
}
