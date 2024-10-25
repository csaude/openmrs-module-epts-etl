package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.dbquickmerge.model.ParentInfo;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.TransformationType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.model.pojo.generic.RecordWithDefaultParentInfo;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
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
	
	private LoadRecord parentLoadRecord;
	
	public LoadRecord(EtlDatabaseObject srcRecord, EtlDatabaseObject dstRecord, SrcConf srcConf, DstConf dstConf,
	    EtlProcessor engine) {
		
		this.srcRecord = srcRecord;
		this.dstRecord = dstRecord;
		
		this.srcConf = srcConf;
		this.dstConf = dstConf;
		
		this.processor = engine;
		
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
		
		this.dstRecord.loadUniqueKeyValues();
		
		this.resultItem = new EtlOperationItemResult<EtlDatabaseObject>(srcRecord);
		
		this.status = LoadStatus.UNDEFINED;
	}
	
	public LoadRecord getParentLoadRecord() {
		return parentLoadRecord;
	}
	
	public void setParentLoadRecord(LoadRecord parentRecord) {
		this.parentLoadRecord = parentRecord;
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
	
	public void markAsFailed() {
		this.setStatus(LoadStatus.FAIL);
	}
	
	public void markAsSuccess() {
		this.setStatus(LoadStatus.SUCCESS);
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
			
			List<EtlDatabaseObject> recursive = retriveRecursiveRelationship(parentInfo.getParentTableConfInDst(), srcConn,
			    dstConn);
			
			if (utilities.arrayHasElement(recursive)) {
				getProcessor().logDebug(
				    "Recursive relationship found reloading parents for record " + this.getDstRecord() + " with parent ");
				
				DatabaseObjectDAO.insert(recursive, srcConn);
			} else {
				List<SrcConf> avaliableSrcForCurrParent = parentInfo.getParentTableConfInDst()
				        .findRelatedSrcConfWhichAsAtLeastOnematchingDst(getEtlOperationConfig());
				
				if (utilities.arrayHasNoElement(avaliableSrcForCurrParent)) {
					throw new ForbiddenOperationException(
					        "There are relashioship which cannot auto resolved as there is no configured etl for "
					                + parentInfo.getParentTableConfInDst().getTableName() + " as source and destination!");
				}
				
				EtlDatabaseObject dstParent = null;
				EtlDatabaseObject recordAsSrc = null;
				
				for (SrcConf src : avaliableSrcForCurrParent) {
					DstConf dst = ((EtlItemConfiguration) src.getParentConf()).findDstTable(getEtlOperationConfig(),
					    parentInfo.getParentTableConfInDst().getTableName());
					
					recordAsSrc = src.createRecordInstance();
					recordAsSrc.setRelatedConfiguration(src);
					
					recordAsSrc.copyFrom(parentInfo.getParentRecordInOrigin());
					
					dstParent = dst.getTransformerInstance().transform(this.getProcessor(), recordAsSrc, dst,
					    TransformationType.INNER, srcConn, dstConn);
					
					if (dstParent != null) {
						
						LoadRecord parentData = new LoadRecord(recordAsSrc, dstParent, src, dst, getTaskProcessor());
						
						parentData.setParentLoadRecord(this);
						
						DBException exception = null;
						
						try {
							parentData.setParentLoadRecord(this);
							
							EtlLoadHelper.performeParentLoading(parentData, srcConn, dstConn);
						}
						catch (DBException e) {
							exception = e;
							
							if (!exception.isIntegrityConstraintViolationException()) {
								this.getResultItem().setException(exception);
							}
						}
						finally {
							
							if (parentData.getResultItem().hasInconsistences()
							        || exception != null && exception.isIntegrityConstraintViolationException()) {
								
								getProcessor().logDebug("The parent for default for parent ["
								        + parentInfo.getParentRecordInOrigin() + "] could not be loaded. The dstRecord [");
								
								this.getResultItem()
								        .addInconsistence(InconsistenceInfo.generate(getDstRecord().generateTableName(),
								            getDstRecord().getObjectId(),
								            parentInfo.getParentTableConfInDst().getTableName(),
								            parentInfo.getParentRecordInOrigin().getObjectId().getSimpleValueAsInt(), null,
								            this.getDstConf().getOriginAppLocationCode()));
								
							}
						}
						
						break;
					}
					
				}
				
				try {
					getDstRecord().changeParentValue(parentInfo.getParentTableConfInDst(), dstParent);
				}
				catch (NullPointerException e) {
					e.printStackTrace();
					
					throw e;
				}
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
				tryToLoadMissingMetadataInfo(refInfo, srcConn, dstConn);
				
				continue;
			}
			
			if (!checkIfParentMustBeLoaded(refInfo)) {
				continue;
			}
			
			EtlDatabaseObject parentInSrc = this.getDstRecord().retrieveParentInSrcUsingDstParentInfo(refInfo,
			    this.getSrcConf(), dstConn);
			
			EtlDatabaseObject parentInDst = null;
			
			if (parentInSrc != null) {
				parentInDst = this.getDstRecord().retrieveParentInDestination(refInfo, parentInSrc, dstConn);
			} else {
				
				try {
					if (refInfo.hasDefaultValueDueInconsistency()) {
						
						Oid key = refInfo.generateParentOidFromChild(getDstRecord());
						
						if (refInfo.useSimplePk()) {
							key.asSimpleKey().setValue(refInfo.getDefaultValueDueInconsistency());
						} else
							throw new ForbiddenOperationException(
							        "There is a defaultValueDueInconsistency but the key is not simple on table "
							                + refInfo.getTableName());
						
						parentInDst = getDstRecord().retrieveParentByOid(refInfo, dstConn);
					} else {
						continue;
					}
				}
				finally {
					this.getResultItem()
					        .addInconsistence(InconsistenceInfo.generate(getDstRecord().generateTableName(),
					            getDstRecord().getObjectId(), refInfo.getTableName(),
					            refInfo.generateParentOidFromChild(getDstRecord()).getSimpleValue(),
					            refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
				}
			}
			
			if (parentInDst == null) {
				parentInDst = refInfo.getDefaultObject(dstConn);
				
				if (parentInDst == null) {
					parentInDst = refInfo.generateAndSaveDefaultObject(dstConn);
				}
				
				//The parentInSrc will be null if it does not exists and were used default parent
				if (parentInSrc != null) {
					this.getParentsWithDefaultValues().add(new ParentInfo(refInfo, parentInSrc));
				}
			}
			
			getDstRecord().changeParentValue(refInfo, parentInDst);
		}
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
	 * @param srcConn
	 * @param dstConn
	 * @param refInfo
	 * @throws DBException
	 * @throws ForbiddenOperationException
	 */
	private void tryToLoadMissingMetadataInfo(ParentTable refInfo, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		EtlDatabaseObject parentInOrigin = getDstRecord().retrieveParentInSrcUsingDstParentInfo(refInfo, getSrcConf(),
		    srcConn);
		
		EtlDatabaseObject parent = null;
		
		if (parentInOrigin != null) {
			parent = this.getDstRecord().retrieveParentByOid(refInfo, dstConn);
			
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
		
		if (refInfo.useSharedPKKey()) {
			TableConfiguration sharedPkConf = refInfo.getSharedKeyRefInfo();
			
			if (!sharedPkConf.isFullLoaded()) {
				sharedPkConf.tryToGenerateTableAlias(this.getEtlConfiguration());
				
				sharedPkConf.fullLoad(dstConn);
			}
		}
		
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
	
	public List<EtlDatabaseObject> retriveRecursiveRelationship(ParentTable refInfo, Connection srcConn, Connection dstConn)
	        throws DBException {
		if (!utilities.arrayHasElement(getDstConf().getParentRefInfo())) {
			return null;
		}
		
		List<EtlDatabaseObject> recursiveRelationship = new ArrayList<>();
		
		if (refInfo.isMetadata()) {
			return null;
		}
		
		performeParentInfoInitialization(dstConn, refInfo);
		
		if (!getDstRecord().hasAllPerentFieldsFilled(refInfo)) {
			return null;
		}
		
		EtlDatabaseObject parentInOrigin = this.getDstRecord().retrieveParentInSrcUsingDstParentInfo(refInfo, srcConf,
		    srcConn);
		
		if (parentInOrigin == null) {
			return null;
		}
		
		EtlDatabaseObject parent;
		
		if (getTaskProcessor().getRelatedEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
			parent = this.getDstRecord().retrieveParentByOid(refInfo, dstConn);
		} else {
			EtlDatabaseObject recInDst = refInfo.createRecordInstance();
			recInDst.setRelatedConfiguration(refInfo);
			recInDst.copyFrom(parentInOrigin);
			recInDst.loadUniqueKeyValues(refInfo);
			recInDst.loadObjectIdData(refInfo);
			
			parent = this.getDstRecord().retrieveParentInDestination(refInfo, parentInOrigin, dstConn);
		}
		
		if (parent == null) {
			if (!refInfo.isFullLoaded()) {
				refInfo.fullLoad(dstConn);
			}
			
			//If the relationship is self recursive
			if (refInfo.getTableName().equals(this.getDstConf().getTableName())) {
				recursiveRelationship.add(RecordWithDefaultParentInfo.init(this.getSrcRecord(), this.getDstRecord(),
				    parentInOrigin, refInfo, srcConn));
			}
			
			/*
			 * Check if there are recursive relationship between the child (this record) and its parent
			 * 
			 * We loop over all the parents of 'refInfo' which is parent of current record
			 * 
			 */
			for (ParentTable p : refInfo.getParentRefInfo()) {
				
				//Mean that the parent 'p' is the very same as the table of this record  
				if (p.getTableName().equals(this.getDstConf().getTableName())) {
					Object parentValue = parentInOrigin.getParentValue(p);
					
					if (parentValue != null) {
						recursiveRelationship.add(RecordWithDefaultParentInfo.init(this.getSrcRecord(), this.getDstRecord(),
						    parentInOrigin, refInfo, srcConn));
						
						break;
					}
					
					/*this.getSrcRecord().loadObjectIdData();
					
					if (parentValue != null && parentValue.equals(this.getSrcRecord().getObjectId().asSimpleValue())) {
						recursiveRelationship.add(RecordWithDefaultParentInfo.init(this.getSrcRecord(), this.getDstRecord(),
						    parentInOrigin, refInfo, srcConn));
						
						break;
					}*/
				}
			}
		}
		
		return recursiveRelationship;
	}
	
	public List<EtlDatabaseObject> retriveRecursiveRelationship(Connection srcConn, Connection dstConn) throws DBException {
		if (!utilities.arrayHasElement(getDstConf().getParentRefInfo())) {
			return null;
		}
		
		List<EtlDatabaseObject> recursiveRelationship = new ArrayList<>();
		
		for (ParentTable refInfo : getDstConf().getParentRefInfo()) {
			List<EtlDatabaseObject> recursiveInfo = retriveRecursiveRelationship(refInfo, srcConn, dstConn);
			
			if (utilities.arrayHasElement(recursiveInfo)) {
				recursiveRelationship.addAll(recursiveInfo);
			}
		}
		
		return recursiveRelationship;
	}
	
	public void saveRecordsWithDefaultsParents(Connection srcConn, Connection dstConn) throws DBException {
		for (ParentInfo parentInfo : this.getParentsWithDefaultValues()) {
			RecordWithDefaultParentInfo defaultParentInfo = RecordWithDefaultParentInfo.init(this.getSrcRecord(),
			    this.getDstRecord(), parentInfo.getParentRecordInOrigin(), parentInfo.getParentTableConfInDst(), srcConn);
			
			getProcessor().logDebug(
			    "Recursive relationship found reloading parents for record " + this.getDstRecord() + " with parent ");
			
			defaultParentInfo.save((TableConfiguration) defaultParentInfo.getRelatedConfiguration(),
			    ConflictResolutionType.KEEP_EXISTING, srcConn);
		}
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
	
	public boolean hasParentLoadRecord() {
		return this.getParentLoadRecord() != null;
	}
	
}
