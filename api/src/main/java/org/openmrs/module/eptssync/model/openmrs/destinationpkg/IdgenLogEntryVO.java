package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class IdgenLogEntryVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int id;
	private int source;
	private String identifier;
	private java.util.Date dateGenerated;
	private int generatedBy;
	private String comment;
 
	public IdgenLogEntryVO() { 
		this.metadata = false;
	} 
 
	public void setId(int id){ 
	 	this.id = id;
	}
 
	public int getId(){ 
		return this.id;
	}
 
	public void setSource(int source){ 
	 	this.source = source;
	}
 
	public int getSource(){ 
		return this.source;
	}
 
	public void setIdentifier(String identifier){ 
	 	this.identifier = identifier;
	}
 
	public String getIdentifier(){ 
		return this.identifier;
	}
 
	public void setDateGenerated(java.util.Date dateGenerated){ 
	 	this.dateGenerated = dateGenerated;
	}
 
	public java.util.Date getDateGenerated(){ 
		return this.dateGenerated;
	}
 
	public void setGeneratedBy(int generatedBy){ 
	 	this.generatedBy = generatedBy;
	}
 
	public int getGeneratedBy(){ 
		return this.generatedBy;
	}
 
	public void setComment(String comment){ 
	 	this.comment = comment;
	}


 
	public String getComment(){ 
		return this.comment;
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
 		return this.id; 
	} 
 
	public void setObjectId(int selfId){ 
		this.id = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.id = rs.getInt("id");
		this.source = rs.getInt("source");
		this.identifier = rs.getString("identifier") != null ? rs.getString("identifier").trim() : null;
		this.dateGenerated =  rs.getTimestamp("date_generated") != null ? new java.util.Date( rs.getTimestamp("date_generated").getTime() ) : null;
		this.generatedBy = rs.getInt("generated_by");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.source == 0 ? null : this.source, this.identifier, this.dateGenerated, this.generatedBy == 0 ? null : this.generatedBy, this.comment};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.source == 0 ? null : this.source, this.identifier, this.dateGenerated, this.generatedBy == 0 ? null : this.generatedBy, this.comment, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO idgen_log_entry(source, identifier, date_generated, generated_by, comment) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE idgen_log_entry SET source = ?, identifier = ?, date_generated = ?, generated_by = ?, comment = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.source == 0 ? null : this.source) + "," + (this.identifier != null ? "\""+identifier+"\"" : null) + "," + (this.dateGenerated != null ? "\""+ DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(dateGenerated)  +"\"" : null) + "," + (this.generatedBy == 0 ? null : this.generatedBy) + "," + (this.comment != null ? "\""+comment+"\"" : null); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.generatedBy != 0) return true;
		if (this.source != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.UsersVO.class, this.generatedBy, false, conn); 
		this.generatedBy = 0;
		if (parentOnDestination  != null) this.generatedBy = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.IdgenIdentifierSourceVO.class, this.source, false, conn); 
		this.source = 0;
		if (parentOnDestination  != null) this.source = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("generatedBy")) return this.generatedBy;		
		if (parentAttName.equals("source")) return this.source;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}