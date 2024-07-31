create database etl_demo_params_power_src_db;

use etl_demo_params_power_src_db;

CREATE TABLE `office` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `uuid` char(38) NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `office_uk` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO office (id,name,uuid) VALUES
	 ('10001','Main office','9acc18fb-44ef-11ef-97f0-e86a64ea1bc5'),
	 ('10002','Anex 1','9acc29ab-44ef-11ef-97f9-e76a64ea1bc5'),
	 ('10003','Anex 1','9acc2be0-44ef-11ef-96f9-e86a64ea1bc5');


CREATE TABLE `person` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `full_name` varchar(250) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `office_id` int(11) NOT NULL, 
   `uuid` char(38) NOT NULL,
    PRIMARY KEY (`id`),
  UNIQUE KEY `person_uk` (`uuid`),
   CONSTRAINT `fk1` FOREIGN KEY (`office_id`) REFERENCES `office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO person (id,full_name, office_id, uuid) VALUES
	 ('100','Josh Muhamad',10001,'9acc18fb-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('101','Rasak Bustan',10002,'9acc29ab-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('102','Mary Cage',10001,'9acc2ab0-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('103','Urbanmo Bernado Kole',10003,'9acc2b04-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('104','Kumhalo Mbangalo',10003,'9acc2b62-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('105','Julios Pietre',10002,'9acc2ba4-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('106','Patricia Kumu',10001,'9acc2be0-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('107','Joao Almeida da Costa',10002,'9acc2c1e-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('108','Rosimaria Augusto',10002,'9acc2c60-44ef-11ef-97f9-e86a64ea1bc5');

CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `person_id` int(11) not null,
  `full_address` varchar(500) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `uuid` char(38) NOT NULL,
   PRIMARY KEY (`id`),
   CONSTRAINT `fk11` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
   UNIQUE KEY `address_uk` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO address (person_id,full_address,creation_date,uuid) VALUES
	 (100,'Josh Muhamad''s address','2024-07-18 14:20:41','4b6f44d2-4500-11ef-97f9-e86a64ea1bc5'),
	 (101,'Rasak Bustan''s address','2024-07-18 14:20:41','4b6f48aa-4500-11ef-97f9-e86a64ea1bc5'),
	 (102,'Mary Cage''s address','2024-07-18 14:20:41','4b6f4987-4500-11ef-97f9-e86a64ea1bc5'),
	 (103,'Urbanmo Bernado Kole''s address','2024-07-18 14:20:41','4b6f4a04-4500-11ef-97f9-e86a64ea1bc5'),
	 (104,'Kumhalo Mbangalo''s address','2024-07-18 14:20:41','4b6f4ad4-4500-11ef-97f9-e86a64ea1bc5'),
	 (105,'Julios Pietre''s address','2024-07-18 14:20:41','4b6f4b43-4500-11ef-97f9-e86a64ea1bc5'),
	 (106,'Patricia Kumu''s address','2024-07-18 14:20:41','4b6f4bb0-4500-11ef-97f9-e86a64ea1bc5'),
	 (107,'Joao Almeida da Costa''s address','2024-07-18 14:20:41','4b6f4c1c-4500-11ef-97f9-e86a64ea1bc5'),
	 (108,'Rosimaria Augusto''s address','2024-07-18 14:20:41','4b6f4c92-4500-11ef-97f9-e86a64ea1bc5');


CREATE TABLE `system_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `table_name` varchar(100) not null,
  `unique_key_field` varchar(100) NOT NULL,  
   PRIMARY KEY (`id`),
   UNIQUE KEY `system_table_uk` (`table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO system_table (table_name, unique_key_field) VALUES
	 ('office', 'uuid'),
	 ('person', 'uuid'),
	 ('address', 'uuid');
	 
	
create database etl_demo_params_power_dst_db;

use etl_demo_params_power_dst_db;

CREATE TABLE `office` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(250) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `uuid` char(38) NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `office_uk` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO office (id,name,uuid) VALUES
	 ('10001','Main office','9acc18fb-44ef-11ef-97f0-e86a64ea1bc5'),
	 ('10002','Anex 1','9acc29ab-44ef-11ef-97f9-e76a64ea1bc5'),
	 ('10003','Anex 1','9acc2be0-44ef-11ef-96f9-e86a64ea1bc5');


CREATE TABLE `person` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `full_name` varchar(250) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `office_id` int(11) NOT NULL, 
   `uuid` char(38) NOT NULL,
    PRIMARY KEY (`id`),
  UNIQUE KEY `person_uk` (`uuid`),
   CONSTRAINT `fk1` FOREIGN KEY (`office_id`) REFERENCES `office` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO person (id,full_name, office_id, uuid) VALUES
	 ('101','Rasak Bustan',10002,'9acc29ab-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('102','Mary Cage',10001,'9acc2ab0-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('103','Urbanmo Bernado Kole',10003,'9acc2b04-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('104','Kumhalo Mbangalo',10003,'9acc2b62-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('105','Julios Pietre',10002,'9acc2ba4-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('106','Patricia Kumu',10001,'9acc2be0-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('107','Joao Almeida da Costa',10002,'9acc2c1e-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('108','Rosimaria Augusto',10002,'9acc2c60-44ef-11ef-97f9-e86a64ea1bc5');

CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `person_id` int(11) not null,
  `full_address` varchar(500) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `uuid` char(38) NOT NULL,
   PRIMARY KEY (`id`),
   CONSTRAINT `fk11` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
   UNIQUE KEY `address_uk` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO address (person_id,full_address,creation_date,uuid) VALUES
	 (101,'Rasak Bustan''s address','2024-07-18 14:20:41','4b6f48aa-4500-11ef-97f9-e86a64ea1bc5'),
	 (103,'Urbanmo Bernado Kole''s address','2024-07-18 14:20:41','4b6f4a04-4500-11ef-97f9-e86a64ea1bc5'),
	 (104,'Kumhalo Mbangalo''s address','2024-07-18 14:20:41','4b6f4ad4-4500-11ef-97f9-e86a64ea1bc5'),
	 (105,'Julios Pietre''s address','2024-07-18 14:20:41','4b6f4b43-4500-11ef-97f9-e86a64ea1bc5'),
	 (107,'Joao Almeida da Costa''s address','2024-07-18 14:20:41','4b6f4c1c-4500-11ef-97f9-e86a64ea1bc5'),
	 (108,'Rosimaria Augusto''s address','2024-07-18 14:20:41','4b6f4c92-4500-11ef-97f9-e86a64ea1bc5');
	 
	 
CREATE TABLE `table_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `source_db` char(100) NOT NULL,    
  `table_name` char(100) NOT NULL,
  `qty_records_on_src` int NOT NULL,
  `qty_records_on_dst` int NOT null,
  PRIMARY KEY (`id`),
  UNIQUE KEY `table_info_uk` (`source_db`, `table_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
	 	 
	 