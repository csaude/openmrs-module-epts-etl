package org.openmrs.module.epts.etl.controller;

import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.etl.model.EtlDynamicSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.slf4j.event.Level;

public class DynamicProcessStarter extends ProcessStarter implements ControllerStarter {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private EptsEtlLogger logger;
	
	private EtlConfiguration etlConfig;
	
	public DynamicProcessStarter(EtlConfiguration etlConfig) {
		super(etlConfig);
		
		if (!etlConfig.isDynamic()) {
			throw new ForbiddenOperationException("The etl cong " + etlConfig.getConfigFilePath() + " is not dynamic!!!");
		}
		
		this.etlConfig = etlConfig;
		
		this.logger = new EptsEtlLogger(DynamicProcessStarter.class);
	}
	
	private List<EtlDatabaseObject> loadAvaliableSrcObjects(EtlConfiguration etlConfig) {
		try {
			OpenConnection conn = etlConfig.getMainConnInfo().openConnection();
			
			if (!etlConfig.hasMainConnInfo()) {
				throw new ForbiddenOperationException("For dynamic etl configuration you must setup the mainConnInfo!!!");
			}
			
			etlConfig.getDynamicSrcConf().setParentConf(etlConfig);
			etlConfig.getDynamicSrcConf().setRelatedEtlConfig(etlConfig);
			etlConfig.getDynamicSrcConf().fullLoad(conn);
			
			EtlDynamicSearchParams searchParams = new EtlDynamicSearchParams(etlConfig.getDynamicSrcConf());
			
			return searchParams.search(null, conn, conn);
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	public EptsEtlLogger getLogger() {
		return logger;
	}
	
	ProcessController init(EtlDatabaseObject src) throws ForbiddenOperationException, DBException {
		OpenConnection conn = this.etlConfig.openMainConn();
		
		ProcessController currentController;
		
		try {
			logger.debug("Initializing ProcessController using " + this.etlConfig.getConfigFilePath());
			
			currentController = new ProcessController(this, this.etlConfig.cloneDynamic(src, conn));
			
			logger.debug("ProcessController Initialized");
		}
		finally {
			if (conn != null) {
				conn.finalizeConnection();
			}
		}
		
		return currentController;
	}
	
	@Override
	public void run() {
		
		try {
			if (EptsEtlLogger.determineLogLevel().equals(Level.DEBUG)) {
				TimeCountDown.sleep(10);
			}
			
			ProcessController currentController = null;
			
			for (EtlDatabaseObject src : loadAvaliableSrcObjects(this.etlConfig)) {
				currentController = init(src);
				
				ThreadPoolService.getInstance().createNewThreadPoolExecutor(currentController.getControllerId())
				        .execute(currentController);
				
				while (!currentController.isFinalized()) {
					TimeCountDown.sleep(30);
					
					logger.warn("THE APPLICATION IS STILL RUNING...", 60 * 15);
				}
			}
			
			if (currentController.isFinished()) {
				logger.warn("ALL JOBS ARE FINISHED");
			} else if (currentController.isStopped()) {
				logger.warn("ALL JOBS ARE STOPPED");
			}
		}
		catch (ForbiddenOperationException e) {
			throw e;
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void finalize(Controller c) {
		c.killSelfCreatedThreads();
		
		ProcessController controller = (ProcessController) c;
		
		if (c.isFinished()) {
			if (controller.getConfiguration().getChildConfigFilePath() != null) {
				throw new ForbiddenOperationException(
				        "You cannot configure childConfigFilePath on dynamic etl configuration!!!!");
			} else {
				controller.finalize();
			}
		} else if (c.isStopped()) {
			logger.warn("THE APPLICATION IS STOPPING DUE STOP REQUESTED!");
			controller.finalize();
		}
		
	}
	
}
