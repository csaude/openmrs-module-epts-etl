package org.openmrs.module.epts.etl.conf.datasource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractEtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlTemplateInfo;
import org.openmrs.module.epts.etl.conf.PrimaryKey;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlSrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.DbmsType;
import org.openmrs.module.epts.etl.conf.types.RelationshipResolutionStrategy;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.FieldTransformingInfo;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlDatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectLoaderHelper;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a query configuration. A query is used on data mapping between source and destination
 * table
 */
public class QueryDataSourceConfig extends AbstractEtlDataConfiguration implements EtlDatabaseObjectConfiguration, EtlAdditionalDataSource, EtlSrcConf {
	
	private final String stringLock = new String("LOCK_STRING");
	
	private String name;
	
	private String query;
	
	private String script;
	
	private List<Field> fields;
	
	private Boolean fullLoaded;
	
	private SrcConf relatedSrcConf;
	
	private Class<? extends EtlDatabaseObject> syncRecordClass;
	
	private Boolean required;
	
	private DatabaseObjectLoaderHelper loadHealper;
	
	private PreparedQuery defaultPreparedQuery;
	
	private EtlTemplateInfo template;
	
	private Boolean doNotLoadFields;
	
	private List<String> dynamicElements;
	
	private RelationshipResolutionStrategy relationshipResolutionStrategy;
	
	public QueryDataSourceConfig() {
		this.loadHealper = new DatabaseObjectLoaderHelper(this);
		
		this.relationshipResolutionStrategy = RelationshipResolutionStrategy.RESOLVE;
	}
	
	public QueryDataSourceConfig(String query, SrcConf relatedSrcVonf) {
		this();
		
		setRelatedSrcConf(relatedSrcVonf);
		
		setQuery(query);
	}
	
	public RelationshipResolutionStrategy getRelationshipResolutionStrategy() {
		return relationshipResolutionStrategy;
	}
	
	public void setRelationshipResolutionStrategy(RelationshipResolutionStrategy relationshipResolutionStrategy) {
		this.relationshipResolutionStrategy = relationshipResolutionStrategy;
	}
	
	public RelationshipResolutionStrategy relationshipResolutionStrategy() {
		return this.relationshipResolutionStrategy;
	}
	
	public List<String> getDynamicElements() {
		return dynamicElements;
	}
	
	public void setDynamicElements(List<String> dynamicElements) {
		this.dynamicElements = dynamicElements;
	}
	
	public Boolean isDoNotLoadFields() {
		return isTrue(doNotLoadFields) || this.hasFields();
	}
	
	public void setDoNotLoadFields(Boolean doNotLoadFields) {
		this.doNotLoadFields = doNotLoadFields;
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
	public DatabaseObjectLoaderHelper getLoadHealper() {
		return this.loadHealper;
	}
	
	public void setLoadHealper(DatabaseObjectLoaderHelper loadHealper) {
		this.loadHealper = loadHealper;
	}
	
	@Override
	public Boolean isRequired() {
		return isTrue(this.required);
	}
	
	public void setRequired(Boolean required) {
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
			
			String query = new String(Files.readAllBytes(Paths.get(pathToScript)));
			
			if (retrieveNearestTemplate() != null) {
				query = EtlDataConfiguration.resolvePlaceholders(query, null, null, null,
				    retrieveNearestTemplate().getParameters());
			}
			
			this.setQuery(query);
			
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public Boolean isFullLoaded() {
		return isTrue(fullLoaded);
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
	
	public PreparedQuery getDefaultPreparedQuery() {
		return defaultPreparedQuery;
	}
	
	public void setDefaultPreparedQuery(PreparedQuery defaultPreparedQuery) {
		this.defaultPreparedQuery = defaultPreparedQuery;
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) throws DBException {
		PreparedQuery query;
		try {
			
			getRelatedEtlConf().logDebug("Initializing full loading of QueryDataSourceConfig [" + this.getName() + "]");
			
			query = PreparedQuery.prepare(this, getRelatedEtlConf(), null, true, DbmsType.determineFromConnection(conn));
			
			getRelatedEtlConf().logTrace("Determining fields for query...");
			
			setFields(SQLUtilities.determineFieldsFromQuery(query.generatePreparedQuery(), null, conn));
			
			getRelatedEtlConf().logDebug("QueryDataSourceConfig [" + this.getName() + "] full loaded!");
			
			this.fullLoaded = true;
		}
		catch (ForbiddenOperationException | DBException e) {
			//Mean that there are missing parameters. Lets try to load the minimal information of fields
			//Note that we are not marking the record as fullLoaded as there will be missing information on fields
			
			setFields(SQLUtilities.determineFieldsFromQuery(this.getQuery()));
		}
		
	}
	
	@Override
	public void prepare(List<EtlDatabaseObject> mainObject, Connection conn) throws DBException {
		if (isPrepared()) {
			return;
		}
		
		synchronized (stringLock) {
			PreparedQuery query = PreparedQuery.prepare(this, mainObject, getRelatedEtlConf(),
			    DbmsType.determineFromConnection(conn));
			
			if (!isDoNotLoadFields()) {
				
				List<Object> paramsAsList = query.generateQueryParameters();
				
				Object[] params = paramsAsList != null ? paramsAsList.toArray() : null;
				
				try {
					setFields(SQLUtilities.determineFieldsFromQuery(query.generatePreparedQuery(), params, conn));
				}
				catch (DBException e) {
					throw new DBException("Error computing the query " + this.getName(), e);
				}
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
	public Boolean existsSyncRecordClass(DBConnectionInfo connInfo) {
		try {
			return getSyncRecordClass(connInfo) != null;
		}
		catch (ForbiddenOperationException e) {
			
			return false_();
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
	public Boolean allowMultipleSrcObjectsForLoading() {
		return Boolean.TRUE;
	}
	
	public void generateRecordClass(DBConnectionInfo connInfo, Boolean fullClass) {
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
	public Boolean hasPK() {
		return Boolean.FALSE;
	}
	
	@Override
	public Boolean isMetadata() {
		return Boolean.FALSE;
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
	public Boolean isDestinationInstallationType() {
		return Boolean.FALSE;
	}
	
	@Override
	public EtlDatabaseObject loadRelatedSrcObject(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject dstObject, List<EtlDatabaseObject> avaliableSrcObjects, Connection srcConn)
	        throws DBException {
		if (!isPrepared()) {
			prepare(avaliableSrcObjects, srcConn);
		}
		
		List<EtlDatabaseObject> list = this.getDefaultPreparedQuery()
		        .cloneAndLoadValues(processor, srcObject, dstObject, avaliableSrcObjects, srcConn)
		        .query(processor != null ? processor.getEngine() : null, srcConn);
		
		if (utilities.listHasNoElement(list)) {
			return null;
		} else if (utilities.arrayHasMoreThanOneElements(list)) {
			throw new ForbiddenOperationException(
			        "The datasource (" + this.getName() + ") returned more than one src objects");
		}
		
		EtlDatabaseObject result = list.get(0);
		
		if (relationshipResolutionStrategy.skip()) {
			for (Field f : result.getFields()) {
				FieldsMapping tf = FieldsMapping.fastCreate(f.getName(), srcConn);
				tf.setRelationshipResolutionStrategy(RelationshipResolutionStrategy.SKIP);
				
				f.setTransformingInfo(new FieldTransformingInfo(tf, result.getFieldValue(f.getName()), this));
			}
		}
		
		return result;
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
	public Boolean hasDateFields() {
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
		
		if (utilities.listHasElement(this.fields)) {
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
	public Boolean hasPK(Connection conn) {
		return Boolean.FALSE;
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
	public Boolean isMustLoadChildrenInfo() {
		return false_();
	}
	
	public static QueryDataSourceConfig fastCreate(String query, SrcConf relatedSrcConf) {
		return new QueryDataSourceConfig(query, relatedSrcConf);
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
		
		if (utilities.listHasElement(allToCloneFrom)) {
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
		this.setQuery(SQLUtilities.tryToReplaceParamsInQuery(this.getQuery(), schemaInfoSrc));
	}
	
	public static void tryToReplacePlaceholders(List<QueryDataSourceConfig> extraQueryDataSource,
	        EtlDatabaseObject schemaInfoSrc) {
		
		if (utilities.listHasElement(extraQueryDataSource)) {
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
	
	@Override
	public ActionOnEtlException getGeneralBehaviourOnEtlException() {
		return relatedSrcConf.getGeneralBehaviourOnEtlException();
	}
	
	@Override
	public EtlTemplateInfo retrieveNearestTemplate() {
		return this.getTemplate() != null ? this.getTemplate() : getParentConf().retrieveNearestTemplate();
	}
	
	@Override
	public void tryToLoadSchemaInfo(EtlDatabaseObject schemaInfoSrc, Connection conn)
	        throws DBException, ForbiddenOperationException, DatabaseResourceDoesNotExists {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void setParentConf(EtlDataConfiguration relatedParent) {
		this.relatedSrcConf = (SrcConf) relatedParent;
	}
	
}
