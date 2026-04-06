# Introduction
The sisrme-etl module is an OpenMRS module designed to support generic and extensible ETL (Extract, Transform, Load) operations, along with additional data processing capabilities. It provides a flexible framework for defining data migration, transformation, and synchronization workflows across different data sources.

The sisrme-etl module can be integrated with an OpenMRS instance or executed as a stand-alone Java application, allowing it to be used in a wide range of deployment scenarios.

Please note that this module is currently under active development. As a result, some features may be incomplete, subject to change, or not yet fully stable.

# Architecture overview
The sisrme-etl module is implemented in Java and follows the OpenMRS module architecture. Its core functionality resides at the API level, enabling it to run both as part of an OpenMRS instance or as a stand-alone application.

At the highest level of the sisrme-etl architecture are **Processes**, which represent a collection of operations executed together to achieve a specific objective. Each process encapsulates the full workflow required to perform a data migration or transformation task.

![eptssync_arquitecture](docs/Eptssync_Arquitecture.png)

From a code perspective, a process is managed by the [ProcessController](api/src/main/java/org/openmrs/module/epts/etl/controller/ProcessController.java), while individual tasks (operations) are handled by the [OperationController](api/src/main/java/org/openmrs/module/epts/etl/controller/OperationController.java).

Processes and their corresponding operations are defined through a JSON configuration file. This file contains all the necessary information required to execute a process and determines the type of ETL workflow to be performed.

The process configuration is mapped to the [EtlConfiguration](api/src/main/java/org/openmrs/module/epts/etl/conf/EtlConfiguration.java) class, while each operation is mapped to the [EtlOperationConfig](api/src/main/java/org/openmrs/module/epts/etl/conf/EtlOperationConfig.java) class.

Each operation defined in the configuration file is applied uniformly across all configured items. These items are defined using the [EtlItemConfiguration](api/src/main/java/org/openmrs/module/epts/etl/conf/EtlItemConfiguration.java), which specifies the rules for extraction, transformation, and loading.

An [OperationController](api/src/main/java/org/openmrs/module/epts/etl/controller/OperationController.java) executes its tasks using a [TaskProcessor](api/src/main/java/org/openmrs/module/epts/etl/engine/TaskProcessor.java). These processors are orchestrated and monitored by the [Engine](api/src/main/java/org/openmrs/module/epts/etl/engine/Engine.java), which manages the execution lifecycle of the ETL process.

The interaction between these core components is illustrated in the diagram below.

![how-the-process-is-performed](docs/how-the-process-is-performed.png)

## The Process Configuration File
The process configuration file is the core element of the application. Each process type requires a specific configuration structure that defines how the ETL workflow should be executed. The configuration is provided as a JSON file, which in most cases is organized into four main sections, as illustrated below.

![config-sections](docs/config-sections.png)

- **Section 1** contains general configurations that are typically applied across all operations and ETL items within the process.
- **Section 2** defines the database connection details for the source and/or destination systems.
- **Section 3** specifies the configuration parameters for each operation in the process.
- **Section 4** lists the ETL item configurations, defining the rules for how data extraction, transformation, and loading should be performed.

## The common configuration
- *processType*: A string representing the process type. The supported types are listed in the section "Supported Process Types".
- *etlRootDirectory*: The absolute path to the directory where all process-related files will be stored.
- *childConfigFilePath*: The absolute path to another JSON configuration file defining a process to be executed after the current one completes. This enables chaining multiple processes in sequence, which is useful for scenarios such as merging multiple databases.
- *originAppLocationCode*: A token representing the location where the process is executed. In merge scenarios, this typically represents the source location.
- *manualStart*: An optional boolean indicating whether the process should be started manually. If set to true, the process will not start automatically when the application starts.
- *params*: A map of configurable parameters that can be referenced throughout the ETL configuration, typically within queries and transformers.
- *disabled*: Indicates whether the process is disabled.
- *syncStageSchema*: An optional value defining the database schema used for staging ETL data. If not specified, the default schema "etl_stage_area" will be used.
- *doNotTransformsPrimaryKeys*: Indicates whether primary keys should be transformed. If false, source primary keys are preserved in the destination; if true, new primary keys are generated.
- *manualMapPrimaryKeyOnField*: If specified, this field will be used as the primary key for tables that do not explicitly define one but contain a matching field. This value can be overridden at the ETL item configuration level.
- *relationshipResolutionStrategy*: Defines how the ETL engine handles relationship (foreign key) resolution. By default, the engine attempts to resolve and validate related records in the destination database.
  Supported values:
  - *RESOLVE* – Default behavior. The ETL engine resolves and retrieves the corresponding record in the destination database.
  - *SKIP* – Skips relationship resolution. The transformed value is written directly to the destination field without validation.
  - *VALIDATE_ONLY* – Only validates that the referenced record exists in the destination database. If not, the process reacts according to the configured error handling strategy.
- *autoIncrementHandlingType*: Defines how auto-increment fields defined at the database schema level should be handled.
  - *AS_SCHEMA_DEFINED* – The ETL process respects the auto-increment behavior defined in the schema (default).
  - *IGNORE_SCHEMA_DEFINITION* – The ETL process ignores the schema definition and manually controls primary key generation.
  This value can be overridden at the ETL item configuration level.
- *primaryKeyInitialIncrementValue*: A numeric value added to the primary key of the first destination record across all configured tables. This property cannot be used when *autoIncrementHandlingType* is set to *AS_SCHEMA_DEFINED*. If provided without explicitly setting *autoIncrementHandlingType*, the value will default to *IGNORE_SCHEMA_DEFINITION*. This setting can be overridden at the *EtlItemConfiguration* or *DstConf* level.
- *dynamicSrcConf*: Enables dynamic configuration generation based on data stored in a database table. This allows a single configuration file to act as a template that is expanded into multiple configurations using table records. This is particularly useful when processing multiple source databases or environments dynamically.
- *finalizer*: Defines additional tasks to be executed after the process has completed.
- *startupScripts*: A list of SQL scripts to be executed during application startup. Scripts must be located in *@etlRootDirectory/dump-scripts/startup*. Scripts should be idempotent to avoid inconsistencies across multiple executions.
- *reRunable*: By default, completed processes are not re-executed. When set to true, the process can be restarted and executed again from the beginning.
- *relatedEtlSrcTables*: Defines additional source tables that are not explicitly configured but are related to the main source tables. Declaring these tables ensures they are treated as part of the ETL domain rather than metadata, which is critical for correct relationship resolution, joins, and dependency handling.
- *defaultInconsistencyBehavior*: Defines the default behavior when a data inconsistency is detected during the ETL process. This acts as a global fallback when no specific behavior is defined at a lower level (e.g., field or transformer).
  Supported values:
  - *MARK_RECORD_AS_FAILED* – The record is marked as failed and processing continues.
  - *SET_TO_NULL* – The affected field value is set to null.
  - *ABORT_PROCESS* – The process is interrupted by throwing an exception.
- *defaultExceptionBehavior*: Defines the default behavior when an exception occurs during any phase of the ETL process (extraction, transformation, or loading). This setting acts as a global fallback when no more specific handling is defined.
  Supported values:
  - *LOG* – The exception is logged and processing continues with the next record.
  - *MARK_RECORD_AS_FAILED* – The record is marked as failed and processing continues.
  - *ABORT_PROCESS* – The exception is propagated and the ETL process is interrupted.
       
## The Database configuration
This section defines the database connection settings used by the ETL process. It supports configuration for both the source database (*srcConnConf*) and the destination database (*dstConnConf*).

Each of these elements accepts the following parameters:

- *dataBaseUserName* – The username used to authenticate with the database.
- *dataBaseUserPassword* – The password used for database authentication.
- *connectionURI* – The JDBC connection URL used to establish the database connection.
- *driverClassName* – The fully qualified name of the JDBC driver class.
- *schema* – An optional parameter used to explicitly define the database schema. This is useful when the schema cannot be inferred from the connection URL or when it differs from the default schema.
- *databaseSchemaPath* – An optional parameter specifying the path to a database schema script. If provided, and the target database does not exist, it will be created using this script.
- Additional JDBC connection pool configurations (from *javax.sql.DataSource* / connection pool implementation), including:
  - *maxActiveConnections* – Maximum number of active connections allowed.
  - *maxIdleConnections* – Maximum number of idle connections in the pool.
  - *minIdleConnections* – Minimum number of idle connections maintained in the pool.
    
## dynamicSrcConf
This parameter enables dynamic generation of the *EtlConfiguration*. In this context, "dynamic" refers to the ability to derive configuration parameters from records stored in a database table, allowing multiple configurations to be produced from a single JSON definition.

With this approach, the configuration file acts as a template, which is expanded at runtime using data retrieved from the specified table. This is particularly useful when working with multiple source databases or environments, where the same ETL logic must be executed repeatedly with different input parameters.

For example, database connection details can be stored in a table (e.g., *src_database*), and each record can be used to generate and execute a distinct ETL configuration.

The basic structure of this parameter is shown below:
```
{
...
	"dynamicSrcConf":{
		"tableName":"",
		"extraConditionForExtract":"",
		"auxExtractTable":[
		
		  ]
		}
...
}
```


- *tableName*: The name of the database table that serves as the source for dynamic configuration generation.
- *extraConditionForExtract*: An optional SQL condition appended to the extraction query when retrieving records from the dynamic source table.
- *auxExtractTable*: An optional list of additional tables used to enrich or filter the extraction process. These tables can be joined to introduce extra conditions or act as supplementary data sources. For full details, refer to [AuxExtractTable](#aux-extract-table).

## The finalizer
The *finalizer* is an optional configuration element responsible for executing post-processing tasks after the ETL process has completed. It is typically used to perform cleanup operations, data adjustments, or any additional logic required once all ETL operations have finished.

A finalizer is implemented as a Java class. Currently, the only supported implementation is [SqlProcessFinalizer](api/src/main/java/org/openmrs/module/epts/etl/controller/SqlProcessFInalizer.java), which allows execution of SQL statements as part of the finalization step.

To use this finalizer, you must provide the *sqlFinalizerQuery*, which defines the SQL command to be executed. Optionally, you can specify the database connection to be used through the *connectionToUse* parameter.

Supported values for *connectionToUse*:
- *mainConnInfo* – Uses the main application connection
- *srcConnInfo* – Uses the source database connection (default)
- *dstConnInfo* – Uses the destination database connection

If *connectionToUse* is not specified, the finalizer will default to using *srcConnInfo*.

```
{
   ...
   "finalizer":{
      "finalizerFullClassName":"",
      "sqlFinalizerQuery":"",
      "connectionToUse":""
   }
   ...
}
```

## The Validator
Defines a list of validation rules that are executed during the ETL process.

Validators allow dynamic validation of data, configuration values, or database state. They can be applied at any level of the ETL configuration and are useful for enforcing data integrity and business rules.

### Structure

```
{
   "validators":[
      {
         "name":"",
         "value":{
            "value":"",
            "transformer":""
         },
         "rule":{
            "type":"",
            "expectedValue":""
         },
         "message":"",
         "phase":"",
         "behavior":"",
         "connectionToUse":""
      }
   ]
}
```

### Fields

- **name** – Unique identifier of the validator
- **value** – Defines how the value to be validated is obtained
  - *value* – source value
  - *transformer* – optional transformer to compute the value
- **rule** – Defines the validation rule
  - *type* – validation type (e.g., EQUALS, NOT_NULL, EXISTS)
  - *expectedValue* – expected value (if applicable)
- **message** – Custom error message if validation fails
- **phase** – When the validation should be executed
  - BEFORE_LOAD
  - AFTER_LOAD
- **behavior** – Action to take if validation fails
  - MARK_RECORD_AS_FAILED
  - IGNORE
  - ABORT_PROCESS
- **connectionToUse** – Specifies which database connection should be used when executing the validator. By default, validators use the *srcConnInfo* connection. This property allows overriding the connection to *dstConnInfo* or *mainConnInfo* when required.  

### Example

```
  "validators": [
    {
      "name": "dst_location_is_hiv_department",
      "connectionToUse":"DST",
      "value": {
        "name": "${destination_location_id}",
        "dataType":"int",
        "transformer": "FAST_SQL_TRANSFORMER(select value_reference from ${dst_db}.location_attribute where attribute_type_id = 25 and location_id = ${destination_location_id})"
      },

      "rule": {
        "type": "EQUALS",
        "expectedValue":233595
      },

      "message": "The provided destination location ${destination_location_id} does not represent a valid hiv  department",
      
      "phase": "BEFORE_LOAD",
      
      "behavior": "ABORT_PROCESS"
    }
  ]
```


## The Operation configuration
This section defines how ETL operations are executed. Each operation controls how the configured ETL items are processed, including execution strategy, performance tuning, and post-processing behavior.

:contentReference[oaicite:0]{index=0}

Each operation can be configured using the following fields:

- *operationType*: Defines the type of operation to be executed for each ETL item in the configuration.
- *processingBatch*: Specifies the number of records to be processed per batch. If not provided, a default value of 1000 is used.
- *threadingMode*: Determines whether processing is performed using a single thread or multiple threads. Supported values:
  - *MULTI* – Multi-threaded processing (default)
  - *SINGLE* – Single-threaded processing
- *fisicalCpuMultiplier*: When using *MULTI* threading, this value multiplies the number of available CPU cores to increase parallelism. Higher values may improve throughput but can also lead to resource contention and reduced performance.
- *processingMode*: Defines how ETL items are processed:
  - *SERIAL* – ETL items are processed one at a time (default)
  - *PARALLEL* – All ETL items are processed concurrently
- *processorFullClassName*: The fully qualified class name of a custom processor implementation to override the default processing behavior.
- *skipFinalDataVerification*: Controls whether the final verification step is executed. This step checks if all source records were successfully processed into the destination. Setting this to true skips the verification, which can improve performance for large datasets.
- *doNotWriteOperationHistory*: By default, the ETL process records execution details in staging tables. Setting this to true disables history tracking, which can improve performance but reduces traceability.
- *useSharedConnectionPerThread*: When using multi-threading, setting this to true forces all threads to share the same database connection. This can reduce deadlocks but may impact performance. It is useful when consistency across batch operations is required.
- *actionType*: Defines the action to be performed on the destination:
  - *CREATE* – Inserts new records (default)
  - *UPDATE* – Updates existing records
  - *DELETE* – Deletes records
- *afterEtlActionType*: Defines the action to be applied to the source record after processing. Currently, only *DELETE* has an effect.
- *dstType*: Specifies the output destination type:
  - *db* – Stores transformed records in the database
  - *json* – Writes records to a JSON file
  - *dump* – Writes SQL statements to a file
  - *csv* – Writes records to a CSV file
  - *console* – Outputs records to the console  
  When a file-based destination is used, files are stored under *@etlRootDirectory/data/@originAppLocationCode*
- *disabled*: If set to true, the operation will not be executed.
- *child*: Defines a nested operation that will be executed after the main operation completes.
- *finishOnNoRemainRecordsToProcess*: Controls early termination of the operation. If true, the process finishes as soon as the expected number of records is processed, even if the maximum record range has not been reached.
- *totalCountStrategy*: Defines how the total number of records to process is calculated:
  - *COUNT_ONCE* – Calculates total count only during the first execution (default)
  - *USE_MAX_RECORD_ID_AS_COUNT* – Uses the maximum record ID as an approximation of total count
  - *USE_PROVIDED_COUNT* – Uses a pre-defined total count
- *totalAvaliableRecordsToProcess*: Specifies the total number of records to process when using *USE_PROVIDED_COUNT* strategy.

## The etl item configuration
## The etl item configuration
The ETL item configuration defines the rules for data extraction, transformation, and loading. Each operation within a process applies its logic to the configured ETL items. These items represent the core units of work in the ETL pipeline.

Each ETL item typically contains two main components:
- *srcConf* – defines how data is extracted from the source
- *dstConf* – defines how data is transformed and loaded into the destination

```
{
{
   "srcConf":{
      "tableName":"",
      "extraConditionForExtract":"",
      "observationDateFields":[
         
      ],
      "sharePkWith":"",
      "metadata":"",
      "removeForbidden":"",
      "uniqueKeys":[
         
      ],
      "parents":[
         
      ],
      "extraTableDataSource":[
         
      ],
      "extraQueryDataSource":[
         
      ],
      "onConflict":""
   },
   "dstConf":[
      
   ],
   "createDstTableIfNotExists":"",
   "autoIncrementHandlingType":"",
   "etlItemSrcConf":{
      
   },
   "disabled":"",
   "childItemConf":[
      
   ],
   "template":{
      "name":"template_name",
      "parameters":{
         "param1":"paramValue1",
         "param2":"paramValue2"
      }
   }
}
```


The *srcConf* defines how data is extracted from the source system, including filtering, joins, and additional data sources. The *dstConf* defines how the extracted data is transformed and loaded into one or more destination tables.

In simple scenarios where no transformation is required, these configurations may be omitted, allowing the ETL engine to automatically map destination fields from the source data.

ETL items can also leverage *templates*, which enable reuse and parameterization of configuration blocks. Templates help reduce duplication and improve maintainability, especially when dealing with similar ETL definitions that differ only by a few parameters (e.g., extraction conditions or target tables). Templates can be applied at multiple levels of the configuration, including *srcConf*, *dstConf*, and *childItemConf*.

Below are the main configuration elements available for an ETL item.

### The "srcConf"
The *srcConf* defines how data is extracted from the source system for a given ETL item. It specifies the main data source, as well as any additional data sources, filters, and extraction rules required during the ETL process.

The main configuration fields are described below:

- *tableName*: The name of the primary source table.
- *parents*: A list of parent table configurations. If no additional configuration is required for parent tables, this property can be omitted, as parent relationships are automatically inferred from the database schema.
- *metadata*: An optional boolean indicating whether the table should be treated as a metadata table.
- *removeForbidden*: An optional boolean indicating whether records from this table can be automatically removed when inconsistencies are detected.
- *observationDateFields*: An optional list of date fields used to identify records affected within a specific time range (e.g., records created or updated within a given period).
- *extraConditionForExtract*: An optional SQL condition appended to the extraction query to filter the records to be processed.
- *uniqueKeys*: An optional list defining unique key constraints. This is not required if the table already defines explicit unique keys at the database level.
- *auxExtractTable*: An optional list of auxiliary tables used to enrich or filter the extraction process. These tables are typically joined to the main table and can also act as additional data sources.
- *extraTableDataSource*: An optional list of auxiliary tables to be used as additional data sources during transformation.
- *extraQueryDataSource*: An optional list of custom queries used as additional data sources.
- *extraObjectDataSource*: An optional list of object-based data source configurations.
- *onConflict*: Refers to [dstConf.onConflict](#onConflict), defining how conflicts should be handled during processing.

Below are additional details for the more complex configurations within *srcConf*.

#### Unique Keys
The *"uniqueKeys"* property allows explicit configuration of unique key constraints for the source table.

If the table already defines unique keys at the database level, this configuration is not required, as the ETL engine will automatically detect and use them. However, in cases where the database metadata is incomplete, unavailable, or needs to be overridden, unique keys can be manually defined using this property.

When specified, the unique keys must follow the structure shown below.

``` {
   "srcConf":{
      "uniqueKeys":[
         {
            "fields":[
               {
                  "name":""
               }
            ]
         }
      ]
   }
}
```
#### Parents configuration
A parent is defined as an object representing a relationship between the current source table and another table (typically via a foreign key).

In most cases, it is not necessary to explicitly configure parents, as the ETL engine automatically detects and resolves parent relationships using the database schema metadata. However, when additional customization is required, parents can be explicitly defined in the configuration.

A parent can include additional properties to control how the relationship is handled during the ETL process. When no extra configuration is needed, the parent entry can be omitted entirely.

When explicitly defined, a parent configuration should follow the structure shown below:

```
{
   "srcConf":{
      "parents":[
         {
            "tableName":"",
            "mapping":[
               {
                  "childFieldName":"",
                  "parentFieldName":"",
                  "defaultValueDueInconsistency":"",
                  "setNullDueInconsistency":"",
                  "ignorable":""
               }
            ],
            "conditionalFields":[
               {
                  "name":"",
                  "value":""
               }
            ],
            "defaultValueDueInconsistency":"",
            "setNullDueInconsistency":""
         }
      ]
   }
}
```
For each parent, it is possible to define a **mapping**, which specifies how child fields relate to parent fields. Within the *mapping* configuration, the following properties can also be defined: *defaultValueDueInconsistency*, *setNullDueInconsistency*, and *ignorable*.

- *defaultValueDueInconsistency*: If defined, this value will replace the original child field value when the referenced parent record cannot be found.
- *setNullDueInconsistency*: A boolean flag. When set to true, the child field value will be set to null if the referenced parent record is not found.
- *ignorable*: Indicates whether the relationship can be ignored when inconsistencies occur.

In addition to field mappings, the parent configuration supports *conditionalFields*. These fields define conditions under which a specific relationship should be applied, allowing relationships to be selectively enforced rather than globally applied to all records.

A common example can be found in the OpenMRS data model. In the *person_attribute* table, the fields *person_attribute_type_id* and *value* are used such that the meaning of *value* depends on the type. For instance, when *person_attribute_type_id = 7*, the *value* represents a reference to a record in the *location* table. In this scenario, a conditional relationship can be defined between *person_attribute* and *location*, where the condition is based on *person_attribute_type_id = 7*.

It is also possible to define global *defaultValueDueInconsistency* and *setNullDueInconsistency* properties at the parent level. When defined at this level, these values apply to all mappings within the relationship, providing a consistent fallback behavior across all child-parent mappings.

#### The auxExtractTable table configuration
<a name="aux-extract-table"></a>

The **"auxExtractTable"** element allows the definition of additional tables to be joined with the main source table during data extraction.

These tables serve two main purposes:
- Enable the inclusion of additional filtering conditions based on related tables
- Act as supplementary data sources for the ETL item during transformation

By configuring *auxExtractTable*, it is possible to enrich the extraction logic with more complex joins and conditions, without modifying the main source table definition. This is particularly useful when the required filtering or data depends on related tables rather than the primary source table alone.

```
{
   "srcConf":{
      "auxExtractTable":[
         {
            "tableName":"",
            "joinExtraCondition":"",
            "joinExtraConditionScope":"",
            "joinFields":[
               {
                  "srcField":"",
                  "srcValue":"",
                  "dstField":"",
                  "dstValue":""
               }
            ],
            "joinType":"",
            "doNotUseAsDatasource":"",
            "auxExtractTable":[
               
            ]
         }
      ]
   }
}
```	 

As shown in the configuration above, each *auxExtractTable* entry supports the following properties:

- **tableName** – The name of the table to be joined with the main source table.
- **joinExtraCondition** – An additional SQL condition to be applied during the join operation.
- **joinFields** – Optional fields defining how the join should be performed. These should only be specified when the relationship between tables is not defined in the data model or when custom/static join conditions are required.
- **joiningType** – The type of join to be performed. Supported values are: *INNER*, *LEFT*, and *RIGHT*.
- **joinExtraConditionScope** – Defines where the *joinExtraCondition* will be applied:
  - *JOIN_CLAUSE* – The condition is applied directly within the JOIN statement.
  - *WHERE_CLAUSE* – The condition is applied in the main query WHERE clause.
- **doNotUseAsDatasource** – When set to true, the auxiliary table will not be used as a data source during transformation. By default, every *auxExtractTable* is also considered an available data source.

**NOTE**: It is possible to define nested *auxExtractTable* elements. Each auxiliary table can itself include additional auxiliary tables, allowing the construction of complex join hierarchies and advanced extraction conditions.

###### The Joining Fields
<a name="joinFields"></a>

The **joinFields** property defines how an *auxExtractTable* is joined with the main source table.

In most cases, this property can be omitted, as the ETL engine is able to automatically determine join conditions based on foreign key relationships defined in the database schema. However, manual configuration of **joinFields** may be required in the following scenarios:

- When the database schema does not define explicit foreign key relationships.
- When custom or static join conditions need to be applied.

Each entry in *joinFields* is typically defined using a pair of fields: *srcField* and *dstField*:
- *srcField* – Refers to a field in the auxiliary table (*auxExtractTable*).
- *dstField* – Refers to a field in the main source table.

For more advanced use cases, static conditions can also be defined using:
- *srcField* and *srcValue*
- *dstField* and *dstValue*

This approach provides additional flexibility, allowing both dynamic field-based joins and fixed-value conditions to be combined when defining the join logic.

#### The extra datasource table configuration

The **"extraTableDataSource"** element allows the definition of additional tables to be used as data sources alongside the main source table defined in the ETL configuration.

These auxiliary tables are typically joined with the main table to enrich the extracted dataset with additional attributes or to enable more advanced filtering conditions during the extraction phase. Unlike *auxExtractTable*, which focuses primarily on join conditions, *extraTableDataSource* explicitly introduces new data sources that can be referenced during transformation.

The relevant configuration for an extra table data source is illustrated below.
```
{
   "srcConf":{
      "extraTableDataSource":[
         {
            "tableName":"",
            "joinExtraCondition":"",
            "joinExtraConditionScope":"", 
            "joinFields":[
               {
                  "srcField":"",
                  "srcValue":"",
                  "dstField":"",
                  "dstValue":""
               }
            ],
            "joinType":"",
            "auxExtractTable": [
	    ]
         }
      ]
   }
}
```

As shown in the configuration above, each **extraTableDataSource** may define the following properties:

- **tableName** – Specifies the name of the additional table that will be used as an auxiliary data source during extraction.

- **joinExtraCondition** – Defines an additional SQL condition applied when joining this table with the main data source. This condition can be used to further restrict or refine the joined records beyond the default join logic.

- **joinFields** – Defines optional join fields used to establish the relationship between the main table and the extra table. These should only be specified when the relationship cannot be automatically inferred from the data model or when custom join logic is required. (See [The Joining Fields](#joinFields))

- **joinType** – Defines the type of SQL join used to connect the extra table with the main table. Supported values are *INNER*, *LEFT*, and *RIGHT*. If not specified, the default join type is *LEFT*. When set to *INNER*, the ETL process will skip the main record if no matching record exists in the extra table.

- **joinExtraConditionScope** – Specifies where the **joinExtraCondition** will be applied within the generated SQL statement:
  - *JOIN_CLAUSE* – The condition is applied within the JOIN clause
  - *WHERE_CLAUSE* – The condition is applied in the main WHERE clause

- **auxExtractTable** – Allows the definition of nested auxiliary tables to be joined with the **extraTableDataSource**. These tables can be used to introduce additional filtering conditions or provide supplementary data for the extraction logic. (See [AuxExtractTable](#aux-extract-table))
- 
#### The extraQueryDataSource configuration

The **"extraQueryDataSource"** element allows the definition of additional queries to be used as data sources alongside the main table. These queries can provide complementary data required during transformation or enable more advanced extraction logic.

The configuration structure is shown below:

```
{
   "srcConf":{
      "extraQueryDataSource":[
         {
            "name":"",
            "query":"", 
            "script":
            "required":""
         }
      ]
   }
}
```


Each *extraQueryDataSource* entry supports the following properties:

- **name** – A unique identifier for the additional query data source.
- **query** – The SQL query to be executed. This query will act as a virtual data source during the ETL process.
- **script** – The relative path to a file containing the SQL query. The application will look for this file under *@etlRootDirectory/dump-scripts/*. This property is only used if the *query* field is not provided.
- **required** – A boolean flag indicating whether the result of the query is mandatory. If set to true and the query does not return any result, the corresponding source record will be skipped.

#### The extraObjectDataSource configuration
The **extraObjectDataSource** allows the definition of custom object-based data sources that can be used during the ETL process.

Unlike table or query data sources, an object data source provides values through explicitly defined fields. These values can either be:
- statically configured within the object data source, or
- dynamically generated using a custom generator.

Generators are user-defined components responsible for producing values at runtime. They can implement custom logic, including complex computations or integrations with external systems. Currently, only Java-based generators are supported.

This type of data source is particularly useful when:
- the required data does not exist in the database
- values must be computed dynamically
- external logic needs to be injected into the ETL process

```
"extraObjectDataSource":[
   {
      "name":"",
      "objectFields":[
         {
            "name":"",
            "value":"",
			"defaultValue": ""
            "transformer":"",
            "dataType":"",
		    "overrideTriggerValue": ""	
         }
      ],
      "objectLanguage":"",
      "fieldsValuesGenerator":""
   }
]	 
```	
Each **extraObjectDataSource** defines an additional object-based data source whose values are dynamically generated during the ETL execution. This configuration allows the creation of synthetic objects that do not necessarily exist as records in the source database but are required to support the destination mapping.

Each **extraObjectDataSource** is defined by the following properties:

- **name**: a unique identifier for the datasource within the ETL item configuration. This name is used to reference the datasource in other configuration sections such as mappings or child destination configurations.
- **objectFields**: the list of fields that compose the generated object. Each field definition contains the following attributes:
  - (1) *name*: the unique name of the field within the datasource object.
  - (2) *value*: defines the value assigned to the field. The value may be a constant, a parameter, or an expression. Parameter values must start with the symbol '@' followed by an identifier. If no value is provided, the system assumes that the value will be generated by the configured fieldsValuesGenerator.
  - (3) *defaultValue*: specifies the default value to be used when the evaluated value results in null.
  <a name="field-transformers"></a>
  - (4) *transformer*: defines a transformation applied to the evaluated field value. Transformers allow complex processing such as expression evaluation, string manipulation, database lookups, or value mapping. The following transformer types are supported:
    - **ARITHMETIC_TRANSFORMER** Evaluates arithmetic expressions defined in the field value. The expression may contain numeric literals, arithmetic operators, and dynamic parameters referencing source fields. Before evaluation, any dynamic parameters are resolved and the resulting expression is evaluated using the exp4j expression engine.;
    - **STRING_TRANSFORMER**: Applies string manipulation operations using standard Java `String` methods, supporting chained method execution. The transformation expression must follow the format: (value).method1(arg1, arg2, ...).method2(...).methodN(...)

      Where:
      - **value** is the initial string to be transformed (can include dynamic placeholders)
      - **methodX** represents any valid method from the Java `String` class
      - **argX** are optional method arguments
        
		The transformer evaluates the expression from left to right, where the result of each method invocation becomes the input of the next method in the chain.

  		Before execution:
      		- Dynamic placeholders within the expression are resolved using available source data
      		- Method arguments are automatically converted to the expected parameter types of the target method
  		The evaluation is performed using Java reflectin, dynamically resolving and invoking the appropriate `String` methods at runtime.
  
		  **Examples:**

            - STRING_TRANSFORMER((John).toUpperCase())
		    - STRING_TRANSFORMER(hello world).substring(0,5).toUpperCase()
		    - STRING_TRANSFORMER(abc123).replace("123","XYZ").concat("-DONE")
		    - STRING_TRANSFORMER(@name).trim().toLowerCase()
        
        If the expression is invalid or a method cannot be resolved/invoked, an exception is raised.
    - **MAPPING_TRANSFORMER(mapping_table,mapping_src_field,mapping_dst_field,extra_condition:extra_condition_value,on_missing:on_missing_value)** performs value transformation using a lookup table stored in the database.
      The transformer searches for a record in the specified mapping table where the value of the source field matches the configured mapping_src_field. If a matching record is found, the value of mapping_dst_field is returned as the transformed value.

      Required parameters:
      - mapping_table – Name of the mapping table
      - mapping_src_field – Source field in the mapping table used for lookup
      - mapping_dst_field – Destination field in the mapping table whose value will be returned

      Optional parameters:
      - extra_condition:extra_condition_value – additional SQL condition used to filter the mapping table;
      - on_missing:on_missing_value – Defines the behavior when no mapping is found
        Supported values:
        - *MARK_RECORD_AS_FAILED* – The record is marked as failed and processing continues
        - *SET_TO_NULL* – The destination field is assigned null
        - *ABORT_PROCESS* – The ETL process is aborted with an exception

		“Optional parameters can be provided in any order using the key:value format. The transformer will automatically detect and apply only the parameters that are defined.”

    - **FAST_SQL_TRANSFORMER(sqlQuery)** Retrieves the field value by executing a SQL query against the source database. The SQL query must return at least one column; only the first column of the first row 
of the result set will be used as the destination field value. If the query returns no rows or the resulting value is null, the transformer will:
      - apply the destination field default value if defined, or
      - raise an ETL transformation exception otherwise.
    - **COALESCE_TRANSFORMER(fieldOrValue1, ..., fieldOrValuen)** Returns the first non-null value obtained from a sequence of candidate inputs. Each parameter is evaluated in order and transformed into a value; if the result of the first parameter is null, the transformer evaluates the second, and so on until a non-null value is found. If all evaluated parameters result in null, the final result is null.
Parameters may represent fixed values, dynamic parameters, or fields from available data sources. When referencing a field, it can be specified using the simple form "field" or the qualified form "dataSourceName.field" when disambiguation between data sources is required.
    - **SIMPLE_VALUE_TRANSFORMER** Performs direct assignment of the source value to the destination field. Any dynamic parameters present in the source value are resolved before assignment. This transformer is used automatically when no explicit transformer is defined for a field mapping.
    - **PARENT_ON_DEMAND_TRANSFORMER(parentTable,parent_field_oin_datasource_object:srcField,on_demand_check_condition:condition,template:templateName,dstField₁:srcFieldOrValue₁,...,dstFieldₙ:srcFieldOrValueₙ, override_fields:fiel1,field2,...)**: Ensures that a related parent record exists in the destination table and returns its primary key as the transformed value. The transformer may resolve the parent from the source database, reuse an already existing parent previously created on demand in the destination database, or create the parent on demand when necessary. The transformer requires the parent table name and at least one of the following special parameters:
      - *parent_field_oin_datasource_object:srcField* – identifies the source field used to resolve the parent record from the source database
      - *on_demand_check_condition:condition* – defines a condition used to search for an already existing parent record previously created on demand in the destination database  
      - *An optional template:templateName* parameter may also be provided to define the template used to initialize the EtlItemConfiguration responsible for creating or loading the parent on demand.
      - Additional parameters may be used to populate fields of the parent record when it needs to be created. Each additional parameter must follow the format **dstField:srcFieldOrValue**.
      - The value assigned to dstField may be:
        - a field from the available data sources (e.g. date_sta
      - *override_fields:fiel1,field2,...* - Defines the list of fields that should be updated on an existing parent record when it is reused by the PARENT_ON_DEMAND_TRANSFORMER. By default, when a parent record already exists in the destination database, it is reused as-is and no updates are applied. However, in some scenarios it may be necessary to update specific fields of the existing parent using the same transformation logic defined for parent creation.
        This parameter allows specifying which fields should be recalculated and overwritten on the existing parent record.
        Format
        ```
        override_fields:field1,field2,...,fieldN
        ```
        Behavior
        
        The specified fields will be recomputed using their corresponding mappings. The computed values will overwrite the existing values in the destination parent record. Only the listed fields are updated; all other fields remain unchanged. The same transformation rules apply as for parent creationrted:encounter_datetime)
        - a constant value (e.g. visit_type_id:42)
        - a dynamic ETL parameter starting with @ (e.g. location_id:@migration_location_id)
        - the literal null to explicitly set the field to null (e.g. date_stopped:null)
        - omitted after the colon to implicitly set the field to null (e.g. indication_concept_id:)
        
		```
		 **ParentOnDemandLoadTransformer**(
		    visit,
		    parent_field_oin_datasource_object:visit_id,
		    on_demand_check_condition:patient_id=patient_id and date_started=encounter_datetime,
		    template:visit_on_demand_template,
		    visit_type_id:42,
		    date_started:encounter_datetime,
		    location_id:@migration_location_id,
		    date_stopped:null,
		    indication_concept_id:
		)
		```

        - In this example, a record in the visit table will be created on demand if it does not yet exist. The date_started field will be populated from encounter_datetime, visit_type_id will receive the constant value 42, location_id will use the dynamic parameter @migration_location_id, and the fields date_stopped and indication_concept_id will be set to null.
- (5) *dataType*: an optional attribute used to explicitly define the data type of the field value. When not specified, the system will infer the type from the transformation result. Supported data types include: *int*, *long*, *double*, *string*, *date*, and *boolean*.
  - (6) *overrideTriggerValue*: specifies a value that triggers the override mechanism. If the transformed field value equals this value, the final value will be replaced by the configured defaultValue.
- **objectLanguage**: defines the scripting or expression language used to process field generation when custom logic is required. This property can be omitted if no custom field generator is used.
- **fieldsValuesGenerator**: specifies the fully qualified class name of a custom field value generator responsible for dynamically producing the values of the fields defined in objectFields.

For demo see [exploring-objectdatasource-field-transformers](docs/demo/README.md#exploring-objectdatasource-field-transformers) session.

#### The use of params within Src Configuration
The *srcConf* supports the use of dynamic parameters in queries and configuration fields. These parameters allow flexible and reusable configurations by enabling values to be injected at runtime.

Parameters are defined as identifiers prefixed with "@", for example: *location_id = @locationId*.

They can be used in multiple contexts, including:

1. As a selected field:  
   *SELECT @param1 AS value FROM tab1 WHERE att2 = 1*

2. In comparison clauses:  
   *SELECT * FROM tab1 WHERE att2 = @param2*

3. In *IN* clauses:  
   *SELECT * FROM tab1 WHERE att1 IN (@param2)*

4. As a database resource (e.g., dynamic table names):  
   *SELECT * FROM @table_name WHERE att1 = value1*

5. In *tableName* definitions across the configuration, for example:  
   *{"tableName":"@mainSchema.@nameOfTable"}*

Parameters can be used in various parts of the configuration, including:
- *joinExtraCondition*
- *extraConditionForExtract*
- *query*
- *tableName*
- and other SQL-related fields

The value of a parameter is resolved following this order of precedence:

1. From the *params* property defined in the process configuration  
2. From other properties within the ETL configuration file  
3. From the current main source object being processed  

For a practical example, see [the-power-of-parameters](docs/demo/README.md#the-power-of-parameters).

### The "DstConf"
The "dstConf '' element is used to configure the destination object in an ETL operation. This element can be omitted if the dst fields can be automatically mapped from the available datasources;
If the "dstConf '' has more than one element or if the mapping cannot be automatically done, then it could be configured following the explanation below.

```
{
   "dstConf":[
      {
         "tableName":"",
         "prefferredDataSource":[
            
         ],
         "ignoreUnmappedFields":"",
		 "srcObjectCondition":""
         "dstType":"",
         "includeAllFieldsFromDataSource":"",
         "autoIncrementHandlingType":"",
         "primaryKeyInitialIncrementValue":"",
         "mapping":[
            {
               "dataSourceName":"",
               "srcField":"",
               "dstField":"",
               "defaultValue":"",
			   "mapToNullValue":"",
               "dataType": "",
               "overrideTriggerValue": "",
 			   "nullValueBehavior":"",
               "skipRelationshipResolution":"",
               "transformer": "",	
            }
         ],
         "joinFields":[
            {
               "srcField":"",
               "srcValue":"",
               "dstField":"",
               "dstValue":""
            }
         ],
         "onConflict":"",
         "winningRecordFieldsInfo":[
            
         ]
      }
   ]
}
```		

Bellow is the explanation for each field:
- **tableName** the destination table name;
- **prefferredDataSource** a comma separated list of tokens representing the data sources names from the "srcConf" in order of preference.  This is important when it comes to auto-mapping, if a certain dst field is present in multiple datasources. If there is only one datasource or if each field in the dst table appears only in one datasource, then this element could be omitted.
- **ignoreUnmappedFields** if there are fields on the dst that were not configured manually and could  not be resolved automatically then the application will fail. To avoid that, then set this field to true;
- **srcObjectCondition**: An optional condition used to select the appropriate source object(s) from the data source. If present, the src objects returned from the SrcConf will be filtered. Only the objects that satisfy this condition will be processed within the etl process.
-  **dstType** the destination type for this specific dstConf. If not present will be applied the "dstType" from operationConfiguration;
- **includeAllFieldsFromDataSource** If true, all the fields from all the data sources in "srcConf" will be included on the destination table (if it is automatically generated by the application); in contrast, only the mapped fields will be included on the destination table.
-  **mapping** is used to manually map the dataSource for specific fields in the dst table. The manual mapping is necessary if the dst field could not be automatically mapped because it does not appear in any dataSource in the srcConf. The relevant field for each mapping are:
   - (1) **dataSourceName** the datasource from were the data will be picked-up; this can be omitted if there is only one datasource containing the srcField or if the "prefferredDataSource" is defined. You can also specify the dataSourceName within the srcField like 'dataSourceName.srcFieldName'
   - (2) **srcField** the field on the dataSource from where the value will be picked up;
   - (3) **dstField** the field in dst which we want to fill;
   - (4) **defaultValue** default value to use when *value* is null;  
   - (5) **mapToNullValue** a boolean which indicates that this field should be filled with null value;
   - (6) **dataType** an optional token to specify the data type for value. By default, the type will match the final expression type from the transformer. Supported types: int, long, double, string, date, boolean      
   - (7) **overrideTriggerValue** the value that triggers the override mechanism. If the transformed field value equals to this value, the  transformed value will be replaced by the configured "defaultValue"
   - (8) **nullValueBehavior** Defines the behavior to be applied when the transformation of a field results in a null value. By default, null values are accepted and assigned to the destination field.
     However, this attribute allows overriding that behavior on a per-field basis.
	 Supported values:
     - *ALLOW* – The null value is accepted and assigned to the destination field. No action is taken.
     - *MARK_RECORD_AS_FAILED* – The record is marked as failed during the ETL process, but processing continues.
     - *ABORT_PROCESS* – An exception is thrown and the ETL process is interrupted according to the configured error handling strategy.
   - (9) **skipRelationshipResolution** defines whether the ETL engine should skip the resolution of relationships (foreign keys) for a given field.
     By default, when a field represents a relationship, the ETL process attempts to resolve the corresponding parent record in the destination database. This typically involves looking up the destination record based on the transformed value.
     When this attribute is set to *true* , the relationship resolution step is skipped, and the value is written directly to the destination field without performing any lookup or validation in the destination database.
     This can improve performance and is useful in scenarios where:
       - the value is already a valid destination identifier referential
       - integrity is guaranteed externally
       - relationship resolution is not required during the ETL process
         
     ⚠️ Warning:
         Disabling relationship resolution may result in invalid foreign key references if the value does not correspond to an existing record in the destination database.      
   - (10) **transformer**: defines a transformation applied to the evaluated field value. Transformers allow complex processing such as expression evaluation, string manipulation, database lookups, or value mapping. Refere to [field transformers](#field-transformers) for more details,
   -  **joinFields** allow the specification of the joining fields to the srcConf. Usually the joining fields can be automatically generated if the src and dst use the same unique keys. The joining fields are important when it comes to determining if all the src records were processed. If the joining fields are not present then the final verification of the process will be skipped for that specific table. (See [The Joining Fields](#joinFields)) 
- **autoIncrementHandlingType**: define how the schema defined auto-increment will be handled. The possible values: (1) AS_SCHEMA_DEFINED meaning that the Etl process will respect the Auto-Increment as defined on table Schema definition. This is the default behavior of the Etl Configuration (2) IGNORE_SCHEMA_DEFINITION meaning that the auto-increment defined by table schema will be ignored and the application itself will handle the key values.
- *primaryKeyInitialIncrementValue*: this override the same property defined on Etl Item Configuration.
<a name="onConflict"></a>
- **onConflict**: Defines how conflicts should be resolved during the ETL process (note: when not present, the correspondent configuration from "srcConf" will be used). A conflict occurs when an incoming record clashes with an existing one in the destination. The supported resolution strategies are:
  - `KEEP_EXISTING`: Retains the existing record unchanged, ignoring the incoming data.
  - `UPDATE_EXISTING`: Replaces the existing record with the incoming one.
-  **winningRecordFieldsInfo** optional list indicating the fields to be checked when there is conflict between an record with existing one on the etl process. When merge existing record, the incoming dstRecord will win if the listed fields have the specified values. Below is an example of winningRecordFieldsInfo.
  
```
{
   "dstConf":{
      "tableName":"location",
      "winningRecordFieldsInfo":[
         [
            {
               "name":"is_selected",
               "value":"1"
            },
            {
               "name":"voided",
               "value":"0"
            }
         ],
         [
            {
               "name":"fullProcessed",
               "value":"tue"
            }
         ]
      ]
   }
}
```	  

As can be seen the "winningRecordFieldsInfo'' is a list of lists, listing the fields which will be used to determine which record will win when there are conflicts between an incoming record and existing one. In the above example, if the incoming record has value 1 on field "is_selected" AND has value 0 on field "voided" OR  if the "fullProcessed" field has value true, then the incoming record will win.  Note that for the outer list the join condition will be "OR" and for the inner list the join condition will be "AND".

### The etlItemSrcConf
The etlItemSrcConf allows the dynamic configuration of Etl Items. This means that item elements can be dynamically gathered from one or more tables. Note that the etlItemSrcConf has the very same elements with SrcConf.     

### Other Properities for Etl Item Configuration
- *manualMapPrimaryKeyOnField*: if present, the value from this field will be mapped as a primary key for all tables that don't have a primary key but have a field with a name matching this field. This value will be overridden by the corresponding value in the SrcConf or DstConf if present there.
- *autoIncrementHandlingType*: define how the schema defined auto-increment will be handled. The possible values: (1) AS_SCHEMA_DEFINED meaning that the Etl process will respect the Auto-Increment as defined on table Schema definition. This is the default behavior of the Etl Configuration (2) IGNORE_SCHEMA_DEFINITION meaning that the auto-increment defined by table schema will be ignored and the application itself will handle the key values. The value for this property can be overridden by the value from the same property from the DstConf.
- *createDstTableIfNotExists*: if set to true, the dst table will be automaticaly created in the dst database if it does not exists.
- *disabled*: this propertie allows the item to bem ignored on the etl process.  
- *primaryKeyInitialIncrementValue*: this overrides the same property globaly defined on Etl Configuration;

### The "childItemConf"
The childItemConf represents a list of ETL configuration items used to process records that are related to the main records defined in the etlItemConf. These typically correspond to records stored in related tables and must often be processed within the same transaction as the main record.

For example, when migrating a person, it may also be necessary to migrate related records such as person_name and person_attribute. In this case, the person entity would be defined in the main etlItemConf, while the related entities (person_name, person_attribute) would be defined inside childItemConf.

This configuration element is defined as shown below:

```
{
   "childItemConf":[
      {
         "relatedParentDstConfName":"",
         "srcConf":{
            "tableName":""
         },
         "dstConf":[
            
         ],
         "childItemConf":[
            
         ]
      }
   ]
}
```
As shown above, a child configuration item has the same structure as the main etlItemConf. The main difference is the optional attribute *relatedParentDstConfName*.

This attribute specifies which dstConf from the parent etlItemConf the child item is associated with. It becomes mandatory when the parent etlItemConf defines more than one *dstConf*, so that the ETL engine can correctly determine the relationship between the parent and child configurations.

Additionally, since *childItemConf* itself supports nested childItemConf, it is possible to define multiple levels of related entities, allowing the ETL process to handle complex hierarchical data structures.

### The Template
Defines a reusable configuration template that can be applied to an ETL item or any of its nested elements. Templates allow the reuse of common configurations, reducing duplication and improving maintainability by parameterizing only the parts that vary (such as extraction conditions, table names, or filters).

A template is referenced using its name and an optional list of parameters:

```
   "template":{
      "name":"template_name",
      "parameters":{
         "param1":"paramValue1",
         "param2":"paramValue2"
      }
   }
```

Templates are defined externally in a JSON file with the following structure:
```
[
   {
      "name":"template_name",
      "parameters":[
         "param1",
         "param2"
      ],
      "template":{
         ...
      }
   }
]
```
By default, templates are loaded from a file named etl_elements_templates.json, which must be located in the same directory as the main ETL configuration file. If a different location is required, it can be specified using the global ETL configuration property:
```
etlTemplatesFilePath
```
This property should contain the full path to the templates file.

Templates can be used in any part of the EtlItemConfiguration, including but not limited to:

  - the root level of the item configuration
  - srcConf
  - dstConf
  - childItemConf
  - or any nested configuration element

When a template is applied, its configuration is merged into the target element, replacing or complementing the existing configuration based on the provided parameters.

This mechanism is especially useful for scenarios where multiple ETL items share similar configurations, differing only in specific parameters such as extraction conditions, field mappings, or filters.

## Default configuration files templates
In this section are listed some templates for configuration files for specific etl processes. For demo please check [this session](https://github.com/csaude/openmrs-module-epts-etl/blob/master/docs/demo/README.md#etl-quick-examples).

### The generic etl configuration template
This could be used for simple or complex etl processes. Using this template each src record will be transformed and saved to the destination database. The template for this process can be found [here](docs/process_templates/generic_etl.json)


### The database merge configuration template
The database merge is a process of joining together one or more databases. The template for this process can be found [here](docs/process_templates/db_merge.json)

### The database extract configuration template
The database extraction is a process of extracting a set of data from the src database to a dst database. The template for this process can be found [here](docs/process_templates/db_extract.json)

### The records update configuration template
The record update can be useful if you want to perform the update of records using data from a src database. The template for this process can be found [here](docs/process_templates/db_update.json)

### The records deletion configuration template
This process performs physical remotion of records on the target database. The template for this process can be found [here](docs/process_templates/db_delete.json) 


## Examples 
For demo examples please check [this session](docs/demo/README.md#etl-quick-examples).

# Running the application
To run this application you should (1) get the jar file either from the releases or (2) cloning and compiling the [eptssync project](https://github.com/FriendsInGlobalHealth/openmrs-module-eptssync.git).

If you go for the second option follow the steps bellow from your machine
```
git clone https://github.com/csaude/openmrs-module-epts-etl.git
cd openmrs-module-epts-etl
mvn clean install -DskipTests
```

Once you have the jar and have set up the configuration file (or configurations file) you can run the application hitting the below command.
```
java -Dlog.level=LOG_LEVEL -jar epts-etl-api-1.0.jar "path/to/configuration/file"
```
The LOG_LEVEL can be one of the following: DEBUG, INFO, WARN, ERR
