package org.openmrs.module.epts.etl.dbsync.engine;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.dbsync.model.SyncMetadata;
import org.openmrs.module.epts.etl.dbsync.model.SyncModel;
import org.openmrs.module.epts.etl.dbsync.model.utils.JsonUtils;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DbsyncJmsToSyncMsgEngine extends EtlEngine {
	
	List<GenericDatabaseObject> loadedSites;
	
	public DbsyncJmsToSyncMsgEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
		
		loadedSites = new ArrayList<>();
	}
	
	@Override
	public EtlDatabaseObject transform(EtlDatabaseObject rec, DstConf mappingInfo, List<? extends EtlObject> etlObjects,
	        Connection srcConn, Connection dstConn) throws DBException, ForbiddenOperationException {
		
		rec.loadObjectIdData(getSrcConf());
		rec.getObjectId().setTabConf(getSrcConf());
		
		if (mappingInfo.getTableName().equals("jms_msg_bkp")) {
			return mappingInfo.transform(rec, srcConn, getSrcApp(), getDstApp());
		} else {
			
			String body = new String((byte[]) rec.getFieldValue("body"), StandardCharsets.UTF_8);
			SyncModel syncModel = JsonUtils.unmarshalSyncModel(body);
			
			SyncMetadata md = syncModel.getMetadata();
			
			EtlDatabaseObject syncMessage = new GenericDatabaseObject(mappingInfo);
			
			syncMessage.setFieldValue("entityPayload", body);
			syncMessage.setFieldValue("identifier", syncModel.getModel().getUuid());
			syncMessage.setFieldValue("modelClassName", syncModel.getTableToSyncModelClass());
			syncMessage.setFieldValue("operation", md.getOperation());
			
			syncMessage.setFieldValue("siteId", loadSiteInfo(md.getSourceIdentifier(), srcConn).getFieldValue("id"));
			
			syncMessage.setFieldValue("dateCreated", new Date());
			syncMessage.setFieldValue("isSnapshot", md.getSnapshot());
			syncMessage.setFieldValue("messageUuid", md.getMessageUuid());
			syncMessage.setFieldValue("dateSentBySender", md.getDateSent());
			syncMessage.setFieldValue("dateCreated", rec.getFieldValue("dateCreated"));
			
			Integer id = (Integer) rec.getFieldValue("id");
			Integer syncMsgMaxId = Integer.parseInt(getRelatedEtlConfiguration().getParamValue("syncMsgMaxId"));
			
			syncMessage.setFieldValue("id", (id + syncMsgMaxId));
			
			return syncMessage;
		}
	}
	
	void addToLoadedSites(GenericDatabaseObject loadedSite) {
		
		if (!this.loadedSites.contains(loadedSite)) {
			this.loadedSites.add(loadedSite);
		}
	}
	
	GenericDatabaseObject loadSiteInfo(String identifier, Connection conn) throws DBException {
		GenericDatabaseObject loadedSite = findSiteOnLoadedSites(identifier);
		
		if (loadedSite == null) {
			List<ParentTable> parents = getSrcConf().findAllRefToParent("site_info", getSrcConf().getSchema());
			
			ParentTable siteInfo = parents.get(0);
			
			UniqueKeyInfo uk = new UniqueKeyInfo();
			
			uk.addKey(Key.fastCreateValued("identifier", identifier));
			
			loadedSite = DatabaseObjectDAO.getByUniqueKey(siteInfo, uk, conn);
		}
		
		return loadedSite;
	}
	
	GenericDatabaseObject findSiteOnLoadedSites(String identifier) {
		for (GenericDatabaseObject site : this.loadedSites) {
			if (site.getFieldValue("identifier").equals(identifier)) {
				return site;
			}
		}
		
		return null;
	}
}
