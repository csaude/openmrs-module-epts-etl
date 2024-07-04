package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class EtlStageAreaObjectDAO extends DatabaseObjectDAO {
	
	public static void saveAll(List<EtlStageAreaObject> records, Connection srcConn) throws DBException {
		
		insertAll(utilities.parseList(records, EtlDatabaseObject.class), records.get(0).getRelatedConfiguration(), srcConn);
		
		List<EtlDatabaseObject> allSrckeys = new ArrayList<>();
		List<EtlDatabaseObject> allDstkeys = new ArrayList<>();
		
		for (EtlStageAreaObject stage : records) {
			stage.loadIdToChilds();
			
			allDstkeys.addAll(stage.getSrcUniqueKeyInfo());
			
			for (List<EtlDatabaseObject> dstKeys : stage.getDstUniqueKeyInfo()) {
				allDstkeys.addAll(dstKeys);
			}
		}
		
		insertAll(allSrckeys, (TableConfiguration) allSrckeys.get(0).getRelatedConfiguration(), srcConn);
		
		if (utilities.arrayHasElement(allDstkeys)) {
			insertAll(allDstkeys, (TableConfiguration) allDstkeys.get(0).getRelatedConfiguration(), srcConn);
		}
	}
}
