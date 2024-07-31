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
- (4) [Run the application using the conf.json as configuration file](https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#running-the-application). 

#### Etl with transformation (Using query datasource)
This example demonstrates how to perform a transformation on the ETL process using a sql query as additional data source. This example is similar to the previous one since the data models are the same, but in this case we will be using "extraQueryDataSource" instead of "extraTableDataSource". Note that sometimes we can use both in some cases. The process is based on [this configuration file](etl-with-query-data-source/conf.json).
Let's take a look at the configuration file. We will be focused on "etlItemConfiguration".

 ![etl-transformation](etl-with-query-data-source/transformation_with_query.png)


- (1) In this configuration we defined the **person** table as the main source table. Because we need additional data from another table, we added an "extraQueryDataSource" to allow the querying of address data. Note that in some cases we may need to use multiple queries and in that case we can add as many "extraQueryDataSource" as we need. 
- (2) The query we are using here is a very simple one, as it only query from one table. Note that we are using a parameter called "id" for person_id; this parameter will be picked up from the main src object, in this case the person table.
- (3) In the dstConf we highlighted the "mapping". Here we map only the fields on the destination table which cannot be automatically mapped, namely: "person_uuid", "person_creation_date". There is no need to map the other fields since they can be automatically mapped.

To run this demo example follow the instructions below:
- (1) Download the content of [this directory](etl-with-query-data-source).
- (2) Edit the [conf.json](etl-with-query-data-source/conf.json) file placing the correct values for the following attributes: "etlRootDirectory", "dataBaseUserName" and "dataBaseUserPassword".
- (3) Run the [sql script](etl-with-query-data-source/db_schema_and_data.sql) to create the databases. This script creates a src database filled with data and an empty dst database.
- (4) [Run the application using the conf.json as configuration file](https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#running-the-application) 

#### Using complex extraction queries
Sometime there is a need to use complex queries to define extraction rules. The below example illustrates the ETL configuration were additional rules are used for extraction. It is based on [this configuration file](extraction-rules/conf.json). Let's take a look at the database model and the complex extraction configuration. Take a look at the img below.

 ![etl-transformation](extraction-rules/extraction_rules.png)

Here the extra extraction rules are defined by two elements: the "selfJoinTables" and "extraConditionForExtract"
 
- (1) the "selfJoinTables" is a list of tables which helps to add conditions from other tables related to the main table. In this example, the main table is "person" and we want to add an extra extract condition from the table "office". So we listed it as a "selfJoinTable". Note that from the data model there is "joinFields" between the two tables as the "person" table has foreign reference to the "office" table. In case were there is no relationship defined from the data model the "joinFields" could be specified manually (see [selfJoinTables configuration](https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#the-selfjointables-table-configuration)).
- (2) within the selfJoinTable we can include additional joining conditions using the attribute "joinExtraCondition". In our example we want to extract only people which are allocated to an annexed office;
- (3) we can also add extra extract condition which does not use self joining tables; the attribute "extraConditionForExtract" allow a generic way to include extra condition for extraction in an ETL process. In our example we want to extract only people which are not present in the destination table which is etl_demo_with_extraction_rules_dst_db.person_data.

  To run this demo example follow the instrunctions below:
 - (1) Download the content of [this directory](extraction-rules).
- (2) Edit the [conf.json](extraction-rules/conf.json) file placing the correct values for the following attributes: "etlRootDirectory", "dataBaseUserName" and "dataBaseUserPassword".
- (3) Run the [sql script](extraction-rules/db_schema_and_data.sql) to create the databases. This script creates a src database filled with data and an empty dst database.
- (4) [Run the application using the conf.json as configuration file](https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#running-the-application)

#### The power of parameters
Parameters allow users to pass dynamic values to queries within the ETL item configuration. As detailed (here)[https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#the-use-of-params-whithin-src-configuration] , parameters can be utilized in four contexts: (1) as a SELECT field, (2) in a COMPARISON clause, (3) in an "IN" clause, and (4) as a DB RESOURCE.

In this demo, we will illustrate the versatility of parameters with an example that covers nearly all the contexts mentioned above.

Imagine we want to compare the record counts between tables from two databases, "A" and "B". Each database contains three tables: "office," "person," and "address." The image below depicts the data model for these databases.

 ![data-model](the-power-of-parameters/power_of_parameters_data_model.png)

To iterate through all the tables in the data model, we have added an auxiliary table called "system_table." This table lists all the tables we want to check and specifies the unique identifier field for each table, which serves as the join field between the source and destination tables. This example assumes a simple scenario where the unique key is composed of a single field. If the unique key comprised multiple fields, we might need to include additional columns for the unique key fields.

Additionally, we have included a table ("table_info") in the destination database to store the verification results.

Now, let's define the transformation rules. Our main source table is "system_table," and the destination table is "table_info." The image below highlights the use of parameters.

 ![etl-conf-file](the-power-of-parameters/etl_with_parameters.png)

In this ETL we have "system_table" as the main source table. Our destination table is "table_info". We are using two "extraQueryDataSource" to extract the count from the source table and destination table. Note that for each record from the "system_table" we use the field "table_name" as parameter to "extraQueryDataSource". Lets explain each parameter present on the queries
- (1) originAppLocationCode: here we use a parameter as a SELECT field; the value for this parameter is present on the configuration file;
- (2) table_name: here the parameter is used again as a SELECT field; the value will be picked up from the main source table. We intentionally put this field here in the "extraQueryDataSource" just for illustration, since this field is present on the main source table and will be automatically be mapped.
- (3) table_name: here the parameter is used in a context of DB_RESOURCE which is a table resource;
- (4) table_name: again here the parameter is used in a context of DB_RESOURCE;
- (5) and (6) unique_key_field: here the parameter is used as DB_RESOURCE of type field.   

  To run this demo example follow the instrunctions below:
 - (1) Download the content of [this directory](the-power-of-parameters).
- (2) Edit the [conf.json](the-power-of-parameters/conf.json) file placing the correct values for the following attributes: "etlRootDirectory", "dataBaseUserName" and "dataBaseUserPassword".
- (3) Run the [sql script](the-power-of-parameters/db_schema_and_data.sql) to create the databases. This script creates a src database filled with data and an empty dst database.
- (4) [Run the application using the conf.json as configuration file](https://github.com/csaude/openmrs-module-epts-etl/tree/master?tab=readme-ov-file#running-the-application)

#### Exploring the Field Transformer
A transformer allows custom transformation to a destination field through a java code. There are some field transformers that can be used out of the box, namely (1) the **ArithmeticFieldTransformer** which allow the evaluation of arithmetic expressions (2) **StringTranformer** which allow the transformation through string methods and (3) the **SimpleValueTranformer** which allow the direct transformation of srcValue.  In this section we will illustrate the use of these transformers.
In this demo we will generate a *Monthly Payslip* based on data contained in src tables and some transformations on these data. The image below shows the involved tables and the result we want to accomplish.

![transformation-with-transformers](out-of-the-box-transformers/payslip.png)

We will be using [this configuration file](out-of-the-box-transformers/conf.json) and below we highlight the "dstConf"

![transformation-with-transformers](out-of-the-box-transformers/out-of-box-transformers.png)

- (1) here we do the necessary transformation to fill the field "pos" in the report. We are using *ArithmeticFieldTransformer*. The value we want to evaluate is contained in the field "srcValue". Note the presence of 3 parameters @year and @month which will be picked from the configuration parameters, @id which will be picked from the main src object.
- (2) here we do the necessary transformation to fill the field "full_month" in the report. Here we are using the *SimpleValueTranformer*. Note that we omitted the transformer as it will be automatically detected by the application. The *SimpleValueTranformer* allow the transformation of constant values or values from parameters.
- (3) here we use *StringTransformer*      


