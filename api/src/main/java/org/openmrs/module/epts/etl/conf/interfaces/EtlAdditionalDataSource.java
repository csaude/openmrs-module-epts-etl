package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Represents an additional data source used in the ETL process.
 * <p>
 * An additional data source is any data source configuration associated with the main
 * ETL source. It can represent:
 * <ul>
 *   <li>A related table</li>
 *   <li>A join with another dataset</li>
 *   <li>A custom SQL query</li>
 * </ul>
 * 
 * This interface defines how related data is fetched and linked to the main ETL flow.
 */
public interface EtlAdditionalDataSource extends EtlDataSource {
	
	/**
	 * Gets the main source configuration associated with this additional data source.
	 *
	 * @return the related {@link SrcConf}
	 */
	SrcConf getRelatedSrcConf();
	
	/**
	 * Sets the main source configuration associated with this additional data source.
	 *
	 * @param relatedSrcConf the source configuration to associate
	 */
	void setRelatedSrcConf(SrcConf relatedSrcConf);
	
	/**
	 * Loads related source objects based on the current data source configuration.
	 * <p>
	 * This method may use the provided list of already available source objects to
	 * assist in resolving relationships (e.g., joins or dependent queries).
	 *
	 * @param avaliableSrcObjects list of already loaded source objects that may be used as context
	 * @param conn the database connection to use
	 * @return a list of related {@link EtlDatabaseObject} instances
	 * @throws DBException if a database error occurs during execution
	 */
	List<EtlDatabaseObject> loadRelatedSrcObjects(List<EtlDatabaseObject> avaliableSrcObjects, Connection conn)
	        throws DBException;
	
	/**
	 * Indicates whether this data source is mandatory.
	 * <p>
	 * If this data source is marked as required and no data is returned, the main
	 * destination record will be skipped (i.e., not written to the destination).
	 *
	 * @return {@code true} if the data source is required; {@code false} otherwise
	 */
	boolean isRequired();
	
	@SuppressWarnings("deprecation")
	default EtlDatabaseObject newInstance() {
		try {
			EtlDatabaseObject obj = getSyncRecordClass().newInstance();
			obj.setRelatedConfiguration(this);
			
			return obj;
		}
		catch (InstantiationException | IllegalAccessException | ForbiddenOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Indicates whether multiple source objects can be used when loading related data.
	 * <p>
	 * If {@code false}, only the primary source object will be considered when invoking
	 * {@link #loadRelatedSrcObjects(List, Connection)}.
	 *
	 * @return {@code true} if multiple source objects are allowed; {@code false} otherwise
	 */
	boolean allowMultipleSrcObjectsForLoading();
	
	/**
	 * Gets the SQL query associated with this additional data source.
	 * <p>
	 * This query is typically used to fetch related data from the database.
	 *
	 * @return the SQL query string
	 */
	String getQuery();
}