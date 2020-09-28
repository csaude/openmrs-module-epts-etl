package org.openmrs.module.eptssync.model.openmrs.destinationpkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class ConceptNumericVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptId;
	private double hiAbsolute;
	private double hiCritical;
	private double hiNormal;
	private double lowAbsolute;
	private double lowCritical;
	private double lowNormal;
	private String units;
	private byte precise;
	private int displayPrecision;
 
	public ConceptNumericVO() { 
		this.metadata = false;
	} 
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setHiAbsolute(double hiAbsolute){ 
	 	this.hiAbsolute = hiAbsolute;
	}
 
	public double getHiAbsolute(){ 
		return this.hiAbsolute;
	}
 
	public void setHiCritical(double hiCritical){ 
	 	this.hiCritical = hiCritical;
	}
 
	public double getHiCritical(){ 
		return this.hiCritical;
	}
 
	public void setHiNormal(double hiNormal){ 
	 	this.hiNormal = hiNormal;
	}
 
	public double getHiNormal(){ 
		return this.hiNormal;
	}
 
	public void setLowAbsolute(double lowAbsolute){ 
	 	this.lowAbsolute = lowAbsolute;
	}
 
	public double getLowAbsolute(){ 
		return this.lowAbsolute;
	}
 
	public void setLowCritical(double lowCritical){ 
	 	this.lowCritical = lowCritical;
	}
 
	public double getLowCritical(){ 
		return this.lowCritical;
	}
 
	public void setLowNormal(double lowNormal){ 
	 	this.lowNormal = lowNormal;
	}
 
	public double getLowNormal(){ 
		return this.lowNormal;
	}
 
	public void setUnits(String units){ 
	 	this.units = units;
	}
 
	public String getUnits(){ 
		return this.units;
	}
 
	public void setPrecise(byte precise){ 
	 	this.precise = precise;
	}
 
	public byte getPrecise(){ 
		return this.precise;
	}
 
	public void setDisplayPrecision(int displayPrecision){ 
	 	this.displayPrecision = displayPrecision;
	}


 
	public int getDisplayPrecision(){ 
		return this.displayPrecision;
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
		this.hiAbsolute = rs.getDouble("hi_absolute");
		this.hiCritical = rs.getDouble("hi_critical");
		this.hiNormal = rs.getDouble("hi_normal");
		this.lowAbsolute = rs.getDouble("low_absolute");
		this.lowCritical = rs.getDouble("low_critical");
		this.lowNormal = rs.getDouble("low_normal");
		this.units = rs.getString("units") != null ? rs.getString("units").trim() : null;
		this.precise = rs.getByte("precise");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.hiAbsolute, this.hiCritical, this.hiNormal, this.lowAbsolute, this.lowCritical, this.lowNormal, this.units, this.precise, this.displayPrecision};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.hiAbsolute, this.hiCritical, this.hiNormal, this.lowAbsolute, this.lowCritical, this.lowNormal, this.units, this.precise, this.displayPrecision, this.conceptId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept_numeric(hi_absolute, hi_critical, hi_normal, low_absolute, low_critical, low_normal, units, precise, display_precision) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept_numeric SET hi_absolute = ?, hi_critical = ?, hi_normal = ?, low_absolute = ?, low_critical = ?, low_normal = ?, units = ?, precise = ?, display_precision = ? WHERE concept_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.hiAbsolute) + "," + (this.hiCritical) + "," + (this.hiNormal) + "," + (this.lowAbsolute) + "," + (this.lowCritical) + "," + (this.lowNormal) + "," + (this.units != null ? "\""+units+"\"" : null) + "," + (this.precise) + "," + (this.displayPrecision); 
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