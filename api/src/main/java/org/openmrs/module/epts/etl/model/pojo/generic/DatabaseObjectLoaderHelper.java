package org.openmrs.module.epts.etl.model.pojo.generic;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.VO;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;

public class DatabaseObjectLoaderHelper implements VOLoaderHelper {
	
	private DatabaseObjectConfiguration tableConf;
	
	public DatabaseObjectLoaderHelper(DatabaseObjectConfiguration tableConf) {
		this.tableConf = tableConf;
	}
	
	@Override
	public void beforeLoad(VO vo) {
		if (!(vo instanceof DatabaseObject)) {
			throw new ForbiddenOperationException("This method is only applied to DatabaseObject instances");
		}
		
		((DatabaseObject) vo).setRelatedConfiguration(this.tableConf);
	}
	
	@Override
	public void afterLoad(VO vo) {
	}
	
}
