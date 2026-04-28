package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.datasource.SqlConditionElement;
import org.openmrs.module.epts.etl.conf.interfaces.EtlTranformTarget;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.SQLUtilities;

public class SqlConditionData {
	
	public static final CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String parametrizedCondition;
	
	private List<SqlConditionElement> conditionElements;
	
	public SqlConditionData() {
		
	}
	
	public SqlConditionData(List<SqlConditionElement> conditionElements, String parametrizedCondition) {
		
	}
	
	public String getParametrizedCondition() {
		return parametrizedCondition;
	}
	
	public void setParametrizedCondition(String parametrizedCondition) {
		this.parametrizedCondition = parametrizedCondition;
	}
	
	public List<SqlConditionElement> getConditionElements() {
		return conditionElements;
	}
	
	public void setConditionElements(List<SqlConditionElement> conditionElements) {
		this.conditionElements = conditionElements;
	}
	
	public static SqlConditionData initializeDynamicSqlConditionElements(EtlTranformTarget transformTarget, String sql,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		String parametrizedOnDemandCheckCondition = sql;
		
		if (utilities.stringHasValue(sql)) {
			//We force sql to be a full query so that can be correctly be processed by extractSqlConditionElements method
			String query = "select * from x where " + sql;
			
			List<SqlConditionElement> elements = SQLUtilities.extractSqlConditionElements(query);
			
			List<SqlConditionElement> toRemove = new ArrayList<>();
			
			for (SqlConditionElement field : elements) {
				
				//We want to skip same situation where a field is compared to a subqueries
				if (!SQLUtilities.isValidSqlCondition(field.toString())) {
					toRemove.add(field);
					
					continue;
				}
				
				field.fullLoad(transformTarget, dstConn);
				
				String regex = "\\b" + Pattern.quote(field.getField()) + "\\s*" + Pattern.quote(field.getOperator()) + "\\s*"
				        + Pattern.quote(field.getValue()) + "\\b";
				
				parametrizedOnDemandCheckCondition = parametrizedOnDemandCheckCondition.replaceAll(regex,
				    field.getField() + " " + field.getOperator() + " ?");
			}
			
			elements.removeAll(toRemove);
			
			return new SqlConditionData(elements, parametrizedOnDemandCheckCondition);
		}
		
		return null;
		
	}
	
}
