# Introdution
The eptssync module is an OpenMRS module designed to perform several operations related to data synchronization between OpenMRS databases and other data models. It can also act as an ETL tool. The eptssync module can be integrated to an OpenMRS instance BUT can also run as a standa-alone java application. Take in mind that this module is still in development and some of its features may not well function now.
# Arquitecture overview
The eptssync module is writen in java and follow the openmrs modules pattern. It's core is implemented on the api level so it can run as stand-allone application.
On the top of eptssync logic there is Processes which represents a set of operations which can be intended as tasks which together complete a process of ETL.
 ![eptssync_arquitecture](docs/Eptssync_Arquitecture.png)
 Some ETL may require a set of processes to have a full job done.
 From the code perspective an process is handled by the [ProcessController](api/src/main/java/org/openmrs/module/eptssync/controller/ProcessController.java) class and the tasks or operations are handled by [OperationController](api/src/main/java/org/openmrs/module/eptssync/controller/OperationController.java) class. 
 
 The process and its operations are configured via json file on which all the necessary informations for a process to be run are specified. The configuration file will determine which kind of process must be performed.
 
 The process configuration is mapped to [SyncConfiguration](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncConfiguration.java) class and each operation are mapped to [SyncOperationConfig](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncOperationConfig.java).
 Each operation defined in the process configuration file will performe the very same task on all tables listed on the configuration file. The table configuration is mapped to [SyncTableConfiguration](api/src/main/java/org/openmrs/module/eptssync/controller/conf/SyncTableConfiguration.java). 
 
 ## The Process Configuration File
 The process configuration file
 
 
