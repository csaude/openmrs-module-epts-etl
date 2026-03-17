package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
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
			Field field = null;
			
			try {
				field  = obj.getField(key.getName());
			}
			catch (ForbiddenOperationException e) {
				field = obj.getField(key.getNameAsClassAtt());
			}
			
			Key k = new Key(key.getName(), key.getDataType(), field.getValue());
			k.setTransformingInfo(field.getTransformingInfo());
			
			oid.addKey(k);
			
			obj.tryToReplaceFieldWithKey(k);
		}
		
		return oid;
	}
	
	public Oid generateDefaultOid(TableConfiguration config) {
		
		Oid oid = new Oid();
		
		for (Key key : this.getFields()) {
			oid.addKey(key.createACopyWithDefaultValue());
		}
		
		return oid;
		
	}
	
}
