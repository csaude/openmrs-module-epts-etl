package org.openmrs.module.epts.etl.etl.model.stage;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class EtlStageAreaObjectDAO extends DatabaseObjectDAO {
	
	public static void saveAll(List<EtlStageAreaInfo> stageObjectInfo, Connection srcConn) throws DBException {
		if (!utilities.arrayHasElement(stageObjectInfo))
			return;
		
		insert(EtlStageAreaInfo.collectAllSrcStageAreaObjectAsEtlDatabaseObject(stageObjectInfo), srcConn);
		
		EtlStageAreaInfo.loadSrcStageIdToSrcKeyInfo(stageObjectInfo);
		
		insert(EtlStageAreaInfo.collectAllSrcKeyInfo(stageObjectInfo), srcConn);
		
		EtlStageAreaInfo.loadSrcStageObjectIdToDstStageObjectId(stageObjectInfo);
		
		insert(EtlStageAreaInfo.collectAllDstStageAreaObjectAsEtlDatabaseObject(stageObjectInfo), srcConn);
		
		EtlStageAreaInfo.loadDstStageObjectIdToDstKeyInfoObject(stageObjectInfo);
		
		insert(EtlStageAreaInfo.collectAllDstKeyInfo(stageObjectInfo), srcConn);
	}
}
