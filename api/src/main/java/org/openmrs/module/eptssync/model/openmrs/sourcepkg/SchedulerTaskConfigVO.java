package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class SchedulerTaskConfigVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int taskConfigId;
	private String name;
	private String description;
	private String schedulableClass;
	private java.util.Date startTime;
	private String startTimePattern;
	private int repeatInterval;
	private int startOnStartup;
	private int started;
	private int createdBy;
	private java.util.Date dateCreated;
	private int changedBy;
	private java.util.Date dateChanged;
	private String uuid;
	private java.util.Date lastExecutionTime;
 
	public SchedulerTaskConfigVO() { 
		this.metadata = false;
	} 
 
	public void setTaskConfigId(int taskConfigId){ 
	 	this.taskConfigId = taskConfigId;
	}
 
	public int getTaskConfigId(){ 
		return this.taskConfigId;
	}
 
	public void setName(String name){ 
	 	this.name = name;
	}
 
	public String getName(){ 
		return this.name;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setSchedulableClass(String schedulableClass){ 
	 	this.schedulableClass = schedulableClass;
	}
 
	public String getSchedulableClass(){ 
		return this.schedulableClass;
	}
 
	public void setStartTime(java.util.Date startTime){ 
	 	this.startTime = startTime;
	}
 
	public java.util.Date getStartTime(){ 
		return this.startTime;
	}
 
	public void setStartTimePattern(String startTimePattern){ 
	 	this.startTimePattern = startTimePattern;
	}
 
	public String getStartTimePattern(){ 
		return this.startTimePattern;
	}
 
	public void setRepeatInterval(int repeatInterval){ 
	 	this.repeatInterval = repeatInterval;
	}
 
	public int getRepeatInterval(){ 
		return this.repeatInterval;
	}
 
	public void setStartOnStartup(int startOnStartup){ 
	 	this.startOnStartup = startOnStartup;
	}
 
	public int getStartOnStartup(){ 
		return this.startOnStartup;
	}
 
	public void setStarted(int started){ 
	 	this.started = started;
	}
 
	public int getStarted(){ 
		return this.started;
	}
 
	public void setCreatedBy(int createdBy){ 
	 	this.createdBy = createdBy;
	}
 
	public int getCreatedBy(){ 
		return this.createdBy;
	}
 
	public void setDateCreated(java.util.Date dateCreated){ 
	 	this.dateCreated = dateCreated;
	}
 
	public java.util.Date getDateCreated(){ 
		return this.dateCreated;
	}
 
	public void setChangedBy(int changedBy){ 
	 	this.changedBy = changedBy;
	}
 
	public int getChangedBy(){ 
		return this.changedBy;
	}
 
	public void setDateChanged(java.util.Date dateChanged){ 
	 	this.dateChanged = dateChanged;
	}
 
	public java.util.Date getDateChanged(){ 
		return this.dateChanged;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setLastExecutionTime(java.util.Date lastExecutionTime){ 
	 	this.lastExecutionTime = lastExecutionTime;
	}


 
	public java.util.Date getLastExecutionTime(){ 
		return this.lastExecutionTime;
	}	public int getOriginRecordId(){ 
		return 0;
	}
 
	public void setOriginRecordId(int originRecordId){ }
 
	public String getOriginAppLocationCode(){ 
		return null;
	}
 
	public void setOriginAppLocationCode(String originAppLocationCode){ }
 
	public int getConsistent(){ 
		return 0;
	}
 
	public void setConsistent(int consistent){ }
 

 
	public int getObjectId() { 
 		return this.taskConfigId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.taskConfigId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.taskConfigId = rs.getInt("task_config_id");
		this.name = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("name") != null ? rs.getString("name").trim() : null);
		this.description = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("description") != null ? rs.getString("description").trim() : null);
		this.schedulableClass = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("schedulable_class") != null ? rs.getString("schedulable_class").trim() : null);
		this.startTime =  rs.getTimestamp("start_time") != null ? new java.util.Date( rs.getTimestamp("start_time").getTime() ) : null;
		this.startTimePattern = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("start_time_pattern") != null ? rs.getString("start_time_pattern").trim() : null);
		this.repeatInterval = rs.getInt("repeat_interval");
		this.startOnStartup = rs.getInt("start_on_startup");
		this.started = rs.getInt("started");
		this.createdBy = rs.getInt("created_by");
		this.dateCreated =  rs.getTimestamp("date_created") != null ? new java.util.Date( rs.getTimestamp("date_created").getTime() ) : null;
		this.changedBy = rs.getInt("changed_by");
		this.dateChanged =  rs.getTimestamp("date_changed") != null ? new java.util.Date( rs.getTimestamp("date_changed").getTime() ) : null;
		this.uuid = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("uuid") != null ? rs.getString("uuid").trim() : null);
		this.lastExecutionTime =  rs.getTimestamp("last_execution_time") != null ? new java.util.Date( rs.getTimestamp("last_execution_time").getTime() ) : null;
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "task_config_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.name, this.description, this.schedulableClass, this.startTime, this.startTimePattern, this.repeatInterval, this.startOnStartup, this.started, this.createdBy == 0 ? null : this.createdBy, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.lastExecutionTime};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.name, this.description, this.schedulableClass, this.startTime, this.startTimePattern, this.repeatInterval, this.startOnStartup, this.started, this.createdBy == 0 ? null : this.createdBy, this.dateCreated, this.changedBy == 0 ? null : this.changedBy, this.dateChanged, this.uuid, this.lastExecutionTime, this.taskConfigId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO scheduler_task_config(name, description, schedulable_class, start_time, start_time_pattern, repeat_interval, start_on_startup, started, created_by, date_created, changed_by, date_changed, uuid, last_execution_time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE scheduler_task_config SET name = ?, description = ?, schedulable_class = ?, start_time = ?, start_time_pattern = ?, repeat_interval = ?, start_on_startup = ?, started = ?, created_by = ?, date_created = ?, changed_by = ?, date_changed = ?, uuid = ?, last_execution_time = ? WHERE task_config_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.name != null ? "\""+ utilities.scapeQuotationMarks(name)  +"\"" : null) + "," + (this.description != null ? "\""+ utilities.scapeQuotationMarks(description)  +"\"" : null) + "," + (this.schedulableClass != null ? "\""+ utilities.scapeQuotationMarks(schedulableClass)  +"\"" : null) + "," + (this.startTime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(startTime)  +"\"" : null) + "," + (this.startTimePattern != null ? "\""+ utilities.scapeQuotationMarks(startTimePattern)  +"\"" : null) + "," + (this.repeatInterval) + "," + (this.startOnStartup) + "," + (this.started) + "," + (this.createdBy == 0 ? null : this.createdBy) + "," + (this.dateCreated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateCreated)  +"\"" : null) + "," + (this.changedBy == 0 ? null : this.changedBy) + "," + (this.dateChanged != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateChanged)  +"\"" : null) + "," + (this.uuid != null ? "\""+ utilities.scapeQuotationMarks(uuid)  +"\"" : null) + "," + (this.lastExecutionTime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(lastExecutionTime)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.changedBy != 0) return true;
		if (this.createdBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.changedBy, true, conn); 
		this.changedBy = 0;
		if (parentOnDestination  != null) this.changedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.createdBy, true, conn); 
		this.createdBy = 0;
		if (parentOnDestination  != null) this.createdBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("changedBy")) return this.changedBy;		
		if (parentAttName.equals("createdBy")) return this.createdBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}