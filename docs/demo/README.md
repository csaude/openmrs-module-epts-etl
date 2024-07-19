## Etl quick examples

NOTE that All the demo examples use mysql as dbms, but you can change the db configuration to run with any of the following databases: postgres, oracle, sqlserver.

#### Simple etl process.
This example shows the basic ETL process where the records from one database are copied to the destination database. This is a very basic process because there is no transformation between the processes. The src and dst structure is the very same between the two databases.
To run this demo follow the instructions below:
- (1) Download the content of [this directory](quick-demo).
- (2) Edit the [conf.json](quick-demo/conf.json) file placing the correct values for the following attributes: "etlRootDirectory", "dataBaseUserName" and "dataBaseUserPassword".
- (3) Run the [sql script](quick-demo/db_schema_and_data.sql) to create the databases. This script creates a src database filled with data and an empty dst database.
- (4) [Run the application using the conf.json as configuration file](https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#running-the-application)

#### Etl with transformation
This example demonstrates how to perform a transformation on the ETL process. The transformation is needed when there are differences between the source table and destination table or if there are multiple data sources to one destination. We want to bring together data from two tables. The process is based on [this configuration file](etl-with-transformation/conf.json).
Let's take a look at the configuration file. We will be focused on "etlItemConfiguration". In below image we represent in the left the data model of source and destination database.

 ![etl-transformation](etl-with-transformation/transformation_configuration.png)


- (1) In this configuration we defined the **person** table as the main source table. Because we also want data from the **address** table, we added it to the list of "extraTableDataSource". Note the use of "INNER" on joinType. This means that only "person" with "address" will be picked up.
- (2) In the dstConf we highlighted the "mapping". Here we map only the fields on the destination table which cannot be automatically mapped, namely: "person_uuid", "person_creation_date", "address_uuid" and "address_creation_date". There is no need to map the other fields since they can be automatically mapped.
- (3) note that for each mapping there is "dataSourceName", "srcField", "dstField". Note that we have two data sources, "person" and "address" and we map their fields accordling.
- (4) note that we added the field "creation_date" on the "ignorableFields" because we do want to include it on the transformation as it has default value on destination table which is CURRENT_TIMESTAMP.

  To run this demo example follow the instrunctions below:
 - (1) Download the content of [this directory](etl-with-transformation).
- (2) Edit the [conf.json](etl-with-transformation/conf.json) file placing the correct values for the following attributes: "etlRootDirectory", "dataBaseUserName" and "dataBaseUserPassword".
- (3) Run the [sql script](etl-with-transformation/db_schema_and_data.sql) to create the databases. This script creates a src database filled with data and an empty dst database.
- (4) [Run the application using the conf.json as configuration file](https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#running-the-application) 
