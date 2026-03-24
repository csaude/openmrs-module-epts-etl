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

public class OpenMrsVisitOnDemandLoadTransformer extends ParentOnDemandLoadTransformer {
	
	String visitTypeParam;
	
	public OpenMrsVisitOnDemandLoadTransformer(String visitTypeParam, List<String> params, TransformableField field,
	    DstConf relatedDstConf) {
		super("visit", "visit_id", field, params, relatedDstConf);
		
		this.visitTypeParam = visitTypeParam;
	}
	
	public String getTransformerDsc() {
		return "OpenMrsVisitOnDemandLoadTransformer: (" + this.visitTypeParam + ")";
	}
	
	public static ParentOnDemandLoadTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field) {
		
		if (parameters == null || parameters.size() < 1) {
			throw new EtlExceptionImpl("A OpenMrsVisitOnDemandLoadTransformer needs at least 1 parameter.\n"
			        + "Eg: OpenMrsVisitOnDemandLoadTransformer(visit_type_id:visit_type_id)");
		}
		
		if (parameters.size() > 1) {
			throw new EtlExceptionImpl("A OpenMrsVisitOnDemandLoadTransformer support only one parameter (visit_type_id).\n"
			        + "Eg: OpenMrsVisitOnDemandLoadTransformer(visit_type_id:visit_type_id)");
			
		}
		
		String visitTypeParam = validateParam(parameters);
		
		String parentTable = "visit";
		String parentTableField = "visit_id";
		
		List<String> defaultObjectData = generateParams(visitTypeParam);
		
		String key = buildCacheKey(parentTable, parentTableField, defaultObjectData);
		
		return INSTANCES.computeIfAbsent(key,
		    k -> new OpenMrsVisitOnDemandLoadTransformer(visitTypeParam, defaultObjectData, field, relatedDstConf));
	}
	
	private static List<String> generateParams(String visitTypeParam) {
		List<String> defaultObjectData = new ArrayList<>();
		
		defaultObjectData.add("date_started:encounter_datetime");
		defaultObjectData.add(visitTypeParam);
		defaultObjectData.add("date_stopped:");
		defaultObjectData.add("indication_concept_id:");
		return defaultObjectData;
	}
	
	private static String validateParam(List<Object> parameters) {
		String visitTypeParam = parameters.get(0).toString();
		
		String[] paramElements = visitTypeParam.split(":", 2);
		
		if (paramElements.length != 2 || !paramElements[0].equals("visit_type_id")) {
			throw new EtlExceptionImpl("Wrong format parameter within the OpenMrsVisitOnDemandLoadTransformer("
			        + visitTypeParam + ")\n" + "The param must be specified as visit_type_id:visit_type_id_value");
		}
		return visitTypeParam;
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		try {
			return super.transform(processor, srcObject, transformedRecord, additionalSrcObjects, field, srcConn, dstConn);
		}
		catch (EtlTransformationException e) {
			if (e.getMessage().contains("does not represent a valid Src Object within")) {
				
				//Retrieve the patient visit on encounter date
				EtlDatabaseObject existingVisitOnEncounterDate = retrieveExistingVisit(srcObject, srcConn, dstConn);
				
				if (existingVisitOnEncounterDate != null) {
					return new FieldTransformingInfo(field,
					        existingVisitOnEncounterDate.getObjectId().asSimpleNumericValue(),
					        (EtlDataSource) existingVisitOnEncounterDate.getRelatedConfiguration());
				} else {
					//Means inconsistency with visit, so ignore it and create new visit
					
					srcObject.setFieldValue("visit_id", null);
					
					//Re-transform with empty visit_id
					
					return super.transform(processor, srcObject, transformedRecord, additionalSrcObjects, field, srcConn,
					    dstConn);
				}
			} else
				throw e;
		}
	}
	
	private EtlDatabaseObject retrieveExistingVisit(EtlDatabaseObject encouter, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		tryToInitDstConfForNonExistingSrcParent(srcConn, dstConn);
		SrcConf srcConf = (SrcConf) encouter.getRelatedConfiguration();
		
		String condition = "patient_id = ? and date_started = ?";
		Object[] params = new Object[2];
		
		ParentTable refInfo = getRelatedDstConf().findParentRefInfoByField("patient_id");
		
		params[0] = encouter.getFieldValue("patient_id");
		params[1] = encouter.getFieldValue("encounter_datetime");
		
		EtlDatabaseObject parentInSrc = encouter.retrieveParentInSrcUsingDstParentInfo(refInfo, srcConf, srcConn);
		
		EtlDatabaseObject parentInDst = null;
		
		if (parentInSrc != null) {
			parentInDst = encouter.retrieveParentInDestination(refInfo, parentInSrc, dstConn);
		}
		
		if (parentInDst == null) {
			throw new EtlTransformationException("The patient " + params[0] + " of encounter "
			        + encouter.getObjectId().asSimpleNumericValue() + " cannot be found on src db", encouter,
			        ActionOnEtlException.ABORT);
		}
		
		params[0] = parentInDst.getObjectId().asSimpleNumericValue();
		
		return getDstConfForNonExistingSrcParent(srcConn, dstConn).find(condition, params, dstConn);
	}
	
}
