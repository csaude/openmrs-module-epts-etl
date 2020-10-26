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
 
public class ConceptWordVO extends AbstractOpenMRSObject implements OpenMRSObject { 
	private int conceptWordId;
	private int conceptId;
	private String word;
	private String locale;
	private int conceptNameId;
	private double weight;
 
	public ConceptWordVO() { 
		this.metadata = false;
	} 
 
	public void setConceptWordId(int conceptWordId){ 
	 	this.conceptWordId = conceptWordId;
	}
 
	public int getConceptWordId(){ 
		return this.conceptWordId;
	}
 
	public void setConceptId(int conceptId){ 
	 	this.conceptId = conceptId;
	}
 
	public int getConceptId(){ 
		return this.conceptId;
	}
 
	public void setWord(String word){ 
	 	this.word = word;
	}
 
	public String getWord(){ 
		return this.word;
	}
 
	public void setLocale(String locale){ 
	 	this.locale = locale;
	}
 
	public String getLocale(){ 
		return this.locale;
	}
 
	public void setConceptNameId(int conceptNameId){ 
	 	this.conceptNameId = conceptNameId;
	}
 
	public int getConceptNameId(){ 
		return this.conceptNameId;
	}
 
	public void setWeight(double weight){ 
	 	this.weight = weight;
	}


 
	public double getWeight(){ 
		return this.weight;
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
 		return this.conceptWordId; 
	} 
 
	public void setObjectId(int selfId){ 
		this.conceptWordId = selfId; 
	} 
 
	public void load(ResultSet rs) throws SQLException{ 
		this.conceptWordId = rs.getInt("concept_word_id");
		this.conceptId = rs.getInt("concept_id");
		this.word = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("word") != null ? rs.getString("word").trim() : null);
		this.locale = AttDefinedElements.removeStrangeCharactersOnString(rs.getString("locale") != null ? rs.getString("locale").trim() : null);
		this.conceptNameId = rs.getInt("concept_name_id");
		this.weight = rs.getDouble("weight");
	} 
 
	@JsonIgnore
	public String generateDBPrimaryKeyAtt(){ 
 		return "concept_word_id"; 
	} 
 
	@JsonIgnore
	public Object[]  getInsertParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.word, this.locale, this.conceptNameId == 0 ? null : this.conceptNameId, this.weight};		return params; 
	} 
 
	@JsonIgnore
	public Object[]  getUpdateParams(){ 
 		Object[] params = {this.conceptId == 0 ? null : this.conceptId, this.word, this.locale, this.conceptNameId == 0 ? null : this.conceptNameId, this.weight, this.conceptWordId};		return params; 
	} 
 
	@JsonIgnore
	public String getInsertSQL(){ 
 		return "INSERT INTO concept_word(concept_id, word, locale, concept_name_id, weight) VALUES(?, ?, ?, ?, ?);"; 
	} 
 
	@JsonIgnore
	public String getUpdateSQL(){ 
 		return "UPDATE concept_word SET concept_id = ?, word = ?, locale = ?, concept_name_id = ?, weight = ? WHERE concept_word_id = ?;"; 
	} 
 
	@JsonIgnore
	public String generateInsertValues(){ 
 		return ""+(this.conceptId == 0 ? null : this.conceptId) + "," + (this.word != null ? "\""+ utilities.scapeQuotationMarks(word)  +"\"" : null) + "," + (this.locale != null ? "\""+ utilities.scapeQuotationMarks(locale)  +"\"" : null) + "," + (this.conceptNameId == 0 ? null : this.conceptNameId) + "," + (this.weight); 
	} 
 
	@Override
	public boolean isGeneratedFromSkeletonClass() {
		return false;
	}

	@Override
	public boolean hasParents() {
		if (this.conceptId != 0) return true;
		if (this.conceptNameId != 0) return true;
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
 
		parentOnDestination = loadParent(org.openmrs.module.eptssync.model.openmrs.destinationpkg.ConceptNameVO.class, this.conceptNameId, false, conn); 
		this.conceptNameId = 0;
		if (parentOnDestination  != null) this.conceptNameId = parentOnDestination.getObjectId();
 
	}

	@Override
	public int getParentValue(String parentAttName) {		
		if (parentAttName.equals("conceptId")) return this.conceptId;		
		if (parentAttName.equals("conceptNameId")) return this.conceptNameId;

		throw new RuntimeException("No found parent for: " + parentAttName);	}


}