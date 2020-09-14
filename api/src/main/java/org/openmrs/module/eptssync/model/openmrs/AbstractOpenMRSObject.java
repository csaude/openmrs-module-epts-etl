package org.openmrs.module.eptssync.model.openmrs;

import java.sql.Connection;

import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractOpenMRSObject extends BaseVO implements OpenMRSObject{
	
	/*
	 * Indicate if there where parents which have been ingored
	 */
	private boolean hasIgnoredParent;
	
	public <T extends OpenMRSObject> T loadParent(Class<T> parentClass, int parentId, boolean ignorable, Connection conn) throws ParentNotYetMigratedException, DBException {
		if (parentId == 0) return null;
		
		T parentOnDestination = OpenMRSObjectDAO.thinGetByOriginRecordId(parentClass, parentId, this.getOriginAppLocationCode(), conn);
 
		if (parentOnDestination != null){
			return parentOnDestination;
		}
		
		if (ignorable) {
			this.hasIgnoredParent = true;
			return null;
		}
			
		throw new ParentNotYetMigratedException(parentId, utilities.createInstance(parentClass).generateTableName(), this.getOriginAppLocationCode());
	}
	
	 
	@Override
	public void save(Connection conn) throws DBException{ 
		OpenMRSObject recordOnDB = OpenMRSObjectDAO.thinGetByOriginRecordId(this.getClass(), this.getOriginRecordId(), this.getOriginAppLocationCode(), conn);
 
		if (recordOnDB != null) {
			this.setObjectId(recordOnDB.getObjectId());
			OpenMRSObjectDAO.update(this, conn);
		}
		else {
			OpenMRSObjectDAO.insert(this, conn);
		}
	} 

	@JsonIgnore
	public boolean hasIgnoredParent() {
		return hasIgnoredParent;
	}
	
	public void setHasIgnoredParent(boolean hasIgnoredParent) {
		this.hasIgnoredParent = hasIgnoredParent;
	}
}
