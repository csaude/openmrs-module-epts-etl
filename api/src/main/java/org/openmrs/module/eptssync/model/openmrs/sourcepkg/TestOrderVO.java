package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class TestOrderVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int orderId;
	private int specimenSource;
	private String laterality;
	private String clinicalHistory;
	private int frequency;
	private int numberOfRepeats;
 
	public TestOrderVO() { 
		this.metadata = false;
	} 
 
	public void setOrderId(int orderId){ 
	 	this.orderId = orderId;
	}
 
	public int getOrderId(){ 
		return this.orderId;
	}
 
	public void setSpecimenSource(int specimenSource){ 
	 	this.specimenSource = specimenSource;
	}
 
	public int getSpecimenSource(){ 
		return this.specimenSource;
	}
 
	public void setLaterality(String laterality){ 
	 	this.laterality = laterality;
	}
 
	public String getLaterality(){ 
		return this.laterality;
	}
 
	public void setClinicalHistory(String clinicalHistory){ 
	 	this.clinicalHistory = clinicalHistory;
	}
 
	public String getClinicalHistory(){ 
		return this.clinicalHistory;
	}
 
	public void setFrequency(int frequency){ 
	 	this.frequency = frequency;
	}
 
	public int getFrequency(){ 
		return this.frequency;
	}
 
	public void setNumberOfRepeats(int numberOfRepeats){ 
	 	this.numberOfRepeats = numberOfRepeats;
	}


 
	public int getNumberOfRepeats(){ 
		return this.numberOfRepeats;
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
 		return this.orderId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.orderId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.orderId = rs.getInt("order_id");
		this.specimenSource = rs.getInt("specimen_source");
		this.laterality = rs.getString("laterality") != null ? rs.getString("laterality").trim() : null;
		this.clinicalHistory = rs.getString("clinical_history") != null ? rs.getString("clinical_history").trim() : null;
		this.frequency = rs.getInt("frequency");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "order_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.specimenSource == 0 ? null : this.specimenSource, this.laterality, this.clinicalHistory, this.frequency == 0 ? null : this.frequency, this.numberOfRepeats};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.specimenSource == 0 ? null : this.specimenSource, this.laterality, this.clinicalHistory, this.frequency == 0 ? null : this.frequency, this.numberOfRepeats, this.orderId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO test_order(specimen_source, laterality, clinical_history, frequency, number_of_repeats) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE test_order SET specimen_source = ?, laterality = ?, clinical_history = ?, frequency = ?, number_of_repeats = ? WHERE order_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.specimenSource == 0 ? null : this.specimenSource) + "," + (this.laterality != null ? "\""+laterality+"\"" : null) + "," + (this.clinicalHistory != null ? "\""+clinicalHistory+"\"" : null) + "," + (this.frequency == 0 ? null : this.frequency) + "," + (this.numberOfRepeats); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.frequency != 0) return true;
		if (this.orderId != 0) return true;
		if (this.specimenSource != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.OrderFrequencyVO.class, this.frequency, true, conn); 
		this.frequency = 0;
		if (parentOnDestination  != null) this.frequency = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.OrdersVO.class, this.orderId, false, conn); 
		this.orderId = 0;
		if (parentOnDestination  != null) this.orderId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.specimenSource, true, conn); 
		this.specimenSource = 0;
		if (parentOnDestination  != null) this.specimenSource = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("frequency")) return this.frequency;		
		if (parentAttName.equals("orderId")) return this.orderId;		
		if (parentAttName.equals("specimenSource")) return this.specimenSource;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}