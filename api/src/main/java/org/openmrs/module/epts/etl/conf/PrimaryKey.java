package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;

public class PrimaryKey extends UniqueKeyInfo {
	
	public PrimaryKey() {
	}
	
	public PrimaryKey(TableConfiguration tabConf) {
		super(tabConf);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		PrimaryKey cloned = new PrimaryKey();
		
		cloned.copy(this);
		
		return cloned;
	}
	
	public boolean isSimpleNumericKey() {
		return isSimpleKey() && retrieveSimpleKey().isNumericColumnType();
	}
	
	public boolean isSimpleKey() {
		return !isCompositeKey();
	}
	
	public Oid generateOid(EtlDatabaseObject obj) {
		Oid oid = new Oid();
		
		for (Key key : this.getFields()) {
			Object keyValue;
			
			try {
				keyValue = obj.getFieldValue(key.getName());
			}
			catch (ForbiddenOperationException e) {
				keyValue = obj.getFieldValue(key.getNameAsClassAtt());
			}
			
			oid.addKey(new Key(key.getName(), key.getType(), keyValue));
		}
		
		return oid;
	}
	
	public Oid generateDefaultOid(AbstractTableConfiguration config) {
		
		Oid oid = new Oid();
		
		for (Key key : this.getFields()) {
			oid.addKey(key.createACopyWithDefaultValue());
		}
		
		return oid;
		
	}
	
}
