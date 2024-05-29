package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.openmrs.module.epts.etl.conf.AuxExtractTable;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.EtlException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.TableOperationProgressInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class EtlSearchParams extends DatabaseObjectSearchParams {
	
	protected int savedCount;
	
	public EtlSearchParams(EtlItemConfiguration config, RecordLimits limits, EtlController relatedController) {
		super(config, limits, relatedController);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray(getSrcTableConf().getTableAlias()));
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SrcConf srcConfig = getSrcTableConf();
		
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addColumnToSelect("distinct " + srcConfig.generateFullAliasedSelectColumns() + "\n");
		
		String clauseFrom = srcConfig.generateSelectFromClauseContent();
		
		if (utilities.arrayHasElement(srcConfig.getSelfJoinTables())) {
			String additionalLeftJoinFields = "";
			
			for (AuxExtractTable aux : srcConfig.getSelfJoinTables()) {
				String joinType = aux.getJoinType().toString();
				String extraJoinQuery = aux.generateJoinConditionsFields();
				
				if (utilities.stringHasValue(extraJoinQuery)) {
					Object[] params = DBUtilities.loadParamsValues(extraJoinQuery,
					    getConfig().getRelatedSyncConfiguration());
					
					extraJoinQuery = DBUtilities.replaceSqlParametersWithQuestionMarks(extraJoinQuery);
					
					searchClauses.addToParameters(params);
				}
				
				String newLine = clauseFrom.toUpperCase().contains("JOIN") ? "\n" : "";
				
				clauseFrom = clauseFrom + " " + newLine + joinType + " join " + aux.getFullTableName() + " "
				        + aux.getTableAlias() + " on " + extraJoinQuery;
				
				if (aux.useSharedPKKey()) {
					
					ParentTable shrd = aux.getSharedTableConf();
					
					clauseFrom += "\n" + joinType + " join " + shrd.generateSelectFromClauseContent() + " on "
					        + shrd.generateJoinCondition();
				}
				
				if (aux.getJoinType().isLeftJoin()) {
					
					if (aux.getPrimaryKey() == null) {
						throw new ForbiddenOperationException("The aux table " + aux.getTableName() + " in relation "
						        + srcConfig.getTableName() + " does not have primary key");
					}
					
					additionalLeftJoinFields = utilities.concatCondition(additionalLeftJoinFields,
					    aux.getPrimaryKey().generateSqlNotNullCheckWithDisjunction(), "or");
				}
			}
			
			if (!additionalLeftJoinFields.isEmpty()) {
				searchClauses.addToClauses(additionalLeftJoinFields);
			}
		}
		
		searchClauses.addToClauseFrom(clauseFrom);
		
		tryToAddLimits(searchClauses);
		
		tryToAddExtraConditionForExport(searchClauses);
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
	}
	
	@Override
	public EtlController getRelatedController() {
		return (EtlController) super.getRelatedController();
	}
	
	@Override
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
		
		int qtyRecordsBetweenLimits = maxRecordId - minRecordId;
		
		if (qtyRecordsBetweenLimits == 0) {
			return 0;
		}
		
		//int qtyProcessors = utilities.getAvailableProcessors();
		
		int qtyProcessors = 1;
		
		int qtyRecordsPerEngine = qtyRecordsBetweenLimits / qtyProcessors;
		
		RecordLimits initialLimits = null;
		
		List<CompletableFuture<Integer>> tasks = new ArrayList<>(qtyProcessors);
		
		for (int i = 0; i < qtyProcessors; i++) {
			RecordLimits limits;
			
			if (initialLimits == null) {
				limits = new RecordLimits(minRecordId, minRecordId + qtyRecordsPerEngine - 1, qtyRecordsPerEngine);
				initialLimits = limits;
			} else {
				// Last processor
				if (i == qtyProcessors - 1) {
					limits = new RecordLimits(initialLimits.getThreadMaxRecord() + 1, maxRecordId, qtyRecordsPerEngine);
				} else {
					limits = new RecordLimits(initialLimits.getThreadMaxRecord() + 1,
					        initialLimits.getThreadMaxRecord() + qtyRecordsPerEngine, qtyRecordsPerEngine);
				}
				initialLimits = limits;
			}
			
			tasks.add(CompletableFuture.supplyAsync(() -> {
				try {
					return SearchParamsDAO.countAll(new EtlSearchParams(getConfig(), limits, getRelatedController()), conn);
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
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
