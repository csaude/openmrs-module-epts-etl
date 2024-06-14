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
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.TableDataSourceConfig;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.exceptions.EtlException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.AbstractSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.TableOperationProgressInfo;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public abstract class AbstractEtlSearchParams<T extends EtlObject> extends AbstractSearchParams<T> {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Date syncStartDate;
	
	private ThreadRecordIntervalsManager limits;
	
	private EtlItemConfiguration config;
	
	private SearchSourceType searchSourceType;
	
	private EtlEngine relatedEngine;
	
	protected int savedCount;
	
	public AbstractEtlSearchParams(EtlItemConfiguration config, ThreadRecordIntervalsManager limits,
	    EtlEngine relatedEtlEngine) {
		this.config = config;
		this.limits = limits;
		this.searchSourceType = SearchSourceType.SOURCE;
		this.relatedEngine = relatedEtlEngine;
	}
	
	public EtlEngine getRelatedEngine() {
		return relatedEngine;
	}
	
	public void setRelatedEngine(EtlEngine relatedEngine) {
		this.relatedEngine = relatedEngine;
	}
	
	public EtlController getRelatedController() {
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
		return config;
	}
	
	public void setConfig(EtlItemConfiguration config) {
		this.config = config;
	}
	
	public ThreadRecordIntervalsManager getLimits() {
		return limits;
	}
	
	public void setLimits(ThreadRecordIntervalsManager limits) {
		this.limits = limits;
	}
	
	protected boolean hasLimits() {
		return this.limits != null;
	}
	
	public void removeLimits() {
		this.limits = null;
	}
	
	/**
	 * @param searchClauses
	 */
	public void tryToAddExtraConditionForExport(SearchClauses<EtlDatabaseObject> searchClauses) {
		if (this.getConfig().getSrcConf().getExtraConditionForExtract() != null) {
			String extraContidion = this.getConfig().getSrcConf().getExtraConditionForExtract();
			
			//@formatter:off
			Object[] params = DBUtilities.loadParamsValues(extraContidion, getConfig().getRelatedSyncConfiguration());
			
			String query = DBUtilities.replaceSqlParametersWithQuestionMarks(extraContidion);
			
			searchClauses.addToClauses(query);
			
			searchClauses.addToParameters(params);		
		}
	}
	
	/**
	 * @param searchClauses
	 * @param tableInfo
	 */
	public void tryToAddLimits(SearchClauses<EtlDatabaseObject> searchClauses) {
		if (this.getLimits() != null && this.getLimits().isInitialized()) {
			
			if (this.getLimits().isOutOfLimits()) {
				throw new ForbiddenOperationException("The current Limits manager is out of limits ["  + this.getLimits() + "]");
			}
			
			if (getSrcTableConf().getPrimaryKey().isSimpleNumericKey()) {
				searchClauses.addToClauses( getSrcConf().getTableAlias() + "." + getSrcTableConf().getPrimaryKey().retrieveSimpleKeyColumnName() + " between ? and ?");
				searchClauses.addToParameters(this.getLimits().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getLimits().getCurrentLastRecordId());
			}else {
				throw new ForbiddenOperationException("Not supported composite or not numeric key for limit query!");
			}
		}		
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getRecordClass() {
		return (Class<T>) getSrcTableConf().getSyncRecordClass(getSrcTableConf().getMainApp());
	}

	public SrcConf getSrcConf() {
		return this.getConfig().getSrcConf();
	}
	
	/**
	 * @return
	 */
	public SrcConf getSrcTableConf() {
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
		return  utilities.getLastRecordOnArray(getConfig().getDstConf());
	}	
	
	public int countAllRecords(Connection conn) throws DBException {
		if (this.savedCount > 0)
			return this.savedCount;
		
		TableOperationProgressInfo progressInfo = null;
		
		try {
			progressInfo = this.getRelatedController().getProgressInfo().retrieveProgressInfo(getConfig());
		}
		catch (NullPointerException e) {
			throw new EtlException("Error on thread " + this.getRelatedController().getControllerId()
			        + ": Progress meter not found for Etl Confinguration [" + getConfig().getConfigCode() + "].");
		}
		
		int maxRecordId = (int) progressInfo.getProgressMeter().getMaxRecordId();
		int minRecordId = (int) progressInfo.getProgressMeter().getMinRecordId();
		
		
		long qtyRecordsBetweenLimits = maxRecordId - minRecordId+1;
		
		int qtyProcessors = utilities.getAvailableProcessors();
		
		if (qtyProcessors > qtyRecordsBetweenLimits) {
			qtyProcessors = (int) qtyRecordsBetweenLimits;
		}
		
		long qtyRecordsPerEngine = qtyRecordsBetweenLimits / qtyProcessors;
		
		List<ThreadRecordIntervalsManager> generatedLimits = new ArrayList<>();
		
		ThreadRecordIntervalsManager initialLimits = null;
		
		List<CompletableFuture<Integer>> tasks = new ArrayList<>(qtyProcessors);
		
		for (int i = 0; i < qtyProcessors; i++) {
			ThreadRecordIntervalsManager limits;
			
			if (initialLimits == null) {
				limits = new ThreadRecordIntervalsManager(minRecordId, minRecordId + qtyRecordsPerEngine - 1, (int) qtyRecordsPerEngine);
				initialLimits = limits;
			} else {
				// Last processor
				if (i == qtyProcessors - 1) {
					long min = initialLimits.getThreadMaxRecordId() + 1;
					long process = maxRecordId - min + 1;
					
					limits = new ThreadRecordIntervalsManager(min, maxRecordId, (int) process);
				} else {
					limits = new ThreadRecordIntervalsManager(initialLimits.getThreadMaxRecordId() + 1,
					        initialLimits.getThreadMaxRecordId() + qtyRecordsPerEngine, (int) qtyRecordsPerEngine);
				}
				initialLimits = limits;
			}
			
			//The limits start qtyRecordsPerProcessing behind, so move next before search
			limits.moveNext();
			
			generatedLimits.add(limits);
			
			tasks.add(CompletableFuture.supplyAsync(() -> {
				try {
					AbstractEtlSearchParams<T> cloned = this.cloneMe();
					
					cloned.setLimits(limits);
					
					return SearchParamsDAO.countAll(cloned, conn);
				}
				catch (DBException e) {
					throw new EtlException(e);
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
			e.printStackTrace();
		}
		
		this.savedCount = (int) finalSum.get();
		
		return this.savedCount;
	}
	
	public List<T> searchNextRecords(EngineMonitor monitor, Connection conn) throws DBException{
		
		if (hasLimits() && getLimits().isOutOfLimits()) return null;
		
		SearchClauses<T> searchClauses = this.generateSearchClauses(conn);
		
		if (this.getOrderByFields() != null) {
			searchClauses.addToOrderByFields(this.getOrderByFields());
		}
		
		String sql = searchClauses.generateSQL(conn);
		
		List<T> l = BaseDAO.search(this.getLoaderHealper(), this.getRecordClass(), sql,
		    searchClauses.getParameters(), conn);
		
		int i = 0;
		
		while (utilities.arrayHasNoElement(l) && this.getLimits().canGoNext()) {
			this.getLimits().save(monitor);
			
			if (i++ == 0) {
				this.getRelatedController()
				        .logInfo("Empty result on fased quering... The application will keep searching next pages");
			} else {
				this.getRelatedController()
				        .logDebug("Empty result on fased quering... The application will keep searching next pages "
				                + this.getLimits());
			}
			this.getLimits().moveNext();
			
			searchClauses = this.generateSearchClauses(conn);
			
			if (this.getOrderByFields() != null) {
				searchClauses.addToOrderByFields(this.getOrderByFields());
			}
			
			sql = searchClauses.generateSQL(conn);
			
			l = BaseDAO.search(this.getLoaderHealper(), this.getRecordClass(), sql, searchClauses.getParameters(),
			    conn);
		}
		
		return l;		
	}

	protected List<T> searchNextRecordsInMultiThreads(TaskProcessor taskProcessor, Connection conn){
		if (!hasLimits()) {
			throw new ForbiddenOperationException("For multithreading search you must specify the limits with min and max records in the searching range");
		}
		
		if (getLimits().isOutOfLimits()) return null;
		
		if (getLimits().getCurrentFirstRecordId() == 0 && getLimits().getCurrentLastRecordId() == 0 ) {
			throw new EtlException("The minRecordId and maxRecordId cannot be zero!!!");
		}
		
		long qtyRecordsBetweenLimits = getLimits().getCurrentLastRecordId() - getLimits().getCurrentFirstRecordId()+1;
		
		int qtyProcessors = utilities.getAvailableProcessors();
		
		if (qtyProcessors > qtyRecordsBetweenLimits) {
			qtyProcessors = (int) qtyRecordsBetweenLimits;
		}
		
		long qtyRecordsPerEngine = qtyRecordsBetweenLimits / qtyProcessors;
		
		ThreadRecordIntervalsManager initialLimits = null;
	
		
		List<CompletableFuture<List<T>>> tasks = new ArrayList<>(qtyProcessors);
		
		List<ThreadRecordIntervalsManager> generatedLimits = new ArrayList<>();
		
		
		
		for (int i = 0; i < qtyProcessors; i++) {
			ThreadRecordIntervalsManager limits;
			
			if (initialLimits == null) {
				limits = new ThreadRecordIntervalsManager(getLimits().getCurrentFirstRecordId(), getLimits().getCurrentFirstRecordId() + qtyRecordsPerEngine - 1, (int)qtyRecordsPerEngine);
				initialLimits = limits;
			} else {
				// Last processor
				if (i == qtyProcessors - 1) {
					long min = initialLimits.getThreadMaxRecordId() + 1;
					long process = getLimits().getCurrentLastRecordId() - min + 1;
					
					limits = new ThreadRecordIntervalsManager(min,  getLimits().getCurrentLastRecordId(), (int)process);
				} else {
					limits = new ThreadRecordIntervalsManager(initialLimits.getThreadMaxRecordId() + 1,
					        initialLimits.getThreadMaxRecordId() + qtyRecordsPerEngine, (int)qtyRecordsPerEngine);
				}
				initialLimits = limits;
			}
			
			limits.setEngine(taskProcessor);
			
			limits.setThreadCode(taskProcessor.getEngineId() + utilities.garantirXCaracterOnNumber(i, 2) + ".tmp");
			
			generatedLimits.add(limits);
			
			//The limits start qtyRecordsPerProcessing behind, so move next before search
			limits.moveNext();
			
			tasks.add(CompletableFuture.supplyAsync(() -> {
				try {
					AbstractEtlSearchParams<T> cloned = this.cloneMe();
					cloned.setLimits(limits);
					
					return cloned.searchNextRecords(taskProcessor.getMonitor(), conn);
				}
				catch (DBException e) {
					throw new EtlException(e);
				}
			}));
		}
		
		// External variable to store the final sum
		 List<T> allSearchedRecords = new ArrayList<>();
		
		// Combine all tasks
		CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
		
		// Handle results when all tasks are complete and update the external variable
		allOf.thenRun(() -> {
			allOf.thenApply(v -> tasks.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        ).thenAccept(allSearchedRecords::addAll).join();
		});
		
		// Block and wait for all tasks to complete (optional)
		try {
			allOf.get();
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		ThreadRecordIntervalsManager.removeAll( generatedLimits, taskProcessor.getMonitor());
		
		
		if (utilities.arrayHasNoElement(allSearchedRecords) && this.getLimits().canGoNext()) {
			this.getLimits().save(taskProcessor.getMonitor());
			
			this.getRelatedController()
				        .logDebug("Empty result on fased quering... The application will keep searching next pages "
				                + this.getLimits());
			
			this.getLimits().moveNext();
			
			return searchNextRecordsInMultiThreads(taskProcessor, conn);
		}
		
		return allSearchedRecords;		
	}

	/**
	 * Clone this search params to another object.
	 * 
	 * Note that the {@link #limits} are not cloned
	 * 
	 * @return the cloned search params
	 */
	protected abstract AbstractEtlSearchParams<T> cloneMe();
	
	protected abstract VOLoaderHelper getLoaderHealper();

	public abstract int countNotProcessedRecords(Connection conn) throws DBException;

	public abstract String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException;

}
