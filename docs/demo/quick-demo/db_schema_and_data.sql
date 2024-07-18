create database etl_demo_src_db;

use etl_demo_src_db;

CREATE TABLE `person` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `full_name` varchar(250) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
   `uuid` char(38) NOT NULL,
    PRIMARY KEY (`id`),
  UNIQUE KEY `person_uk` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `person_id` int(11) not null,
  `full_address` varchar(500) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `uuid` char(38) NOT NULL,
   PRIMARY KEY (`id`),
   CONSTRAINT `fk1` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
   UNIQUE KEY `address_uk` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



create database etl_demo_dst_db;

use etl_demo_dst_db;

CREATE TABLE `person` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `full_name` varchar(250) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
   `uuid` char(38) NOT NULL,
    PRIMARY KEY (`id`),
  UNIQUE KEY `person_uk` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `person_id` int(11) not null,
  `full_address` varchar(500) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `uuid` char(38) NOT NULL,
   PRIMARY KEY (`id`),
   CONSTRAINT `fk1` FOREIGN KEY (`person_id`) REFERENCES `person` (`id`),
   UNIQUE KEY `address_uk` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO etl_demo_src_db.person (id,full_name,uuid) VALUES
	 ('100','Josh Muhamad','9acc18fb-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('101','Rasak Bustan','9acc29ab-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('102','Mary Cage','9acc2ab0-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('103','Urbanmo Bernado Kole','9acc2b04-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('104','Kumhalo Mbangalo','9acc2b62-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('105','Julios Pietre','9acc2ba4-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('106','Patricia Kumu','9acc2be0-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('107','Joao Almeida da Costa','9acc2c1e-44ef-11ef-97f9-e86a64ea1bc5'),
	 ('108','Rosimaria Augusto','9acc2c60-44ef-11ef-97f9-e86a64ea1bc5');


INSERT INTO etl_demo_src_db.address (person_id,full_address,creation_date,uuid) VALUES
	 (100,'Josh Muhamad''s address','2024-07-18 14:20:41','4b6f44d2-4500-11ef-97f9-e86a64ea1bc5'),
	 (101,'Rasak Bustan''s address','2024-07-18 14:20:41','4b6f48aa-4500-11ef-97f9-e86a64ea1bc5'),
	 (102,'Mary Cage''s address','2024-07-18 14:20:41','4b6f4987-4500-11ef-97f9-e86a64ea1bc5'),
	 (103,'Urbanmo Bernado Kole''s address','2024-07-18 14:20:41','4b6f4a04-4500-11ef-97f9-e86a64ea1bc5'),
	 (104,'Kumhalo Mbangalo''s address','2024-07-18 14:20:41','4b6f4ad4-4500-11ef-97f9-e86a64ea1bc5'),
	 (105,'Julios Pietre''s address','2024-07-18 14:20:41','4b6f4b43-4500-11ef-97f9-e86a64ea1bc5'),
	 (106,'Patricia Kumu''s address','2024-07-18 14:20:41','4b6f4bb0-4500-11ef-97f9-e86a64ea1bc5'),
	 (107,'Joao Almeida da Costa''s address','2024-07-18 14:20:41','4b6f4c1c-4500-11ef-97f9-e86a64ea1bc5'),
	 (108,'Rosimaria Augusto''s address','2024-07-18 14:20:41','4b6f4c92-4500-11ef-97f9-e86a64ea1bc5');
