# Introduction
The eptssync module is an OpenMRS module designed to perform several operations related to data exchange between OpenMRS databases and other data models. The eptssync module can be integrated to an OpenMRS instance BUT can also run as a stand-alone java application. Take in mind that this module is still in development and some of its features may not well function now.
# Architecture overview
The EPTSSync module is written in Java and follows the OpenMRS modules pattern. Its core is implemented on the API level so it can run as a stand-alone application.
On the top of EPTSSync logic there is Processes which represents a set of operations which can be intended as tasks which together complete a certain objective.

 ![eptssync_arquitecture](docs/Eptssync_Arquitecture.png)

From the code perspective an process is handled by the [ProcessController](api/src/main/java/org/openmrs/module/eptssync/controller/ProcessController.java) class and the tasks or operations are handled by [OperationController](api/src/main/java/org/openmrs/module/eptssync/controller/OperationController.java) class. 
 
The process and its operations are configured via JSON file on which all the necessary information for a process to be run are specified. The configuration file will determine which kind of process must be performed.
 
The process configuration is mapped to [SyncConfiguration](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncConfiguration.java) class and each operation are mapped to [SyncOperationConfig](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncOperationConfig.java).
Each operation defined in the process configuration file will perform the very same task on all tables listed on the configuration file. The table configuration is mapped to [SyncTableConfiguration](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncTableConfiguration.java).
 
An [Operation Controller](api/src/main/java/org/openmrs/module/eptssync/controller/OperationController.java) performs its task using an [Engine](api/src/main/java/org/openmrs/module/eptssync/engine/Engine.java). The engines are monitored by [EngineMonitor](api/src/main/java/org/openmrs/module/eptssync/monitor/EngineMonitor.java) class. The interaction between the core classes is illustrated on the image below.
 
![how-the-process-is-performed](docs/how-the-process-is-performed.png)
 
## The Process Configuration File
The process configuration file is the heart of the application. For each process type there is a specific configuration setup which must be done. A configuration file is a JSON file which in almost all cases has 4 sections as shown below.

 ![config-sections](docs/config-sections.png)
 
- **The section 1** contains the general configurations usually applied to all the operations and involved tables.
- **The section 2** defines the auxiliary application info. Usually an application info defines database connection info for example for source database and/or destination database.
- **The section 3** defines the operations configuration parameters.
- **The section 4** lists the involved tables and some specific configurations for each table.

## The common configuration
Below are listed the parameters which can appear in the first section of the configuration file:
- *processType*: A string representing the Process Type. The supported type is listed on the section "Supported Types"
- *modelType*: the database model type. There are two types of models: "OPENMRS" and "OTHER". An OPENMRS model is the OpenMRS Data Model and any other model which is not OpenMRS is treated as "OTHER"
- *syncRootDirectory*: a full path to directory where the process stuff will be placed on
- *classPath*: a full path to the application jar
- *childConfigFilePath*: a full path to another JSON configuration file which defines a process which will be executed when the current is finished. This parameter enables the possibility to execute several processes in sequence. This can be useful for ex. if there is a need to perform a merge of multiple databases.
- *originAppLocationCode*: a token representing the location where the process is running for. In the case of the merge process this will be the source location.
- *automaticStart*: a boolean indicating that the process related to this configuration file will automatically start or not. 
- *observationDate*: an observation date in milliseconds. These parameters indicate which records should be involved in the process in terms of dates. These dates could be: date of creation, date of updates. The fields to be checked should appear on the table configuration.

## The AppsInfo configuration
As said before the appsInfo section contains the configurations of source and/or destination database. It is a list of objects each one representing an appInfo. Below are listed the common parameters which can be configured in each appInfo.
- *applicationCode*: the code of the application.
- *pojoPackageName*: the java package name where the autogenerated Pojo classes will be placed on
- *connInfo*: an object defining the database connection parameters.


## The tables configuration
The table configuration section lists the tables which will be involved in the process. Each operation in a process will perform its task on these tables. Below are listed the properties which can appear in a table configuration.
- *tableName*: table name
- *parents*: list of configured parents. Note that if there is no additional configuration for the parent, there is no need to include this properitie;
- *conditionalParents*: the conditional parents are parents that have no database referential relationship. For ex. in openmrs model there is a relationship between *person_attribute* and *location*. This relationship exists when some conditions are observed (when the person_attribute.value=7)  
- *metadata*: a boolean indicating that the table is a metadata table;
- *removeForbidden*: a boolean that indicate if records from this table can be automatically removed when there is inconsistencies
- *observationDateFields*: list of date fields which will be checked when an operation need to look for records which had some action in certain period (ex. records created or updated within a period)
- *sharePkWith*: this indicate if the primary key of this table is shared with a parent. In this case that parent should be mentioned here.
- *extraConditionForExport*: the extra sql condition to be injected when the operation queries for records to process.

###### Parents configuration
A parent if configured as an object and can have additional properties. Note that when there are no additional properties you can omit the parent on the list of parents. When the parent appear on the table configuration it can have below properties:
- *tableName*: the table name
- *defaultValueDueInconsistency*: default value which will be used when the inconsistency check find any orphan record of this parent
- *setNullDueInconsistency*: a boolean which indicate that if the record is orphan of this parent then the field can be nulled

###### Conditional parents configuration
The conditional parents are parents that have no database referential relationship. For ex. in openmrs model there is a relationship between *person_attribute* and *location*. This relationship exists when some conditions are observed (when the person_attribute.value=7)  
A conditional parent is configured as an object and can have bellow properties:  
- *tableName*: the parent table name
- *refColumnName*: the name of column which has conditional relationship
- *conditionField*: the name of column which determines the conditional relationship;
- *conditionValue*: the conditional value for *conditionField*.

## Supported processes and its configuration files
In this section are listed all the avaliable process and the template of its configuration files.

###### QUICK_MERGE_WITH_ENTITY_GENERATION

This process performs a merge of a source database to a destination database in the same network. The process is called "quick" because no staging area and no data transport is done here. the operation for this process are:
- *DATABASE_PREPARATION*: prepare the sync stage area database;
- *POJO_GENERATION*: Generated the java POJO classes of the involved tables
- *DB_QUICK_MERGE_EXISTING_RECORDS*: performs the updates of existing records in destination database
- *DB_QUICK_MERGE_MISSING_RECORDS*: performs the merge of missing records in destination database

Note that since the POJO classes are dynamically generated (both for source and destination dbs) the source and destination model can be slightly different.  

The template for this process can be found [here](docs/process_templates/quick_merge_with_entity_generation.json)  



###### QUICK_MERGE_WHITOUT_ENTITY_GENERATION
This process is similar to QUICK_MERGE_WITH_ENTITY_GENERATION BUT here no POJO is generated since the process assumes that the two data models (source and destination) are uniform. But to use this process you need to first run the QUICK_MERGE_WITH_ENTITY_GENERATION so that the POJO will be generated and for the next merges you can use this process.

The template for this process can be found [here](docs/process_templates/quick_merge_whitout_entity_generation.json)

###### DB_INCONSISTENCY_CHECK
This process performs a referential inconsistency check on a specific database. One of the follow action will be taken to identified inconsistencies:
- The offending records will be moved from the database;
- The missing records will be replaced by the default parents.

All the affected records will be recorded in a staging area in a table called "inconsistence_info".
This process is performed by the follow operations:
- DATABASE_PREPARATION: prepare the sync stage area database;
- POJO_GENERATION: Generated the java POJO classes of the involved tables
- INCONSISTENCY_SOLVER: perform the inconsistency check and solver

The template for this process can be found [here](docs/process_templates/db_inconsistency_check.json)


###### DB_RE_SYNC
Performe the database re-sync from an openmrs database to dbsync application. This process is performed using the follow operations
- DATABASE_PREPARATION: prepare the sync stage area database;
- CHANGED_RECORDS_DETECTOR: performe a resync of updated records; 
- NEW_RECORDS_DETECTOR: performe a resync of new records. 

The template for this process can be found [here](docs/process_templates/db_re_sync.json)

###### DB_EXPORT
This process can be used to perform a remote sync between two databases using json files. The full sync process will need an [DATABASE_MERGE_FROM_JSON](#DATABASE_MERGE_FROM_JSON) to be run in the destination database.
The DB_EXPORT process is run in the source database and uses below operations:
- DATABASE_PREPARATION: prepare the sync stage area database;
- POJO_GENERATION: Generated the java POJO classes of the involved tables
- INCONSISTENCY_SOLVER: perform the inconsistency check and solver on the database
- EXPORT: export the database to json files
- TRANSPORT: transport the json files from source to the destination server. NOTE that by now the transport is using a simple copy command from one folder to another but in the future the transport could support injection of several transport mechanisms.

The template for this process can be found [here](docs/process_templates/db_export.json)

###### DATABASE_MERGE_FROM_JSON
This process completes the sync process started by a DB_EXPORT process. This process is supposed to run in a destination database.  
To perform its task this process uses below operations:
- DATABASE_PREPARATION: prepare the sync stage area database;
- POJO_GENERATION: Generated the java POJO classes of the involved tables
- LOAD: load json files to stage area
- DB_MERGE_FROM_JSON: perform the merge on destination using the stage area json data;
- CONSOLIDATION: perform the referential data consolidation 
- 
The template for this process can be found [here](docs/process_templates/database_merge_from_json.json)

# Running the application
To run this application you should (1) get the jar file either from the releases or (2) cloning and compiling the [eptssync project](https://github.com/FriendsInGlobalHealth/openmrs-module-eptssync.git).

If you go for the second option follow the steps bellow from your machine
```
git clone https://github.com/FriendsInGlobalHealth/openmrs-module-eptssync.git
cd openmrs-module-eptssync
mvn clean install -DskipTests
```

Once you have the jar and have set up the configuration file (or configurations file) you run the application hitting the below command.
```
java -Dlog.level=LOG_LEVEL -jar eptssync-api-1.0-SNAPSHOT.jar "path/to/configuration/file"
```
The LOG_LEVEL can be one of the following: DEBUG, INFO, WARN, ERR
