select 	women_status
from  ( 
		select 	inicio_real.patient_id,
				gravida_real.data_gravida,  lactante_real.data_parto,
				if(
					max(gravida_real.data_gravida) is null and max(lactante_real.data_parto) is null,null,
					if(
						max(gravida_real.data_gravida) is null,'Lactante',
						if(
							max(lactante_real.data_parto) is null,'Gravida',
							if(
									max(lactante_real.data_parto) > max(gravida_real.data_gravida),'Lactante','Gravida')
								)
							)
				) women_status 
							
		from (	 
				select	p.patient_id
				from 	patient p  inner join encounter e on e.patient_id=p.patient_id
				where 	p.patient_id = ?
						and e.voided=0 
						and p.voided=0 
						and e.encounter_type in (5,7) 
						and e.encounter_datetime <= ? 
				
				union  
				
				select 	pg.patient_id 
				from 	patient p inner join patient_program pg on p.patient_id = pg.patient_id
				where 	p.patient_id = ? 
						and pg.voided=0 
						and p.voided=0 
						and program_id in (1,2) 
						and date_enrolled <= ? 
					
				union  
				
				Select p.patient_id 
				from 	patient p 	inner join encounter e on p.patient_id=e.patient_id
									inner join obs o on e.encounter_id=o.encounter_id
				where 	p.patient_id = ?
						and p.voided=0 
						and e.voided=0 
						and o.voided=0 
						and e.encounter_type=53 
						and o.concept_id=23891 
						and o.value_datetime is not null 
						and o.value_datetime <= ? 
			) inicio_real  left join  (
										Select 	p.patient_id,
												e.encounter_datetime data_gravida 
										from patient p 	inner join encounter e on p.patient_id=e.patient_id 
														inner join obs o on e.encounter_id=o.encounter_id
										where 	p.patient_id = ?
												and p.voided=0 
												and e.voided=0 
												and o.voided=0 
												and concept_id=1982 
												and value_coded=1065 
												and e.encounter_type in (5,6) 
												and e.encounter_datetime  between ? and ? 
										
										union  
										
										Select 	p.patient_id,
												e.encounter_datetime data_gravida 
										from 	patient p 	inner join encounter e on p.patient_id=e.patient_id
															inner join obs o on e.encounter_id=o.encounter_id
										where 	p.patient_id = ?
												and p.voided=0 
												and e.voided=0 
												and o.voided=0 
												and concept_id=1279 and 
												e.encounter_type in (5,6) 
												and e.encounter_datetime between ? and ? 
										
										union  
										
										Select 	p.patient_id,
												e.encounter_datetime data_gravida 
										from patient p 	inner join encounter e on p.patient_id=e.patient_id
														inner join obs o on e.encounter_id=o.encounter_id
										where 	p.patient_id = ?
												and p.voided=0 
												and e.voided=0 
												and o.voided=0 
												and concept_id=1600 
												and e.encounter_type in (5,6) 
												and e.encounter_datetime between ? and ? 
										
										union  
										
										Select 	p.patient_id,
												e.encounter_datetime data_gravida 
										from patient p 	inner join encounter e on p.patient_id=e.patient_id
														inner join obs o on e.encounter_id=o.encounter_id
										where 	p.patient_id = ?
												and p.voided=0 
												and e.voided=0 
												and o.voided=0 
												and concept_id=6334 
												and value_coded=6331 
												and e.encounter_type in (5,6) 
												and e.encounter_datetime between ? and ? 
										
										union  
										
										select 	pp.patient_id,
												pp.date_enrolled data_gravida 
										from 	patient_program pp
										where 	pp.patient_id = ?
												and pp.program_id=8 
												and pp.voided=0 
												and pp.date_enrolled between ? and ? 
										
										union
										
										Select 	p.patient_id,
												obsART.value_datetime data_gravida 
										from patient p 	inner join encounter e on p.patient_id=e.patient_id
														inner join obs o on e.encounter_id=o.encounter_id
														inner join obs obsART on e.encounter_id=obsART.encounter_id
										where 	p.patient_id = ?
												and p.voided=0 
												and e.voided=0 
												and o.voided=0 
												and o.concept_id=1982 
												and o.value_coded=1065 
												and e.encounter_type=53 
												and obsART.value_datetime between ? and ? 
												and obsART.concept_id=1190 
												and obsART.voided=0  
										
										union
										
										Select 	p.patient_id,
												o.value_datetime data_gravida 
										from 	patient p 	inner join encounter e on p.patient_id=e.patient_id
															inner join obs o on e.encounter_id=o.encounter_id
										where 	p.patient_id = ?
												and p.voided=0 
												and e.voided=0 
												and o.voided=0 
												and o.concept_id=1465 
												and e.encounter_type=6 
												and o.value_datetime between ? and ? 
									) gravida_real on gravida_real.patient_id = inicio_real.patient_id	left join   (
																														Select 	p.patient_id,
																																o.value_datetime data_parto 
																														from 	patient p 	inner join encounter e on p.patient_id=e.patient_id
																																			inner join obs o on e.encounter_id=o.encounter_id
																														where  	p.patient_id = ?
																																and p.voided=0 
																																and e.voided=0 
																																and o.voided=0 
																																and concept_id=5599 
																																and e.encounter_type in (5,6) 
																																and o.value_datetime between ? and ? 
																															
																														union  
																														
																														Select 	p.patient_id, 
																																e.encounter_datetime data_parto 
																														from 	patient p 	inner join encounter e on p.patient_id=e.patient_id
																																			inner join obs o on e.encounter_id=o.encounter_id
																														where 	p.patient_id = ?
																																and p.voided=0 
																																and e.voided=0 
																																and o.voided=0 
																																and concept_id=6332 
																																and value_coded=1065 
																																and	e.encounter_type=6 
																																and e.encounter_datetime between ? and ? 
																														
																														union  
																														
																														Select 	p.patient_id, 
																																obsART.value_datetime data_parto 
																														from 	patient p 	inner join encounter e on p.patient_id=e.patient_id
																																			inner join obs o on e.encounter_id=o.encounter_id
																																			inner join obs obsART on e.encounter_id=obsART.encounter_id
																														where 	p.patient_id = ?
																																and p.voided=0 
																																and e.voided=0 
																																and o.voided=0 
																																and o.concept_id=6332 
																																and o.value_coded=1065 
																																and e.encounter_type=53 
																																and obsART.value_datetime between ? and ? 
																																and obsART.concept_id=1190 
																																and obsART.voided=0  
																														
																														union
																														
																														Select 	p.patient_id, 
																																e.encounter_datetime data_parto 
																														from 	patient p 	inner join encounter e on p.patient_id=e.patient_id
																																			inner join obs o on e.encounter_id=o.encounter_id
																														where 	p.patient_id = ?
																																and p.voided=0 
																																and e.voided=0 
																																and o.voided=0 
																																and concept_id=6334 
																																and value_coded=6332 
																																and e.encounter_type in (5,6) 
																																and e.encounter_datetime between ? and ? 
																														
																														union  
																														
																														select 	pg.patient_id,
																																ps.start_date data_parto 
																														from 	patient p 	inner join patient_program pg on p.patient_id=pg.patient_id
																																			inner join patient_state ps on pg.patient_program_id=ps.patient_program_id
																														where 	p.patient_id = ? 
																																and pg.voided=0 
																																and ps.voided=0 
																																and p.voided=0 
																																and pg.program_id=8 
																																and ps.state=27 
																																and ps.start_date between ? and ? 
																													) lactante_real on lactante_real.patient_id=inicio_real.patient_id
			where lactante_real.data_parto is not null or gravida_real.data_gravida is not null
			group by inicio_real.patient_id  
	) gravidaLactante inner join person pe on pe.person_id=gravidaLactante.patient_id		 
where pe.voided=0 and pe.gender='F';