package org.openmrs.module.epts.etl.utilities.tools;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class TestEtlObject extends AbstractDatabaseObject {
	
	@Override
	public List<? extends EtlDatabaseObject> getAuxLoadObject() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object[] getInsertParamsWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getInsertSQLWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object[] getInsertParamsWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getInsertSQLWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getUpdateSQL() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object[] getUpdateParams() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateInsertValuesWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateInsertValuesWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithObjectId(String insertQuestionMarks) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getInsertSQLQuestionMarksWithObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setInsertSQLQuestionMarksWithoutObjectId(String insertQuestionMarks) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getInsertSQLQuestionMarksWithoutObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean hasParents() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Object getParentValue(ParentTable refInfo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateFullFilledUpdateSql() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public EtlDatabaseObject getSharedPkObj() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setSharedPkObj(EtlDatabaseObject sharedPkObj) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadWithDefaultValues(Connection conn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void copyFrom(EtlDatabaseObject parentRecordInOrigin) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Object getFieldValue(String paramName) throws ForbiddenOperationException {
		switch (paramName) {
			case "a":
				return "25"; // For @a
			case "b":
				return "'male'"; // For @b
			case "tableName":
				return "persons"; // For @tableName
			case "columnName":
				return "id"; // For @columnName
			default:
				return ""; // Return empty string if no match found
		}
	}
	
	@Override
	public void tryToReplaceFieldWithKey(Key k) {
		// TODO Auto-generated method stub
		
	}
	
}
