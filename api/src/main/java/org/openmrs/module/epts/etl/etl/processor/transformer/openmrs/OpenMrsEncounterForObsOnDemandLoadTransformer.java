package org.openmrs.module.epts.etl.etl.processor.transformer.openmrs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.FieldTransformingInfo;
import org.openmrs.module.epts.etl.etl.processor.transformer.ParentOnDemandLoadTransformer;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class OpenMrsEncounterForObsOnDemandLoadTransformer extends ParentOnDemandLoadTransformer {
	
	String encounterTypeParam;
	
	String encounterTypeId;
	
	public OpenMrsEncounterForObsOnDemandLoadTransformer(String encounterTypeParam, List<String> params,
	    TransformableField field, DstConf relatedDstConf) {
		super("encounter", "encounter_id", field, params, relatedDstConf);
		
		this.encounterTypeParam = encounterTypeParam;
		
		String[] paramElements = encounterTypeParam.split(":", 2);
		
		encounterTypeId = paramElements[1];
		
		setIgnorableFields(utilities.parseToList("date_changed", "changed_by"));
	}
	
	public String getTransformerDsc() {
		return "OpenMrsEncounterOnDemandLoadTransformer: (" + this.encounterTypeParam + ")";
	}
	
	public static ParentOnDemandLoadTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field) {
		
		if (parameters == null || parameters.size() < 1) {
			throw new EtlExceptionImpl("A OpenMrsEncounterOnDemandLoadTransformer needs at least 1 parameter.\n"
			        + "Eg: OpenMrsEncounterOnDemandLoadTransformer(encounter_type:encounter_type_id)");
		}
		
		/*
		if (parameters.size() > 1) {
			throw new EtlExceptionImpl(
			        "A OpenMrsEncounterOnDemandLoadTransformer support only one parameter (encounter_type_id).\n"
			                + "Eg: OpenMrsEncounterOnDemandLoadTransformer(encounter_type_id:encounter_type_id)");
			
		}*/
		
		String encounterTypeParam = validateParam(parameters);
		
		String parentTable = "encounter";
		String parentTableField = "encounter_id";
		
		parameters.remove(0);
		
		List<String> defaultObjectData = generateParams(encounterTypeParam, parameters);
		
		String key = buildCacheKey(parentTable, parentTableField, defaultObjectData);
		
		return INSTANCES.computeIfAbsent(key, k -> new OpenMrsEncounterForObsOnDemandLoadTransformer(encounterTypeParam,
		        defaultObjectData, field, relatedDstConf));
	}
	
	private static List<String> generateParams(String encounterTypeParam, List<Object> otherParameters) {
		List<String> defaultObjectData = new ArrayList<>();
		
		defaultObjectData.add(encounterTypeParam);
		defaultObjectData.add("encounter_datetime:obs_datetime");
		defaultObjectData.add("patient_id:person_id");
		
		for (Object obj : otherParameters) {
			Integer pos = getParameterPos(defaultObjectData, obj.toString());
			
			if (pos != null) {
				defaultObjectData.remove(pos.intValue());
			}
			
			defaultObjectData.add(obj.toString());
		}
		
		return defaultObjectData;
	}
	
	private static Integer getParameterPos(List<String> parmeters, String param) {
		for (int i = 0; i < parmeters.size(); i++) {
			String pInfo = parmeters.get(i);
			
			String pName = pInfo.split(":", 2)[0];
			
			if (param.startsWith(pName)) {
				return i;
			}
		}
		
		return null;
	}
	
	private static String validateParam(List<Object> parameters) {
		String encounterTypeParam = parameters.get(0).toString();
		
		String[] paramElements = encounterTypeParam.split(":", 2);
		
		if (paramElements.length != 2 || !paramElements[0].equals("encounter_type")) {
			throw new EtlExceptionImpl("Wrong format parameter within the OpenMrsEncounterOnDemandLoadTransformer("
			        + encounterTypeParam + ")\n" + "The param must be specified as encounter_type:encounter_type_id_value");
		}
		return encounterTypeParam;
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		EtlDatabaseObject existingEncounterOnEncounterDate = retrieveExistingEncounter(srcObject, srcConn, dstConn);
		
		if (existingEncounterOnEncounterDate != null) {
			return new FieldTransformingInfo(field, existingEncounterOnEncounterDate.getObjectId().asSimpleNumericValue(),
			        (EtlDataSource) existingEncounterOnEncounterDate.getRelatedConfiguration());
		} else {
			srcObject.setFieldValue("encounter_id", null);
			
			return super.transform(processor, srcObject, transformedRecord, additionalSrcObjects, field, srcConn, dstConn);
		}
	}
	
	private EtlDatabaseObject retrieveExistingEncounter(EtlDatabaseObject obs, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		tryToInitDstConfForNonExistingSrcParent(srcConn, dstConn);
		SrcConf srcConf = (SrcConf) obs.getRelatedConfiguration();
		
		String condition = "patient_id = ? and encounter_datetime = ? and encounter_type = ?";
		Object[] params = new Object[3];
		
		ParentTable refInfo = getRelatedDstConf().findParentRefInfoByField("person_id");
		
		params[0] = obs.getFieldValue("person_id");
		params[1] = obs.getFieldValue("obs_datetime");
		params[2] = this.encounterTypeId;
		
		EtlDatabaseObject parentInSrc = obs.retrieveParentInSrcUsingDstParentInfo(refInfo, srcConf, srcConn);
		
		EtlDatabaseObject parentInDst = null;
		
		if (parentInSrc != null) {
			parentInDst = obs.retrieveParentInDestination(refInfo, parentInSrc, dstConn);
		}
		
		if (parentInDst == null) {
			throw new EtlTransformationException("The patient " + params[0] + " of encounter "
			        + obs.getObjectId().asSimpleNumericValue() + " cannot be found on src db", obs,
			        ActionOnEtlException.ABORT);
		}
		
		params[0] = parentInDst.getObjectId().asSimpleNumericValue();
		
		return getDstConfForNonExistingSrcParent(srcConn, dstConn).find(condition, params, dstConn);
	}
	
}
