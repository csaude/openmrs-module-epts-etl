create database etl_demo_field_transformer_db;

use etl_demo_field_transformer_db;

CREATE TABLE `salary_parameter` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `scale` varchar(30) NOT NULL,
  `base` double NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `salary_parameter_uk` (`scale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO salary_parameter (id,scale,base) VALUES
	 ('1','SCLAE_I', 150),
	 ('2','SCLAE_II', 200),
	 ('3','SCLAE_III', 275);


CREATE TABLE `employer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `full_name` varchar(250) NOT NULL,
  `salary_scale_id` int(11) NOT NULL,
   PRIMARY KEY (`id`),
   CONSTRAINT `fk1` FOREIGN KEY (`salary_scale_id`) REFERENCES `salary_parameter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO employer (id, full_name, salary_scale_id) VALUES
	 (1, 'Risimey Khumalu',3),
	 (2, 'Barnabe Rustafar',2),
	 (3, 'Murth Jr',2),
	 (4, 'Matakule Julamo',3),
	 (5, 'Thomas Julai',1),
	 (6, 'Yumna Gugunhanhe',1),
	 (7, 'Lolocate Joane',2),
	 (8, 'Phumalang Juma',3),
	 (9, 'Lazaro Marmajo',1),
	 (10, 'Rasack Mane',1);

CREATE TABLE `employer_work_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `year` int(4) not null,
  `month` int(2) not null,
  `employer_id` int(11) NOT NULL,
  `worked_days` int(11) NOT NULL, 
   PRIMARY KEY (`id`),
   CONSTRAINT `employer_work_info_fk1` FOREIGN KEY (`employer_id`) REFERENCES `employer` (`id`),
   UNIQUE KEY `employer_work_info_UK` (`employer_id`, `year`, `month`)    
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO employer_work_info (year, month, employer_id, worked_days) VALUES
	 (2024, 8, 1, 30),
	 (2024, 8, 2, 28),
	 (2024, 8, 3, 30),
	 (2024, 8, 4, 27),
	 (2024, 8, 5, 27),
	 (2024, 8, 6, 29),
	 (2024, 8, 7, 30),
	 (2024, 8, 8, 26),
	 (2024, 8, 9, 30),
	 (2024, 8, 10, 29);

CREATE TABLE `monthly_payslip` (
  `pos` int(11),
  `full_month` varchar(7),
  `full_name` varchar(250) NOT NULL,
  `salary` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	 