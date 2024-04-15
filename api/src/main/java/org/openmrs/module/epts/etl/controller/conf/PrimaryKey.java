package org.openmrs.module.epts.etl.controller.conf;

import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;

public class PrimaryKey extends UniqueKeyInfo {
	
	public boolean isSimpleNumericKey() {
		return !isCompositeKey() && retrieveSimpleKey().isNumericColumnType();
	}
	
	public Oid generateOid(DatabaseObject obj) {
		Oid oid = new Oid();
		
		for (Key key : this.getFields()) {
			oid.addKey(new Key(key.getName(), key.getType(), obj.getFieldValue(key.getNameAsClassAtt())));
		}
		
		return oid;
	}
	
}
