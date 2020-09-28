package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class DrugOrderVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int orderId;
	private int drugInventoryId;
	private double dose;
	private byte asNeeded;
	private String dosingType;
	private double quantity;
	private String asNeededCondition;
	private int numRefills;
	private String dosingInstructions;
	private int duration;
	private int durationUnits;
	private int quantityUnits;
	private int route;
	private int doseUnits;
	private int frequency;
	private String brandName;
	private byte dispenseAsWritten;
 
	public DrugOrderVO() { 
		this.metadata = false;
	} 
 
	public void setOrderId(int orderId){ 
	 	this.orderId = orderId;
	}
 
	public int getOrderId(){ 
		return this.orderId;
	}
 
	public void setDrugInventoryId(int drugInventoryId){ 
	 	this.drugInventoryId = drugInventoryId;
	}
 
	public int getDrugInventoryId(){ 
		return this.drugInventoryId;
	}
 
	public void setDose(double dose){ 
	 	this.dose = dose;
	}
 
	public double getDose(){ 
		return this.dose;
	}
 
	public void setAsNeeded(byte asNeeded){ 
	 	this.asNeeded = asNeeded;
	}
 
	public byte getAsNeeded(){ 
		return this.asNeeded;
	}
 
	public void setDosingType(String dosingType){ 
	 	this.dosingType = dosingType;
	}
 
	public String getDosingType(){ 
		return this.dosingType;
	}
 
	public void setQuantity(double quantity){ 
	 	this.quantity = quantity;
	}
 
	public double getQuantity(){ 
		return this.quantity;
	}
 
	public void setAsNeededCondition(String asNeededCondition){ 
	 	this.asNeededCondition = asNeededCondition;
	}
 
	public String getAsNeededCondition(){ 
		return this.asNeededCondition;
	}
 
	public void setNumRefills(int numRefills){ 
	 	this.numRefills = numRefills;
	}
 
	public int getNumRefills(){ 
		return this.numRefills;
	}
 
	public void setDosingInstructions(String dosingInstructions){ 
	 	this.dosingInstructions = dosingInstructions;
	}
 
	public String getDosingInstructions(){ 
		return this.dosingInstructions;
	}
 
	public void setDuration(int duration){ 
	 	this.duration = duration;
	}
 
	public int getDuration(){ 
		return this.duration;
	}
 
	public void setDurationUnits(int durationUnits){ 
	 	this.durationUnits = durationUnits;
	}
 
	public int getDurationUnits(){ 
		return this.durationUnits;
	}
 
	public void setQuantityUnits(int quantityUnits){ 
	 	this.quantityUnits = quantityUnits;
	}
 
	public int getQuantityUnits(){ 
		return this.quantityUnits;
	}
 
	public void setRoute(int route){ 
	 	this.route = route;
	}
 
	public int getRoute(){ 
		return this.route;
	}
 
	public void setDoseUnits(int doseUnits){ 
	 	this.doseUnits = doseUnits;
	}
 
	public int getDoseUnits(){ 
		return this.doseUnits;
	}
 
	public void setFrequency(int frequency){ 
	 	this.frequency = frequency;
	}
 
	public int getFrequency(){ 
		return this.frequency;
	}
 
	public void setBrandName(String brandName){ 
	 	this.brandName = brandName;
	}
 
	public String getBrandName(){ 
		return this.brandName;
	}
 
	public void setDispenseAsWritten(byte dispenseAsWritten){ 
	 	this.dispenseAsWritten = dispenseAsWritten;
	}


 
	public byte getDispenseAsWritten(){ 
		return this.dispenseAsWritten;
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
		this.drugInventoryId = rs.getInt("drug_inventory_id");
		this.dose = rs.getDouble("dose");
		this.asNeeded = rs.getByte("as_needed");
		this.dosingType = rs.getString("dosing_type") != null ? rs.getString("dosing_type").trim() : null;
		this.quantity = rs.getDouble("quantity");
		this.asNeededCondition = rs.getString("as_needed_condition") != null ? rs.getString("as_needed_condition").trim() : null;
		this.numRefills = rs.getInt("num_refills");
		this.dosingInstructions = rs.getString("dosing_instructions") != null ? rs.getString("dosing_instructions").trim() : null;
		this.duration = rs.getInt("duration");
		this.durationUnits = rs.getInt("duration_units");
		this.quantityUnits = rs.getInt("quantity_units");
		this.route = rs.getInt("route");
		this.doseUnits = rs.getInt("dose_units");
		this.frequency = rs.getInt("frequency");
		this.brandName = rs.getString("brand_name") != null ? rs.getString("brand_name").trim() : null;
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "order_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.drugInventoryId == 0 ? null : this.drugInventoryId, this.dose, this.asNeeded, this.dosingType, this.quantity, this.asNeededCondition, this.numRefills, this.dosingInstructions, this.duration, this.durationUnits == 0 ? null : this.durationUnits, this.quantityUnits == 0 ? null : this.quantityUnits, this.route == 0 ? null : this.route, this.doseUnits == 0 ? null : this.doseUnits, this.frequency == 0 ? null : this.frequency, this.brandName, this.dispenseAsWritten};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.drugInventoryId == 0 ? null : this.drugInventoryId, this.dose, this.asNeeded, this.dosingType, this.quantity, this.asNeededCondition, this.numRefills, this.dosingInstructions, this.duration, this.durationUnits == 0 ? null : this.durationUnits, this.quantityUnits == 0 ? null : this.quantityUnits, this.route == 0 ? null : this.route, this.doseUnits == 0 ? null : this.doseUnits, this.frequency == 0 ? null : this.frequency, this.brandName, this.dispenseAsWritten, this.orderId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO drug_order(drug_inventory_id, dose, as_needed, dosing_type, quantity, as_needed_condition, num_refills, dosing_instructions, duration, duration_units, quantity_units, route, dose_units, frequency, brand_name, dispense_as_written) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE drug_order SET drug_inventory_id = ?, dose = ?, as_needed = ?, dosing_type = ?, quantity = ?, as_needed_condition = ?, num_refills = ?, dosing_instructions = ?, duration = ?, duration_units = ?, quantity_units = ?, route = ?, dose_units = ?, frequency = ?, brand_name = ?, dispense_as_written = ? WHERE order_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.drugInventoryId == 0 ? null : this.drugInventoryId) + "," + (this.dose) + "," + (this.asNeeded) + "," + (this.dosingType != null ? "\""+dosingType+"\"" : null) + "," + (this.quantity) + "," + (this.asNeededCondition != null ? "\""+asNeededCondition+"\"" : null) + "," + (this.numRefills) + "," + (this.dosingInstructions != null ? "\""+dosingInstructions+"\"" : null) + "," + (this.duration) + "," + (this.durationUnits == 0 ? null : this.durationUnits) + "," + (this.quantityUnits == 0 ? null : this.quantityUnits) + "," + (this.route == 0 ? null : this.route) + "," + (this.doseUnits == 0 ? null : this.doseUnits) + "," + (this.frequency == 0 ? null : this.frequency) + "," + (this.brandName != null ? "\""+brandName+"\"" : null) + "," + (this.dispenseAsWritten); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.doseUnits != 0) return true;
		if (this.durationUnits != 0) return true;
		if (this.frequency != 0) return true;
		if (this.quantityUnits != 0) return true;
		if (this.route != 0) return true;
		if (this.orderId != 0) return true;
		if (this.drugInventoryId != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.doseUnits, true, conn); 
		this.doseUnits = 0;
		if (parentOnDestination  != null) this.doseUnits = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.durationUnits, true, conn); 
		this.durationUnits = 0;
		if (parentOnDestination  != null) this.durationUnits = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.OrderFrequencyVO.class, this.frequency, true, conn); 
		this.frequency = 0;
		if (parentOnDestination  != null) this.frequency = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.quantityUnits, true, conn); 
		this.quantityUnits = 0;
		if (parentOnDestination  != null) this.quantityUnits = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.route, true, conn); 
		this.route = 0;
		if (parentOnDestination  != null) this.route = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.OrdersVO.class, this.orderId, false, conn); 
		this.orderId = 0;
		if (parentOnDestination  != null) this.orderId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.DrugVO.class, this.drugInventoryId, true, conn); 
		this.drugInventoryId = 0;
		if (parentOnDestination  != null) this.drugInventoryId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("doseUnits")) return this.doseUnits;		
		if (parentAttName.equals("durationUnits")) return this.durationUnits;		
		if (parentAttName.equals("frequency")) return this.frequency;		
		if (parentAttName.equals("quantityUnits")) return this.quantityUnits;		
		if (parentAttName.equals("route")) return this.route;		
		if (parentAttName.equals("orderId")) return this.orderId;		
		if (parentAttName.equals("drugInventoryId")) return this.drugInventoryId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}