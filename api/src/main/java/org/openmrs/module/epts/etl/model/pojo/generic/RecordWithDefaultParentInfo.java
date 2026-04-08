package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class RecordWithDefaultParentInfo extends GenericDatabaseObject {
	
	private EtlDatabaseObject dstRelatedObject;
	
	private EtlDatabaseObject srcRelatedObject;
	
	private EtlDatabaseObject parentRecordInOrigin;
	
	private ParentTable parentRefInfo;
	
	public static RecordWithDefaultParentInfo init(EtlDatabaseObject srcObject, EtlDatabaseObject dstObject,
	        EtlDatabaseObject parentInOrigin, ParentTable parentRefInfo, Connection conn) throws DBException {
		
		DatabaseObjectConfiguration recursiveRecordTableInfo = parentRefInfo.getRelatedEtlConf()
		        .getRecordWithDefaultParentsInfoTabConf();
		
		if (!recursiveRecordTableInfo.isFullLoaded()) {
			recursiveRecordTableInfo.fullLoad(conn);
		}
		
		RecordWithDefaultParentInfo rec = new RecordWithDefaultParentInfo();
		
		rec.setRelatedConfiguration(recursiveRecordTableInfo);
		
		srcObject.loadObjectIdData();
		
		if (parentInOrigin == null) {
			throw new ForbiddenOperationException("parentInOrigin cannot be null");
		}
		
		rec.setFieldValue("record_origin_location_code", parentRefInfo.getRelatedEtlConf().getOriginAppLocationCode());
		rec.setFieldValue("src_table_name", srcObject.generateTableName());
		rec.setFieldValue("dst_table_name", dstObject.generateTableName());
		rec.setFieldValue("src_rec_id", srcObject.getObjectId().asSimpleValue());
		rec.setFieldValue("dst_rec_id", dstObject.getObjectId().asSimpleValue());
		rec.setFieldValue("parent_table", parentRefInfo.getTableName());
		rec.setFieldValue("parent_field", parentRefInfo.getChildColumnOnSimpleMapping());
		rec.setFieldValue("src_parent_id", parentInOrigin.getObjectId().asSimpleNumericValue());
		rec.setFieldValue("inconsistent_parent", -1);
		
		return rec;
		
	}
	
	public void setSrcRelatedObject(EtlDatabaseObject srcRelatedObject) {
		this.srcRelatedObject = srcRelatedObject;
	}
	
	public EtlDatabaseObject getSrcRelatedObject() {
		return srcRelatedObject;
	}
	
	public String getRecordOriginLocationCode() {
		return this.getFieldValue("record_origin_location_code").toString();
	}
	
	public String getSrcTableName() {
		return this.getFieldValue("src_table_name").toString();
	}
	
	public String getDstTableName() {
		return this.getFieldValue("dst_table_name").toString();
	}
	
	public String getParentTable() {
		return this.getFieldValue("parent_table").toString();
	}
	
	public String getParentField() {
		return this.getFieldValue("parent_field").toString();
	}
	
	public Long getSrcRecId() {
		return Long.parseLong(this.getFieldValue("src_rec_id").toString());
	}
	
	public Long getDstRecId() {
		return Long.parseLong(this.getFieldValue("dst_rec_id").toString());
	}
	
	public Long getSrcParentId() {
		return Long.parseLong(this.getFieldValue("src_parent_id").toString());
	}
	
	public void fullLoad(EtlItemConfiguration relatedItemConf, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		SrcConf relatedSrcConf = relatedItemConf.getSrcConf();
		
		if (!relatedItemConf.isFullLoaded()) {
			throw new ForbiddenOperationException("The relatedItemConf must be full loaded!!!");
		}
		
		DstConf dstConf = relatedItemConf.findDstTable(null, this.getDstTableName());
		
		this.parentRefInfo = relatedSrcConf.getFieldIsRelatedParent(Field.fastCreateField(this.getParentField()));
		
		//The srcObject and dstRelatedObject should be the same for multiple default parents for same record
		if (this.getSrcRelatedObject() == null) {
			this.setSrcRelatedObject(DatabaseObjectDAO.getByOid(relatedSrcConf,
			    Oid.fastCreate(relatedSrcConf.getPrimaryKey().asSimpleKey().getName(), this.getSrcRecId()), srcConn));
		}
		
		if (this.getDstRelatedObject() == null) {
			this.setDstRelatedObject(DatabaseObjectDAO.getByOid(dstConf,
			    Oid.fastCreate(dstConf.getPrimaryKey().asSimpleKey().getName(), this.getDstRecId()), dstConn));
		}
		
		if (this.srcRelatedObject == null) {
			throw new EtlExceptionImpl("The SrcRelatedObject is not anymore on database: " + this);
		}
		
		if (this.dstRelatedObject == null) {
			throw new EtlExceptionImpl("The DstRelatedObject is not anymore on database: " + this);
		}
		
		this.parentRecordInOrigin = DatabaseObjectDAO.getByOid(this.parentRefInfo,
		    Oid.fastCreate(this.parentRefInfo.getPrimaryKey().asSimpleKey().getName(), this.getSrcParentId()), srcConn);
	}
	
	public EtlDatabaseObject getParentRecordInOrigin() {
		return parentRecordInOrigin;
	}
	
	public ParentTable getParentRefInfo() {
		return parentRefInfo;
	}
	
	public EtlDatabaseObject getDstRelatedObject() {
		return dstRelatedObject;
	}
	
	public void setDstRelatedObject(EtlDatabaseObject relatedDstObject) {
		this.dstRelatedObject = relatedDstObject;
	}
	
	public static List<RecordWithDefaultParentInfo> getAllOfSrcRecord(SrcConf srcTable, Long srcRecId, Connection srcConn)
	        throws DBException {
		TableConfiguration tabConf = srcTable.getRelatedEtlConf().getRecordWithDefaultParentsInfoTabConf();
		
		if (!tabConf.isFieldsLoaded()) {
			tabConf.fullLoad(srcConn);
		}
		
		String sql = "";
		sql += " select " + tabConf.generateFullAliasedSelectColumns();
		sql += " from   " + tabConf.generateSelectFromClauseContent();
		sql += " where  src_rec_id = ? ";
		sql += "		and src_table_name = ?";
		
		Object[] params = { srcRecId, srcTable.getTableName() };
		
		return DatabaseObjectDAO.search(tabConf.getLoadHealper(), RecordWithDefaultParentInfo.class, sql, params, srcConn);
	}
	
	public void setAsInconsistent(Connection conn) throws DBException {
		this.setFieldValue("inconsistent_parent", 1);
		
		this.update((TableConfiguration) this.getRelatedConfiguration(), conn);
	}
	
	public static void deleteAllSuccessifulyProcessed(SrcConf srcConf, OpenConnection srcConn) throws DBException {
		EtlConfigurationTableConf skippedRecordTabConf = srcConf.getRelatedEtlConf()
		        .getRecordWithDefaultParentsInfoTabConf();
		
		DatabaseObjectDAO.removeAll(skippedRecordTabConf,
		    "src_table_name = '" + srcConf.getTableName() + "' and inconsistent_parent = -1", srcConn);
	}
	
}
