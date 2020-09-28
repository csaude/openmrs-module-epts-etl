package org.openmrs.module.eptssync.model.openmrs.sourcepkg; 
 
import org.openmrs.module.eptssync.model.openmrs.generic.*; 
 
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities; 
 
import org.openmrs.module.eptssync.utilities.db.conn.DBException; 
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException; 
 
import java.sql.Connection; 
import java.sql.SQLException; 
import java.sql.ResultSet; 
 
import com.fasterxml.jackson.annotation.JsonIgnore; 
 
public class DrugIngredientVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int drugId;
	private int ingredientId;
	private String uuid;
	private double strength;
	private int units;
 
	public DrugIngredientVO() { 
		this.metadata = false;
	} 
 
	public void setDrugId(int drugId){ 
	 	this.drugId = drugId;
	}
 
	public int getDrugId(){ 
		return this.drugId;
	}
 
	public void setIngredientId(int ingredientId){ 
	 	this.ingredientId = ingredientId;
	}
 
	public int getIngredientId(){ 
		return this.ingredientId;
	}
 
	public void setUuid(String uuid){ 
	 	this.uuid = uuid;
	}
 
	public String getUuid(){ 
		return this.uuid;
	}
 
	public void setStrength(double strength){ 
	 	this.strength = strength;
	}
 
	public double getStrength(){ 
		return this.strength;
	}
 
	public void setUnits(int units){ 
	 	this.units = units;
	}


 
	public int getUnits(){ 
		return this.units;
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
 		return this.drugId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.drugId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.drugId = rs.getInt("drug_id");
		this.ingredientId = rs.getInt("ingredient_id");
		this.uuid = rs.getString("uuid") != null ? rs.getString("uuid").trim() : null;
		this.strength = rs.getDouble("strength");
			} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "drug_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.ingredientId == 0 ? null : this.ingredientId, this.uuid, this.strength, this.units == 0 ? null : this.units};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.ingredientId == 0 ? null : this.ingredientId, this.uuid, this.strength, this.units == 0 ? null : this.units, this.drugId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO drug_ingredient(ingredient_id, uuid, strength, units) VALUES(?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE drug_ingredient SET ingredient_id = ?, uuid = ?, strength = ?, units = ? WHERE drug_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.ingredientId == 0 ? null : this.ingredientId) + "," + (this.uuid != null ? "\""+uuid+"\"" : null) + "," + (this.strength) + "," + (this.units == 0 ? null : this.units); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.drugId != 0) return true;
		if (this.ingredientId != 0) return true;
		if (this.units != 0) return true;
		return false;
	}

	@Override
	public int retrieveSharedPKKey(Connection conn) throws ParentNotYetMigratedException, DBException {
		throw new RuntimeException("No PKSharedInfo defined!");	}

	@Override
	public void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject parentOnDestination = null;
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.DrugVO.class, this.drugId, false, conn); 
		this.drugId = 0;
		if (parentOnDestination  != null) this.drugId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.ingredientId, false, conn); 
		this.ingredientId = 0;
		if (parentOnDestination  != null) this.ingredientId = parentOnDestination.getObjectId();
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.sourcepkg.ConceptVO.class, this.units, true, conn); 
		this.units = 0;
		if (parentOnDestination  != null) this.units = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("drugId")) return this.drugId;		
		if (parentAttName.equals("ingredientId")) return this.ingredientId;		
		if (parentAttName.equals("units")) return this.units;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}