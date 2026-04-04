package org.openmrs.module.epts.etl.etl.model;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.EtlActionType;
import org.openmrs.module.epts.etl.conf.types.EtlDstType;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.model.stage.EtlStageAreaObjectDAO;
import org.openmrs.module.epts.etl.etl.model.stage.EtlStageObjectInfo;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.TransformationType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.EtlInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class EtlLoadHelper {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<EtlDatabaseObject> srcObjects;
	
	private EtlProcessor processor;
	
	private LoadingType loadingType;
	
	private List<DstConf> dstConf;
	
	public EtlLoadHelper(EtlProcessor processor, List<EtlDatabaseObject> srcObjects, LoadingType loadingType) {
		this.processor = processor;
		this.srcObjects = srcObjects;
		this.loadingType = loadingType;
		
		this.dstConf = new ArrayList<>();
		
		for (EtlDatabaseObject obj : srcObjects) {
			if (obj.hasDestinationRecords()) {
				for (EtlDatabaseObject dst : obj.getDestinationObjects()) {
					if (!this.dstConf.contains(dst.getRelatedConfiguration())) {
						this.dstConf.add((DstConf) dst.getRelatedConfiguration());
					}
				}
			}
		}
		
	}
	
	public List<DstConf> getDstConf() {
		return dstConf;
	}
	
	public EtlProcessor getProcessor() {
		return this.processor;
	}
	
	public Engine<? extends EtlDatabaseObject> getEngine() {
		return getProcessor().getEngine();
	}
	
	public EtlController getController() {
		return (EtlController) getEngine().getRelatedOperationController();
	}
	
	public EtlOperationConfig getEtlOperationConfig() {
		return getController().getOperationConfig();
	}
	
	public boolean isPrincipalLoading() {
		return this.loadingType.isPrincipal();
	}
	
	public List<EtlDatabaseObject> getSrcObjects() {
		return srcObjects;
	}
	
	public List<EtlStageObjectInfo> generateStageInfoForAll(Connection srcConn, Connection dstConn) throws DBException {
		
		List<EtlStageObjectInfo> info = new ArrayList<>(this.getSrcObjects().size());
		
		for (EtlDatabaseObject rec : this.getSrcObjects()) {
			info.add(EtlStageObjectInfo.generate(rec, srcConn, dstConn));
		}
		
		return info;
	}
	
	public void load(Connection srcConn, Connection dstConn) throws ParentNotYetMigratedException, DBException {
		
		for (DstConf dst : this.getDstConf()) {
			load(dst, srcConn, dstConn);
			
			if (hasUnresolvedError(dst)) {
				logError("Found issues loading to " + dst);
				logError("Aborting operation");
				
				return;
			}
		}
		
		for (EtlDatabaseObject obj : getAllTransformedObjects()) {
			EtlInfo info = obj.getEtlInfo();
			
			if (info.hasExceptionOnEtl()) {
				info.markAsFailed();
			} else {
				info.markAsSuccess();
			}
		}
		
		if (getEtlOperationConfig().writeOperationHistory()) {
			EtlStageAreaObjectDAO.saveAll(generateStageInfoForAll(srcConn, dstConn), srcConn);
		}
		
		if (getEtlOperationConfig().getAfterEtlActionType().isDelete()) {
			for (EtlDatabaseObject obj : this.getSrcObjects()) {
				DatabaseObjectDAO.remove(obj, srcConn);
			}
		}
	}
	
	private boolean hasUnresolvedError(DstConf dst) {
		
		for (EtlDatabaseObject r : this.getSrcObjects()) {
			EtlDatabaseObject dstObject = r.retriveDestinationRecord(dst);
			
			if (dstObject != null && dstObject.getEtlInfo().hasExceptionOnEtl()) {
				
				if (dstObject.getEtlInfo().getExceptionOnEtl() instanceof InconsistentStateException
				        && dst.getRelatedEtlConf().getDefaultInconsistencyBehavior().markRecordAsFailed()) {
					
					continue;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	private void load(DstConf dstConf, Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		EtlDstType dstType = getProcessor().determineDstType(dstConf);
		
		if (dstType.isDb()) {
			loadToDb(dstConf, srcConn, dstConn);
		} else if (dstType.isFile()) {
			loadToFile(dstConf);
		} else if (dstType.isInstantaneo()) {
			getEngine().requestDisplayOfEtlResult(dstConf, getAllTransformedObjects(dstConf));
		} else {
			throw new ForbiddenOperationException("Unsupported dstType '" + dstType + "'");
		}
	}
	
	private void loadToDb(DstConf dstConf, Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		if (!dstConf.isFullLoaded()) {
			dstConf.fullLoad();
		}
		
		this.beforeLoadToDb(dstConf, srcConn, dstConn);
		
		this.onLoadToDb(dstConf, dstConn);
		
		this.afterLoadToDb(dstConf, srcConn, dstConn);
		
	}
	
	/**
	 * @param srcConn
	 * @param dstConn
	 * @param config
	 * @param processedRecords
	 * @throws ParentNotYetMigratedException
	 * @throws DBException
	 */
	public void afterLoadToDb(DstConf dstConf, Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		if (getActionType().isCreate() || getActionType().isUpdate()) {
			for (EtlDatabaseObject loadRec : this.getAllReadyTransformedObjects(dstConf)) {
				
				if (loadRec.getEtlInfo().hasParentsWithDefaultValues()) {
					loadRec.getEtlInfo().saveRecordsWithDefaultsParents(srcConn, dstConn);
				}
				
				loadRec.getEtlInfo().markAsSuccess();
			}
		}
	}
	
	void tryToAddToResult(EtlOperationItemResult<EtlDatabaseObject> resultItem) {
		if (isPrincipalLoading()) {
			getProcessor().getTaskResultInfo().addOrUpdate(resultItem);
		}
	}
	
	void loadAndAddResult(EtlOperationResultHeader<EtlDatabaseObject> result, DstConf dstConf) {
		if (isPrincipalLoading()) {
			if (result != null) {
				getProcessor().getTaskResultInfo().addAllFromOtherResult(result);
			}
		}
	}
	
	/**
	 * @param dstConn
	 * @param config
	 * @param objects
	 * @throws DBException
	 * @throws ForbiddenOperationException
	 */
	public void onLoadToDb(DstConf dstConf, Connection dstConn) throws DBException, ForbiddenOperationException {
		
		List<EtlDatabaseObject> objects = getAllReadyTransformedObjects(dstConf);
		
		if (getActionType().isCreate()) {
			logDebug("Starting the insertion of " + objects.size() + " " + dstConf.getTableName() + " on db...");
			
			dstConf.setUseMysqlInsertIgnore(this.getEtlOperationConfig().useseMysqlInsertIgnore());
			
			this.loadAndAddResult(DatabaseObjectDAO.load(objects, dstConf, dstConn), dstConf);
			
			logDebug(objects.size() + " " + dstConf.getTableName() + "  inserted on db!");
		} else if (getActionType().isUpdate()) {
			logDebug("Starting the update of " + objects.size() + " " + dstConf.getTableName() + " on db...");
			
			this.loadAndAddResult(DatabaseObjectDAO.updateAll(objects, dstConf, dstConn), dstConf);
			logDebug(objects.size() + " " + dstConf.getTableName() + " updated from db!");
			
		} else if (getActionType().isDelete()) {
			logDebug("Starting the deletion of " + objects.size() + " " + dstConf.getTableName() + " on db...");
			
			this.loadAndAddResult(DatabaseObjectDAO.deleteAll(objects, dstConf, dstConn), dstConf);
			
			logDebug(objects.size() + " " + dstConf.getTableName() + "  deleted on db!");
			
		} else {
			throw new ForbiddenOperationException("Unsupported operation " + getActionType() + " on ETL");
		}
	}
	
	public List<EtlDatabaseObject> getAllSuccedTransformedObjects(DstConf dstConf) {
		return getAllTransformedObjects(dstConf, EtlStatus.SUCCESS);
	}
	
	private List<EtlDatabaseObject> getAllReadyTransformedObjects(DstConf dstConf) {
		return getAllTransformedObjects(dstConf, EtlStatus.READY);
	}
	
	public List<EtlDatabaseObject> getAllTransformedObjects(DstConf dstConf) {
		return getAllTransformedObjects(dstConf, null);
	}
	
	public List<EtlDatabaseObject> getAllTransformedObjects() {
		List<EtlDatabaseObject> allOfDst = new ArrayList<>();
		
		for (EtlDatabaseObject obj : this.getSrcObjects()) {
			if (!obj.hasDestinationRecords())
				continue;
			
			allOfDst.addAll(obj.getDestinationObjects());
		}
		
		return allOfDst;
	}
	
	public List<EtlDatabaseObject> getAllTransformedObjects(DstConf dstConf, EtlStatus status) {
		List<EtlDatabaseObject> allOfDst = new ArrayList<>();
		
		for (EtlDatabaseObject srcObject : this.getSrcObjects()) {
			EtlDatabaseObject dstObject = srcObject.retriveDestinationRecord(dstConf);
			
			if (dstObject != null && (status == null || dstObject.getEtlInfo().getStatus().equals(status))) {
				allOfDst.add(dstObject);
			}
		}
		
		return allOfDst;
	}
	
	/**
	 * @param srcConn
	 * @param dstConn
	 * @param objects
	 * @param processedRecords
	 * @throws DBException
	 * @throws ParentNotYetMigratedException
	 * @throws MissingParentException
	 */
	public void beforeLoadToDb(DstConf dstConf, Connection srcConn, Connection dstConn)
	        throws DBException, ParentNotYetMigratedException, MissingParentException {
		
		List<EtlDatabaseObject> toLoad = this.getAllTransformedObjects(dstConf);
		
		this.logDebug("Preparing the load of " + toLoad.size());
		
		for (EtlDatabaseObject obj : toLoad) {
			
			if (obj.getEtlInfo().hasExceptionOnEtl())
				continue;
			
			EtlInfo etlInfo = obj.getEtlInfo();
			
			this.logTrace("Preparing the load of dstRecord " + etlInfo.getTransformedObject());
			
			if (getActionType().isCreate() || getActionType().isUpdate()) {
				
				etlInfo.loadDstParentInfo(srcConn, dstConn);
				String errorMsg = "Found inconsistences on dstRecord " + etlInfo.getTransformedObject()
				        + " but all were resolved!";
				
				if (!etlInfo.getResultItem().hasUnresolvedInconsistences()) {
					etlInfo.setStatus(EtlStatus.READY);
					if (etlInfo.getResultItem().hasInconsistences()) {
						this.logTrace(errorMsg);
					}
				} else {
					
					InconsistentStateException e = new InconsistentStateException(
					        etlInfo.getResultItem().getInconsistenceInfo());
					
					if (getProcessor().getRelatedEtlConfiguration().getDefaultInconsistencyBehavior().abortProcess()) {
						throw e;
					} else {
						etlInfo.setExceptionOnEtl(e);
						etlInfo.setStatus(EtlStatus.FAIL);
					}
				}
			} else {
				etlInfo.setStatus(EtlStatus.READY);
			}
			
			tryToAddToResult(etlInfo.getResultItem());
			
		}
		
	}
	
	/**
	 * @return
	 */
	public EtlActionType getActionType() {
		return getEtlOperationConfig().getActionType();
	}
	
	public void loadToFile(DstConf dstConf) throws ParentNotYetMigratedException, DBException {
		List<EtlDatabaseObject> toLoad = this.getAllTransformedObjects(dstConf);
		
		this.logDebug("Preparing the load of " + toLoad.size());
		
		for (EtlDatabaseObject obj : toLoad) {
			obj.getEtlInfo().markAsReady();
		}
		
		List<EtlDatabaseObject> objs = getAllReadyTransformedObjects(dstConf);
		
		String dataFile = getEngine().getDataDir().getAbsolutePath() + File.separator + objs.get(0).generateTableName();
		
		String data = null;
		
		if (getEngine().isJsonDst()) {
			data = utilities.parseToJSON(objs);
			
			dataFile += ".json";
		} else if (getEngine().isCsvDst()) {
			dataFile += ".csv";
			
			data = utilities.parseToCSVWithoutHeader(objs, dstConf.getExcludedFields(), dstConf.getCsvDelimiter());
		} else if (getEngine().isDumpDst()) {
			dataFile += ".sql";
			
			data = TableConfiguration.generateInsertDump(objs);
		}
		
		synchronized (getEngine()) {
			boolean includeHeader = FileUtilities.isEmpty(new File(dataFile));
			
			if (includeHeader) {
				FileUtilities.write(dataFile,
				    utilities.generateCsvHeader(objs.get(0), dstConf.getExcludedFields(), dstConf.getCsvDelimiter()));
			}
			
			FileUtilities.write(dataFile, data);
		}
		
		getProcessor().getTaskResultInfo()
		        .addAllToRecordsWithNoError(EtlOperationItemResult.parseFromEtlDatabaseObject(objs));
	}
	
	void logTrace(String msg) {
		getProcessor().logTrace(msg);
	}
	
	void logDebug(String msg) {
		getProcessor().logDebug(msg);
	}
	
	void logInfo(String msg) {
		getProcessor().logInfo(msg);
	}
	
	void logWarn(String msg) {
		getProcessor().logWarn(msg);
	}
	
	void logError(String msg) {
		getProcessor().logError(msg);
	}
	
	public static void performeParentLoading(EtlDatabaseObject srcObject, Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		EtlInfo etlInfo = srcObject.getDestinationObjects().get(0).getEtlInfo();
		
		String msg = "Initializing the load of parent record ["
		        + ((TableConfiguration) srcObject.getRelatedConfiguration()).getFullTableDescription() + srcObject + "]";
		
		String tree = "";
		
		EtlInfo parent = srcObject.getEtlInfo();
		
		while (parent != null) {
			if (tree.isEmpty()) {
				tree = parent.getTransformedObject().toString();
			} else {
				tree = tree + " <<<< " + parent.getTransformedObject().toString();
			}
			
			parent = parent.getParentEtlInfo();
		}
		
		msg += " Tree Info: [" + tree + "]";
		
		etlInfo.getProcessor().logTrace(msg);
		new EtlLoadHelper(etlInfo.getProcessor(), utilities.parseToList(srcObject), LoadingType.INNER)
		        .load(etlInfo.getDstConf(), srcConn, dstConn);
	}
	
	public static EtlLoadHelper fastLoadRecord(EtlProcessor processor, EtlDatabaseObject srcRecord, DstConf dstConf,
	        TransformationType transformationType, Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		EtlDatabaseObject dstObject = dstConf.getTransformerInstance().transform(processor, srcRecord, dstConf, null,
		    transformationType, srcConn, dstConn);
		
		srcRecord.addDestinationRecord(dstObject);
		
		String msg = "Initializing the load of record [" + dstConf.getFullTableDescription() + dstObject + "]";
		
		processor.logTrace(msg);
		
		EtlLoadHelper lp = new EtlLoadHelper(processor, utilities.parseToList(srcRecord), LoadingType.INNER);
		
		lp.load(dstConf, srcConn, dstConn);
		
		return lp;
	}
}
