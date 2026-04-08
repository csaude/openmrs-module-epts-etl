package org.openmrs.module.epts.etl.etl.model.stage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class EtlStageObjectInfo {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private EtlStageAreaObject srcStageInfoObject;
	
	private List<EtlStageAreaObject> dstStageInfoObject;
	
	private EtlStageObjectInfo(EtlDatabaseObject srcObj, Connection srcConn, Connection dstConn) throws DBException {
		
		this.setSrcStageInfoObject(EtlStageAreaObject.generateSrc(srcObj, srcConn, dstConn));
		
		if (!srcObj.hasDestinationRecords()) {
			throw new EtlExceptionImpl("No dst objects found with src object: " + srcObj, srcObj,
			        ActionOnEtlException.ABORT_PROCESS);
		}
		
		this.setDstStageInfoObject(
		    EtlStageAreaObject.generateDst(srcStageInfoObject, srcObj.getDestinationObjects(), dstConn, srcConn));
	}
	
	public static EtlStageObjectInfo generate(EtlDatabaseObject rec, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		EtlStageObjectInfo recInfo = new EtlStageObjectInfo(rec, srcConn, dstConn);
		
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
	
	public List<EtlStageAreaObject> getNewDstStageInfoObject() {
		
		if (hasDstStageInfoObject()) {
			List<EtlStageAreaObject> allNew = new ArrayList<>();
			
			for (EtlStageAreaObject o : this.getDstStageInfoObject()) {
				if (!o.hasValuedObjectId()) {
					allNew.add(o);
				}
			}
		}
		
		return null;
	}
	
	public List<EtlStageAreaObject> getOldDstStageInfoObject() {
		
		if (hasDstStageInfoObject()) {
			List<EtlStageAreaObject> old = new ArrayList<>();
			
			for (EtlStageAreaObject o : this.getDstStageInfoObject()) {
				if (o.hasValuedObjectId()) {
					old.add(o);
				}
			}
		}
		
		return null;
	}
	
	private void setDstStageInfoObject(List<EtlStageAreaObject> dstStageInfoObject) {
		this.dstStageInfoObject = dstStageInfoObject;
	}
	
	private void loadDstStageObjectIdToDstKeyInfoObject() {
		
		for (EtlStageAreaObject obj : getDstStageInfoObject()) {
			if (obj.getRelatedEtlObject().getEtlInfo().isInSuccessStatus()) {
				obj.loadIdToChilds();
			}
		}
	}
	
	private boolean hasDstStageInfoObject() {
		return utilities.listHasElement(this.getDstStageInfoObject());
	}
	
	public static void loadSrcStageObjectIdToDstStageObjectId(List<EtlStageObjectInfo> stageObjectInfo) {
		if (utilities.listHasNoElement(stageObjectInfo))
			return;
		
		for (EtlStageObjectInfo st : stageObjectInfo) {
			st.loadSrcIdToDstStageObject();
		}
	}
	
	public static void loadSrcStageIdToSrcKeyInfo(List<EtlStageObjectInfo> stageInfo) {
		
		if (utilities.listHasNoElement(stageInfo))
			return;
		
		for (EtlStageObjectInfo sti : stageInfo) {
			sti.loadSrcStageObjectIdToSrcKeyInfoObject();
		}
	}
	
	public static void loadDstStageObjectIdToDstKeyInfoObject(List<EtlStageObjectInfo> stageObjectInfo) {
		if (utilities.listHasNoElement(stageObjectInfo))
			return;
		
		for (EtlStageObjectInfo st : stageObjectInfo) {
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
		if (utilities.listHasNoElement(this.getSrcStageInfoObject().getKeyInfo()))
			return;
		
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
	
	private static List<EtlStageAreaObject> collectSrcObjects(List<EtlStageObjectInfo> stageInfo, Boolean existing) {
		if (utilities.listHasNoElement(stageInfo))
			return null;
		
		List<EtlStageAreaObject> collected = new ArrayList<>();
		
		for (EtlStageObjectInfo sti : stageInfo) {
			EtlStageAreaObject obj = sti.getSrcStageInfoObject();
			
			boolean collectAll = existing == null;
			boolean collectExisting = existing != null && existing;
			boolean collectNotExisting = existing != null && !existing;
			
			if (collectAll) {
				collected.add(obj);
			} else if (collectNotExisting && !obj.alreadyExistOnDB()) {
				collected.add(obj);
			} else if (collectExisting && obj.alreadyExistOnDB()) {
				collected.add(obj);
			}
		}
		
		return collected;
	}
	
	public static List<EtlStageAreaObject> collectSrcObjects(List<EtlStageObjectInfo> stageInfo) {
		return collectSrcObjects(stageInfo, null);
	}
	
	public static List<EtlStageAreaObject> collectNotExistingSrcObjects(List<EtlStageObjectInfo> stageInfo) {
		return collectSrcObjects(stageInfo, false);
	}
	
	public static List<EtlStageAreaObject> collectExistingSrcObjects(List<EtlStageObjectInfo> stageInfo) {
		return collectSrcObjects(stageInfo, true);
	}
	
	private static List<EtlStageAreaObject> collectDstObjects(List<EtlStageObjectInfo> stageInfo, Boolean existing) {
		if (utilities.listHasNoElement(stageInfo))
			return null;
		
		List<EtlStageAreaObject> collected = new ArrayList<>();
		
		for (EtlStageObjectInfo sti : stageInfo) {
			if (sti.hasDstStageInfoObject()) {
				for (EtlStageAreaObject obj : sti.getDstStageInfoObject()) {
					
					boolean collectAll = existing == null;
					boolean collectExisting = existing != null && existing;
					boolean collectNotExisting = existing != null && !existing;
					
					if (collectAll) {
						collected.add(obj);
					} else if (collectNotExisting && !obj.alreadyExistOnDB()) {
						collected.add(obj);
					} else if (collectExisting && obj.alreadyExistOnDB()) {
						collected.add(obj);
					}
				}
			}
		}
		
		return collected;
	}
	
	public static List<EtlStageAreaObject> collectDstObjects(List<EtlStageObjectInfo> stageInfo) {
		return collectDstObjects(stageInfo, null);
	}
	
	public static List<EtlStageAreaObject> collectNotExistingDstObjects(List<EtlStageObjectInfo> stageInfo) {
		return collectDstObjects(stageInfo, false);
	}
	
	public static List<EtlStageAreaObject> collectExistingDstObjects(List<EtlStageObjectInfo> stageInfo) {
		return collectDstObjects(stageInfo, true);
	}
	
	public static List<EtlDatabaseObject> collectDstKeyInfo(List<EtlStageObjectInfo> stageInfo) {
		if (utilities.listHasNoElement(stageInfo))
			return null;
		
		List<EtlDatabaseObject> collected = new ArrayList<>(stageInfo.size());
		
		for (EtlStageObjectInfo sti : stageInfo) {
			collected.addAll(EtlStageAreaObject.collectAllKeyInfo(sti.getDstStageInfoObject()));
		}
		
		return collected;
	}
	
	public static List<EtlDatabaseObject> collectSrcKeyInfoForNotExistingObjects(List<EtlStageObjectInfo> stageInfo) {
		if (utilities.listHasNoElement(stageInfo))
			return null;
		
		List<EtlDatabaseObject> collected = new ArrayList<>(stageInfo.size());
		
		collected.addAll(EtlStageAreaObject.collectAllKeyInfo(collectNotExistingSrcObjects(stageInfo)));
		
		return collected;
	}
	
}
