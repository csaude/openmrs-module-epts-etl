package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.AbstractEtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlTemplateInfo;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlSrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlTranformTarget;
import org.openmrs.module.epts.etl.conf.interfaces.JavaObjectFieldsValuesGenerator;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ObjectLanguageType;
import org.openmrs.module.epts.etl.conf.types.OnMultipleDataSourceFoundBehavior;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.FieldTransformingInfo;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class ObjectDataSource extends AbstractEtlDataConfiguration implements EtlAdditionalDataSource, EtlSrcConf, EtlTranformTarget {
	
	private String name;
	
	private ObjectLanguageType objectLanguage;
	
	private List<DataSourceField> objectFields;
	
	private Boolean fullLoaded;
	
	private SrcConf relatedSrcConf;
	
	private Boolean required;
	
	private String fieldsValuesGenerator;
	
	private JavaObjectFieldsValuesGenerator fieldsValuesGeneratorInstance;
	
	private EtlTemplateInfo template;
	
	private List<String> dynamicElements;
	
	private List<String> prefferredDataSource;
	
	private List<EtlDataSource> allAvaliableDataSource;
	
	private List<EtlDataSource> allNotPrefferredDataSource;
	
	private List<EtlDataSource> allPrefferredDataSource;
	
	private List<FieldsMapping> allMapping;
	
	private Boolean ignoreUnmappedFields;
	
	private OnMultipleDataSourceFoundBehavior onMultipleDataSourceForSameMapping;
	
	@Override
	public List<String> getDynamicElements() {
		return dynamicElements;
	}
	
	public void setDynamicElements(List<String> dynamicElements) {
		this.dynamicElements = dynamicElements;
	}
	
	@Override
	public EtlTemplateInfo getTemplate() {
		return template;
	}
	
	@Override
	public void setTemplate(EtlTemplateInfo template) {
		this.template = template;
	}
	
	@Override
	public EtlConfiguration getRelatedEtlConf() {
		return this.relatedSrcConf.getRelatedEtlConf();
	}
	
	public String getFieldsValuesGenerator() {
		return fieldsValuesGenerator;
	}
	
	public void setFieldsValuesGenerator(String fieldsValuesGenerator) {
		this.fieldsValuesGenerator = fieldsValuesGenerator;
	}
	
	public JavaObjectFieldsValuesGenerator getFieldsValuesGeneratorInstance() {
		return fieldsValuesGeneratorInstance;
	}
	
	public void setFieldsValuesGeneratorInstance(JavaObjectFieldsValuesGenerator fieldsValuesGeneratorInstance) {
		this.fieldsValuesGeneratorInstance = fieldsValuesGeneratorInstance;
	}
	
	public ObjectLanguageType getObjectLanguage() {
		return objectLanguage;
	}
	
	public void setObjectLanguage(ObjectLanguageType objectLanguage) {
		this.objectLanguage = objectLanguage;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setRequired(Boolean required) {
		this.required = required;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Boolean isFullLoaded() {
		return isTrue(this.fullLoaded);
	}
	
	@Override
	public synchronized void fullLoad() throws DBException {
		OpenConnection mainConn = getRelatedEtlConf().getSrcConnInfo().openConnection();
		
		OpenConnection dstConn = null;
		
		try {
			fullLoad(mainConn);
		}
		finally {
			mainConn.finalizeConnection();
			
			if (dstConn != null) {
				dstConn.finalizeConnection();
			}
		}
	}
	
	public Boolean hasObjectFields() {
		return utilities.listHasElement(this.getObjectFields());
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) throws DBException {
		if (isFullLoaded()) {
			return;
		}
		
		this.tryToLoadFieldValueGenerator();
		
		if (hasObjectFields()) {
			for (DataSourceField field : this.getObjectFields()) {
				field.setParent(this);
				
				field.loadType(null, this, conn);
				
				FieldsMapping auxFieldMapping = FieldsMapping.fastCreate(field, conn);
				
				if (auxFieldMapping.hasDataSourceName()) {
					field.setValue(null);
					field.setSrcField(null);
					
					field.setAuxFieldMapping(auxFieldMapping);
				}
				
				field.tryToLoadTransformer(null, conn);
				field.loadType(null, this, conn);
			}
		} else {
			throw new ForbiddenOperationException(
			        "You must specify the 'objectFields' on extraObjectDataSource configuration (" + this.getName() + ")");
		}
	}
	
	@Override
	public String generateClassName() {
		throw new ForbiddenOperationException("Forbiden Method");
	}
	
	@Override
	public EtlDataConfiguration getParentConf() {
		return this.relatedSrcConf;
	}
	
	@Override
	public String getObjectName() {
		return this.name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<? extends Field> getFields() {
		return this.objectFields;
	}
	
	public List<DataSourceField> getObjectFields() {
		return this.objectFields;
	}
	
	public void setObjectFields(List<DataSourceField> objectFields) {
		this.objectFields = objectFields;
	}
	
	@Override
	public UniqueKeyInfo getPrimaryKey() {
		return null;
	}
	
	@Override
	public String getSharePkWith() {
		return null;
	}
	
	@Override
	public Boolean hasPK() {
		return false_();
	}
	
	@Override
	public Boolean hasPK(Connection conn) throws DBException {
		return false_();
	}
	
	@Override
	public Boolean isMetadata() {
		return false_();
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return this.relatedSrcConf.getRelatedConnInfo();
	}
	
	@Override
	public void setSyncRecordClass(Class<? extends EtlDatabaseObject> syncRecordClass) {
		throw new ForbiddenOperationException("Forbiden Method");
	}
	
	@Override
	public Boolean isDestinationInstallationType() {
		return false_();
	}
	
	@Override
	public void generateRecordClass(DBConnectionInfo connInfo, Boolean fullClass) {
	}
	
	@Override
	public List<ParentTable> getParentRefInfo() {
		return null;
	}
	
	@Override
	public List<ChildTable> getChildRefInfo() {
		return null;
	}
	
	@Override
	public DatabaseObjectLoaderHelper getLoadHealper() {
		return null;
	}
	
	@Override
	public Boolean isMustLoadChildrenInfo() {
		return false_();
	}
	
	@Override
	public String getAlias() {
		return this.getName();
	}
	
	@Override
	public String generateSelectFromQuery() {
		return null;
	}
	
	@Override
	public void setRelatedEtlConfig(EtlConfiguration relatedSyncConfiguration) {
	}
	
	@Override
	public List<Extension> getExtension() {
		return null;
	}
	
	@Override
	public void setExtension(List<Extension> extension) {
	}
	
	@Override
	public SrcConf getRelatedSrcConf() {
		return this.relatedSrcConf;
	}
	
	@Override
	public void setRelatedSrcConf(SrcConf relatedSrcConf) {
		this.relatedSrcConf = relatedSrcConf;
	}
	
	@Override
	public EtlDatabaseObject loadRelatedSrcObject(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject dstObject, List<EtlDatabaseObject> avaliableSrcObjects, Connection conn) throws DBException {
		
		Map<String, FieldTransformingInfo> values = this.getFieldsValuesGeneratorInstance().generateObjectFields(processor,
		    srcObject, dstObject, this, avaliableSrcObjects, conn, conn);
		
		EtlDatabaseObject obj = this.newInstance();
		
		for (DataSourceField field : this.getObjectFields()) {
			FieldTransformingInfo valueInfo = values.get(field.getName());
			
			obj.setFieldValue(field.getName(), valueInfo.getTransformedValue());
			
			obj.getField(field.getName()).setTransformingInfo(valueInfo);
			valueInfo.setTransformationDatasource(this);
		}
		
		return obj;
	}
	
	@Override
	public Class<? extends EtlDatabaseObject> getSyncRecordClass() throws ForbiddenOperationException {
		return GenericDatabaseObject.class;
	}
	
	@Override
	public Boolean isRequired() {
		return isTrue(required);
	}
	
	@Override
	public Boolean allowMultipleSrcObjectsForLoading() {
		return true_();
	}
	
	@Override
	public String getQuery() {
		return null;
	}
	
	public Boolean hasFieldsValuesGenerator() {
		return utilities.stringHasValue(fieldsValuesGenerator);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void tryToLoadFieldValueGenerator() {
		if (this.hasFieldsValuesGenerator()) {
			
			try {
				ClassLoader loader = JavaObjectFieldsValuesGenerator.class.getClassLoader();
				
				Class<? extends JavaObjectFieldsValuesGenerator> transformerClazz = (Class<? extends JavaObjectFieldsValuesGenerator>) loader
				        .loadClass(this.getFieldsValuesGenerator());
				
				this.setFieldsValuesGeneratorInstance(transformerClazz.newInstance());
			}
			catch (Exception e) {
				throw new ForbiddenOperationException("Error loading fields generator class ["
				        + this.getFieldsValuesGenerator() + "]!!! " + e.getLocalizedMessage());
			}
		} else {
			this.setFieldsValuesGenerator((DefaultObjectFieldsValuesGenerator.class.getCanonicalName()));
			
			this.setFieldsValuesGeneratorInstance(DefaultObjectFieldsValuesGenerator.getInstance());
		}
	}
	
	@Override
	public TableConfiguration findFullConfiguredConfInAllRelatedTable(String fullTableName, List<Integer> a) {
		return null;
	}
	
	public static List<ObjectDataSource> cloneAll(List<ObjectDataSource> allToCloneFrom, SrcConf relatedSrcConf,
	        Connection conn) throws DBException {
		
		List<ObjectDataSource> allCloned = null;
		
		if (utilities.listHasElement(allToCloneFrom)) {
			allCloned = new ArrayList<>(allToCloneFrom.size());
			
			for (ObjectDataSource aux : allToCloneFrom) {
				ObjectDataSource cloned = new ObjectDataSource();
				cloned.clone(aux, relatedSrcConf, conn);
				
				allCloned.add(cloned);
			}
		}
		
		return allCloned;
	}
	
	public void clone(ObjectDataSource toCloneFrom, SrcConf relatedSrcConf, Connection conn) throws DBException {
		this.setName(toCloneFrom.getName());
		this.setRelatedSrcConf(relatedSrcConf);
		this.setRequired(toCloneFrom.isRequired());
		
		this.setObjectLanguage(toCloneFrom.getObjectLanguage());
		
		this.setObjectFields(DataSourceField.cloneAll(toCloneFrom.getObjectFields(), this));
		this.setRelatedSrcConf(relatedSrcConf);
		this.setRequired(toCloneFrom.isRequired());
		this.setFieldsValuesGenerator(toCloneFrom.getFieldsValuesGenerator());
		this.setFieldsValuesGeneratorInstance(toCloneFrom.getFieldsValuesGeneratorInstance());
	}
	
	public static void tryToReplacePlaceholders(List<ObjectDataSource> extraObjectDataSource,
	        EtlDatabaseObject schemaInfoSrc) {
		if (utilities.listHasElement(extraObjectDataSource)) {
			for (ObjectDataSource a : extraObjectDataSource) {
				a.tryToReplacePlaceholders(schemaInfoSrc);
			}
		}
		
	}
	
	@Override
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
		DataSourceField.tryToReplacePlaceholders(this.getObjectFields(), schemaInfoSrc);
	}
	
	@Override
	public ActionOnEtlException getGeneralBehaviourOnEtlException() {
		return this.getRelatedEtlConf().getGeneralBehaviourOnEtlException();
	}
	
	@Override
	public PreparedQuery getDefaultPreparedQuery() {
		throw new ForbiddenOperationException("Forbiden Method");
	}
	
	@Override
	public void setDefaultPreparedQuery(PreparedQuery defaultPreparedQuery) {
		throw new ForbiddenOperationException("Forbiden Method");
	}
	
	@Override
	public String toString() {
		String fields = this.objectFields != null ? this.objectFields.toString() : "";
		
		fields = utilities.stringHasValue(fields) ? "(" + fields + ")" : "";
		
		return this.getName() + fields;
	}
	
	@Override
	public void tryToLoadSchemaInfo(EtlDatabaseObject schemaInfoSrc, Connection conn)
	        throws DBException, ForbiddenOperationException, DatabaseResourceDoesNotExists {
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration relatedParent) {
		this.relatedSrcConf = (SrcConf) relatedParent;
	}
	
	@Override
	public Boolean isIgnoreUnmappedFields() {
		return ignoreUnmappedFields;
	}
	
	@Override
	public SrcConf getSrcConf() {
		return this.relatedSrcConf;
	}
	
	public Boolean getFullLoaded() {
		return fullLoaded;
	}
	
	public void setFullLoaded(Boolean fullLoaded) {
		this.fullLoaded = fullLoaded;
	}
	
	public List<String> getPrefferredDataSource() {
		return prefferredDataSource;
	}
	
	public void setPrefferredDataSource(List<String> prefferredDataSource) {
		this.prefferredDataSource = prefferredDataSource;
	}
	
	@Override
	public List<EtlDataSource> getAllAvaliableDataSource() {
		return allAvaliableDataSource;
	}
	
	@Override
	public void setAllAvaliableDataSource(List<EtlDataSource> allAvaliableDataSource) {
		this.allAvaliableDataSource = allAvaliableDataSource;
	}
	
	@Override
	public List<EtlDataSource> getAllNotPrefferredDataSource() {
		return allNotPrefferredDataSource;
	}
	
	@Override
	public void setAllNotPrefferredDataSource(List<EtlDataSource> allNotPrefferredDataSource) {
		this.allNotPrefferredDataSource = allNotPrefferredDataSource;
	}
	
	@Override
	public List<EtlDataSource> getAllPrefferredDataSource() {
		return allPrefferredDataSource;
	}
	
	@Override
	public void setAllPrefferredDataSource(List<EtlDataSource> allPrefferredDataSource) {
		this.allPrefferredDataSource = allPrefferredDataSource;
	}
	
	@Override
	public List<FieldsMapping> getAllMapping() {
		return allMapping;
	}
	
	@Override
	public void setAllMapping(List<FieldsMapping> allMapping) {
		this.allMapping = allMapping;
	}
	
	public Boolean getIgnoreUnmappedFields() {
		return ignoreUnmappedFields;
	}
	
	public void setIgnoreUnmappedFields(Boolean ignoreUnmappedFields) {
		this.ignoreUnmappedFields = ignoreUnmappedFields;
	}
	
	public OnMultipleDataSourceFoundBehavior getOnMultipleDataSourceForSameMapping() {
		return onMultipleDataSourceForSameMapping;
	}
	
	public void setOnMultipleDataSourceForSameMapping(OnMultipleDataSourceFoundBehavior onMultipleDataSourceForSameMapping) {
		this.onMultipleDataSourceForSameMapping = onMultipleDataSourceForSameMapping;
	}
	
	public Boolean getRequired() {
		return required;
	}
	
	@Override
	public void loadDataSourceInfo(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Boolean isLoadedDataSourceInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public OnMultipleDataSourceFoundBehavior onMultipleDataSourceForSameMapping() {
		return this.onMultipleDataSourceForSameMapping;
	}
	
	@Override
	public void setMapping(List<FieldsMapping> mapping) {
	}
	
}
