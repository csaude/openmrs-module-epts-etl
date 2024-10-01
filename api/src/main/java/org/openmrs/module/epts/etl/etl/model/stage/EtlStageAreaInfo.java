package org.openmrs.module.epts.etl.etl.model.stage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.etl.model.EtlLoadHelperRecord;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class EtlStageAreaInfo {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private EtlStageAreaObject srcStageInfoObject;
	
	private List<EtlStageAreaObject> dstStageInfoObject;
	
	private EtlLoadHelperRecord etlInfo;
	
	private EtlStageAreaInfo(EtlLoadHelperRecord etlInfo, Connection srcConn, Connection dstConn) throws DBException {
		this.etlInfo = etlInfo;
		
		this.setSrcStageInfoObject(EtlStageAreaObject.generateSrc(etlInfo.getSrcObject(), srcConn));
		
		this.setDstStageInfoObject(EtlStageAreaObject.generateDst(etlInfo.getDstRecords(), dstConn));
	}
	
	public static EtlStageAreaInfo generate(EtlLoadHelperRecord rec, Connection srcConn, Connection dstConn)
	        throws DBException {
		EtlStageAreaInfo recInfo = new EtlStageAreaInfo(rec, srcConn, dstConn);
		
		return recInfo;
	}
	
	public EtlStageAreaObject getSrcStageInfoObject() {
		return srcStageInfoObject;
	}
	
	private void setSrcStageInfoObject(EtlStageAreaObject srcStageInfoObject) {
		this.srcStageInfoObject = srcStageInfoObject;
	}
	
	public List<EtlStageAreaObject> getDstStageInfoObject() {
		return dstStageInfoObject;
	}
	
	private void setDstStageInfoObject(List<EtlStageAreaObject> dstStageInfoObject) {
		this.dstStageInfoObject = dstStageInfoObject;
	}
	
	public EtlLoadHelperRecord getEtlInfo() {
		return etlInfo;
	}
	
	private void loadDstStageObjectIdToDstKeyInfoObject() {
		
		if (!hasDstStageInfoObject()) {
			return;
		}
		
		for (EtlStageAreaObject obj : getDstStageInfoObject()) {
			obj.loadIdToChilds();
		}
	}
	
	public static List<EtlStageAreaObject> collectAllSrcStageAreaObject(List<EtlStageAreaInfo> stageInfo) {
		if (utilities.arrayHasNoElement(stageInfo))
			return null;
		
		List<EtlStageAreaObject> collected = new ArrayList<>(stageInfo.size());
		
		for (EtlStageAreaInfo sti : stageInfo) {
			collected.add(sti.getSrcStageInfoObject());
		}
		
		return collected;
	}
	
	public static List<EtlDatabaseObject> collectAllSrcStageAreaObjectAsEtlDatabaseObject(List<EtlStageAreaInfo> stageInfo) {
		return utilities.parseList(collectAllSrcStageAreaObject(stageInfo), EtlDatabaseObject.class);
	}
	
	public static List<EtlStageAreaObject> collectAllDstStageAreaObject(List<EtlStageAreaInfo> stageInfo) {
		if (utilities.arrayHasNoElement(stageInfo))
			return null;
		
		List<EtlStageAreaObject> collected = new ArrayList<>(stageInfo.size());
		
		for (EtlStageAreaInfo sti : stageInfo) {
			collected.addAll(sti.getDstStageInfoObject());
		}
		
		return collected;
	}
	
	public static List<EtlDatabaseObject> collectAllDstStageAreaObjectAsEtlDatabaseObject(List<EtlStageAreaInfo> stageInfo) {
		return utilities.parseList(collectAllDstStageAreaObject(stageInfo), EtlDatabaseObject.class);
	}
	
	public static List<EtlDatabaseObject> collectAllDstKeyInfo(List<EtlStageAreaInfo> stageInfo) {
		if (utilities.arrayHasNoElement(stageInfo))
			return null;
		
		List<EtlDatabaseObject> collected = new ArrayList<>(stageInfo.size());
		
		for (EtlStageAreaInfo sti : stageInfo) {
			collected.addAll(EtlStageAreaObject.collectAllKeyInfo(sti.getDstStageInfoObject()));
		}
		
		return collected;
	}
	
	public static List<EtlDatabaseObject> collectAllSrcKeyInfo(List<EtlStageAreaInfo> stageInfo) {
		if (utilities.arrayHasNoElement(stageInfo))
			return null;
		
		List<EtlDatabaseObject> collected = new ArrayList<>(stageInfo.size());
		
		collected.addAll(EtlStageAreaObject.collectAllKeyInfo(collectAllSrcStageAreaObject(stageInfo)));
		
		return collected;
	}
	
	private boolean hasDstStageInfoObject() {
		return utilities.arrayHasElement(this.getDstStageInfoObject());
	}
	
	public static void loadSrcStageObjectIdToDstStageObjectId(List<EtlStageAreaInfo> stageObjectInfo) {
		if (utilities.arrayHasNoElement(stageObjectInfo))
			return;
		
		for (EtlStageAreaInfo st : stageObjectInfo) {
			st.loadSrcIdToDstStageObject();
		}
	}
	
	public static void loadSrcStageIdToSrcKeyInfo(List<EtlStageAreaInfo> stageInfo) {
		
		if (utilities.arrayHasNoElement(stageInfo))
			return;
		
		for (EtlStageAreaInfo sti : stageInfo) {
			sti.loadSrcStageObjectIdToSrcKeyInfoObject();
		}
	}
	
	public static void loadDstStageObjectIdToDstKeyInfoObject(List<EtlStageAreaInfo> stageObjectInfo) {
		if (utilities.arrayHasNoElement(stageObjectInfo))
			return;
		
		for (EtlStageAreaInfo st : stageObjectInfo) {
			st.loadDstStageObjectIdToDstKeyInfoObject();
		}
		
	}
	
	private void loadSrcIdToDstStageObject() {
		if (hasDstStageInfoObject()) {
			List<EtlStageAreaObject> newDstStageInfo = new ArrayList<>();
			
			for (EtlStageAreaObject obj : this.getDstStageInfoObject()) {
				if (this.getSrcStageInfoObject().getFieldValue("id") != null) {
					obj.setFieldValue("stage_record_id", this.getSrcStageInfoObject().getFieldValue("id"));
					newDstStageInfo.add(obj);
				}
			}
			
			this.setDstStageInfoObject(newDstStageInfo);
		}
	}
	
	private void loadSrcStageObjectIdToSrcKeyInfoObject() {
		//The src info
		List<EtlDatabaseObject> newSrcStageInfo = new ArrayList<>();
		
		for (EtlDatabaseObject obj : this.getSrcStageInfoObject().getKeyInfo()) {
			if (this.getSrcStageInfoObject().getFieldValue("id") != null) {
				obj.setFieldValue("stage_record_id", this.getSrcStageInfoObject().getFieldValue("id"));
				
				newSrcStageInfo.add(obj);
			}
		}
		
		this.getSrcStageInfoObject().setKeyInfo(newSrcStageInfo);
	}
	
}
