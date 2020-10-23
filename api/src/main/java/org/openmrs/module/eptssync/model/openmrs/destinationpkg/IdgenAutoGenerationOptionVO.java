package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.utilities.AttDefinedElements; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import java.io.File; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class IdgenAutoGenerationOptionVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int id;
	private int identifierType;
	private int source;
	private byte manualEntryEnabled;
	private byte automaticGenerationEnabled;
	private int location;
 
	public IdgenAutoGenerationOptionVO() { 
		this.metadata = false;
	} 
 
	public void setId(int id){ 
	 	this.id = id;
	}
 
	public int getId(){ 
		return this.id;
	}
 
	public void setIdentifierType(int identifierType){ 
	 	this.identifierType = identifierType;
	}
 
	public int getIdentifierType(){ 
		return this.identifierType;
	}
 
	public void setSource(int source){ 
	 	this.source = source;
	}
 
	public int getSource(){ 
		return this.source;
	}
 
	public void setManualEntryEnabled(byte manualEntryEnabled){ 
	 	this.manualEntryEnabled = manualEntryEnabled;
	}
 
	public byte getManualEntryEnabled(){ 
		return this.manualEntryEnabled;
	}
 
	public void setAutomaticGenerationEnabled(byte automaticGenerationEnabled){ 
	 	this.automaticGenerationEnabled = automaticGenerationEnabled;
	}
 
	public byte getAutomaticGenerationEnabled(){ 
		return this.automaticGenerationEnabled;
	}
 
	public void setLocation(int location){ 
	 	this.location = location;
	}


 
	public int getLocation(){ 
		return this.location;
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
		this.identifierType = rs.getInt("identifier_type");
		this.source = rs.getInt("source");
		this.manualEntryEnabled = rs.getByte("manual_entry_enabled");
		this.automaticGenerationEnabled = rs.getByte("automatic_generation_enabled");
		this.location = rs.getInt("location");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.identifierType == 0 ? null : this.identifierType, this.source == 0 ? null : this.source, this.manualEntryEnabled, this.automaticGenerationEnabled, this.location == 0 ? null : this.location};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.identifierType == 0 ? null : this.identifierType, this.source == 0 ? null : this.source, this.manualEntryEnabled, this.automaticGenerationEnabled, this.location == 0 ? null : this.location, this.id};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO idgen_auto_generation_option(identifier_type, source, manual_entry_enabled, automatic_generation_enabled, location) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE idgen_auto_generation_option SET identifier_type = ?, source = ?, manual_entry_enabled = ?, automatic_generation_enabled = ?, location = ? WHERE id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.identifierType == 0 ? null : this.identifierType) + "," + (this.source == 0 ? null : this.source) + "," + (this.manualEntryEnabled) + "," + (this.automaticGenerationEnabled) + "," + (this.location == 0 ? null : this.location); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.identifierType != 0) return true;
		if (this.location != 0) return true;
		if (this.source != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.PatientIdentifierTypeVO.class, this.identifierType, false, conn); 
		this.identifierType = 0;
		if (parentOnDestination  != null) this.identifierType = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.LocationVO.class, this.location, true, conn); 
		this.location = 0;
		if (parentOnDestination  != null) this.location = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.IdgenIdentifierSourceVO.class, this.source, false, conn); 
		this.source = 0;
		if (parentOnDestination  != null) this.source = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("identifierType")) return this.identifierType;		
		if (parentAttName.equals("location")) return this.location;		
		if (parentAttName.equals("source")) return this.source;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}