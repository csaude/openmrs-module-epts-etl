SELECT 	person.birthdate,
		person.gender,
		tarvEnrollment.enrollment_date, 
		tarvEnrollment.location_id, 
		location.name AS location_name,
		round(datediff(tarvEnrollment.enrollment_date, person.birthdate)/365) AS age_enrollment, 
		round(datediff(now(), person.birthdate)/365) AS openmrs_current_age,
		CASE marital_status_obs.value_coded
			 WHEN 1057 THEN 'SINGLE'
			 WHEN 5555 THEN 'MARRIED'
			 WHEN 1059 THEN 'WIDOWED'
			 WHEN 1060 THEN 'LIVING WITH PARTNER'
			 WHEN 1056 THEN 'SEPARATED'
			 WHEN 1058 THEN 'DIVORCED'
			 ELSE null 
		end as marital_status_at_enrollment,
		CASE
			WHEN pregnancy_status_obs_1982.value_coded = 44 THEN 'YES'
			WHEN pregnancy_status_obs_1279.value_numeric is not null THEN 'YES'
			WHEN pregnancy_status_patient_program.program_id is not null THEN 'YES'
			ELSE null
		END AS pregnancy_status_at_enrollment,
		education.education_at_enrollment, 
		occupation.occupation_at_enrollment, 
		partner_status.partner_status_at_enrollment ,
		who_stage.who_clinical_stage_at_enrollment_date,
		who_stage.who_clinical_stage_at_enrollment,
		weight.weight_date,
		weight.weight_enrollment,
		height.height_enrollment,
		height.height_date
		
		
FROM person LEFT JOIN (
						SELECT 	patient_id, 
								MIN(enrollment_date) as enrollment_date, 
								location_id 
						FROM ( 
							 SELECT encounter.patient_id,
									min(obs.value_datetime) AS enrollment_date,
									encounter.location_id 
							 FROM 	encounter INNER JOIN obs ON obs.encounter_id = encounter.encounter_id 
							 WHERE 	encounter.patient_id = 428
									AND obs.value_datetime <= now()
									AND encounter.voided = 0 
									AND obs.voided = 0 
									AND encounter.encounter_type = 53 
									AND obs.value_datetime IS NOT NULL 
									AND obs.concept_id = 23808 
							 
							 UNION 
							 
							 SELECT	encounter.patient_id,
									min(encounter.encounter_datetime) AS enrollment_date,
									encounter.location_id
							 FROM 	encounter INNER JOIN obs ON obs.encounter_id = encounter.encounter_id 
							 WHERE 	encounter.patient_id = 428
									AND encounter.encounter_datetime <= now()				 
									AND encounter.voided = 0 
									AND obs.voided = 0 
									AND encounter.encounter_type IN(5,7) 
							 
							 UNION 
							 
							 SELECT patient_program.patient_id, 
									MIN(patient_program.date_enrolled) AS enrollment_date, 
									patient_program.location_id
							 FROM 	patient_program
							 WHERE 	patient_program.patient_id = 428
									AND patient_program.date_enrolled <= now()  
									AND patient_program.program_id = 1
									AND patient_program.voided = 0 
						) all_tarvEnrollment
						WHERE patient_id IS NOT NULL
					) tarvEnrollment ON person.person_id =  tarvEnrollment.patient_id 
						
			 LEFT JOIN (
							SELECT 
								
								CASE obs.value_coded 
									 WHEN 1445 THEN 'NONE'
									 WHEN 1446 THEN 'PRIMARY SCHOOL'
									 WHEN 1447 THEN 'SECONDARY SCHOOL'
									 WHEN 6124 THEN 'TECHNICAL SCHOOL'
									 WHEN 1444 THEN 'SECONDARY SCHOOL'
									 WHEN 6125 THEN 'TECHNICAL SCHOOL'
									 WHEN 1448 THEN 'UNIVERSITY'
									 ELSE null 
								END education_at_enrollment
							FROM (							
									SELECT 	obs.person_id patient_id,
											min(obs.obs_datetime) obs_datetime
									FROM 	obs        
									WHERE 	obs.person_id = 428
											AND obs.concept_id=1443 
											AND obs.voided=0
							) max_obs INNER 
								
								
					) education ON education.patient_id = person.person_id

			LEFT JOIN (
						SELECT  max_occupation.patient_id,
								obs.value_text occupation_at_enrollment
						FROM 
							(
								SELECT  obs.person_id patient_id,
										max(obs_datetime) obs_datetime
								FROM 	obs
								WHERE 	obs.person_id = 428
										AND obs.concept_id=1459 
										AND voided = 0 
							) max_occupation inner join obs on obs.person_id = max_occupation.patient_id AND obs.concept_id=1459  AND voided = 0 and obs.obs_datetime = max_occupation.obs_datetime
						) occupation ON occupation.patient_id = person.person_id

			LEFT JOIN (
						SELECT  obs.person_id patient_id,
								CASE obs.value_coded
									WHEN 1169 THEN 'HIV INFECTED'
									WHEN 1066 THEN 'NO'
									WHEN 1457 THEN 'NO INFORMATION'
									ELSE null 
								END  partner_status_at_enrollment
						FROM 	obs
						WHERE 	obs.person_id = 428
								AND obs.concept_id = 1449 
								AND voided = 0
					) partner_status ON partner_status.patient_id = person.person_id
			LEFT JOIN (	
						SELECT  min_encounter.patient_id,
								encounter_datetime  as who_clinical_stage_at_enrollment_date,
								CASE obs.value_coded
									WHEN 1204 THEN 'I'
									WHEN 1205 THEN 'II'
									WHEN 1206 THEN 'III'
									WHEN 1207 THEN 'IV'
									ELSE null 
								END as WHO_clinical_stage_at_enrollment
						FROM  (
								SELECT  encounter.patient_id,
										min(encounter_datetime) encounter_datetime
								FROM 	encounter INNER JOIN obs ON obs.encounter_id = encounter.encounter_id
								WHERE  	encounter.patient_id = 428 
										AND encounter.voided = 0 
										AND encounter.encounter_type in(6,53) 
										AND obs.obs_datetime = encounter.encounter_datetime 
										AND obs.concept_id = 5356
							  ) min_encounter inner join obs on min_encounter.patient_id = obs.person_id and  min_encounter.encounter_datetime = obs.obs_datetime and obs.voided = 0 and obs.concept_id = 5356
						) who_stage ON who_stage.patient_id = person.person_id
							
			LEFT JOIN (
						
						SELECT  mini_eunconter.patient_id,
								obs.value_numeric AS weight_enrollment,
								encounter_datetime AS weight_date	
						FROM (
								SELECT  encounter.patient_id,
										min(encounter_datetime) encounter_datetime
								  FROM	encounter INNER JOIN obs ON obs.encounter_id = encounter.encounter_id
								  WHERE	encounter.patient_id = 428 
										AND encounter.voided=0 
										AND encounter.encounter_type in(1,6) 
										AND obs.obs_datetime = encounter.encounter_datetime 
										AND obs.concept_id = 5089
							) mini_eunconter inner join obs on mini_eunconter.patient_id = obs.person_id and  mini_eunconter.encounter_datetime = obs.obs_datetime and obs.voided = 0 and obs.concept_id = 5089
						) weight ON weight.patient_id = person.person_id
			
			LEFT JOIN (
						
						SELECT mini_encounter.patient_id,
							   obs.value_numeric height_enrollment,
							   mini_encounter.encounter_datetime height_date
						FROM (		
							    SELECT  encounter.patient_id,
										min(encounter_datetime) encounter_datetime
								FROM	encounter INNER JOIN obs ON obs.encounter_id = encounter.encounter_id
								WHERE  	encounter.patient_id = 428 
										AND encounter.voided = 0 
										AND encounter.encounter_type in(1,6) 
										AND obs.obs_datetime = encounter.encounter_datetime 
										AND obs.concept_id = 5090 
								) mini_encounter INNER JOIN obs on mini_encounter.patient_id = obs.person_id and  mini_encounter.encounter_datetime = obs.obs_datetime and obs.voided = 0 and obs.concept_id = 5090 
					) height on height.patient_id = person.person_id
			
			
					
			LEFT JOIN location ON location.location_id = tarvEnrollment.location_id
			LEFT JOIN obs marital_status_obs ON marital_status_obs.person_id = person.person_id AND marital_status_obs.concept_id=1054 AND marital_status_obs.voided = 0
			LEFT JOIN obs pregnancy_status_obs_1982 ON pregnancy_status_obs_1982.person_id = person.person_id AND  pregnancy_status_obs_1982.concept_id = 1982 AND pregnancy_status_obs_1982.obs_datetime = tarvEnrollment.enrollment_date
			LEFT JOIN obs pregnancy_status_obs_1279 ON pregnancy_status_obs_1279.person_id = person.person_id AND  pregnancy_status_obs_1279.concept_id = 1279 AND pregnancy_status_obs_1279.obs_datetime = tarvEnrollment.enrollment_date 
			LEFT JOIN patient_program pregnancy_status_patient_program ON pregnancy_status_patient_program.patient_id = tarvEnrollment.patient_id AND  pregnancy_status_patient_program.program_id = 8 AND pregnancy_status_patient_program.voided = 0	
WHERE 	person.person_id = 428