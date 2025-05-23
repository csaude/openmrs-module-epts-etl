package org.openmrs.module.epts.etl.conf.datasource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractBaseConfiguration;
import org.openmrs.module.epts.etl.conf.AbstractEtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.PrimaryKey;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DbmsType;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a query configuration. A query is used on data mapping between source and destination
 * table
 */
public class QueryDataSourceConfig extends AbstractBaseConfiguration implements DatabaseObjectConfiguration, EtlAdditionalDataSource {
	
	private final String stringLock = new String("LOCK_STRING");
	
	private String name;
	
	private String query;
	
	private String script;
	
	private List<Field> fields;
	
	private boolean fullLoaded;
	
	private SrcConf relatedSrcConf;
	
	private Class<? extends EtlDatabaseObject> syncRecordClass;
	
	private boolean required;
	
	private DatabaseObjectLoaderHelper loadHealper;
	
	private PreparedQuery defaultPreparedQuery;
	
	public QueryDataSourceConfig() {
		this.loadHealper = new DatabaseObjectLoaderHelper(this);
	}
	
	public QueryDataSourceConfig(String query, SrcConf relatedSrcVonf) {
		setRelatedSrcConf(relatedSrcVonf);
		
		setQuery(query);
	}
	
	@Override
	public DatabaseObjectLoaderHelper getLoadHealper() {
		return this.loadHealper;
	}
	
	public void setLoadHealper(DatabaseObjectLoaderHelper loadHealper) {
		this.loadHealper = loadHealper;
	}
	
	private boolean isPrepared() {
		return this.defaultPreparedQuery != null;
	}
	
	private PreparedQuery getDefaultPreparedQuery() {
		return defaultPreparedQuery;
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
		if (!utilities.stringHasValue(this.query) && utilities.stringHasValue(this.getScript())) {
			loadQueryFromFile();
		}
		
		if (!utilities.stringHasValue(this.query)) {
			throw new ForbiddenOperationException("No query was defined!");
		}
		
		return utilities.removeNewline(this.query);
	}
	
	private void loadQueryFromFile() {
		String pathToScript = getRelatedEtlConf().getSqlScriptsDirectory().getAbsolutePath() + File.separator + this.script;
		
		try {
			this.setQuery(new String(Files.readAllBytes(Paths.get(pathToScript))));
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
		PreparedQuery query;
		try {
			
			getRelatedEtlConf().logDebug("Initializing full loading of QueryDataSourceConfig [" + this.getName() + "]");
			
			query = PreparedQuery.prepare(this, getRelatedEtlConf(), true, DbmsType.determineFromConnection(conn));
			
			getRelatedEtlConf().logTrace("Determining fields for query...");
			
			setFields(DBUtilities.determineFieldsFromQuery(query.generatePreparedQuery(), null, conn));
			
			getRelatedEtlConf().logDebug("QueryDataSourceConfig [" + this.getName() + "] full loaded!");
			
			this.fullLoaded = true;
		}
		catch (ForbiddenOperationException | DBException e) {
			//Mean that there are missing parameters. Lets try to load the minimal information of fields
			//Note that we are not marking the record as fullLoaded as there will be missing information on fields
			
			setFields(DBUtilities.determineFieldsFromQuery(this.getQuery()));
		}
		
	}
	
	public void prepare(List<EtlDatabaseObject> mainObject, Connection conn) throws DBException {
		if (isPrepared()) {
			return;
		}
		
		synchronized (stringLock) {
			PreparedQuery query = PreparedQuery.prepare(this, mainObject, getRelatedEtlConf(),
			    DbmsType.determineFromConnection(conn));
			
			List<Object> paramsAsList = query.generateQueryParameters();
			
			Object[] params = paramsAsList != null ? paramsAsList.toArray() : null;
			
			try {
				setFields(DBUtilities.determineFieldsFromQuery(query.generatePreparedQuery(), params, conn));
			}
			catch (DBException e) {
				throw new DBException("Error computing the query " + this.getName(), e);
			}
			
			this.defaultPreparedQuery = query;
		}
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
	
	@Override
	public boolean allowMultipleSrcObjects() {
		return true;
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
	public EtlDatabaseObject loadRelatedSrcObject(List<EtlDatabaseObject> avaliableSrcObjects, Connection srcConn)
	        throws DBException {
		if (!isPrepared()) {
			prepare(avaliableSrcObjects, srcConn);
		}
		
		return this.getDefaultPreparedQuery().cloneAndLoadValues(avaliableSrcObjects).query(srcConn);
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
	public String generateSelectFromQuery() {
		return null;
	}
	
	@Override
	public boolean isMustLoadChildrenInfo() {
		return false;
	}
	
	public static QueryDataSourceConfig fastCreate(String query, SrcConf relatedSrcVonf) {
		return new QueryDataSourceConfig(query, relatedSrcVonf);
	}
	
	@Override
	public String toString() {
		return "Query " + getName() + "\nQuery\n--------------------\n" + getQuery() + "\n--------------------";
	}
	
	@Override
	public TableConfiguration findFullConfiguredConfInAllRelatedTable(String fullTableName, List<Integer> a) {
		return null;
	}
	
	public static List<QueryDataSourceConfig> cloneAll(List<QueryDataSourceConfig> allToCloneFrom, SrcConf relatedSrcConf,
	        Connection conn) throws DBException {
		
		List<QueryDataSourceConfig> allCloned = null;
		
		if (utilities.arrayHasElement(allToCloneFrom)) {
			allCloned = new ArrayList<>(allToCloneFrom.size());
			
			for (QueryDataSourceConfig aux : allToCloneFrom) {
				QueryDataSourceConfig cloned = new QueryDataSourceConfig();
				cloned.clone(aux, relatedSrcConf, conn);
				
				allCloned.add(cloned);
			}
		}
		
		return allCloned;
	}
	
	public void clone(QueryDataSourceConfig toCloneFrom, SrcConf relatedSrcConf, Connection conn) throws DBException {
		this.setName(toCloneFrom.getName());
		this.setQuery(toCloneFrom.getQuery());
		this.setScript(toCloneFrom.getScript());
		this.setRelatedSrcConf(relatedSrcConf);
		this.setRequired(toCloneFrom.isRequired());
	}
	
	public void tryToFillParams(EtlDatabaseObject schemaInfoSrc) {
		this.setQuery(DBUtilities.tryToReplaceParamsInQuery(this.getQuery(), schemaInfoSrc));
	}
	
	public static void tryToReplacePlaceholders(List<QueryDataSourceConfig> extraQueryDataSource,
	        EtlDatabaseObject schemaInfoSrc) {
		
		if (utilities.arrayHasElement(extraQueryDataSource)) {
			for (QueryDataSourceConfig a : extraQueryDataSource) {
				a.tryToReplacePlaceholders(schemaInfoSrc);
			}
		}
		
	}
	
	@Override
	public void tryToReplacePlaceholders(EtlDatabaseObject schemaInfoSrc) {
		setName(utilities.tryToReplacePlaceholders(this.getName(), schemaInfoSrc));
		setQuery(utilities.tryToReplacePlaceholders(getQuery(), schemaInfoSrc));
		setScript(utilities.tryToReplacePlaceholders(this.getScript(), schemaInfoSrc));
	}
	
}
