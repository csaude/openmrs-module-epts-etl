package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.datasource.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class JavaObjectDataSource implements EtlAdditionalDataSource {
	
	private String name;
	
	private List<Field> fields;
	
	private boolean fullLoaded;
	
	private SrcConf relatedSrcConf;
	
	private boolean required;
	
	private EtlConfiguration relatedSyncConfiguration;
	
	private String javaObjectDataSourceGeneratorClazz;
	
	private JavaObjectDataSourceGenerator fieldGeneratorInstance;
	
	public EtlConfiguration getRelatedSyncConfiguration() {
		return relatedSyncConfiguration;
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
	public void fullLoad() throws DBException {
		throw new ForbiddenOperationException("Forbiden Method");
	}
	
	@Override
	public void fullLoad(Connection conn) throws DBException {
		throw new ForbiddenOperationException("Forbiden Method");
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
	
	@Override
	public List<Field> getFields() {
		return this.fields;
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
		this.relatedSyncConfiguration = relatedSyncConfiguration;
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
		return null;
	}
	
	@Override
	public void setRelatedSrcConf(SrcConf relatedSrcConf) {
	}
	
	@Override
	public EtlDatabaseObject loadRelatedSrcObject(List<EtlDatabaseObject> avaliableSrcObjects, Connection conn)
	        throws DBException {
		
		Map<String, Object> values = get
		
		
		return null;
	}
	
	@Override
	public boolean isRequired() {
		return required;
	}
	
	@Override
	public boolean allowMultipleSrcObjects() {
		return true;
	}
	
}
