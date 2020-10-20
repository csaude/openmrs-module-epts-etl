
use openmrs_ile;

/*SAVE ENCOUNTER_PROVIDER WITHOUT ENCOUTER BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/encounter_provider_without_encounter.txt"
from encounter_provider
where 1 = 1
	  and (	not exists (	select * 
			   				from encounter
			   				where encounter.encounter_id = encounter_provider.encounter_id) or 
	 	
			exists  (	select *
	 		 			from encounter
			 			where 1 = 1
			 					and encounter.encounter_id = encounter_provider.encounter_id
								and (	not exists (select * 
					 				   				from patient 
					 				   				where encounter.patient_id = patient.patient_id) or
		 				  
						 				  not exists (	select * 
									 				  	from location 
									 				  	where encounter.location_id = location.location_id)	or 
						 				  	
		 						not exists (	select *	
		 										from location 
					 				  			where encounter.location_id = location.location_id)	or 
					 				  			
					 	 not exists (	select *
														from visit
														where visit.visit_id = encounter.visit_id) or	  
							 						  			
							  
		 				 			exists(	select *
											from visit
											where 1 = 1
								  					and encounter.visit_id  = visit.visit_id 
								  					and not exists (	select * 
								 				  						from location 
								 				  						where visit.location_id = location.location_id))			  
		 				  
		 				  )
			));
/*REMOVE ENCOUNTER_PROVIDER WITHOUT ENCOUTER BEFORE REMOVING THEM*/
delete 
from encounter_provider
where 1 = 1
	  and (	not exists (	select * 
			 			  	from encounter
			   				where encounter.encounter_id = encounter_provider.encounter_id) or
	 	
			exists  (	select *
				 		from encounter
						 where 1 = 1
						 		and encounter.encounter_id = encounter_provider.encounter_id
								and (	not exists (	select * 
						 				 			 	from patient 
						 				  				where encounter.patient_id = patient.patient_id) or
						 				  				
						 				 not exists (	select * 
									 				  	from location 
									 				  	where encounter.location_id = location.location_id)	or  				
						 			
						 				 not exists (	select *
														from visit
														where visit.visit_id = encounter.visit_id) or	  
							 				  
						 						exists(	select *
														from visit
														where 1 = 1
															  and encounter.visit_id  = visit.visit_id 
															  and not exists (	select * 
																 				from location 
																 				where visit.location_id = location.location_id))			  
						 				  
						 				  )
			));	  

/*SET previous_version TO NULL IF OBS IS MARKED TO BE REMOVED*/
select concat('update obs set previous_version = null where obs_id = ', obs_id, ';') INTO OUTFILE "/var/lib/mysql-files/ile/obs_set_previous_version_to_null.sql"
from  obs  outer_obs
where 1 = 1
	  and previous_version is not null 
	  and (previous_version in (	select obs_id 
						from  obs  
						where 1 = 1
							  and (	not exists (	select * 
						 							from encounter 
						 							where 1 = 1
						 						  			and obs.encounter_id = encounter.encounter_id) or
							 		
					 				exists(	select *
								 		 	from encounter
										 	where 1 = 1
										 			and encounter.encounter_id = obs.encounter_id
													and (	not exists (	select * 
											 				  				from patient 
											 				  				where encounter.patient_id = patient.patient_id) or
											 				 not exists (	select * 
														 				  	from location 
														 				  	where encounter.location_id = location.location_id)	or  
									 				  												 				  
											 			not exists (	select *
																		from visit
																		where visit.visit_id = encounter.visit_id) or		  
										 		
											 			exists(	select *
																from visit
																where 1 = 1
																	  and encounter.visit_id  = visit.visit_id 
																	  and not exists (	select * 
																	 				  	from location 
																	 				  	where visit.location_id = location.location_id))			  
											 				  
											 		)) or
									 
									 not exists  (select *
											from person
											where person.person_id = obs.person_id))) or
							
			NOT exists (SELECT *
					FROM obs 
					WHERE outer_obs.previous_version  = obs_id)
										
												
	);
/*EXECTE THE FILE ON MYSQL*/
source /var/lib/mysql-files/ile/obs_set_previous_version_to_null.sql;

cat datacorrection.sql | docker exec -i 7d4 /usr/bin/mysql -u root --password=root openmrs_ile


/*SET obs_group_id TO NULL IF OBS IS MARKED TO BE REMOVED*/
select concat('update obs set obs_group_id = null where obs_id = ', obs_id, ';') INTO OUTFILE "/var/lib/mysql-files/ile/obs_set_obs_group_id_to_null.sql"
from  obs outer_obs
where 1 = 1
	  and obs_group_id is not null 
	  and( obs_group_id in (	select obs_id 
								from  obs  
								where 1 = 1
									  and (	not exists (	select * 
										 					from encounter 
										 					where 1 = 1
										 						  and obs.encounter_id = encounter.encounter_id) or
									 		exists(	select *
											 		from encounter
													where 1 = 1
													 	and encounter.encounter_id = obs.encounter_id
													  	and (	not exists (	select * 
											 				 		 			from patient 
											 				  					where encounter.patient_id = patient.patient_id) or
											 				  					
											 				  not exists (	select * 
														 				  	from location 
														 				  	where encounter.location_id = location.location_id)	or  
											 				  					
											 				  	not exists (	select *
																				from visit
																				where visit.visit_id = encounter.visit_id) or		  
											 				  
													 			exists(	select *
																		from visit
																		where 1 = 1
																			  and encounter.visit_id  = visit.visit_id 
																			  and not exists (select * 
																			 				  from location 
																			 				  where visit.location_id = location.location_id))			  
													 				  
													 				  )) or
												 not exists  (select *
														from person
														where person.person_id = obs.person_id))) or
			
												not exists (SELECT *
												FROM obs 
												WHERE outer_obs.obs_group_id  = obs.obs_id )												
	);
/*EXECTE THE FILE ON MYSQL*/
source /var/lib/mysql-files/ile/obs_set_obs_group_id_to_null.sql;
	
/*SAVE OBS WITHOUT ENCOUNTER*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/obs_without_encounter.txt"
from  obs  
where 1 = 1
	  and (not exists (	select * 
	 					from encounter 
	 					where 1 = 1
	 						  and obs.encounter_id = encounter.encounter_id) or
	 		
	 		exists(	select *
		 		 	from encounter
				 	where 1 = 1
				 			and encounter.encounter_id = obs.encounter_id
							and (	not exists (	select * 
					 				  				from patient 
					 				  				where encounter.patient_id = patient.patient_id) or
					 		
					 			 not exists (	select * 
									 				  	from location 
									 				  	where encounter.location_id = location.location_id)	or  
									 				  	
						 			not exists (	select *
													from visit
													where visit.visit_id = encounter.visit_id) or	  
									 				  
						 			 exists(	select *
												from visit
												where 1 = 1
												  		and encounter.visit_id  = visit.visit_id 
												  		and not exists (	select * 
												 				  			from location 
												 				  			where visit.location_id = location.location_id)))) or
			 not exists  (	select *
							from person
							where person.person_id = obs.person_id));
	 				
delete
from  obs  
where 1 = 1
	  and (not exists (	select * 
	 					from encounter 
	 					where 1 = 1
	 						  and obs.encounter_id = encounter.encounter_id) or
	 		
	 		exists(	select *
		 		 	from encounter
				 	where 1 = 1
				 			and encounter.encounter_id = obs.encounter_id
							and (	not exists (	select * 
					 				  				from patient 
					 				  				where encounter.patient_id = patient.patient_id) or
					 				 not exists (	select * 
									 				  	from location 
									 				  	where encounter.location_id = location.location_id)	or  
									 				  		
						 			not exists (	select *
													from visit
													where visit.visit_id = encounter.visit_id) or	  
									 				  
						 			 exists(	select *
												from visit
												where 1 = 1
												  		and encounter.visit_id  = visit.visit_id 
												  		and not exists (	select * 
												 				  			from location 
												 				  			where visit.location_id = location.location_id)))) or
			 not exists  (	select *
							from person
							where person.person_id = obs.person_id));
								 				     
/*SAVE GAAC_MEMBER WITHOUT PACIENT BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/gaac_member_without_patient.txt" 
from gaac_member
where 1 = 1
	  and not exists (	select * 
	 				  	from patient 
	 				  	where gaac_member.member_id = patient.patient_id);

/*REMOVE GAAC_MEMBER WITHOUT PACIENT BEFORE REMOVING THEM*/
delete 
from gaac_member 
where 1 = 1
	  and not exists (	select * 
	 				  	from patient 
					  	where gaac_member.member_id = patient.patient_id);
					  
/*SAVE ENCONTER WITHOUT PACIENT BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/encounter_with_visit_without_location.txt" 
from encounter 
where 1 = 1
	  and (	not exists (	select * 
	 				  		from patient 
					  		where encounter.patient_id = patient.patient_id) or
					  		
			not exists (	select * 
		 				  	from location 
		 				  	where encounter.location_id = location.location_id)	or  			  		

			not exists (	select *
							from visit
							where visit.visit_id = encounter.visit_id) or	  
			 exists(	select *
						from visit
						where 1 = 1
							  and encounter.visit_id  = visit.visit_id 
							  and not exists (select * 
							 				  from location 
							 				  where visit.location_id = location.location_id)));	
							  
/*REMOVE ENCONTER WITHOUT PACIENT BEFORE REMOVING THEM*/
delete 
from encounter 
where 1 = 1
	  and (	not exists (	select * 
	 				  		from patient 
					  		where encounter.patient_id = patient.patient_id) or
					  		
			 not exists (	select * 
		 				  	from location 
		 				  	where encounter.location_id = location.location_id)	or  			
			not exists (	select *
							from visit
							where visit.visit_id = encounter.visit_id) or	 
							
			not exists (	select * 
		 				  	from location 
		 				  	where encounter.location_id = location.location_id)	or 
		 				  					 
			 exists(	select *
						from visit
						where 1 = 1
							  and encounter.visit_id  = visit.visit_id 
							  and not exists (select * 
							 				  from location 
							 				  where visit.location_id = location.location_id)));						  
/*SAVE PATIENT_IDENTIFIER WITHOUT PACIENT BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/patient_identifier_without_patient.txt" 
from patient_identifier
where 1 = 1
	  and not exists (select * 
	 				  from patient 
	 				  where patient_identifier.patient_id = patient.patient_id);

/*REMOVE PATIENT_IDENTIFIER WITHOUT PACIENT*/
delete 
from patient_identifier 
where 1 = 1
	  and not exists (select * 
	 				  from patient 
					  where patient_identifier.patient_id = patient.patient_id);	
					  
					  
/*SAVE VISIT WITHOUT PACIENT BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/visit_without_patient.txt" 
from visit
where 1 = 1
	  and not exists (select * 
	 				  from patient 
	 				  where visit.patient_id = patient.patient_id);

/*REMOVE PATIENT_IDENTIFIER WITHOUT PACIENT*/
delete 
from visit
where 1 = 1
	  and not exists (	select * 
	 				  	from patient 
	 				  	where visit.patient_id = patient.patient_id);
	 				  
	 				  					  				  
/*SAVE VISIT WITHOUT LOCATION BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/visit_without_location.txt" 
from visit
where 1 = 1
	  and not exists (	select * 
	 				  	from location 
	 				  	where visit.location_id = location.location_id);

/*REMOVE PATIENT_IDENTIFIER WITHOUT PACIENT*/
delete 
from visit
where 1 = 1
	  and not exists (	select * 
	 				  	from location 
	 				  	where visit.location_id = location.location_id);	
					  
/*SAVE PERSON_ADDRESS WITHOUT PERSON BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/person_address_without_patient.txt" 
from person_address
where 1 = 1
	  and not exists (select * 
	 				  from person 
	 				  where person_address.person_id = person.person_id);

/*REMOVE PERSON_ADDRESS WITHOUT PACIENT*/
delete 
from person_address 
where 1 = 1
	  and not exists (select * 
	 				  from person 
					  where person_address.person_id = person.person_id);					  


/*SAVE PERSON_NAME WITHOUT PERSON BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/person_name_without_patient.txt" 
from person_name
where 1 = 1
	  and not exists (select * 
	 				  from person 
	 				  where person_name.person_id = person.person_id);

/*REMOVE PERSON_NAME WITHOUT PACIENT*/
delete 
from person_name 
where 1 = 1
	  and not exists (select * 
	 				  from person 
					  where person_name.person_id = person.person_id);					  

/*SAVE PATIENT_STATE WITHOUT PACIENT_PROGRAM BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/patient_state_without_patient_program.txt" 
from patient_state
where 1 = 1
	  and (not exists (select * 
	 				  from patient_program 
	 				  where patient_state.patient_program_id = patient_program.patient_program_id)
	 	OR
	 	
	 	exists (select *
	 		from patient_program
	 		where 1 = 1
	 			and patient_state.patient_program_id = patient_program.patient_program_id
	 			and not exists (select * 
		 				  from patient 
		 				  where patient.patient_id = patient_program.patient_id)));

/*REMOVE PATIENT_STATE WITHOUT PACIENT_PROGRAM*/
delete 
from patient_state
where 1 = 1
	  and (not exists (select * 
	 				  from patient_program 
	 				  where patient_state.patient_program_id = patient_program.patient_program_id)
	 	OR
	 	
	 	exists (select *
	 		from patient_program
	 		where 1 = 1
	 			and patient_state.patient_program_id = patient_program.patient_program_id
	 			and not exists (select * 
		 				  from patient 
		 				  where patient.patient_id = patient_program.patient_id)));
/*SAVE PATIENT_PROGRAM WITHOUT PATIENT  BEFORE REMOVING THEM*/
select * INTO OUTFILE "/var/lib/mysql-files/ile/patient_program_without_patient.txt" 
from patient_program
where 1 = 1
	  and not exists (select * 
	 				  from patient 
	 				  where patient.patient_id = patient_program.patient_id);

/*REMOVE PATIENT_PROGRAM WITHOUT PACIENT*/
delete 
from patient_program
where 1 = 1
	  and not exists (select * 
	 				  from patient 
	 				  where patient.patient_id = patient_program.patient_id);				  



/*HARMONIZE METADATAS */	 				  					 

UPDATE
    openmrs_ile.person_attribute_type AS dest inner join openmrs_metadata.person_attribute_type AS src on dest.uuid  = src.uuid 
SET
    dest.name = src.name, 
    dest.format = src.format, 
    dest.foreign_key = src.foreign_key, 
    dest.sort_weight = src.sort_weight, 
    dest.searchable = src.searchable
WHERE
    dest.uuid  = src.uuid ;

UPDATE
    openmrs_ile.visit_type AS dest inner join openmrs_metadata.visit_type AS src on dest.uuid  = src.uuid 
SET
    dest.name = src.name
WHERE
    dest.uuid  = src.uuid ;
   

UPDATE
    openmrs_ile.relationship_type AS dest inner join openmrs_metadata.relationship_type AS src on dest.uuid  = src.uuid 
SET
    dest.a_is_to_b = src.a_is_to_b , 
    dest.b_is_to_a = src.b_is_to_a 
WHERE
    dest.uuid  = src.uuid ;
   
 
 UPDATE
    openmrs_ile.patient_identifier_type AS dest inner join openmrs_metadata.patient_identifier_type AS src on dest.uuid  = src.uuid 
SET
    dest.name = src.name , 
    dest.format = src.format,
    dest.check_digit = src.check_digit ,
    dest.format_description = src.format_description,
    dest.validator  = src.validator
    
WHERE
    dest.uuid  = src.uuid ;  
   
 
 UPDATE
    openmrs_ile.program_workflow AS dest inner join openmrs_metadata.program_workflow AS src on dest.uuid  = src.uuid 
SET
    dest.program_id = src.program_id , 
    dest.concept_id = src.concept_id 
    
WHERE
    dest.uuid  = src.uuid ;  
   
 
UPDATE
    openmrs_ile.program AS dest inner join openmrs_metadata.program AS src on dest.uuid  = src.uuid
SET
    dest.name = src.name
WHERE
    dest.uuid  = src.uuid ;
  
UPDATE
    openmrs_ile.program_workflow_state AS dest inner join openmrs_metadata.program_workflow_state AS src on dest.uuid  = src.uuid 
SET
    dest.program_workflow_id = src.program_workflow_id , 
    dest.concept_id = src.concept_id,
    dest.initial = src.initial ,
    dest.terminal  = src.terminal 
    
WHERE
    dest.uuid  = src.uuid ;   
   
   
UPDATE
    openmrs_ile.gaac_affinity_type AS dest inner join openmrs_metadata.gaac_affinity_type AS src on dest.uuid  = src.uuid 
SET
    dest.name = src.name 
    
WHERE
    dest.uuid  = src.uuid ;    
   
 UPDATE
    openmrs_ile.gaac_reason_leaving_type AS dest inner join openmrs_metadata.gaac_reason_leaving_type AS src on dest.uuid  = src.uuid 
SET
    dest.name = src.name 
    
WHERE
    dest.uuid  = src.uuid ; 