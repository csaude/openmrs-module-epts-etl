package org.openmrs.module.epts.etl.conf.interfaces;

import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlField;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;

public interface EtlDataSource extends DatabaseObjectConfiguration {
	
	String getName();
	
	default void loadOwnFieldsToEtlFields(List<EtlField> etlFields, boolean presereOriginalNames) {
		if (etlFields == null)
			throw new ForbiddenOperationException("The 'etlFields' is null");
		
		if (this instanceof MainJoiningEntity) {
			MainJoiningEntity dsAsJoining = (MainJoiningEntity) this;
			
			if (!dsAsJoining.doNotUseAsDatasource()) {
				etlFields.addAll(EtlField.converteFromDataSourceFields(this, presereOriginalNames));
			}
			
			if (dsAsJoining.hasAuxExtractTable()) {
				for (JoinableEntity j : dsAsJoining.getJoiningTable()) {
					if (!j.doNotUseAsDatasource()) {
						j.loadOwnFieldsToEtlFields(etlFields, false);
					}
				}
			}
		} else {
			etlFields.addAll(EtlField.converteFromDataSourceFields(this, presereOriginalNames));
		}
	}
}
