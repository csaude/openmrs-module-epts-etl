package org.openmrs.module.epts.etl.processor.transformer;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.dbsync.model.SyncMetadata;
import org.openmrs.module.epts.etl.dbsync.model.SyncModel;
import org.openmrs.module.epts.etl.dbsync.model.utils.JsonUtils;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.EtlRecordTransformer;
import org.openmrs.module.epts.etl.etl.processor.transformer.TransformationType;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DbsyncJmsToSyncMsgTransformer implements EtlRecordTransformer {
	
	static List<GenericDatabaseObject> loadedSites = new ArrayList<>();
	
	CommonUtilities utilities = CommonUtilities.getInstance();
	
	@Override
	public EtlDatabaseObject transform(EtlProcessor processor, EtlDatabaseObject srcObject, DstConf dstConf,
	        EtlDatabaseObject migratedDstParent, TransformationType transformationType, Connection srcConn,
	        Connection dstConn) throws DBException, EtlTransformationException {
		
		TableConfiguration srcConf = (TableConfiguration) srcObject.getRelatedConfiguration();
		
		srcObject.loadObjectIdData(srcConf);
		srcObject.getObjectId().setTabConf(srcConf);
		
		String body = new String((byte[]) srcObject.getFieldValue("body"), StandardCharsets.UTF_8);
		SyncModel syncModel = JsonUtils.unmarshalSyncModel(body);
		
		SyncMetadata md = syncModel.getMetadata();
		
		EtlDatabaseObject syncMessage = new GenericDatabaseObject(dstConf);
		
		syncMessage.setFieldValue("entityPayload", body);
		syncMessage.setFieldValue("identifier", syncModel.getModel().getUuid());
		syncMessage.setFieldValue("modelClassName", syncModel.getTableToSyncModelClass());
		syncMessage.setFieldValue("operation", md.getOperation());
		
		syncMessage.setFieldValue("siteId", loadSiteInfo(srcConf, md.getSourceIdentifier(), srcConn).getFieldValue("id"));
		
		syncMessage.setFieldValue("dateCreated", new Date());
		syncMessage.setFieldValue("isSnapshot", md.getSnapshot());
		syncMessage.setFieldValue("messageUuid", md.getMessageUuid());
		syncMessage.setFieldValue("dateSentBySender", md.getDateSent());
		syncMessage.setFieldValue("dateCreated", srcObject.getFieldValue("dateCreated"));
		
		Integer id = (Integer) srcObject.getFieldValue("id");
		Integer syncMsgMaxId = Integer.parseInt(dstConf.getRelatedEtlConf().getParamValue("idIncrementValue"));
		
		syncMessage.setFieldValue("id", (id + syncMsgMaxId));
		
		//syncMessage.setSrcRelatedObject(srcObject);
		
		return syncMessage;
	}
	
	static void addToLoadedSites(GenericDatabaseObject loadedSite) {
		
		if (!loadedSites.contains(loadedSite)) {
			loadedSites.add(loadedSite);
		}
	}
	
	static GenericDatabaseObject loadSiteInfo(TableConfiguration srcConf, String identifier, Connection conn)
	        throws DBException {
		GenericDatabaseObject loadedSite = findSiteOnLoadedSites(identifier);
		
		if (loadedSite == null) {
			List<ParentTable> parents = srcConf.findAllRefToParent("site_info", srcConf.getSchema());
			
			ParentTable siteInfo = parents.get(0);
			
			UniqueKeyInfo uk = new UniqueKeyInfo();
			
			uk.addKey(Key.fastCreateValued("identifier", identifier));
			
			loadedSite = DatabaseObjectDAO.getByUniqueKey(siteInfo, uk, conn);
		}
		
		return loadedSite;
	}
	
	static GenericDatabaseObject findSiteOnLoadedSites(String identifier) {
		for (GenericDatabaseObject site : loadedSites) {
			if (site.getFieldValue("identifier").equals(identifier)) {
				return site;
			}
		}
		
		return null;
	}
	
}
