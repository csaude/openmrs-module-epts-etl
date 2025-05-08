package org.openmrs.module.epts.etl.problems_solver.processor;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.problems_solver.model.TmpUserVO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * @author jpboane
 */
public class DuplicatedUsersProcessor extends EtlProcessor {
	
	TableConfiguration usersTableConf;
	
	public DuplicatedUsersProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    Boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public EtlController getRelatedOperationController() {
		return (EtlController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn) throws DBException {
		
		logDebug("RESOLVING PROBLEM ON " + etlObjects.size() + "' " + getMainSrcTableName());
		
		int i = 1;
		
		if (this.usersTableConf == null) {
			this.usersTableConf = new GenericTableConfiguration("users", getSrcConf());
			this.usersTableConf.setMustLoadChildrenInfo(true);
			this.usersTableConf.fullLoad(srcConn);
			
			this.getSrcConf().setSyncRecordClass(TmpUserVO.class);
		}
		
		for (EtlDatabaseObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			List<TmpUserVO> allDuplicatedByUuid = DatabaseObjectDAO.getByField(this.getSrcConf(), "user_uuid",
			    record.getFieldValue("user_uuid").toString(), srcConn);
			
			logDebug(startingStrLog + " RESOLVING..." + record);
			
			TmpUserVO preservedUser = TmpUserVO.getWinningRecord(allDuplicatedByUuid, srcConn);
			preservedUser.setUsersSyncTableConfiguration(this.getSrcConf());
			
			for (int j = 0; j < allDuplicatedByUuid.size(); j++) {
				TmpUserVO dup = allDuplicatedByUuid.get(j);
				
				if (dup.isWinning()) {
					continue;
				}
				
				dup.setUsersSyncTableConfiguration(this.getSrcConf());
				
				GenericDatabaseObject user = new GenericDatabaseObject(this.usersTableConf);
				
				user.setFieldValue("user_id", dup.getUserId());
				user.setUuid(dup.getUuid());
				
				try {
					logDebug("REMOVING USER [" + dup + "]");
					
					user.remove(srcConn);
					dup.markAsDeletable();
					dup.markAsProcessed(srcConn);
				}
				catch (DBException e) {
					logWarn("THE USER HAS RECORDS ASSOCIETED... HARMONIZING...");
					logWarn(e.getLocalizedMessage());
					
					dup.markAsUndeletable();
					preservedUser.harmonize(srcConn);
				}
				
				finally {
					dup.save((TableConfiguration) dup.getRelatedConfiguration(), srcConn);
					
					try {
						srcConn.commit();
					}
					catch (Exception e) {
						logWarn(e.getLocalizedMessage());
					}
				}
			}
			
			i++;
		}
	}
	
}
