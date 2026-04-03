package org.openmrs.module.epts.etl.etl.model.stage;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class EtlStageAreaObjectDAO extends DatabaseObjectDAO {
	
	public static void saveSrcInfo(EtlStageObjectInfo stageObjectInfo, Connection srcConn) throws DBException {
		List<EtlStageObjectInfo> stageObjectInfoList = utilities.parseToList(stageObjectInfo);
		
		saveAllSrc(stageObjectInfoList, srcConn);
	}
	
	public static void saveAll(List<EtlStageObjectInfo> stageObjectInfo, Connection srcConn) throws DBException {
		if (!utilities.listHasElement(stageObjectInfo))
			return;
		
		saveAllSrc(stageObjectInfo, srcConn);
		saveAllDst(stageObjectInfo, srcConn);
	}
	
	public static void saveAllSrc(List<EtlStageObjectInfo> stageObjectInfo, Connection srcConn) throws DBException {
		if (!utilities.listHasElement(stageObjectInfo))
			return;
		
		doInsert(EtlStageObjectInfo.collectNotExistingSrcObjects(stageObjectInfo), srcConn);
		
		EtlStageObjectInfo.loadSrcStageIdToSrcKeyInfo(stageObjectInfo);
		
		insert(EtlStageObjectInfo.collectSrcKeyInfoForNotExistingObjects(stageObjectInfo), srcConn);
	}
	
	public static void saveAllDst(List<EtlStageObjectInfo> stageObjectInfo, Connection srcConn) throws DBException {
		
		EtlStageObjectInfo.loadSrcStageObjectIdToDstStageObjectId(stageObjectInfo);
		
		doInsert(EtlStageObjectInfo.collectNotExistingDstObjects(stageObjectInfo), srcConn);
		
		EtlStageObjectInfo.loadDstStageObjectIdToDstKeyInfoObject(stageObjectInfo);
		
		insert(EtlStageObjectInfo.collectDstKeyInfo(stageObjectInfo), srcConn);
	}
	
	private static void doInsert(List<EtlStageAreaObject> records, Connection srcConn) throws DBException {
		DatabaseObjectDAO.insert(utilities.parseList(records, EtlDatabaseObject.class), srcConn);
	}
	
	public static EtlStageAreaObject getByKey(EtlConfigurationTableConf conf, String compactedObjectUk,
	        String recordOriginLocationCode, Connection conn) throws DBException {
		String query = conf.generateSelectFromQuery() + " WHERE compacted_object_uk = ? and record_origin_location_code = ?";
		
		Object[] params = { compactedObjectUk, recordOriginLocationCode };
		
		return (EtlStageAreaObject) BaseDAO.find(conf.getLoadHealper(), conf.getSyncRecordClass(), query, params, conn);
	}
	
	public static EtlStageAreaObject get(EtlConfigurationTableConf conf, String condition, Object[] params, Connection conn)
	        throws DBException {
		
		String query = conf.generateSelectFromQuery() + " WHERE " + condition;
		
		return (EtlStageAreaObject) BaseDAO.find(conf.getLoadHealper(), conf.getSyncRecordClass(), query, params, conn);
		
	}
	
	@SuppressWarnings("unchecked")
	public static List<EtlDatabaseObject> getAll(EtlConfigurationTableConf conf, String condition, Object[] params,
	        Connection conn) throws DBException {
		
		String query = conf.generateSelectFromQuery() + " WHERE " + condition;
		
		return (List<EtlDatabaseObject>) BaseDAO.search(conf.getLoadHealper(), conf.getSyncRecordClass(), query, params,
		    conn);
	}
}
