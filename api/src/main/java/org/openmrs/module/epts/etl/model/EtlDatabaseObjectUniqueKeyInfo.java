package org.openmrs.module.epts.etl.model;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;

public class EtlDatabaseObjectUniqueKeyInfo extends UniqueKeyInfo {
	
	public EtlDatabaseObjectUniqueKeyInfo(UniqueKeyInfo srcInfo, EtlDatabaseObject etlObject) {
		this.copy(srcInfo);
		
		this.loadValuesToFields(etlObject);
	}
	
	public static List<EtlDatabaseObjectUniqueKeyInfo> generate(TableConfiguration tabConf,
	        EtlDatabaseObject etlDatabaseObject) {
		
		if (tabConf.hasUniqueKeys()) {
			List<EtlDatabaseObjectUniqueKeyInfo> list = new ArrayList<>(tabConf.getUniqueKeys().size());
			
			for (UniqueKeyInfo uk : tabConf.getUniqueKeys()) {
				list.add(new EtlDatabaseObjectUniqueKeyInfo(uk, etlDatabaseObject));
			}
			
			return list;
			
		} else
			return null;
	}
	
}
