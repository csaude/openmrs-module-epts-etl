package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.model.ParentInfo;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.engine.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class LoadRecord {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected EtlDatabaseObject srcRecord;
	
	protected EtlDatabaseObject dstRecord;
	
	protected DstConf dstConf;
	
	protected List<ParentInfo> parentsWithDefaultValues;
	
	protected EtlProcessor processor;
	
	private SrcConf srcConf;
	
	private EtlOperationItemResult<EtlDatabaseObject> resultItem;
	
	private LoadStatus status;
	
	public LoadRecord(EtlDatabaseObject srcRecord, EtlDatabaseObject dstRecord, SrcConf srcConf, DstConf dstConf,
	    EtlProcessor engine) {
		
		this.srcRecord = srcRecord;
		this.dstRecord = dstRecord;
		
		this.srcConf = srcConf;
		this.dstConf = dstConf;
		
		this.processor = engine;
		
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
		
		this.dstRecord.setUniqueKeysInfo(UniqueKeyInfo.cloneAllAndLoadValues(this.dstConf.getUniqueKeys(), this.dstRecord));
		
		this.resultItem = new EtlOperationItemResult<EtlDatabaseObject>(srcRecord);
		
		this.status = LoadStatus.UNDEFINED;
	}
	
	public EtlDatabaseObject getSrcRecord() {
		return srcRecord;
	}
	
	public boolean isInReadyStatus() {
		return this.getStatus().isReady();
	}
	
	public boolean isInSuccessStatus() {
		return this.getStatus().isSuccess();
	}
	
	public boolean isInFailStatus() {
		return this.getStatus().isFail();
	}
	
	public boolean isInSkipStatus() {
		return this.getStatus().isSkip();
	}
	
	public boolean isInUndefinedStatus() {
		return this.getStatus().isUndefined();
	}
	
	public LoadStatus getStatus() {
		return status;
	}
	
	public void setStatus(LoadStatus status) {
		this.status = status;
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
	
	public EtlOperationItemResult<EtlDatabaseObject> getResultItem() {
		return resultItem;
	}
	
	public EtlProcessor getTaskProcessor() {
		return processor;
	}
	
	public DBConnectionInfo getSrcConnInfo() {
		return getTaskProcessor().getSrcConnInfo();
	}
	
	public DBConnectionInfo getDstConnInfo() {
		return getTaskProcessor().getDstConnInfo();
	}
	
	public SrcConf getSrcConf() {
		return srcConf;
	}
	
	public EtlConfiguration getEtlConfiguration() {
		return getDstConf().getRelatedEtlConf();
	}
	
	public EtlDatabaseObject getDstRecord() {
		return dstRecord;
	}
	
	public DstConf getDstConf() {
		return dstConf;
	}
	
	public List<ParentInfo> getParentsWithDefaultValues() {
		return parentsWithDefaultValues;
	}
	
	public boolean hasParentsWithDefaultValues() {
		return utilities.arrayHasElement(getParentsWithDefaultValues());
	}
	
	public void reloadParentsWithDefaultValues(Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		for (ParentInfo parentInfo : this.getParentsWithDefaultValues()) {
			
			List<SrcConf> avaliableSrcForCurrParent = parentInfo.getParentTableConfInDst()
			        .findRelatedSrcConfWhichAsAtLeastOnematchingDst();
			
			if (utilities.arrayHasNoElement(avaliableSrcForCurrParent)) {
				throw new ForbiddenOperationException(
				        "There are relashioship which cannot auto resolved as there is no configured etl for "
				                + parentInfo.getParentTableConfInDst().getTableName() + " as source and destination!");
			}
			
			EtlDatabaseObject dstParent = null;
			
			for (SrcConf src : avaliableSrcForCurrParent) {
				DstConf dst = src.getParentConf().findDstTable(parentInfo.getParentTableConfInDst().getTableName());
				
				EtlDatabaseObject recordAsSrc = src.createRecordInstance();
				recordAsSrc.setRelatedConfiguration(src);
				
				recordAsSrc.copyFrom(parentInfo.getParentRecordInOrigin());
				
				dstParent = dst.transform(recordAsSrc, srcConn, getSrcConnInfo(), getDstConnInfo());
				
				if (dstParent != null) {
					LoadRecord parentData = new LoadRecord(recordAsSrc, dstParent,
					        parentInfo.getParentTableConfInDst().findRelatedSrcConf(), dst, getTaskProcessor());
					
					try {
						EtlLoadHelper.quickLoad(parentData, srcConn, dstConn);
					}
					catch (DBException e) {
						if (e.isIntegrityConstraintViolationException()) {
							processor.logDebug("The parent for default for parent [" + parentInfo.getParentRecordInOrigin()
							        + "] could not be loaded. The dstRecord [");
							
							this.getResultItem()
							        .addInconsistence(InconsistenceInfo.generate(getDstRecord().generateTableName(),
							            getDstRecord().getObjectId(), parentInfo.getParentTableConfInDst().getTableName(),
							            parentInfo.getParentRecordInOrigin().getObjectId().getSimpleValueAsInt(), null,
							            this.getDstConf().getOriginAppLocationCode()));
							
						} else {
							this.getResultItem().setException(e);
						}
					}
					
					break;
				}
			}
			
			EtlDatabaseObject parent = DatabaseObjectDAO.getByUniqueKeys(dstParent, dstConn);
			
			if (parent != null) {
				getDstRecord().changeParentValue(parentInfo.getParentTableConfInDst(), parent);
			} else {
				getTaskProcessor().logWarn(
				    "The parent " + parentInfo.getParentRecordInOrigin() + " exists on db but not avaliable yet. dstRecord "
				            + this.getDstRecord() + ". The task will keep trying...");
				
				TimeCountDown.sleep(10);
			}
		}
	}
	
	protected void loadDstParentInfo(Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, MissingParentException, DBException {
		
		if (!utilities.arrayHasElement(getDstConf().getParentRefInfo())) {
			return;
		}
		
		for (ParentTable refInfo : getDstConf().getParentRefInfo()) {
			performeParentInfoInitialization(dstConn, refInfo);
			
			if (!getDstRecord().hasAllPerentFieldsFilled(refInfo)) {
				continue;
			}
			
			if (refInfo.isMetadata()) {
				tryToLoadMissingMetadataInfo(srcConn, dstConn, refInfo);
				
				continue;
			}
			
			tryToInitializeSharedParentInfo(dstConn, refInfo);
			
			if (!checkIfParentMustBeLoaded(refInfo)) {
				continue;
			}
			
			EtlDatabaseObject parentInOrigin = tryToLoadParentInOrigin(refInfo, srcConn);
			
			if (parentInOrigin == null) {
				continue;
			}
			
			EtlDatabaseObject sharedkeyParentInOrigin;
			
			try {
				sharedkeyParentInOrigin = tryToLoadSharedParentInOrigin(refInfo, parentInOrigin, srcConn);
			}
			catch (MissingParentException e) {
				//Inconsistence found
				
				continue;
			}
			
			EtlDatabaseObject parent = getParentInDestination(parentInOrigin, sharedkeyParentInOrigin, refInfo, dstConn);
			
			if (parent == null) {
				parent = refInfo.getDefaultObject(dstConn);
				
				if (parent == null) {
					parent = refInfo.generateAndSaveDefaultObject(dstConn);
				}
				this.getParentsWithDefaultValues().add(new ParentInfo(refInfo, parentInOrigin));
			}
			
			getDstRecord().changeParentValue(refInfo, parent);
		}
	}
	
	/**
	 * @param dstConn
	 * @param refInfo
	 * @param parentInOrigin
	 * @param sharedkeyParentInOrigin
	 * @param parent
	 * @return
	 * @throws DBException
	 */
	private EtlDatabaseObject getParentInDestination(EtlDatabaseObject parentInOrigin,
	        EtlDatabaseObject sharedkeyParentInOrigin, ParentTable refInfo, Connection dstConn) throws DBException {
		
		EtlDatabaseObject parent = null;
		
		if (getTaskProcessor().getRelatedEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
			parent = retrieveParentByOid(refInfo, parentInOrigin, dstConn);
		} else {
			
			//Retrieve parent using shared key parent unique key
			if (sharedkeyParentInOrigin != null) {
				EtlDatabaseObject recInDst = refInfo.getSharedKeyRefInfo().createRecordInstance();
				recInDst.setRelatedConfiguration(refInfo.getSharedKeyRefInfo());
				recInDst.copyFrom(sharedkeyParentInOrigin);
				recInDst.loadUniqueKeyValues(refInfo.getSharedKeyRefInfo());
				recInDst.loadObjectIdData(refInfo.getSharedKeyRefInfo());
				
				EtlDatabaseObject sharedPkParent = retrieveParentByUnikeKeys(recInDst, dstConn);
				
				if (sharedPkParent != null) {
					parent = sharedPkParent.getSharedKeyChildRelatedObject(refInfo, dstConn);
				}
				
			} else {
				EtlDatabaseObject recInDst = refInfo.createRecordInstance();
				recInDst.setRelatedConfiguration(refInfo);
				recInDst.copyFrom(parentInOrigin);
				recInDst.loadUniqueKeyValues(refInfo);
				recInDst.loadObjectIdData(refInfo);
				
				parent = retrieveParentByUnikeKeys(recInDst, dstConn);
			}
		}
		
		return parent;
	}
	
	private EtlDatabaseObject tryToLoadSharedParentInOrigin(ParentTable refInfo, EtlDatabaseObject parentInOrigin,
	        Connection srcConn) throws DBException, MissingParentException {
		
		EtlDatabaseObject sharedkeyParentInOrigin = null;
		
		//Try to load the shared key parent and generate inconsistence if is not exists
		if (refInfo.useSharedPKKey() && !refInfo.hasItsOwnKeys()) {
			sharedkeyParentInOrigin = parentInOrigin.getSharedKeyParentRelatedObject(srcConn);
			
			if (sharedkeyParentInOrigin == null && !refInfo.hasDefaultValueDueInconsistency()) {
				this.getResultItem().addInconsistence(
				    InconsistenceInfo.generate(getDstRecord().generateTableName(), getDstRecord().getObjectId(),
				        refInfo.getTableName(), refInfo.generateParentOidFromChild(getDstRecord()).getSimpleValue(),
				        refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
				
				throw new MissingParentException(null);
			}
		}
		
		return sharedkeyParentInOrigin;
	}
	
	private EtlDatabaseObject tryToLoadParentInOrigin(ParentTable refInfo, Connection srcConn) throws DBException {
		EtlDatabaseObject parentInOrigin = getDstRecord().getRelatedParentObject(refInfo, getSrcConf(), srcConn);
		
		if (parentInOrigin == null) {
			
			if (refInfo.hasDefaultValueDueInconsistency()) {
				
				Oid key = refInfo.generateParentOidFromChild(getDstRecord());
				
				if (refInfo.useSimplePk()) {
					key.asSimpleKey().setValue(refInfo.getDefaultValueDueInconsistency());
				} else
					throw new ForbiddenOperationException(
					        "There is a defaultValueDueInconsistency but the key is not simple on table "
					                + refInfo.getTableName());
				
				parentInOrigin = getDstRecord().getRelatedParentObjectOnSrc(refInfo, key, getSrcConf(), srcConn);
			}
			
			this.getResultItem().addInconsistence(
			    InconsistenceInfo.generate(getDstRecord().generateTableName(), getDstRecord().getObjectId(),
			        refInfo.getTableName(), refInfo.generateParentOidFromChild(getDstRecord()).getSimpleValue(),
			        refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
		}
		
		return parentInOrigin;
	}
	
	/**
	 * Check if this records fulfill the condition for a parent if it is conditional
	 * 
	 * @param refInfo
	 * @return true if this record fulfill the conditions for parent or if the parent is not
	 *         conditional
	 */
	private boolean checkIfParentMustBeLoaded(ParentTable refInfo) {
		if (refInfo.hasConditionalFields()) {
			if (refInfo.hasMoreThanOneConditionalFields()) {
				throw new ForbiddenOperationException("Currently not supported multiple conditional fields");
			}
			
			String conditionalFieldName = refInfo.getConditionalFields().get(0).getName();
			Object conditionalvalue = refInfo.getConditionalFields().get(0).getValue();
			
			if (!conditionalvalue.equals(getDstRecord().getFieldValue(conditionalFieldName).toString())) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
	
	/**
	 * @param dstConn
	 * @param refInfo
	 * @throws DBException
	 */
	private void tryToInitializeSharedParentInfo(Connection dstConn, ParentTable refInfo) throws DBException {
		if (refInfo.useSharedPKKey()) {
			if (!refInfo.getSharedKeyRefInfo().isFullLoaded()) {
				refInfo.getSharedKeyRefInfo().tryToGenerateTableAlias(this.getEtlConfiguration());
				
				refInfo.getSharedKeyRefInfo().fullLoad(dstConn);
			}
		}
	}
	
	/**
	 * @param srcConn
	 * @param dstConn
	 * @param refInfo
	 * @throws DBException
	 * @throws ForbiddenOperationException
	 */
	private void tryToLoadMissingMetadataInfo(Connection srcConn, Connection dstConn, ParentTable refInfo)
	        throws DBException, ForbiddenOperationException {
		EtlDatabaseObject parentInOrigin = getDstRecord().getRelatedParentObject(refInfo, getSrcConf(), srcConn);
		EtlDatabaseObject parent = null;
		
		if (parentInOrigin != null) {
			parent = retrieveParentByOid(refInfo, parentInOrigin, dstConn);
			
			if (parent == null) {
				getTaskProcessor().logWarn(
				    "Missing metadata " + parentInOrigin + ". This issue will be documented on inconsitence_info table");
				
				this.getResultItem().addInconsistence(
				    InconsistenceInfo.generate(getDstRecord().generateTableName(), getDstRecord().getObjectId(),
				        refInfo.getTableName(), refInfo.generateParentOidFromChild(getDstRecord()).getSimpleValue(),
				        refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
			}
		} else {
			getTaskProcessor().logWarn("Missing metadata " + refInfo.generateParentOidFromChild(getDstRecord())
			        + ". This issue will be documented on inconsitence_info table");
			
			this.getResultItem().addInconsistence(
			    InconsistenceInfo.generate(getDstRecord().generateTableName(), getDstRecord().getObjectId(),
			        refInfo.getTableName(), refInfo.generateParentOidFromChild(getDstRecord()).getSimpleValue(),
			        refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
		}
	}
	
	/**
	 * @param dstConn
	 * @param refInfo
	 * @throws DBException
	 */
	private void performeParentInfoInitialization(Connection dstConn, ParentTable refInfo) throws DBException {
		if (!refInfo.isFullLoaded()) {
			refInfo.tryToGenerateTableAlias(this.getEtlConfiguration());
			
			refInfo.fullLoad(dstConn);
		}
	}
	
	private EtlDatabaseObject retrieveParentByOid(ParentTable refInfo, EtlDatabaseObject parentInOrigin, Connection dstConn)
	        throws DBException {
		
		return DatabaseObjectDAO.getByOid(refInfo, parentInOrigin.getObjectId(), dstConn);
	}
	
	private EtlDatabaseObject retrieveParentByUnikeKeys(EtlDatabaseObject parent, Connection dstConn) throws DBException {
		return DatabaseObjectDAO.getByUniqueKeys(parent, dstConn);
	}
	
	/**
	 * @param loadRecord
	 * @param srcConn
	 * @param destConn
	 * @throws DBException
	 * @throws ParentNotYetMigratedException
	 * @throws SQLException
	 */
	public static void determineMissingMetadataParent(LoadRecord loadRecord, Connection srcConn, Connection destConn)
	        throws MissingParentException, DBException {
		TableConfiguration dstConf = loadRecord.getDstConf();
		
		EtlDatabaseObject dstRecord = loadRecord.getDstRecord();
		
		for (ParentTable refInfo : dstConf.getParentRefInfo()) {
			if (!refInfo.isMetadata())
				continue;
			
			Object oParentId = dstRecord.getParentValue(refInfo);
			
			if (oParentId != null) {
				Integer parentId = (Integer) oParentId;
				
				EtlDatabaseObject parent = DatabaseObjectDAO.getByOid(refInfo,
				    Oid.fastCreate(refInfo.getParentColumnOnSimpleMapping(), parentId), destConn);
				
				if (parent == null)
					throw new MissingParentException(dstRecord, parentId, refInfo.getTableName(),
					        loadRecord.getDstConf().getOriginAppLocationCode(), refInfo, null);
			}
		}
	}
	
	public boolean hasUnresolvedRecursiveRelationship(Connection srcConn, Connection dstConn) throws DBException {
		if (!utilities.arrayHasElement(getDstConf().getParentRefInfo())) {
			return false;
		}
		
		for (ParentTable refInfo : getDstConf().getParentRefInfo()) {
			
			if (refInfo.isMetadata()) {
				continue;
			}
			
			if (!refInfo.getTableName().equals(getDstConf().getTableName())) {
				continue;
			}
			
			performeParentInfoInitialization(dstConn, refInfo);
			
			if (!getDstRecord().hasAllPerentFieldsFilled(refInfo)) {
				continue;
			}
			
			Oid key = refInfo.generateParentOidFromChild(getDstRecord());
			
			TableConfiguration tabConfInSrc = this.getSrcConf().findFullConfiguredConfInAllRelatedTable(
			    refInfo.generateFullTableNameOnSchema(getSrcConf().getSchema()));
			
			if (tabConfInSrc == null) {
				tabConfInSrc = new GenericTableConfiguration(this.getSrcConf());
				tabConfInSrc.setTableName(refInfo.getTableName());
				tabConfInSrc.setRelatedEtlConfig(this.getEtlConfiguration());
				tabConfInSrc.fullLoad(srcConn);
			}
			
			EtlDatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(tabConfInSrc, key, srcConn);
			
			if (parentInOrigin == null) {
				continue;
			}
			
			EtlDatabaseObject parent;
			
			if (getTaskProcessor().getRelatedEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
				parent = retrieveParentByOid(refInfo, parentInOrigin, dstConn);
			} else {
				EtlDatabaseObject recInDst = refInfo.createRecordInstance();
				recInDst.setRelatedConfiguration(refInfo);
				recInDst.copyFrom(parentInOrigin);
				recInDst.loadUniqueKeyValues(refInfo);
				recInDst.loadObjectIdData(refInfo);
				
				parent = retrieveParentByUnikeKeys(recInDst, dstConn);
			}
			
			if (parent == null) {
				return true;
			}
			
		}
		
		return false;
		
	}
	
	public EtlDatabaseObject parseToEtlObject() {
		return this.getDstRecord();
	}
	
	public static List<EtlDatabaseObject> parseToEtlObject(List<LoadRecord> recs) {
		List<EtlDatabaseObject> parse = new ArrayList<>(recs.size());
		
		if (recs != null) {
			for (LoadRecord load : recs) {
				parse.add(load.parseToEtlObject());
			}
		}
		return parse;
	}
	
	@Override
	public String toString() {
		return "Etl from [" + getSrcRecord() + "] to " + getDstRecord();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LoadRecord)) {
			return false;
		}
		
		LoadRecord other = (LoadRecord) obj;
		
		return this.getSrcRecord().equals(other.getSrcRecord()) && this.getDstConf().equals(other.getDstConf());
	}
	
}
