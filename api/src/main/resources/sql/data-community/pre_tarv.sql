SELECT 	preTarv_final.patient_id, 
		preTarv_final.enrollment_date, 
		preTarv_final.location_id, 
		location.name as location_name,
		person.birthdate,
		round(datediff(preTarv_final.enrollment_date,person.birthdate)/365) as age_enrollment, 
		round(datediff(?,person.birthdate)/365) as openmrs_current_age,
		gender,
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
			WHEN pregnancy_status_obs_1982.value_coded=44 THEN 'YES'
			WHEN pregnancy_status_obs_1279.value_numeric is not null THEN 'YES'
			WHEN pregnancy_status_patient_program.program_id is not null THEN 'YES'
			ELSE null
		END AS pregnancy_status_at_enrollment
		
FROM (
		SELECT preTarv.patient_id, MIN(preTarv.initialDate) enrollment_date, preTarv.location as location_id 
		FROM ( 
			 SELECT 	p.patient_id,
						min(o.value_datetime) AS initialDate,
						e.location_id as location 
			 FROM patient p INNER JOIN encounter e  ON e.patient_id=p.patient_id 
							INNER JOIN obs o on o.encounter_id=e.encounter_id 
			 WHERE 	p.patient_id = ?
					AND o.value_datetime <= ?
					AND e.voided=0 
					AND o.voided=0 
					AND e.encounter_type=53 
					AND o.value_datetime IS NOT NULL AND o.concept_id=23808 
			 
			 UNION 
			 
			 SELECT p.patient_id,min(e.encounter_datetime) AS initialDate,e.location_id as location 
			 FROM patient p INNER JOIN encounter e  ON e.patient_id=p.patient_id 
							INNER JOIN obs o on o.encounter_id=e.encounter_id 
			 WHERE 	p.patient_id = ?
					AND e.encounter_datetime <= ?				 
					AND e.voided=0 
					AND o.voided=0 
					AND e.encounter_type IN(5,7) 
			 
			 UNION 
			 
			 SELECT pg.patient_id, MIN(pg.date_enrolled) AS initialDate, pg.location_id as location
			 FROM patient p INNER JOIN patient_program pg on pg.patient_id=p.patient_id 
			 WHERE 	p.patient_id = ?
					AND pg.date_enrolled <= ?  
					AND pg.program_id=1
					AND pg.voided=0 
		) preTarv
) preTarv_final	inner join person on person.person_id = 	preTarv_final.patient_id and round(datediff(?,person.birthdate)/365)  > 2
				left join location on location.location_id = preTarv_final.location_id
				left join obs marital_status_obs on marital_status_obs.person_id = person.person_id and marital_status_obs.concept_id=1054 and marital_status_obs.voided=0
				left join obs pregnancy_status_obs_1982 on pregnancy_status_obs_1982.person_id = person.person_id and  pregnancy_status_obs_1982.concept_id=1982 and pregnancy_status_obs_1982.obs_datetime=preTarv_final.enrollment_date
				left join obs pregnancy_status_obs_1279 on pregnancy_status_obs_1279.person_id = person.person_id and  pregnancy_status_obs_1279.concept_id=1279 and pregnancy_status_obs_1279.obs_datetime=preTarv_final.enrollment_date 
				left join patient_program pregnancy_status_patient_program on pregnancy_status_patient_program.patient_id = preTarv_final.patient_id and  pregnancy_status_patient_program.program_id=8 and pregnancy_status_patient_program.voided=0  		
WHERE 	enrollment_date IS not null		