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
