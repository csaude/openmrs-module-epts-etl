package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ReportingReportRequestVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int id;
	private String uuid;
	private String baseCohortUuid;
	private String baseCohortParameters;
	private String reportDefinitionUuid;
	private String reportDefinitionParameters;
	private String rendererType;
	private String rendererArgument;
	private int requestedBy;
	private java.util.Date requestDatetime;
	private String priority;
	private String status;
	private java.util.Date evaluationStartDatetime;
	private java.util.Date evaluationCompleteDatetime;
	private java.util.Date renderCompleteDatetime;
	private String description;
	private String schedule;
	private byte processAutomatically;
	private int minimumDaysToPreserve;
 
	public ReportingReportRequestVO() { 
		this.metadata = false;
	} 
 
	public void setId(int id){ 
	 	this.id = id;
	}
 
	public int getId(){ 
		return this.id;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setBaseCohortUuid(String baseCohortUuid){ 
	 	this.baseCohortUuid = baseCohortUuid;
	}
 
	public String getBaseCohortUuid(){ 
		return this.baseCohortUuid;
	}
 
	public void setBaseCohortParameters(String baseCohortParameters){ 
	 	this.baseCohortParameters = baseCohortParameters;
	}
 
	public String getBaseCohortParameters(){ 
		return this.baseCohortParameters;
	}
 
	public void setReportDefinitionUuid(String reportDefinitionUuid){ 
	 	this.reportDefinitionUuid = reportDefinitionUuid;
	}
 
	public String getReportDefinitionUuid(){ 
		return this.reportDefinitionUuid;
	}
 
	public void setReportDefinitionParameters(String reportDefinitionParameters){ 
	 	this.reportDefinitionParameters = reportDefinitionParameters;
	}
 
	public String getReportDefinitionParameters(){ 
		return this.reportDefinitionParameters;
	}
 
	public void setRendererType(String rendererType){ 
	 	this.rendererType = rendererType;
	}
 
	public String getRendererType(){ 
		return this.rendererType;
	}
 
	public void setRendererArgument(String rendererArgument){ 
	 	this.rendererArgument = rendererArgument;
	}
 
	public String getRendererArgument(){ 
		return this.rendererArgument;
	}
 
	public void setRequestedBy(int requestedBy){ 
	 	this.requestedBy = requestedBy;
	}
 
	public int getRequestedBy(){ 
		return this.requestedBy;
	}
 
	public void setRequestDatetime(java.util.Date requestDatetime){ 
	 	this.requestDatetime = requestDatetime;
	}
 
	public java.util.Date getRequestDatetime(){ 
		return this.requestDatetime;
	}
 
	public void setPriority(String priority){ 
	 	this.priority = priority;
	}
 
	public String getPriority(){ 
		return this.priority;
	}
 
	public void setStatus(String status){ 
	 	this.status = status;
	}
 
	public String getStatus(){ 
		return this.status;
	}
 
	public void setEvaluationStartDatetime(java.util.Date evaluationStartDatetime){ 
	 	this.evaluationStartDatetime = evaluationStartDatetime;
	}
 
	public java.util.Date getEvaluationStartDatetime(){ 
		return this.evaluationStartDatetime;
	}
 
	public void setEvaluationCompleteDatetime(java.util.Date evaluationCompleteDatetime){ 
	 	this.evaluationCompleteDatetime = evaluationCompleteDatetime;
	}
 
	public java.util.Date getEvaluationCompleteDatetime(){ 
		return this.evaluationCompleteDatetime;
	}
 
	public void setRenderCompleteDatetime(java.util.Date renderCompleteDatetime){ 
	 	this.renderCompleteDatetime = renderCompleteDatetime;
	}
 
	public java.util.Date getRenderCompleteDatetime(){ 
		return this.renderCompleteDatetime;
	}
 
	public void setDescription(String description){ 
	 	this.description = description;
	}
 
	public String getDescription(){ 
		return this.description;
	}
 
	public void setSchedule(String schedule){ 
	 	this.schedule = schedule;
	}
 
	public String getSchedule(){ 
		return this.schedule;
	}
 
	public void setProcessAutomatically(byte processAutomatically){ 
	 	this.processAutomatically = processAutomatically;
	}
 
	public byte getProcessAutomatically(){ 
		return this.processAutomatically;
	}
 
	public void setMinimumDaysToPreserve(int minimumDaysToPreserve){ 
	 	this.minimumDaysToPreserve = minimumDaysToPreserve;
	}


 
	public int getMinimumDaysToPreserve(){ 
		return this.minimumDaysToPreserve;
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
 		return this.id; 
	} 
 
	public void setObjectId(int selfId){ 
		this.id = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.id = rs.getInt("id");
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.baseCohortUuid = rs.getString("base_cohort_uuid") != null ? rs.getString("base_cohort_uuid").trim() : null;
		this.baseCohortParameters = rs.getString("base_cohort_parameters") != null ? rs.getString("base_cohort_parameters").trim() : null;
		this.reportDefinitionUuid = rs.getString("report_definition_uuid") != null ? rs.getString("report_definition_uuid").trim() : null;
		this.reportDefinitionParameters = rs.getString("report_definition_parameters") != null ? rs.getString("report_definition_parameters").trim() : null;
		this.rendererType = rs.getString("renderer_type") != null ? rs.getString("renderer_type").trim() : null;
		this.rendererArgument = rs.getString("renderer_argument") != null ? rs.getString("renderer_argument").trim() : null;
		this.requestedBy = rs.getInt("requested_by");
		this.requestDatetime =  rs.getTimestamp("request_datetime") != null ? new java.util.Date( rs.getTimestamp("request_datetime").getTime() ) : null;
		this.priority = rs.getString("priority") != null ? rs.getString("priority").trim() : null;
		this.status = rs.getString("status") != null ? rs.getString("status").trim() : null;
		this.evaluationStartDatetime =  rs.getTimestamp("evaluation_start_datetime") != null ? new java.util.Date( rs.getTimestamp("evaluation_start_datetime").getTime() ) : null;
		this.evaluationCompleteDatetime =  rs.getTimestamp("evaluation_complete_datetime") != null ? new java.util.Date( rs.getTimestamp("evaluation_complete_datetime").getTime() ) : null;
		this.renderCompleteDatetime =  rs.getTimestamp("render_complete_datetime") != null ? new java.util.Date( rs.getTimestamp("render_complete_datetime").getTime() ) : null;
		this.description = rs.getString("description") != null ? rs.getString("description").trim() : null;
		this.schedule = rs.getString("schedule") != null ? rs.getString("schedule").trim() : null;
		this.processAutomatically = rs.getByte("process_automatically");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.uuid, this.baseCohortUuid, this.baseCohortParameters, this.reportDefinitionUuid, this.reportDefinitionParameters, this.rendererType, this.rendererArgument, this.requestedBy == 0 ? null : this.requestedBy, this.requestDatetime, this.priority, this.status, this.evaluationStartDatetime, this.evaluationCompleteDatetime, this.renderCompleteDatetime, this.description, this.schedule, this.processAutomatically, this.minimumDaysToPreserve};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.uuid, this.baseCohortUuid, this.baseCohortParameters, this.reportDefinitionUuid, this.reportDefinitionParameters, this.rendererType, this.rendererArgument, this.requestedBy == 0 ? null : this.requestedBy, this.requestDatetime, this.priority, this.status, this.evaluationStartDatetime, this.evaluationCompleteDatetime, this.renderCompleteDatetime, this.description, this.schedule, this.processAutomatically, this.minimumDaysToPreserve, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO reporting_report_request(uuid, base_cohort_uuid, base_cohort_parameters, report_definition_uuid, report_definition_parameters, renderer_type, renderer_argument, requested_by, request_datetime, priority, status, evaluation_start_datetime, evaluation_complete_datetime, render_complete_datetime, description, schedule, process_automatically, minimum_days_to_preserve) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE reporting_report_request SET uuid = ?, base_cohort_uuid = ?, base_cohort_parameters = ?, report_definition_uuid = ?, report_definition_parameters = ?, renderer_type = ?, renderer_argument = ?, requested_by = ?, request_datetime = ?, priority = ?, status = ?, evaluation_start_datetime = ?, evaluation_complete_datetime = ?, render_complete_datetime = ?, description = ?, schedule = ?, process_automatically = ?, minimum_days_to_preserve = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.baseCohortUuid != null ? "\""+baseCohortUuid+"\"" : null) + "," + (this.baseCohortParameters != null ? "\""+baseCohortParameters+"\"" : null) + "," + (this.reportDefinitionUuid != null ? "\""+reportDefinitionUuid+"\"" : null) + "," + (this.reportDefinitionParameters != null ? "\""+reportDefinitionParameters+"\"" : null) + "," + (this.rendererType != null ? "\""+rendererType+"\"" : null) + "," + (this.rendererArgument != null ? "\""+rendererArgument+"\"" : null) + "," + (this.requestedBy == 0 ? null : this.requestedBy) + "," + (this.requestDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(requestDatetime)  +"\"" : null) + "," + (this.priority != null ? "\""+priority+"\"" : null) + "," + (this.status != null ? "\""+status+"\"" : null) + "," + (this.evaluationStartDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(evaluationStartDatetime)  +"\"" : null) + "," + (this.evaluationCompleteDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(evaluationCompleteDatetime)  +"\"" : null) + "," + (this.renderCompleteDatetime != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(renderCompleteDatetime)  +"\"" : null) + "," + (this.description != null ? "\""+description+"\"" : null) + "," + (this.schedule != null ? "\""+schedule+"\"" : null) + "," + (this.processAutomatically) + "," + (this.minimumDaysToPreserve); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.requestedBy != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.UsersVO.class, this.requestedBy, false, conn); 
		this.requestedBy = 0;
		if (parentOnDestination  != null) this.requestedBy = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("requestedBy")) return this.requestedBy;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}