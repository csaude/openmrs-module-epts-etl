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
	
	public SrcConf srcConf;
	
	public DbExtractRecord(EtlDatabaseObject record, SrcConf srcConf, TableConfiguration config, AppInfo srcApp,
	    AppInfo destApp, boolean writeOperationHistory) {
		
		super(record, config, srcApp, destApp, writeOperationHistory);
		
		this.srcConf = srcConf;
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
				
				if (!refInfo.hasAlias()) {
					refInfo.setTableAlias(srcConf.generateAlias(refInfo));
				}
				
				refInfo.fullLoad(dstConn);
			}
			
			Oid key = refInfo.generateParentOidFromChild(record);
			
			if (key.hasNullFields()) {
				continue;
			}
			
			if (refInfo.useSharedPKKey()) {
				if (!refInfo.getSharedKeyRefInfo().isFullLoaded()) {
					if (!refInfo.getSharedKeyRefInfo().hasAlias()) {
						refInfo.getSharedKeyRefInfo().setTableAlias(srcConf.generateAlias(refInfo.getSharedKeyRefInfo()));
					}
					
					refInfo.getSharedKeyRefInfo().fullLoad(dstConn);
				}
			}
			
			EtlDatabaseObject parent = DatabaseObjectDAO.getByOid(refInfo, key, dstConn);
			
			if (parent == null) {
				EtlDatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(refInfo, key, srcConn);
				
				if (parentInOrigin == null) {
					throw new MissingParentException(key, refInfo.getTableName(), this.config.getOriginAppLocationCode(),
					        refInfo);
				}
				
				if (this.config.useSharedPKKey() && this.config.getSharedTableConf().equals(refInfo)) {
					//Force the extraction of shared pk record
					DstConf dstSharedConf = this.config.getSharedKeyRefInfo().findRelatedDstConf();
					
					if (dstSharedConf == null) {
						throw new ForbiddenOperationException(
						        "There are relashioship which cannot auto resolved as there is no configured etl for "
						                + this.config.getSharedKeyRefInfo().getTableName() + " as destination!");
					}
					
					EtlDatabaseObject dstParent = dstSharedConf.generateDstObject(parentInOrigin, srcConn, srcApp, destApp);
					
					DbExtractRecord parentData = new DbExtractRecord(dstParent, this.srcConf, dstSharedConf, srcApp, destApp,
					        this.writeOperationHistory);
					
					parentData.extract(srcConn, dstConn);
					
				} else {
					EtlDatabaseObject defaultParentInDst = refInfo.getDefaultObject(dstConn);
					
					if (defaultParentInDst == null) {
						defaultParentInDst = config.generateAndSaveDefaultObject(dstConn);
					}
					
					record.changeParentValue(refInfo, defaultParentInDst);
					
					this.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
				}
			}
		}
	}
	
	public void extract(Connection srcConn, Connection destConn) throws DBException {
		
		if (!config.isFullLoaded())
			config.fullLoad();
		
		try {
			
			this.tryToLoadDefaultParents(srcConn, destConn);
			
			DatabaseObjectDAO.insertWithObjectId(record, destConn);
			
			if (config.useSimpleNumericPk()) {
				this.destinationRecordId = record.getObjectId().getSimpleValueAsInt();
			}
			
		}
		catch (DBException e) {
			if (e.isDuplicatePrimaryOrUniqueKeyException()) {} else if (e.isIntegrityConstraintViolationException()) {
				determineMissingMetadataParent(this, srcConn, destConn);
			} else
				throw e;
		}
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
		
		if (writeOperationHistory) {
			save(srcConn);
		}
	}
	
	public void reloadParentsWithDefaultValues(Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		
		for (ParentInfo parentInfo : this.parentsWithDefaultValues) {
			
			DstConf dstSharedConf = parentInfo.getParentTableConfInDst().findRelatedDstConf();
			
			if (dstSharedConf == null) {
				throw new ForbiddenOperationException(
				        "There are relashioship which cannot auto resolved as there is no configured etl for "
				                + parentInfo.getParentTableConfInDst().getTableName() + " as destination!");
			}
			
			if (!dstSharedConf.getParentConf().isFullLoaded()) {
				dstSharedConf.getParentConf().fullLoad();
			}
			
			if (!dstSharedConf.isFullLoaded()) {
				dstSharedConf.fullLoad(destConn);
			}
			
			EtlDatabaseObject parentFromInSrcConf = dstSharedConf.getParentConf()
			        .retrieveRecordInSrc(parentInfo.getParentRecordInOrigin(), srcConn);
			
			if (parentFromInSrcConf == null) {
				throw new ForbiddenOperationException("The record " + parentInfo.getParentRecordInOrigin()
				        + " is needed for extraction of " + this.record + " but this cannot be extracted");
			}
			
			EtlDatabaseObject parent = dstSharedConf.generateDstObject(parentFromInSrcConf, srcConn, srcApp, destApp);
			
			DbExtractRecord parentData = new DbExtractRecord(parent, this.srcConf, parentInfo.getParentTableConfInDst(),
			        srcApp, destApp, this.writeOperationHistory);
			
			parentData.extract(srcConn, destConn);
			
			record.changeParentValue(parentInfo.getParentTableConfInDst(), parent);
		}
		
		record.update(srcConf, destConn);
		
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
	
	public static void extractAll(Map<String, List<DbExtractRecord>> mergingRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		for (String key : mergingRecs.keySet()) {
			extractAll(utilities.parseList(mergingRecs.get(key), DbExtractRecord.class), srcConn, dstConn);
		}
	}
	
	@Override
	public String toString() {
		return "[" + this.record.toString() + ", from " + this.srcConf.getTableName() + " to "
		        + this.getRecord().getRelatedConfiguration().getObjectName();
	}
}
