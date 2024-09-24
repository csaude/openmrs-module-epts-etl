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
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.PreparedQuery;
import org.openmrs.module.epts.etl.conf.datasource.QueryDataSourceConfig;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.datasource.TableDataSourceConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadCurrentIntervals;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
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
import org.openmrs.module.epts.etl.utilities.db.conn.DbmsType;

public abstract class AbstractEtlSearchParams<T extends EtlDatabaseObject> extends AbstractSearchParams<T> {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Date syncStartDate;
	
	private ThreadRecordIntervalsManager<T> threadRecordIntervalsManager;
	
	private SearchSourceType searchSourceType;
	
	private Engine<T> relatedEngine;
	
	protected int savedCount;
	
	public AbstractEtlSearchParams(Engine<T> relatedEtlEngine, ThreadRecordIntervalsManager<T> limits) {
		this.threadRecordIntervalsManager = limits;
		this.searchSourceType = SearchSourceType.SOURCE;
		this.relatedEngine = relatedEtlEngine;
	}
	
	public Engine<T> getRelatedEngine() {
		return relatedEngine;
	}
	
	public void setRelatedEngine(Engine<T> relatedEngine) {
		this.relatedEngine = relatedEngine;
	}
	
	public OperationController<T> getRelatedController() {
		return getRelatedEngine().getRelatedOperationController();
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
		return getRelatedEngine().getEtlItemConfiguration();
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
	
	/**
	 * @param searchClauses
	 */
	public void tryToAddExtraConditionForExport(SearchClauses<EtlDatabaseObject> searchClauses, DbmsType dbmsType) {
		if (this.getSrcConf().getExtraConditionForExtract() != null) {
			String extraContidion = this.getConfig().getSrcConf().getExtraConditionForExtract();
			PreparedQuery pQ = PreparedQuery.prepare(QueryDataSourceConfig.fastCreate(extraContidion, getSrcConf()),
			    getConfig().getRelatedEtlConf(), true, dbmsType);
			
			List<Object> paramsAsList = pQ.generateQueryParameters();
			
			Object[] params = paramsAsList != null ? paramsAsList.toArray() : null;
			
			searchClauses.addToClauses(pQ.generatePreparedQuery());
			
			searchClauses.addToParameters(params);
		}
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
		return this.getConfig().getSrcConf();
	}
	
	/**
	 * @return
	 */
	public List<TableDataSourceConfig> getExtraTableDataSource() {
		return this.getConfig().getSrcConf().getExtraTableDataSource();
	}
	
	/**
	 * @return
	 */
	public AbstractTableConfiguration getDstLastTableConfiguration() {
		return utilities.getLastRecordOnArray(getConfig().getDstConf());
	}
	
	public int countAllRecords(Connection conn) throws DBException {
		TableOperationProgressInfo progressInfo = null;
		
		try {
			progressInfo = this.getRelatedController().getProgressInfo().retrieveProgressInfo(getConfig());
		}
		catch (NullPointerException e) {
			throw new EtlExceptionImpl("Error on thread " + this.getRelatedController().getControllerId()
			        + ": Progress meter not found for Etl Confinguration [" + getConfig().getConfigCode() + "].");
		}
		
		long minRecordId = progressInfo.getProgressMeter().getMinRecordId();
		long maxRecordId = progressInfo.getProgressMeter().getMaxRecordId();
		
		return countAllRecords(minRecordId, maxRecordId, conn);
	}
	
	public int countAllRecords(long minRecordId, long maxRecordId, Connection conn) throws DBException {
		if (this.savedCount > 0)
			return this.savedCount;
		
		long qtyRecordsBetweenLimits = maxRecordId - minRecordId + 1;
		
		int qtyProcessors = utilities.getAvailableProcessors();
		
		if (this.getRelatedEngine() != null
		        && this.getRelatedEngine().getRelatedEtlOperationConfig().isDisableMultithreadingSearch()) {
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
	
	public List<T> search(IntervalExtremeRecord intervalExtremeRecord, Connection srcConn, Connection dstCOnn)
	        throws DBException {
		SearchClauses<T> searchClauses = this.generateSearchClauses(intervalExtremeRecord, srcConn, dstCOnn);
		
		if (this.getOrderByFields() != null) {
			searchClauses.addToOrderByFields(this.getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(srcConn);
		
		if (getRelatedEngine() != null) {
			getRelatedEngine().logTrace("Using query for intervals " + intervalExtremeRecord + " > \n------------ \n "
			        + this.generateFulfilledQuery(intervalExtremeRecord, srcConn, dstCOnn) + "\n----------------");
		}
		
		return BaseDAO.search(this.getLoaderHealper(), this.getRecordClass(), sql, searchClauses.getParameters(), srcConn);
	}
	
	public List<T> searchNextRecordsInMultiThreads(IntervalExtremeRecord interval, Connection srcConn, Connection dstConn)
	        throws DBException {
		if (interval == null) {
			throw new ForbiddenOperationException("For multithreading search you must specify the IntervalExtremeRecord");
		}
		
		ThreadCurrentIntervals currIntervals = new ThreadCurrentIntervals(interval.getMinRecordId(),
		        interval.getMaxRecordId(), utilities.getAvailableProcessors());
		
		List<CompletableFuture<List<T>>> tasks = new ArrayList<>(currIntervals.getInternalIntervals().size());
		
		for (IntervalExtremeRecord limits : currIntervals.getInternalIntervals()) {
			
			tasks.add(CompletableFuture.supplyAsync(() -> {
				try {
					return this.search(limits, srcConn, dstConn);
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
		
		// Block and wait for all tasks to complete (optional)
		try {
			allOf.get();
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		/*
		if (utilities.arrayHasNoElement(allSearchedRecords) && this.getThreadRecordIntervalsManager().canGoNext()) {
			this.getThreadRecordIntervalsManager().save();
			
			this.getRelatedController()
			        .logDebug("Empty result on fased quering... The application will keep searching next pages "
			                + this.getThreadRecordIntervalsManager());
			
			this.getThreadRecordIntervalsManager().moveNext();
			
			return searchNextRecordsInMultiThreads(srcConn, dstConn);
		}
		*/
		
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
	
	public abstract int countNotProcessedRecords(Connection conn) throws DBException;
	
	public abstract String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException;
	
}
