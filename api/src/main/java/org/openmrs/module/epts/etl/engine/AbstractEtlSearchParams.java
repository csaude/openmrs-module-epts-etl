package org.openmrs.module.epts.etl.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.datasource.PreparedQuery;
import org.openmrs.module.epts.etl.conf.datasource.QueryDataSourceConfig;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.datasource.TableDataSourceConfig;
import org.openmrs.module.epts.etl.conf.interfaces.SqlFunctionType;
import org.openmrs.module.epts.etl.conf.types.DbmsType;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadCurrentIntervals;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.AbstractSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.TableOperationProgressInfo;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public abstract class AbstractEtlSearchParams<T extends EtlDatabaseObject> extends AbstractSearchParams<T> {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Date syncStartDate;
	
	private ThreadRecordIntervalsManager<T> threadRecordIntervalsManager;
	
	private SearchSourceType searchSourceType;
	
	protected int savedCount;
	
	protected SrcConf srcConf;
	
	private MigrationFinalCheckStatus finalCheckStatus;
	
	public AbstractEtlSearchParams(SrcConf srcConf, ThreadRecordIntervalsManager<T> limits) {
		this.threadRecordIntervalsManager = limits;
		this.searchSourceType = SearchSourceType.SOURCE;
		this.srcConf = srcConf;
		
		this.finalCheckStatus = MigrationFinalCheckStatus.NOT_INITIALIZED;
	}
	
	public void setSearchSourceType(SearchSourceType searchSourceType) {
		this.searchSourceType = searchSourceType;
	}
	
	public SearchSourceType getSearchSourceType() {
		return searchSourceType;
	}
	
	public Date getSyncStartDate() {
		return syncStartDate;
	}
	
	public void setSyncStartDate(Date syncStartDate) {
		this.syncStartDate = syncStartDate;
	}
	
	public EtlItemConfiguration getConfig() {
		return getSrcConf().getParentConf();
	}
	
	public ThreadRecordIntervalsManager<T> getThreadRecordIntervalsManager() {
		return threadRecordIntervalsManager;
	}
	
	public void setThreadRecordIntervalsManager(ThreadRecordIntervalsManager<T> limits) {
		this.threadRecordIntervalsManager = limits;
	}
	
	protected boolean hasLimits() {
		return this.threadRecordIntervalsManager != null;
	}
	
	public void removeLimits() {
		this.threadRecordIntervalsManager = null;
	}
	
	public MigrationFinalCheckStatus getFinalCheckStatus() {
		return finalCheckStatus;
	}
	
	public void setFinalCheckStatus(MigrationFinalCheckStatus finalCheckStatus) {
		this.finalCheckStatus = finalCheckStatus;
	}
	
	@SuppressWarnings({ "unchecked" })
	private void tryToAddExtraCondition(String extraCondition, SearchClauses<T> searchClauses, T parentObject,
	        List<T> dataSourceObjects, DbmsType dbmsType) throws EtlTransformationException, DBException {
		
		if (dataSourceObjects != null) {
			this.getRelatedEtlConf().logTrace("Search for parentObject..." + dataSourceObjects);
		}
		
		if (extraCondition != null) {
			
			List<EtlDatabaseObject> ds = (List<EtlDatabaseObject>) collectDataSourceObjects(parentObject, dataSourceObjects);
			
			PreparedQuery pQ = PreparedQuery.prepare(QueryDataSourceConfig.fastCreate(extraCondition, this.getSrcConf()),
			    this.getRelatedEtlConf(), ds, true, dbmsType);
			
			pQ = pQ.cloneAndLoadValues(null, parentObject, null, ds, null);
			
			List<Object> paramsAsList = pQ.generateQueryParameters();
			
			Object[] params = paramsAsList != null ? paramsAsList.toArray() : null;
			
			searchClauses.addToClauses(pQ.generatePreparedQuery());
			
			searchClauses.addToParameters(params);
		}
	}
	
	/**
	 * @param searchClauses
	 * @throws DBException
	 * @throws EtlTransformationException
	 */
	public void tryToAddExtraConditionForExport(SearchClauses<T> searchClauses, T parentObject, List<T> dataSourceObjects,
	        DbmsType dbmsType) throws EtlTransformationException, DBException {
		
		tryToAddExtraCondition(this.srcConf.getExtraConditionForExtract(), searchClauses, parentObject, dataSourceObjects,
		    dbmsType);
	}
	
	public void tryToAddExtraJoinExtraConditions(SearchClauses<T> searchClauses, T parentObject, List<T> dataSourceObjects,
	        DbmsType dbmsType) throws EtlTransformationException, DBException {
		
		if (!srcConf.isJoinable()) {
			return;
		}
		
		if (parentObject == null) {
			throw new EtlExceptionImpl("The joinable srcConf requires the parent object!");
		}
		
		String extraCondition = srcConf.generateConditionsFields(parentObject, srcConf.getJoinFields(),
		    srcConf.getJoinExtraCondition());
		
		tryToAddExtraCondition(extraCondition, searchClauses, parentObject, dataSourceObjects, dbmsType);
	}
	
	public EtlConfiguration getRelatedEtlConf() {
		return srcConf.getRelatedEtlConf();
	}
	
	/**
	 * @param searchClauses
	 * @param tableInfo
	 */
	public void tryToAddLimits(IntervalExtremeRecord intervalExtremeRecord, SearchClauses<EtlDatabaseObject> searchClauses) {
		if (intervalExtremeRecord != null) {
			if (getSrcConf().getPrimaryKey().isSimpleNumericKey()) {
				searchClauses.addToClauses(getSrcConf().getTableAlias() + "."
				        + getSrcConf().getPrimaryKey().retrieveSimpleKeyColumnName() + " between ? and ?");
				searchClauses.addToParameters(intervalExtremeRecord.getMinRecordId());
				searchClauses.addToParameters(intervalExtremeRecord.getMaxRecordId());
			} else {
				throw new ForbiddenOperationException("Not supported composite or not numeric key for limit query!");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getRecordClass() {
		return (Class<T>) getSrcConf().getSyncRecordClass(getSrcConf().getSrcConnInfo());
	}
	
	public SrcConf getSrcConf() {
		return this.srcConf;
	}
	
	public void setSrcConf(SrcConf srcConf) {
		this.srcConf = srcConf;
	}
	
	/**
	 * @return
	 */
	public List<TableDataSourceConfig> getExtraTableDataSource() {
		return getSrcConf().getExtraTableDataSource();
	}
	
	/**
	 * @return
	 */
	public AbstractTableConfiguration getDstLastTableConfiguration() {
		return utilities.getLastRecordOnArray(srcConf.getParentConf().getDstConf());
	}
	
	public int countAllRecords(OperationController<T> controller, Connection conn) throws DBException {
		TableOperationProgressInfo progressInfo = null;
		
		try {
			progressInfo = controller.getProgressInfo().retrieveProgressInfo(getConfig());
		}
		catch (NullPointerException e) {
			throw new EtlExceptionImpl("Error on thread " + controller.getControllerId()
			        + ": Progress meter not found for Etl Confinguration [" + getConfig().getConfigCode() + "].");
		}
		
		long minRecordId = progressInfo.getProgressMeter().getMinRecordId();
		long maxRecordId = progressInfo.getProgressMeter().getMaxRecordId();
		
		return countAllRecords(minRecordId, maxRecordId, conn);
	}
	
	public EtlOperationConfig getRelatedEtlOperationConfig() {
		return getRelatedEtlConf().getOperations().get(0);
	}
	
	public int countAllRecords(long minRecordId, long maxRecordId, Connection conn) throws DBException {
		if (this.savedCount > 0)
			return this.savedCount;
		
		long qtyRecordsBetweenLimits = maxRecordId - minRecordId + 1;
		
		int qtyProcessors = utilities.getAvailableProcessors();
		
		if (getRelatedEtlOperationConfig().isDisableMultithreadingSearch()) {
			qtyProcessors = 1;
		}
		
		if (qtyProcessors > qtyRecordsBetweenLimits) {
			qtyProcessors = (int) qtyRecordsBetweenLimits;
		}
		
		ThreadCurrentIntervals currIntervas = new ThreadCurrentIntervals(minRecordId, maxRecordId, qtyProcessors);
		
		List<CompletableFuture<Integer>> tasks = new ArrayList<>(qtyProcessors);
		
		for (IntervalExtremeRecord limits : currIntervas.getInternalIntervals()) {
			tasks.add(CompletableFuture.supplyAsync(() -> {
				try {
					return SearchParamsDAO.countAll(this, limits, conn);
				}
				catch (DBException e) {
					throw new EtlExceptionImpl(e);
				}
			}));
		}
		
		// External variable to store the final sum
		AtomicLong finalSum = new AtomicLong(0);
		
		// Combine all tasks
		CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
		
		// Handle results when all tasks are complete and update the external variable
		allOf.thenRun(() -> {
			long sum = tasks.stream().map(CompletableFuture::join).collect(Collectors.summingLong(Integer::intValue));
			finalSum.set(sum);
		});
		
		// Block and wait for all tasks to complete (optional)
		try {
			allOf.get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		
		this.savedCount = (int) finalSum.get();
		
		return this.savedCount;
	}
	
	public List<T> search(IntervalExtremeRecord intervalExtremeRecord, T parentObject, List<T> auxDataSourceObjects,
	        Connection srcConn, Connection dstCOnn) throws DBException {
		SearchClauses<T> searchClauses = this.generateSearchClauses(intervalExtremeRecord, parentObject,
		    auxDataSourceObjects, srcConn, dstCOnn);
		
		if (this.getOrderByFields() != null) {
			searchClauses.addToOrderByFields(this.getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(srcConn);
		
		if (getRelatedEtlConf() != null) {
			getRelatedEtlConf().logTrace(
			    "Using query for intervals " + intervalExtremeRecord + " > \n------------ \n " + this.generateFulfilledQuery(
			        intervalExtremeRecord, parentObject, auxDataSourceObjects, srcConn, dstCOnn) + "\n----------------");
		}
		
		Object[] params = searchClauses.getParameters();
		
		try {
			return BaseDAO.search(this.getLoaderHealper(), this.getRecordClass(), sql, params, srcConn);
		}
		catch (DBException e) {
			throw e;
		}
	}
	
	public long retrieveExtremeRecord(SqlFunctionType sqlFunction, Connection conn) throws DBException {
		return SearchParamsDAO.retrieveExtremRecord(this, sqlFunction, null, conn);
	}
	
	public List<T> searchNextRecordsInMultiThreads(IntervalExtremeRecord interval, T parentObject,
	        List<T> auxDataSourceObjects, Connection srcConn, Connection dstConn) throws DBException {
		if (interval == null) {
			throw new ForbiddenOperationException("For multithreading search you must specify the IntervalExtremeRecord");
		}
		
		ThreadCurrentIntervals currIntervals = new ThreadCurrentIntervals(interval.getMinRecordId(),
		        interval.getMaxRecordId(), utilities.getAvailableProcessors());
		
		List<CompletableFuture<List<T>>> tasks = new ArrayList<>(currIntervals.getInternalIntervals().size());
		
		for (IntervalExtremeRecord limits : currIntervals.getInternalIntervals()) {
			
			tasks.add(CompletableFuture.supplyAsync(() -> {
				try {
					return this.search(limits, parentObject, auxDataSourceObjects, srcConn, dstConn);
				}
				catch (DBException e) {
					throw new EtlExceptionImpl(e);
				}
			}));
		}
		
		// External variable to store the final sum
		List<T> allSearchedRecords = new ArrayList<>();
		
		// Combine all tasks
		CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
		
		// Handle results when all tasks are complete and update the external variable
		allOf.thenRun(() -> {
			allOf.thenApply(
			    v -> tasks.stream().map(CompletableFuture::join).flatMap(List::stream).collect(Collectors.toList()))
			        .thenAccept(allSearchedRecords::addAll).join();
		});
		
		// Block and wait for all tasks to complete 
		try {
			allOf.get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new EtlExceptionImpl("Error Happened when searching for records", e);
		}
		
		return allSearchedRecords;
	}
	
	/**
	 * Clone this search params to another object. Note that the
	 * {@link #threadRecordIntervalsManager} are not cloned
	 * 
	 * @return the cloned search params
	 */
	public abstract AbstractEtlSearchParams<T> cloneMe();
	
	protected abstract VOLoaderHelper getLoaderHealper();
	
	public abstract int countNotProcessedRecords(OperationController<T> controller, Connection conn) throws DBException;
	
	public abstract String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException;
	
}
