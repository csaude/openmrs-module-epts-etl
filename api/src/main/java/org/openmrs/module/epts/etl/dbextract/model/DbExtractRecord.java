package org.openmrs.module.epts.etl.dbextract.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.ParentTable;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.dbquickmerge.model.ParentInfo;
import org.openmrs.module.epts.etl.dbquickmerge.model.QuickMergeRecord;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DbExtractRecord extends QuickMergeRecord {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public SrcConf srcConf;
	
	public DbExtractRecord(DatabaseObject record, SrcConf srcConf, AbstractTableConfiguration config, AppInfo srcApp,
	    AppInfo destApp, boolean writeOperationHistory) {
		
		super(record, config, srcApp, destApp, writeOperationHistory);
		
		this.srcConf = srcConf;
	}
	
	private void tryToLoadDefaultParents(Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		AbstractTableConfiguration config = this.config;
		
		if (!utilities.arrayHasElement(config.getParentRefInfo()))
			return;
		
		DatabaseObject record = this.record;
		
		for (ParentTable refInfo : config.getParentRefInfo()) {
			if (refInfo.isMetadata())
				continue;
			
			if (refInfo.getTableName().equals("person")) {
				System.out.println("Stop");
			}
			
			if (!refInfo.isFullLoaded()) {
				refInfo.fullLoad(dstConn);
				
				if (!refInfo.hasAlias()) {
					refInfo.setTableAlias(srcConf.generateAlias(refInfo));
				}
			}
			
			Oid key = refInfo.generateParentOidFromChild(record);
			
			if (key.hasNullFields()) {
				continue;
			}
			
			DatabaseObject parent = DatabaseObjectDAO.getByOid(refInfo, key, dstConn);
			
			if (parent == null) {
				DatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(refInfo, key, srcConn);
				
				if (parentInOrigin == null) {
					throw new MissingParentException(key, refInfo.getTableName(), this.config.getOriginAppLocationCode(),
					        refInfo);
				}
				
				DatabaseObject defaultParentInDst = config.getDefaultObject(dstConn);
				
				if (defaultParentInDst == null) {
					defaultParentInDst = config.generateAndSaveDefaultObject(dstConn);
				}
				
				record.changeParentValue(refInfo, defaultParentInDst);
				
				this.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
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
			
			DbExtractRecord parentData = new DbExtractRecord(parentInfo.getParentRecord(), this.srcConf,
			        parentInfo.getParentTableConf(), srcApp, destApp, this.writeOperationHistory);
			
			parentData.extract(srcConn, destConn);
			
			record.changeParentValue(parentInfo.getParentTableConf(), parentInfo.getParentRecord());
		}
		
		
		record.update(srcConf, destConn);
		
	}
	
	public static void extractAll(List<DbExtractRecord> extractRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(extractRecs)) {
			return;
		}
		
		AbstractTableConfiguration config = extractRecs.get(0).config;
		
		if (!config.isFullLoaded()) {
			config.fullLoad();
		}
		
		List<DatabaseObject> objects = new ArrayList<DatabaseObject>(extractRecs.size());
		
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
