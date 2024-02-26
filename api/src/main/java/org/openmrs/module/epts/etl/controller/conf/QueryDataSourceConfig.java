package org.openmrs.module.epts.etl.controller.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.SyncExtraDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.PojobleDatabaseObject;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a query configuration. A query is used on data mapping between source and destination
 * table
 */
public class QueryDataSourceConfig extends BaseConfiguration implements PojobleDatabaseObject, SyncDataSource {
	
	private String name;
	
	private String query;
	
	private String script;
	
	private List<Field> fields;
	
	private boolean fullLoaded;
	
	private SyncExtraDataSource relatedSrcExtraDataSrc;
	
	private Class<DatabaseObject> syncRecordClass;
	
	private List<QueryParameter> paramConfig;
	
	private boolean required;
	
	@Override
	public boolean isRequired() {
		return this.required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	public List<QueryParameter> getParamConfig() {
		return paramConfig;
	}
	
	public void setParamConfig(List<QueryParameter> paramConfig) {
		this.paramConfig = paramConfig;
	}
	
	public SyncExtraDataSource getRelatedSrcExtraDataSrc() {
		return relatedSrcExtraDataSrc;
	}
	
	public void setRelatedSrcExtraDataSrc(SyncExtraDataSource relatedSrcExtraDataSrc) {
		this.relatedSrcExtraDataSrc = relatedSrcExtraDataSrc;
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
		String pathToScript = getRelatedSyncConfiguration().getSqlScriptsDirectory().getAbsolutePath() + File.separator
		        + this.script;
		
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
		OpenConnection conn = getRelatedSrcExtraDataSrc().getRelatedDestinationTableConf().getMainApp().openConnection();
		
		try {
			fullLoad(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) throws DBException {
		String query = DBUtilities.replaceSqlParametersWithQuestionMarks(this.getQuery());
		
		setFields(DBUtilities.determineFieldsFromQuery(query, conn));
		
		this.fullLoaded = true;
	}
	
	@JsonIgnore
	@Override
	public Class<DatabaseObject> getSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		if (syncRecordClass == null)
			this.syncRecordClass = DatabaseEntityPOJOGenerator.tryToGetExistingCLass(generateFullClassName(application),
			    getRelatedSyncConfiguration());
		
		if (syncRecordClass == null) {
			OpenConnection conn = application.openConnection();
			
			try {
				generateRecordClass(application, true);
			}
			finally {
				conn.finalizeConnection();
			}
		}
		
		if (syncRecordClass == null) {
			throw new ForbiddenOperationException("The related pojo of query " + getObjectName() + " was not found!!!!");
		}
		
		return syncRecordClass;
	}
	
	public SyncConfiguration getRelatedSyncConfiguration() {
		return relatedSrcExtraDataSrc.getRelatedDestinationTableConf().getRelatedSyncConfiguration();
	}
	
	@JsonIgnore
	public boolean existsSyncRecordClass(AppInfo application) {
		try {
			return getSyncRecordClass(application) != null;
		}
		catch (ForbiddenOperationException e) {
			
			return false;
		}
	}
	
	public void setSyncRecordClass(Class<DatabaseObject> syncRecordClass) {
		this.syncRecordClass = syncRecordClass;
	}
	
	@JsonIgnore
	public String getClasspackage(AppInfo application) {
		return application.getPojoPackageName() + "._query_result";
	}
	
	@JsonIgnore
	public String generateFullClassName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return utilities.concatStringsWithSeparator(fullPackageName, generateClassName(), ".");
	}
	
	@JsonIgnore
	public String generateFullPackageName(AppInfo application) {
		String rootPackageName = "org.openmrs.module.epts.etl.model.pojo";
		
		String packageName = getClasspackage(application);
		
		String fullPackageName = utilities.concatStringsWithSeparator(rootPackageName, packageName, ".");
		
		return fullPackageName;
	}
	
	public void generateRecordClass(AppInfo application, boolean fullClass) {
		try {
			if (fullClass) {
				this.syncRecordClass = DatabaseEntityPOJOGenerator.generate(this, application);
			} else {
				this.syncRecordClass = DatabaseEntityPOJOGenerator.generateSkeleton(this, application);
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
	
	public void generateSkeletonRecordClass(AppInfo application) {
		try {
			this.syncRecordClass = DatabaseEntityPOJOGenerator.generateSkeleton(this, application);
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
	public File getPOJOSourceFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOSourceFilesDirectory();
	}
	
	@Override
	public String getObjectName() {
		return this.name;
	}
	
	@Override
	public String getPrimaryKey() {
		return null;
	}
	
	@Override
	public String getPrimaryKeyAsClassAtt() {
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
	public boolean isNumericColumnType() {
		return false;
	}
	
	@Override
	public List<RefInfo> getParents() {
		return null;
	}
	
	@Override
	public List<RefInfo> getConditionalParents() {
		return null;
	}
	
	@Override
	public boolean isMetadata() {
		return false;
	}
	
	@Override
	public File getPOJOCopiledFilesDirectory() {
		return getRelatedSyncConfiguration().getPOJOCompiledFilesDirectory();
	}
	
	@Override
	public File getClassPath() {
		return new File(getRelatedSyncConfiguration().getClassPath());
	}
	
	@Override
	public boolean isDestinationInstallationType() {
		return false;
	}
	
	@Override
	public DatabaseObject loadRelatedSrcObject(DatabaseObject mainObject, Connection srcConn, AppInfo srcAppInfo)
	        throws DBException {
		
		//@formatter:off
		Object[] params = DBUtilities.loadParamsValues(this.getQuery(), this.paramConfig, mainObject, getRelatedSyncConfiguration());
		
		String query = DBUtilities.replaceSqlParametersWithQuestionMarks(this.getQuery());
		
		return DatabaseObjectDAO.find(this.getSyncRecordClass(srcAppInfo), query, params, srcConn);
	}
	

	
}
