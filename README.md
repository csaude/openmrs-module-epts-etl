# Introduction
The eptssync module is an OpenMRS module designed to perform several operations related to data synchronization between OpenMRS databases and other data models. It can also act as an ETL tool. The eptssync module can be integrated to an OpenMRS instance BUT can also run as a stand-alone java application. Take in mind that this module is still in development and some of its features may not well function now.
# Architecture overview
The EPTSSync module is written in Java and follows the OpenMRS modules pattern. Its core is implemented on the API level so it can run as a stand-alone application.
On the top of EPTSSync  logic there is Processes which represents a set of operations which can be intended as tasks which together complete a process of ETL.

 ![eptssync_arquitecture](docs/Eptssync_Arquitecture.png)

Some ETL may require a set of processes to have a full job done.
From the code perspective an process is handled by the [ProcessController](api/src/main/java/org/openmrs/module/eptssync/controller/ProcessController.java) class and the tasks or operations are handled by [OperationController](api/src/main/java/org/openmrs/module/eptssync/controller/OperationController.java) class. 
 
The process and its operations are configured via JSON file on which all the necessary information for a process to be run are specified. The configuration file will determine which kind of process must be performed.
 
The process configuration is mapped to [SyncConfiguration](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncConfiguration.java) class and each operation are mapped to [SyncOperationConfig](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncOperationConfig.java).
Each operation defined in the process configuration file will perform the very same task on all tables listed on the configuration file. The table configuration is mapped to [SyncTableConfiguration](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncTableConfiguration.java).
 
An [Operation Controller](api/src/main/java/org/openmrs/module/eptssync/controller/OperationController.java) performs its task using an [Engine](api/src/main/java/org/openmrs/module/eptssync/engine/Engine.java). The engines are monitored by [EngineMonitor](api/src/main/java/org/openmrs/module/eptssync/monitor/EngineMonitor.java) class.
 
The interaction between the core classes is illustrated on the image below.
 
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

### Parents configuration
A parent if configured as an object and can have additional properties. Note that when there are no additional properties you can omit the parent on the list of parents. When the parent appear on the table configuration it can have below properties:
- *tableName*: the table name
- *defaultValueDueInconsistency*: default value which will be used when the inconsistency check find any orphan record of this parent
- *setNullDueInconsistency*: a boolean which indicate that if the record is orphan of this parent then the field can be nulled

### Conditional parents configuration
The conditional parents are parents that have no database referential relationship. For ex. in openmrs model there is a relationship between *person_attribute* and *location*. This relationship exists when some conditions are observed (when the person_attribute.value=7)  
A conditional parent is configured as an object and can have bellow properties:  
- *tableName*: the parent table name
- *refColumnName*: the name of column which has conditional relationship
- *conditionField*: the name of column which determines the conditional relationship;
- *conditionValue*: the conditional value for *conditionField*. 
