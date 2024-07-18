package org.openmrs.module.epts.etl.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a query configuration. A query is used on data mapping between source and destination
 * table
 */
public class QueryDataSourceConfig extends AbstractBaseConfiguration implements DatabaseObjectConfiguration, EtlAdditionalDataSource {
	
	private String name;
	
	private String query;
	
	private String script;
	
	private List<Field> fields;
	
	private boolean fullLoaded;
	
	private SrcConf relatedSrcConf;
	
	private Class<? extends EtlDatabaseObject> syncRecordClass;
	
	private boolean required;
	
	private DatabaseObjectLoaderHelper loadHealper;
	
	public QueryDataSourceConfig() {
		this.loadHealper = new DatabaseObjectLoaderHelper(this);
	}
	
	@Override
	public DatabaseObjectLoaderHelper getLoadHealper() {
		return this.loadHealper;
	}
	
	public void setLoadHealper(DatabaseObjectLoaderHelper loadHealper) {
		this.loadHealper = loadHealper;
	}
	
	@Override
	public boolean isRequired() {
		return this.required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	@Override
	public SrcConf getRelatedSrcConf() {
		return relatedSrcConf;
	}
	
	@Override
	public void setRelatedSrcConf(SrcConf relatedSrcConf) {
		this.relatedSrcConf = relatedSrcConf;
	}
	
	public String getScript() {
		return script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
	
	public List<Field> getFields() {
		return fields;
	}
	
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getQuery() {
		if (!utilities.stringHasValue(query) && utilities.stringHasValue(this.script)) {
			loadQueryFromFile();
		}
		
		if (!utilities.stringHasValue(query)) {
			throw new ForbiddenOperationException("No query was defined!");
		}
		
		return utilities.removeNewline(query);
	}
	
	private void loadQueryFromFile() {
		String pathToScript = getRelatedEtlConf().getSqlScriptsDirectory().getAbsolutePath() + File.separator + this.script;
		
		try {
			this.query = new String(Files.readAllBytes(Paths.get(pathToScript)));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public boolean isFullLoaded() {
		return fullLoaded;
	}
	
	@Override
	public void fullLoad() throws DBException {
		OpenConnection conn = this.relatedSrcConf.getRelatedConnInfo().openConnection();
		
		try {
			fullLoad(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) throws DBException {
		PreparedQuery query = PreparedQuery.prepare(this.getQuery(), getRelatedEtlConf(), true);
		
		setFields(DBUtilities.determineFieldsFromQuery(query.generatePreparedQuery(), conn));
		
		this.fullLoaded = true;
	}
	
	@JsonIgnore
	@Override
	public Class<? extends EtlDatabaseObject> getSyncRecordClass() throws ForbiddenOperationException {
		return this.getSyncRecordClass(this.relatedSrcConf.getRelatedConnInfo());
	}
	
	@Override
	public Class<? extends EtlDatabaseObject> getSyncRecordClass(DBConnectionInfo connInfo)
	        throws ForbiddenOperationException {
		if (syncRecordClass == null)
			syncRecordClass = GenericDatabaseObject.class;
		
		return syncRecordClass;
	}
	
	public EtlConfiguration getRelatedEtlConf() {
		return this.relatedSrcConf.getRelatedEtlConf();
	}
	
	@JsonIgnore
	public boolean existsSyncRecordClass(DBConnectionInfo connInfo) {
		try {
			return getSyncRecordClass(connInfo) != null;
		}
		catch (ForbiddenOperationException e) {
			
			return false;
		}
	}
	
	public void setSyncRecordClass(Class<? extends EtlDatabaseObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}
	
	@JsonIgnore
	@Override
	public String getClasspackage(DBConnectionInfo connInfo) {
		return connInfo.getPojoPackageName() + "._query_result";
	}
	
	@JsonIgnore
	@Override
	public String generateFullClassName(DBConnectionInfo connInfo) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(connInfo);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return utilities.concatStringsWithSeparator(fullPackageName, generateClassName(), ".");
	}
	
	@JsonIgnore
	public String generateFullPackageName(DBConnectionInfo connInfo) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(connInfo);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return fullPackageName;
	}
	
	public void generateRecordClass(DBConnectionInfo connInfo, boolean fullClass) {
		try {
			if (fullClass) {
				this.syncRecordClass = DatabaseEntityPOJOGenerator.generate(this, connInfo);
			} else {
				this.syncRecordClass = DatabaseEntityPOJOGenerator.generateSkeleton(this, connInfo);
			}
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public void generateSkeletonRecordClass(DBConnectionInfo connInfo) {
		try {
			this.syncRecordClass = DatabaseEntityPOJOGenerator.generateSkeleton(this, connInfo);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@JsonIgnore
	@Override
	public String generateClassName() {
		return generateClassName(this.name + "_query_result");
	}
	
	private String generateClassName(String tableName) {
		String[] nameParts = tableName.split("_");
		
		String className = utilities.capitalize(nameParts[0]);
		
		for (int i = 1; i < nameParts.length; i++) {
			className += utilities.capitalize(nameParts[i]);
		}
		
		return className + "VO";
	}
	
	@Override
	public AbstractEtlDataConfiguration getParentConf() {
		return this.relatedSrcConf;
	}
	
	@Override
	public File getPOJOSourceFilesDirectory() {
		return getRelatedEtlConf().getPOJOSourceFilesDirectory();
	}
	
	@Override
	public String getObjectName() {
		return this.name;
	}
	
	@Override
	public PrimaryKey getPrimaryKey() {
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
	public boolean isMetadata() {
		return false;
	}
	
	@Override
	public File getPOJOCopiledFilesDirectory() {
		return getRelatedEtlConf().getPOJOCompiledFilesDirectory();
	}
	
	@Override
	public File getClassPath() {
		return new File(getRelatedEtlConf().getClassPath());
	}
	
	@Override
	public boolean isDestinationInstallationType() {
		return false;
	}
	
	@Override
	public EtlDatabaseObject loadRelatedSrcObject(EtlDatabaseObject mainObject, Connection srcConn) throws DBException {
		
		PreparedQuery pQ = PreparedQuery.prepare(this.getQuery(), mainObject, getRelatedEtlConf(), false);
		
		List<Object> paramsAsList = pQ.generateQueryParameters();
		
		Object[] params = paramsAsList != null ? paramsAsList.toArray() : null;
		
		return DatabaseObjectDAO.find(this.loadHealper, this.getSyncRecordClass(), pQ.generatePreparedQuery(), params,
		    srcConn);
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
	public boolean hasDateFields() {
		for (Field t : this.fields) {
			if (t.isDateField()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public List<Field> cloneFields(EtlDatabaseObject originalObject) {
		List<Field> clonedFields = new ArrayList<>();
		
		if (utilities.arrayHasElement(this.fields)) {
			for (Field field : this.fields) {
				clonedFields.add(field.createACopy());
			}
		}
		
		return clonedFields;
	}
	
	@Override
	public String getAlias() {
		return getName();
	}
	
	@Override
	public void setRelatedEtlConfig(EtlConfiguration relatedSyncConfiguration) {
	}
	
	@Override
	public boolean hasPK(Connection conn) {
		return false;
	}
	
	@Override
	public DBConnectionInfo getRelatedConnInfo() {
		return getRelatedSrcConf().getSrcConnInfo();
	}
	
	@Override
	public List<AuxExtractTable> getSelfJoinTables() {
		return null;
	}
	
	@Override
	public void setSelfJoinTables(List<AuxExtractTable> setSelfJoinTables) {
	}
	
	@Override
	public String generateSelectFromQuery() {
		return null;
	}
	
	@Override
	public boolean isMustLoadChildrenInfo() {
		return false;
	}
	
}
