package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptDerivedVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptId;
	private String rule;
	private java.util.Date compileDate;
	private String compileStatus;
	private String className;
 
	public ConceptDerivedVO() { 
		this.metadata = false;
	} 
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setRule(String rule){ 
	 	this.rule = rule;
	}
 
	public String getRule(){ 
		return this.rule;
	}
 
	public void setCompileDate(java.util.Date compileDate){ 
	 	this.compileDate = compileDate;
	}
 
	public java.util.Date getCompileDate(){ 
		return this.compileDate;
	}
 
	public void setCompileStatus(String compileStatus){ 
	 	this.compileStatus = compileStatus;
	}
 
	public String getCompileStatus(){ 
		return this.compileStatus;
	}
 
	public void setClassName(String className){ 
	 	this.className = className;
	}


 
	public String getClassName(){ 
		return this.className;
	}	public String getUuid(){ 
		return null;
	}
 
	public void setUuid(String uuid){ }
 
	public int getOriginRecordId(){ 
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
 		return this.conceptId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptId = rs.getInt("concept_id");
		this.rule = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("rule") != null ? rs.getString("rule").trim() : null);
		this.compileDate =  rs.getTimestamp("compile_date") != null ? new java.util.Date( rs.getTimestamp("compile_date").getTime() ) : null;
		this.compileStatus = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("compile_status") != null ? rs.getString("compile_status").trim() : null);
		this.className = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("class_name") != null ? rs.getString("class_name").trim() : null);
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.rule, this.compileDate, this.compileStatus, this.className};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.rule, this.compileDate, this.compileStatus, this.className, this.conceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept_derived(rule, compile_date, compile_status, class_name) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept_derived SET rule = ?, compile_date = ?, compile_status = ?, class_name = ? WHERE concept_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.rule != null ? "\""+ utilities.scapeQuotationMarks(rule)  +"\"" : null) + "," + (this.compileDate != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(compileDate)  +"\"" : null) + "," + (this.compileStatus != null ? "\""+ utilities.scapeQuotationMarks(compileStatus)  +"\"" : null) + "," + (this.className != null ? "\""+ utilities.scapeQuotationMarks(className)  +"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.conceptId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptVO.class, this.conceptId, false, conn); 
		this.conceptId = 0;
		if (parentOnDestination  != null) this.conceptId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("conceptId")) return this.conceptId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}