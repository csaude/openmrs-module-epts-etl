package org.openmrs.module.epts.etl.transport.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.BaseVO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * @author jpboane
 */
public class TransportRecord extends BaseVO implements EtlDatabaseObject {
	
	private File file;
	
	private File relatedMinimalInfoFile;
	
	private File bkpDirectory;
	
	private File destDirectory;
	
	public TransportRecord(File file, File destDirectory, File bkpDirectory) {
		this.file = file;
		this.bkpDirectory = bkpDirectory;
		this.destDirectory = destDirectory;
	}
	
	public File getFile() {
		return file;
	}
	
	public File generateRelatedMinimalInfoFile() {
		if (this.relatedMinimalInfoFile == null) {
			String[] parts = this.file.getAbsolutePath().split(".json");
			String minimalFile = parts[0] + "_minimal.json";
			
			this.relatedMinimalInfoFile = new File(minimalFile);
		}
		
		return this.relatedMinimalInfoFile;
	}
	
	public File getDestinationFile() {
		return new File(destFileName + ".json");
	}
	
	public File getMinimalDestinationFile() {
		return new File(pathToBkpMinimalFile + ".json");
	}
	
	private String destFileName;
	
	private String pathToBkpMinimalFile;
	
	public void moveToBackUpDirectory() {
		String pathToBkpFile = "";
		
		pathToBkpFile += bkpDirectory.getAbsolutePath();
		pathToBkpFile += FileUtilities.getPathSeparator();
		
		pathToBkpFile += FileUtilities.generateFileNameFromRealPath(this.file.getAbsolutePath());
		
		FileUtilities.renameTo(this.file.getAbsolutePath(), pathToBkpFile);
		
		// NOW, MOVE MINIMAL FILE
		pathToBkpMinimalFile = "";
		pathToBkpMinimalFile += this.bkpDirectory.getAbsolutePath();
		pathToBkpMinimalFile += FileUtilities.getPathSeparator();
		
		pathToBkpMinimalFile += FileUtilities
		        .generateFileNameFromRealPath(generateRelatedMinimalInfoFile().getAbsolutePath());
		
		FileUtilities.renameTo(generateRelatedMinimalInfoFile().getAbsolutePath(), pathToBkpMinimalFile);
	}
	
	public void transport() {
		try {
			destFileName = "";
			
			destFileName += this.destDirectory.getAbsolutePath();
			destFileName += FileUtilities.getPathSeparator();
			
			//To make a file only avaliable after a copy process terminated, the name must be without extension
			destFileName += FileUtilities.generateFileNameFromRealPathWithoutExtension(this.file.getAbsolutePath());
			
			copy(this.file, new File(destFileName));
			
			String minimalDestFileName = "";
			
			minimalDestFileName += this.destDirectory.getAbsolutePath();
			minimalDestFileName += FileUtilities.getPathSeparator();
			minimalDestFileName += FileUtilities
			        .generateFileNameFromRealPathWithoutExtension(this.generateRelatedMinimalInfoFile().getAbsolutePath());
			
			copy(this.generateRelatedMinimalInfoFile(), new File(minimalDestFileName));
			
			FileUtilities.renameTo(minimalDestFileName, minimalDestFileName + ".json");
			FileUtilities.renameTo(destFileName, destFileName + ".json");
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private static void copy(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		}
		finally {
			is.close();
			os.close();
		}
	}
	
	@SuppressWarnings("unused")
	private void copy_old(File source, File dest) throws IOException {
		try {
			FileUtilities.copyFile(source, dest);
		}
		catch (IOException e) {
			if (e.getLocalizedMessage().contains("Failed to copy full contents from")) {
				/*The file is on the creation process
				 * wait and try again
				*/
				TimeCountDown.sleep(10);
				copy(source, dest);
			}
		}
	}
	
	@Override
	public void refreshLastSyncDateOnOrigin(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void refreshLastSyncDateOnDestination(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Oid getObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setObjectId(Oid objectId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<UniqueKeyInfo> getUniqueKeysInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setUniqueKeysInfo(List<UniqueKeyInfo> uniqueKeysInfo) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadDestParentInfo(TableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
	        throws ParentNotYetMigratedException, DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getInsertSQLWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object[] getInsertParamsWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getInsertSQLWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getUpdateSQL() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object[] getUpdateParams() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateInsertValuesWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateInsertValuesWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithObjectId(String insertQuestionMarks) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getInsertSQLQuestionMarksWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithoutObjectId(String insertQuestionMarks) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getInsertSQLQuestionMarksWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean hasIgnoredParent() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void save(TableConfiguration syncTableInfo, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update(TableConfiguration syncTableInfo, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setUuid(String uuid) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasParents() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object getParentValue(ParentTable refInfo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateFullFilledUpdateSql() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void loadObjectIdData(TableConfiguration tabConf) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EtlDatabaseObject getSharedPkObj() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void consolidateData(TableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void resolveInconsistence(TableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EtlStageRecordVO retrieveRelatedSyncInfo(TableConfiguration tableInfo, String recordOriginLocationCode,
	        Connection conn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public EtlDatabaseObject retrieveParentInDestination(Integer parentId, String recordOriginLocationCode,
	        TableConfiguration parentTableConfiguration, boolean ignorable, Connection conn)
	        throws ParentNotYetMigratedException, DBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public EtlStageRecordVO getRelatedSyncInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setRelatedSyncInfo(EtlStageRecordVO relatedSyncInfo) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String generateMissingInfo(Map<ParentTableImpl, Integer> missingParents) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void remove(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Map<ParentTableImpl, Integer> loadMissingParents(TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void removeDueInconsistency(TableConfiguration syncTableInfo, Map<ParentTableImpl, Integer> missingParents,
	        Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void changeParentValue(ParentTable refInfo, EtlDatabaseObject newParent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setParentToNull(ParentTableImpl refInfo) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void changeObjectId(TableConfiguration abstractTableConfiguration, Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void changeParentForAllChildren(EtlDatabaseObject newParent, TableConfiguration syncTableInfo, Connection conn)
	        throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean hasExactilyTheSameDataWith(EtlDatabaseObject srcObj) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object getFieldValue(String fieldName) throws ForbiddenOperationException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void fastCreateSimpleNumericKey(long i) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadWithDefaultValues(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void copyFrom(EtlDatabaseObject parentRecordInOrigin) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EtlDatabaseObject getSrcRelatedObject() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setSrcRelatedObject(EtlDatabaseObject srcRelatedObject) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setSharedPkObj(EtlDatabaseObject sharedPkObj) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ConflictResolutionType getConflictResolutionType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setConflictResolutionType(ConflictResolutionType conflictResolutionType) {
		// TODO Auto-generated method stub
		
	}
}
