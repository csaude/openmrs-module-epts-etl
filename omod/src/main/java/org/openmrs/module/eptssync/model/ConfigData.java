package org.openmrs.module.eptssync.model;

public class ConfigData {
	public static String generateDefaultSourcetConfig() {
		return	
				"{\n"+
					"installationType: \"source\",\n"+
					
					/*"syncRootDirectory: \"D:/JEE/Workspace/FGH/data/sync\",\n"+
					"classPath: \"D:/JEE/Workspace/FGH/openmrs-module-eptssync/api/target/eptssync-api-1.0-SNAPSHOT.jar\",\n"+
					"childConfigFilePath: \"D:/JEE/Workspace/FGH/openmrs-module-eptssync/api/target/conf/dest_sync_config.json\",\n"+*/
					
					"syncRootDirectory: \"/home/jpboane/working/prg/jee/workspace/data/sync\",\n"+
					"classPath: \"/home/jpboane/working/prg/jee/workspace/w02/openmrs-module-eptssync/api/target/eptssync-api-1.0-SNAPSHOT.jar\",\n"+
					//"childConfigFilePath: \"/home/jpboane/working/prg/jee/workspace/w02/openmrs-module-eptssync/api/target/conf/dest_sync_config.json\",\n"+
					
					"firstExport: true,\n"+
					"automaticStart: true, \n"+
					
					"connInfo: {\n"+
						"dataBaseUserName: \"root\",\n"+
						"dataBaseUserPassword: \"root\",\n"+
						"connectionURI: \"jdbc:mysql://10.10.2.2:3307/openmrs_morrumbala?autoReconnect=true&useSSL=false\",\n"+
						"driveClassName: \"com.mysql.jdbc.Driver\"\n"+
					"},\n"+
					
					"operations: [\n"+
						"{\n"+
							"operationType: \"database_preparation\",\n"+
							"maxRecordPerProcessing: 1,\n"+
							"maxSupportedEngines: 1,\n"+
							"minRecordsPerEngine: 1,\n"+
							"disabled: false,\n"+
							"processingMode: \"parallel\",\n"+
							
							"child: {\n"+
								"operationType: \"pojo_generation\",\n"+
								"maxRecordPerProcessing: 1,\n"+
								"maxSupportedEngines: 1,\n"+
								"minRecordsPerEngine: 1,\n"+
								"disabled: false,\n"+
								"processingMode: \"sequencial\",\n"+
								
								"child: {\n"+
										"operationType: \"inconsistency_solver\",\n"+
										"maxRecordPerProcessing: 1000, \n"+
										"maxSupportedEngines: 15,\n"+
										"minRecordsPerEngine: 1000,\n"+
										"disabled: false,\n"+
										"doIntegrityCheckInTheEnd: true,\n"+
										"processingMode: \"sequencial\",\n"+
								
										"child:	{\n"+
											"operationType: \"export\",\n"+
											"maxRecordPerProcessing: 1000,\n"+
											"maxSupportedEngines: 15,\n"+
											"minRecordsPerEngine: 100,\n"+
											"disabled: false,\n"+
											"processingMode: \"sequencial\",\n"+
													
											"child:{\n"+
												"operationType: \"transport\",\n"+
												"maxRecordPerProcessing: 1000,\n"+
												"maxSupportedEngines: 15,\n"+
												"minRecordsPerEngine: 50,\n"+
												"disabled: false,\n"+
												"processingMode: \"sequencial\"\n"+
											"}\n"+
										"}\n"+
									
								"}\n"+
							"}\n"+
						"}\n"+
					"]," + generateConfigTables() +
				"}";
	}
	
	public static String generateDefaultDestinationConfig() {
		return	
				"{\n"+
					"installationType: \"destination\",\n"+
					
					/*"syncRootDirectory: \"D:/JEE/Workspace/FGH/data/sync\",\n"+
					"classPath: \"D:/JEE/Workspace/FGH/openmrs-module-eptssync/api/target/eptssync-api-1.0-SNAPSHOT.jar\",\n"+*/
					
					"syncRootDirectory: \"/home/jpboane/working/prg/jee/workspace/data/sync\",\n"+
					"classPath: \"/home/jpboane/working/prg/jee/workspace/w02/openmrs-module-eptssync/api/target/eptssync-api-1.0-SNAPSHOT.jar\",\n"+
					
					"firstExport: true,\n"+
					"automaticStart: false, \n"+
					
					"connInfo: {\n"+
						"dataBaseUserName: \"root\",\n"+
						"dataBaseUserPassword: \"root\",\n"+
						"connectionURI: \"jdbc:mysql://localhost:3307/openmrs_module_eptssync_test?autoReconnect=true&useSSL=false\",\n"+
						"driveClassName: \"com.mysql.jdbc.Driver\"\n"+
					"},\n"+
					
					"operations: [\n"+
						"{\n"+
							"operationType: \"database_preparation\",\n"+
							"maxRecordPerProcessing: 1,\n"+
							"maxSupportedEngines: 1,\n"+
							"minRecordsPerEngine: 1,\n"+
							"disabled: false,\n"+
							"processingMode: \"parallel\",\n"+
							
							"child: {\n"+
								"operationType: \"pojo_generation\",\n"+
								"maxRecordPerProcessing: 1,\n"+
								"maxSupportedEngines: 1,\n"+
								"minRecordsPerEngine: 1,\n"+
								"disabled: false,\n"+
								"processingMode: \"sequencial\",\n"+
								
								"child: {\n"+
										"operationType: \"load\",\n"+
										"maxRecordPerProcessing: 1000, \n"+
										"maxSupportedEngines: 15,\n"+
										"minRecordsPerEngine: 1000,\n"+
										"disabled: false,\n"+
										"processingMode: \"sequencial\",\n"+
										
										"child:	{\n"+
											"operationType: \"synchronization\",\n"+
											"maxRecordPerProcessing: 1000,\n"+
											"maxSupportedEngines: 15,\n"+
											"minRecordsPerEngine: 100,\n"+
											"disabled: false,\n"+
											"processingMode: \"sequencial\",\n"+
											"doIntegrityCheckInTheEnd: \"true\",\n"+
													
											"child:{\n"+
												"operationType: \"consolidation\",\n"+
												"maxRecordPerProcessing: 1000,\n"+
												"maxSupportedEngines: 15,\n"+
												"minRecordsPerEngine: 50,\n"+
												"disabled: false,\n"+
												"processingMode: \"sequencial\"\n"+
											"}\n"+
										"}\n"+
									
								"}\n"+
							"}\n"+
						"}\n"+
					"]," + generateConfigTables() +
				"}";
	}
	private static String generateConfigTables() {
		return "tablesConfigurations: [ \n"+
								"{\n"+	
									"tableName: \"users\","+
									"parents: [{tableName: \"person\", defaultValueDueInconsistency: 1}, {tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"removeForbidden: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"person\","+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"person_address\",\n"+
									"parents: [{tableName: \"person\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"person_attribute\",\n"+
									"parents: [{tableName: \"person\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"person_name\","+
									"parents: [{tableName: \"person\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"relationship\",\n"+
									"parents: [{tableName: \"person\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"patient\",\n"+
									"sharePkWith: \"person\",\n"+
									"parents: [{tableName: \"person\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"patient_identifier\",\n"+
									"parents: [{tableName: \"patient\"}, {tableName: \"location\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"patient_program\",\n"+
									"parents: [{tableName: \"patient\"}, {tableName: \"location\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"patient_state\",\n"+
									"parents: [{tableName: \"patient_program\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"visit\",\n"+
									"parents: [{tableName: \"patient\"}, {tableName: \"location\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"visit_attribute\",\n"+
									"parents: [{tableName: \"visit\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"encounter\",\n"+
									"parents: [{tableName: \"patient\"}, {tableName: \"location\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}, {tableName: \"visit\"}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"provider\",\n"+
									"parents: [{tableName: \"person\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{"+
									"tableName: \"encounter_provider\",\n"+
									"parents: [{tableName: \"encounter\"}, {tableName: \"provider\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}, {tableName: \"visit\"}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"orders\",\n"+
									"parents: [{tableName: \"encounter\"}, {tableName: \"provider\"}, {tableName: \"orders\"}, {tableName: \"patient\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"obs\",\n"+
									"parents: [{tableName: \"person\"}, {tableName: \"location\"}, {tableName: \"orders\"}, {tableName: \"obs\"}, {tableName: \"encounter\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}, {tableName: \"visit\"}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"note\",\n"+
									"parents: [{tableName: \"encounter\"}, {tableName: \"patient\"}, {tableName: \"note\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}]\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"location\",\n"+
									"parents: [{tableName: \"location\", defaultValueDueInconsistency: 1}, {tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true,\n"+
									"extraConditionForExport_: \"exists (select encounter_id from encounter where encounter.location_id = location.location_id)\"\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"concept_datatype\",\n"+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"concept\",\n"+
									"parents: [{tableName: \"concept_datatype\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"person_attribute_type\",\n"+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"provider_attribute_type\",\n"+
									"parents: [{tableName: \"program_workflow\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"program\",\n"+
									"parents: [{tableName: \"concept\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"program_workflow\",\n"+
									"parents: [{tableName: \"program\"}, {tableName: \"concept\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"program_workflow_state\",\n"+
									"parents: [{tableName: \"program_workflow\"}, {tableName: \"concept\"}, {tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"encounter_type\",\n"+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"visit_type\",\n"+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"relationship_type\",\n"+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+
								"\n"+
								"{\n"+
									"tableName: \"patient_identifier_type\",\n"+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+		
								"\n"+
								
								"{\n"+
									"tableName: \"cohort\",\n"+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1}],\n"+
									"metadata: true\n"+
								"},\n"+							
								"\n"+
								"{\n"+
									"tableName: \"cohort_member\",\n"+
									"parents: [{tableName: \"users\", defaultValueDueInconsistency: 1},{tableName: \"cohort\"}, {tableName: \"patient\", refColumnName: \"patient_id\", refColumnType: \"int\"} ],\n"+
									"metadata: true\n"+
								"}\n"+		
									
						"]\n";
	}
}
