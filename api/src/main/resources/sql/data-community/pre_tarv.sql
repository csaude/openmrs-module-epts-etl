SELECT 	person.birthdate,
		person.gender,
		tarvEnrollment.enrollment_date, 
		tarvEnrollment.location_id, 
		location.name AS health_facility,
		round(datediff(tarvEnrollment.enrollment_date, person.birthdate)/365) AS age_enrollment, 
		round(datediff(@endDate, person.birthdate)/365) AS openmrs_current_age,
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
		height.height_date,
		art_initiation.art_initiation_date,
		art_regimen.art_regimen_code,
		patient_status.patient_status_date,
		patient_status.patient_status_code,
		tb_status.tb_at_screening,
		tb_co_infection.tb_co_infection_status,
		pmtct.pmtct_entry_date,
		pmtct.pmtct_exit_date,
		dmc_status.current_status_in_dmc
				
FROM person LEFT JOIN (
						SELECT 	patient_id, 
								MIN(enrollment_date) as enrollment_date, 
								location_id 
						FROM ( 
							 SELECT encounter.patient_id,
									min(obs.value_datetime) AS enrollment_date,
									encounter.location_id 
							 FROM 	encounter INNER JOIN obs ON obs.encounter_id = encounter.encounter_id 
							 WHERE 	encounter.patient_id = @patientId
									AND obs.value_datetime <= @endDate
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
							 WHERE 	encounter.patient_id = @patientId
									AND encounter.encounter_datetime <= @endDate				 
									AND encounter.voided = 0 
									AND obs.voided = 0 
									AND encounter.encounter_type IN(5,7) 
							 
							 UNION 
							 
							 SELECT patient_program.patient_id, 
									MIN(patient_program.date_enrolled) AS enrollment_date, 
									patient_program.location_id
							 FROM 	patient_program
							 WHERE 	patient_program.patient_id = @patientId
									AND patient_program.date_enrolled <= @endDate  
									AND patient_program.program_id = 1
									AND patient_program.voided = 0 
						) all_tarvEnrollment
						WHERE patient_id IS NOT NULL
					) tarvEnrollment ON person.person_id =  tarvEnrollment.patient_id 
						
			 LEFT JOIN (
						SELECT max_obs.person_id patient_id,
							CASE obs.value_coded 
								 WHEN 1445 THEN 'NONE'
								 WHEN 1446 THEN 'PRIMARY SCHOOL'
								 WHEN 1447 THEN 'SECONDARY SCHOOL'
								 WHEN 6124 THEN 'TECHNICAL SCHOOL'
								 WHEN 1444 THEN 'SECONDARY SCHOOL'
								 WHEN 6125 THEN 'TECHNICAL SCHOOL'
								 WHEN 1448 THEN 'UNIVERSITY'
								 ELSE null 
							END education_at_enrollment,
							obs.obs_datetime 
						FROM (							
								SELECT 	obs.person_id,
										min(obs.obs_datetime) obs_datetime
								FROM 	obs        
								WHERE 	obs.person_id = @patientId
										AND obs.concept_id=1443 
										AND obs.voided=0
						) max_obs INNER JOIN obs on obs.person_id = max_obs.person_id and obs.concept_id=1443 and obs.voided=0 and obs.obs_datetime = max_obs.obs_datetime 
							
								
					) education ON education.patient_id = person.person_id

			LEFT JOIN (
						SELECT  max_occupation.patient_id,
								obs.value_text occupation_at_enrollment
						FROM 
							(
								SELECT  obs.person_id patient_id,
										max(obs_datetime) obs_datetime
								FROM 	obs
								WHERE 	obs.person_id = @patientId
										AND obs.concept_id = 1459 
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
						WHERE 	obs.person_id = @patientId
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
								WHERE  	encounter.patient_id = @patientId 
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
								  WHERE	encounter.patient_id = @patientId 
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
								WHERE  	encounter.patient_id = @patientId 
										AND encounter.voided = 0 
										AND encounter.encounter_type in(1,6) 
										AND obs.obs_datetime = encounter.encounter_datetime 
										AND obs.concept_id = 5090 
								) mini_encounter INNER JOIN obs on mini_encounter.patient_id = obs.person_id and  mini_encounter.encounter_datetime = obs.obs_datetime and obs.voided = 0 and obs.concept_id = 5090 
					) height on height.patient_id = person.person_id
					
			LEFT JOIN   (	
						   SELECT patient_id,
								  min(data_inicio) art_initiation_date
						   FROM
								 (
									 SELECT encounter.patient_id,
											min(encounter.encounter_datetime) data_inicio
									  FROM 	encounter INNER JOIN obs oN obs.encounter_id = encounter.encounter_id
									  WHERE encounter.patient_id = @patientId
											AND encounter.voided = 0
											AND encounter.encounter_type IN (18, 6, 9)
											AND obs.voided = 0
											AND obs.concept_id = 1255
											AND obs.value_coded = 1256
										
									  UNION 
									  
									  SELECT encounter.patient_id,
											 min(obs.value_datetime) data_inicio
									  FROM 	encounter INNER JOIN obs ON encounter.encounter_id = obs.encounter_id
									  WHERE encounter.patient_id = @patientId
											AND encounter.voided = 0
											AND encounter.encounter_type IN (18, 6, 9)
											AND obs.voided = 0
											AND obs.concept_id = 1190
											AND obs.value_datetime IS NOT NULL
									  
								  UNION 
								  
								  SELECT patient_program.patient_id,
										 patient_program.date_enrolled data_inicio
								  FROM 	patient_program
								  WHERE patient_program.patient_id = @patientId
										AND patient_program.voided = 0
										AND patient_program.program_id = 2
										
								  UNION 
								  
								  SELECT encounter.patient_id,
										 MIN(encounter.encounter_datetime) AS data_inicio
								  FROM 	encounter
								  WHERE encounter.patient_id = @patientId
										AND encounter.encounter_type = 18
										AND encounter.voided = 0
								) inicio
						   ) art_initiation ON art_initiation.patient_id = person.person_id
			LEFT JOIN (
							SELECT 	cpn.patient_id,
									cpn.data_cpn,  
									CASE obs.value_coded
										WHEN 6388 THEN 'ON ARV TREATMENT AT ANC ENTRANCE' 
										WHEN 631  THEN 'NEVIRAPINE' 
										WHEN 1801 THEN 'ZIDOVUDINE + NEVIRAPINE' 
										WHEN 1256 THEN 'START DRUGS' 
										WHEN 1257 THEN 'CONTINUE REGIMEN' 
										WHEN 797 THEN 'ZIDOVUDINE'
										WHEN 792 THEN 'STAVUDINE + LAMIVUDINE + NEVIRAPINE'
										WHEN 628 THEN 'LAMIVUDINE'
										WHEN 87 THEN 'SULFADOXINE AND PYRIMETHAMINE'
										WHEN 1800 THEN 'TARV TREATMENT'
										WHEN 916 THEN 'TRIMETHOPRIM AND SULFAMETHOXAZOLE'
										WHEN 1107 THEN 'NONE'
										WHEN 630 THEN 'ZIDOVUDINE AND LAMIVUDINE'
										ELSE null 
									END	as art_regimen_code
							  FROM
								  ( SELECT  encounter.patient_id,
											min(encounter.encounter_datetime) data_cpn
									FROM  	encounter
									WHERE   encounter.patient_id = @patientId 
											AND encounter.voided=0  
											AND encounter.encounter_type in (11) 
								  ) cpn INNER JOIN obs on obs.person_id = cpn.patient_id AND obs.obs_datetime = cpn.data_cpn AND obs.voided=0 AND obs.concept_id = 1504 
							) art_regimen ON art_regimen.patient_id = person.person_id
			LEFT JOIN (
						SELECT 	pg.patient_id,
								ps.start_date patient_status_date,
								CASE ps.state
									WHEN 7 THEN 'TRASFERRED OUT'
									WHEN 8 THEN 'SUSPENDED'
									WHEN 9 THEN 'ART LTFU'
									WHEN 10 THEN 'DEAD'
									ELSE null 
								END AS patient_status_code 
						FROM 	patient_program pg INNER JOIN patient_state ps ON pg.patient_program_id = ps.patient_program_id
						where 	pg.patient_id = @patientId
								AND pg.voided = 0 
								AND ps.voided = 0 
								AND pg.program_id = 2 
								AND ps.state in (7,8,9,10) 
								AND ps.end_date is null 
								AND ps.start_date BETWEEN @startDate AND @endDate
					) patient_status on patient_status.patient_id = person.person_id
			LEFT JOIN (
			
						SELECT 	tb.patient_id,
								IF(obs.value_coded = 1065, 'YES', IF(obs.value_coded = 1066, 'NO', NULL)) AS tb_at_screening
						FROM (
								SELECT e.patient_id,
									   MIN(encounter_datetime) AS encounter_datetime
								FROM encounter e
									 INNER JOIN obs o ON o.encounter_id = e.encounter_id
								WHERE e.patient_id = @patientId
								  AND e.voided = 0
								  AND e.encounter_type IN (6, 9)
								  AND o.obs_datetime = e.encounter_datetime
								  AND o.concept_id IN (6257, 23758)
							) AS tb INNER JOIN obs ON obs.person_id = tb.patient_id AND obs.voided = 0 AND obs.obs_datetime = tb.encounter_datetime AND obs.concept_id IN (6257, 23758)
			) tb_status on tb_status.patient_id = person.person_id
		LEFT JOIN (
					SELECT e.patient_id,
						   CASE o.value_coded
							   WHEN 664 THEN 'NEGATIVE'
							   WHEN 703 THEN 'POSITIVE'
							   WHEN 1065 THEN 'YES'
							   WHEN 1066 THEN 'NO'
							   ELSE NULL
						   END AS tb_co_infection_status
					FROM encounter e
							 INNER JOIN obs o ON o.encounter_id = e.encounter_id
					WHERE e.patient_id = @patientId
					  AND e.encounter_type IN (6, 9)
					  AND e.voided = 0
					  AND o.voided = 0
					  AND o.concept_id IN (6277, 23761)
					) tb_co_infection ON tb_co_infection.patient_id = person.person_id
					
			LEFT JOIN (
						SELECT 	patient_id,
								date_enrolled AS pmtct_entry_date,
								date_completed AS pmtct_exit_date
						FROM  patient_program
						WHERE patient_id = @patientId
							  AND voided = 0 
							  AND program_id = 8 
					)	pmtct ON pmtct.patient_id = person.person_id	
			LEFT JOIN (
						select 	pg.patient_id,ps.start_date,
								case ps.state
									when 7 then 'TRASFERRED OUT'
									when 8 then 'SUSPENDED'
									when 9 then 'ART LTFU'
									when 10 then 'DEAD'
								else null end as current_status_in_DMC
						from 	patient_program pg inner join patient_state ps on pg.patient_program_id=ps.patient_program_id
						where 	pg.patient_id = @patientId
								and pg.voided = 0 
								and ps.voided = 0 
								and pg.program_id = 2 
								and ps.state in (7, 8, 9, 10) 
								and ps.end_date is null 
								and ps.start_date <= @endDate
			) dmc_status  ON dmc_status.patient_id = person.person_id

			LEFT JOIN location ON location.location_id = tarvEnrollment.location_id
			LEFT JOIN obs marital_status_obs ON marital_status_obs.person_id = person.person_id AND marital_status_obs.concept_id=1054 AND marital_status_obs.voided = 0
			LEFT JOIN obs pregnancy_status_obs_1982 ON pregnancy_status_obs_1982.person_id = person.person_id AND  pregnancy_status_obs_1982.concept_id = 1982 AND pregnancy_status_obs_1982.obs_datetime = tarvEnrollment.enrollment_date
			LEFT JOIN obs pregnancy_status_obs_1279 ON pregnancy_status_obs_1279.person_id = person.person_id AND  pregnancy_status_obs_1279.concept_id = 1279 AND pregnancy_status_obs_1279.obs_datetime = tarvEnrollment.enrollment_date 
			LEFT JOIN patient_program pregnancy_status_patient_program ON pregnancy_status_patient_program.patient_id = tarvEnrollment.patient_id AND  pregnancy_status_patient_program.program_id = 8 AND pregnancy_status_patient_program.voided = 0	
WHERE 	person.person_id = @patientId