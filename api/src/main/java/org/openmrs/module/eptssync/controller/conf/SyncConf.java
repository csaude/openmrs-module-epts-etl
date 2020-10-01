package org.openmrs.module.eptssync.controller.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.ws.rs.ForbiddenException;

import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class SyncConf implements MonitoredOperation, Runnable{
	private String syncRootDirectory;
	private String syncStageSchema;
	
	private String originAppLocationCode;
	
	private List<SyncTableInfo> syncTableInfo;
	
	private boolean firstExport;
	private boolean mustCreateClasses;
	private boolean mustRecompileTable;
	private DBConnectionInfo connInfo;
	private DBConnectionService connService;
	
	private int operationStatus;
	private boolean stopRequested;
	private String classpackage;
	private boolean mustCreateStageSchemaElements;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String installationType;
	private File relatedConfFile;

	private List<SyncOperationConfig> operations;
	
	private static final String[] supportedInstallationTypes = {"source", "destination"};
	
	private SyncConf() {
	}
	
	public String getInstallationType() {
		return installationType;
	}
	
	public void setInstallationType(String installationType) {
		if (!utilities.isStringIn(installationType, supportedInstallationTypes)) {
			throw new ForbiddenException("The 'installationType' of syncConf file must be in "+supportedInstallationTypes);
		}
		this.installationType = installationType;
	}
	
	public boolean isDestinationInstallationType() {
		return this.installationType.equals(supportedInstallationTypes[1]);
	}
	
	public boolean mustCreateStageSchemaElements() {
		return mustCreateStageSchemaElements;
	}
	
	public boolean isMustCreateStageSchemaElements() {
		return mustCreateStageSchemaElements;
	}
	
	public void setMustCreateStageSchemaElements(boolean mustCreateStageSchemaElements) {
		this.mustCreateStageSchemaElements = mustCreateStageSchemaElements;
	}
	
	public String getClasspackage() {
		return classpackage;
	}
	
	public void setClasspackage(String classpackage) {
		this.classpackage = classpackage;
	}
	public boolean isMustCreateClasses() {
		return mustCreateClasses;
	}

	public void setMustCreateClasses(boolean mustCreateClasses) {
		this.mustCreateClasses = mustCreateClasses;
	}

	public DBConnectionInfo getConnInfo() {
		return connInfo;
	}
	
	public void setConnInfo(DBConnectionInfo connInfo) {
		this.connInfo = connInfo;
	}
	
	public boolean isMustRecompileTable() {
		return mustRecompileTable;
	}
	
	public void setMustRecompileTable(boolean mustRecompileTable) {
		this.mustRecompileTable = mustRecompileTable;
	}
	
	public int getDefaultQtyRecordsPerEngine(String operationType) {
		return  findOperation(operationType).getDefaultQtyRecordsPerEngine();
	}
	
	public boolean isDoIntegrityCheckInTheEnd(String operationType) {
		return  findOperation(operationType).isDoIntegrityCheckInTheEnd();
	}
	
	public int getDefaultQtyRecordsPerSelect(String operationType) {
		return findOperation(operationType).getDefaultQtyRecordsPerSelect();
	}

	public boolean isFirstExport() {
		return firstExport;
	}

	public void setFirstExport(boolean firstExport) {
		this.firstExport = firstExport;
	}

	public List<SyncTableInfo> getSyncTableInfo() {
		return syncTableInfo;
	}
	
	public String getSyncRootDirectory() {
		return syncRootDirectory;
	}

	public void setSyncRootDirectory(String syncRootDirectory) {
		this.syncRootDirectory = syncRootDirectory;
	}

	public void setSyncTableInfo(List<SyncTableInfo> syncTableInfo) {
		this.syncTableInfo = syncTableInfo;
	}

	public String getSyncStageSchema() {
		return syncStageSchema;
	}
	
	public void setSyncStageSchema(String syncStageSchema) {
		this.syncStageSchema = syncStageSchema;
	}
	
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}

	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}

	private void fullLoadInfo() {
		changeStatusToRunning();
		
		OpenConnection conn = openConnection();
		
		try {
			if (mustCreateStageSchemaElements() && !this.isImportStageSchemaExists()) {
				this.createStageSchema();
			}
			
			for (SyncTableInfo info : this.syncTableInfo) {
				info.setRelatedSyncTableInfoSource(this);
				info.tryToUpgradeDataBaseInfo();
				if (mustCreateClasses) info.generateRecordClass();
			}
			
			if (mustCreateClasses) {
				recompileAllAvaliableClasses(conn);
			}
			
			conn.markAsSuccessifullyTerminected();
			
			changeStatusToFinished();
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
		
	}
	
	private void recompileAllAvaliableClasses(Connection conn) {
		for (SyncTableInfo info : this.syncTableInfo) {
			if (utilities.createInstance(info.getSyncRecordClass()).isGeneratedFromSkeletonClass()) {
				info.generateRecordClass();
			}
		
			for (ParentRefInfo i: info.getChildRefInfo()) {
				
				if (i.getReferenceTableInfo().getTableName().equals("person")) {
					System.out.println("Stop");
				}
				
				if (!i.getReferenceTableInfo().isFullLoaded()) {
					SyncTableInfo existingTableInfo = retrieveTableInfoByTableName(i.getReferenceTableInfo().getTableName());
					
					if (existingTableInfo != null) {
						i.setReferenceTableInfo(existingTableInfo);
					}
					else {
						i.getReferenceTableInfo().fullLoad();
					}
				}
				
				if (!i.existsRelatedReferenceClass() || utilities.createInstance(i.determineRelatedReferenceClass()).isGeneratedFromSkeletonClass()) {
					i.generateRelatedReferenceClass();
				}
			}
			
			for (ParentRefInfo i: info.getParentRefInfo()) {
				if (!i.getReferencedTableInfo().isFullLoaded()) {
					SyncTableInfo existingTableInfo = retrieveTableInfoByTableName(i.getReferencedTableInfo().getTableName());
					
					if (existingTableInfo != null) {
						i.setReferencedTableInfo(existingTableInfo);
					}
					else {
						i.getReferencedTableInfo().fullLoad();
					}
				}
				
				if (!i.existsRelatedReferencedClass() || utilities.createInstance(i.determineRelatedReferencedClass()).isGeneratedFromSkeletonClass()) {
					i.generateRelatedReferencedClass(conn);
				}
			}
		}
	}

	private SyncTableInfo retrieveTableInfoByTableName(String tableName) {
		for (SyncTableInfo info : this.syncTableInfo) {
			if (info.getTableName().equals(tableName)) return info;
		}
		
		for (SyncTableInfo info : this.syncTableInfo) {
			
			for (ParentRefInfo child : info.getChildRefInfo()) {
				if (child.getReferenceTableInfo().getTableName().equals(tableName)) {
					if (child.getReferenceTableInfo().isFullLoaded()) {
						return child.getReferenceTableInfo();
					}
				}
			}
		}
		
		return null;
	}


	public void setRelatedConfFile(File relatedConfFile) {
		this.relatedConfFile = relatedConfFile;
	}
	
	public File getRelatedConfFile() {
		return relatedConfFile;
	}
	
	public static SyncConf loadFromFile(File file) throws IOException {
		SyncConf conf = SyncConf.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		
		conf.setRelatedConfFile(file);
		
		return conf;
	}
	
	private static SyncConf loadFromJSON (String json) {
		try {
			return new ObjectMapperProvider().getContext(SyncConf.class).readValue(json, SyncConf.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} 
	}
	
	public OpenConnection openConnection() {
		if (connService == null) connService = DBConnectionService.init(getConnInfo());
		
		return connService.openConnection();
	}
	
	private void createStageSchema() {
		OpenConnection conn = openConnection();
		
		try {
			Statement st = conn.createStatement();

			st.addBatch("CREATE DATABASE " + getSyncStageSchema());

			st.executeBatch();

			st.close();
			
			conn.markAsSuccessifullyTerminected();
		} catch (SQLException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}

	}

	private boolean isImportStageSchemaExists() {
		OpenConnection conn = openConnection();
		
		try {
			return DBUtilities.isResourceExist(null, DBUtilities.RESOURCE_TYPE_SCHEMA, getSyncStageSchema(), conn);
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public TimeController getTimer() {
		return null;
	}

	@Override
	public boolean stopRequested() {
		return this.stopRequested;
	}
	
	@Override
	public boolean isNotInitialized() {
		return this.operationStatus == MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	@Override
	public boolean isRunning() {
		return this.operationStatus == MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public boolean isStopped() {
		return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
	}
	
	@Override
	public boolean isPaused() {
		return this.operationStatus == MonitoredOperation.STATUS_PAUSED;
	}
	
	@Override
	public boolean isSleeping() {
		return this.operationStatus == MonitoredOperation.STATUS_SLEEPENG;
	}

	@Override
	public void changeStatusToSleeping() {
		this.operationStatus = MonitoredOperation.STATUS_SLEEPENG;
	}
	
	@Override
	public void changeStatusToRunning() {
		this.operationStatus = MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public void changeStatusToStopped() {
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;		
	}
	
	@Override
	public void changeStatusToFinished() {
		this.operationStatus = MonitoredOperation.STATUS_FINISHED;	
	}
	
	@Override	
	public void changeStatusToPaused() {
		this.operationStatus = MonitoredOperation.STATUS_PAUSED;	
	}

	@Override
	public void requestStop() {	
	}

	@Override
	public void run() {
		fullLoadInfo();
	}

	public SyncTableInfo find(SyncTableInfo tableInf) {
		return utilities.findOnList(this.syncTableInfo, tableInf);
	}

	public String getDesignation() {
		return this.installationType + "_" + this.originAppLocationCode;
	}
	
	private SyncOperationConfig findOperation(String operationType) {
		return utilities.findOnArray(this.operations, SyncOperationConfig.fastCreate(operationType));
	}
}
