package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.datasource.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.datasource.DataSourceField;
import org.openmrs.module.epts.etl.conf.datasource.DefaultObjectFieldsValuesGenerator;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.types.ObjectLanguageType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class ObjectDataSource implements EtlAdditionalDataSource {
	
	private String name;
	
	private ObjectLanguageType objectLanguage;
	
	private List<DataSourceField> objectFields;
	
	private boolean fullLoaded;
	
	private SrcConf relatedSrcConf;
	
	private boolean required;
	
	private String fieldsValuesGenerator;
	
	private JavaObjectFieldsValuesGenerator fieldsValuesGeneratorInstance;
	
	public EtlConfiguration getRelatedSyncConfiguration() {
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
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public List<AuxExtractTable> getAuxExtractTable() {
		throw new ForbiddenOperationException("Forbiden Method");
	}
	
	@Override
	public void setAuxExtractTable(List<AuxExtractTable> auxExtractTable) {
		throw new ForbiddenOperationException("Forbiden Method");
	}
	
	@Override
	public boolean isFullLoaded() {
		return this.fullLoaded;
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
	
	public boolean hasObjectFields() {
		return utilities.arrayHasElement(this.getObjectFields());
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) throws DBException {
		if (isFullLoaded()) {
			return;
		}
		
		this.tryToLoadFieldValueGenerator();
		
		if (hasObjectFields()) {
			for (DataSourceField f : this.getObjectFields()) {
				f.setDataSource(this);
				f.tryToLoadTransformer();
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
	public boolean hasPK() {
		return false;
	}
	
	@Override
	public boolean hasPK(Connection conn) throws DBException {
		return false;
	}
	
	@Override
	public boolean isMetadata() {
		return false;
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
	public boolean isDestinationInstallationType() {
		return false;
	}
	
	@Override
	public void generateRecordClass(DBConnectionInfo connInfo, boolean fullClass) {
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
	public boolean isMustLoadChildrenInfo() {
		return false;
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
	public EtlDatabaseObject loadRelatedSrcObject(List<EtlDatabaseObject> avaliableSrcObjects, Connection conn)
	        throws DBException {
		
		Map<String, Object> values = this.getFieldsValuesGeneratorInstance().generateObjectFields(this, avaliableSrcObjects,
		    conn, conn);
		
		EtlDatabaseObject obj = this.newInstance();
		
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			
			obj.setFieldValue(key, value);
		}
		
		return obj;
	}
	
	@Override
	public boolean isRequired() {
		return required;
	}
	
	@Override
	public boolean allowMultipleSrcObjects() {
		return true;
	}
	
	@Override
	public String getQuery() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean hasFieldsValuesGenerator() {
		return utilities.stringHasValue(fieldsValuesGenerator);
	}
	
	@SuppressWarnings("unchecked")
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
	
}
