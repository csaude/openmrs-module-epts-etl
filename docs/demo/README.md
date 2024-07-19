## Etl quick examples

NOTE that All the demo examples use mysql as dbms, but you can change the db configuration to run with any of the following databases: postgres, oracle, sqlserver.

#### Simple etl process.
This example shows the basic etl process where the records from one database are copied to the destination database. This is a very basic process because there is no transformation between the processes.
To run this demo follow the instructions below:
- (1) Download the content of [this directory](quick-demo).
- (2) Edit the [conf.json](quick-demo/conf.json) file placing the correct values for the following attributes: "etlRootDirectory", "dataBaseUserName" and "dataBaseUserPassword".
- (3) Run the [sql script](quick-demo/db_schema_and_data.sql) to create the databases. This script creates a src database filled with data and an empty dst database.
- (4) [Run the application using the conf.json as configuration file](https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#running-the-application)

#### Etl with transformation
In this example we will demostrate how to perfome a transformation on the ETL process. The transformation is needed when there are differences between the souce table and destination table or if there are multiple datasources to one destination. We want to bring together data from two tables. 
