package org.openmrs.module.epts.etl.inconsistenceresolver.model;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.BaseVO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class InconsistenceInfo extends BaseVO {
	
	private Integer id;
	
	private EtlDatabaseObject obj;
	
	private ParentTable refInfo;
	
	private String recordOriginLocationCode;
	
	public InconsistenceInfo() {
	}
	
	public InconsistenceInfo(EtlDatabaseObject obj, ParentTable refInfo, String recordOriginLocationCode) {
		this.obj = obj;
		this.refInfo = refInfo;
		this.recordOriginLocationCode = recordOriginLocationCode;
	}
	
	public boolean isResolved() {
		return this.getDefaultParentId() != null;
	}
	
	public String getRecordOriginLocationCode() {
		return recordOriginLocationCode;
	}
	
	public void setRecordOriginLocationCode(String recordOriginLocationCode) {
		this.recordOriginLocationCode = recordOriginLocationCode;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getTableName() {
		return this.obj.getRelatedConfiguration().getObjectName();
	}
	
	public Oid getRecordId() {
		return this.obj.getObjectId();
	}
	
	public EtlDatabaseObject getObj() {
		return obj;
	}
	
	public String getParentTableName() {
		return this.refInfo.getTableName();
	}
	
	public Integer getParentId() {
		return refInfo.generateParentOidFromChild(obj).asSimpleNumericValue().intValue();
	}
	
	public Object getDefaultParentId() {
		return refInfo.getDefaultValueDueInconsistency();
		
	}
	
	public static InconsistenceInfo generate(EtlDatabaseObject record,  ParentTable refInfo,
	        String recordOriginLocationCode) {
		InconsistenceInfo info = new InconsistenceInfo(record, refInfo, recordOriginLocationCode);
		
		return info;
	}
	
	public void save(TableConfiguration tableConfiguration, Connection conn) throws DBException {
		InconsistenceInfoDAO.insert(this, tableConfiguration, conn);
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
	}
	
	@Override
	public String toString() {
		String str = getTableName() + "(" + getRecordId() + "). Parent " + getParentTableName() + "(" + getParentId() + ")";
		
		return str;
	}
	
	public Map<ParentTableImpl, Integer> parseToMissingInfo() {
		Map<ParentTableImpl, Integer> missingParents = new HashMap<>();
		
		missingParents.put((ParentTableImpl) this.refInfo, getParentId());
		
		return missingParents;
	}
	
	public static Map<ParentTableImpl, Integer> parseToMissingInfo(List<InconsistenceInfo> inconsistences) {
		Map<ParentTableImpl, Integer> missingParents = new HashMap<>();
		
		for (InconsistenceInfo i : inconsistences) {
			missingParents.put((ParentTableImpl) i.refInfo, i.getParentId());
		}
		
		return missingParents;
	}
}
