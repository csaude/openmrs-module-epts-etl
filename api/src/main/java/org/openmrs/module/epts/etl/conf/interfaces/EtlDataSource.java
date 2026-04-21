package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlField;
import org.openmrs.module.epts.etl.conf.datasource.PreparedQuery;
import org.openmrs.module.epts.etl.conf.types.DbmsType;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.exceptions.DatabaseResourceDoesNotExists;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public interface EtlDataSource extends DatabaseObjectConfiguration {
	
	static final Object LOCK = new Object();
	
	String getName();
	
	PreparedQuery getDefaultPreparedQuery();
	
	void setDefaultPreparedQuery(PreparedQuery defaultPreparedQuery);
	
	List<String> getDynamicElements();
	
	default boolean hasDynamicElements() {
		return utilities.listHasElement(this.getDynamicElements());
	}
	
	default Boolean isPrepared() {
		return this.getDefaultPreparedQuery() != null;
	}
	
	default void prepare(List<EtlDatabaseObject> mainObject, Connection conn) throws DBException {
		if (isPrepared()) {
			return;
		}
		
		synchronized (LOCK) {
			PreparedQuery query = PreparedQuery.prepare(this, mainObject, getRelatedEtlConf(),
			    DbmsType.determineFromConnection(conn));
			
			setDefaultPreparedQuery(query);
		}
	}
	
	default List<EtlDatabaseObject> searchRecords(Engine<? extends EtlDatabaseObject> engine,
	        EtlDatabaseObject parentSrcObject, Connection srcConn) throws DBException {
		
		List<EtlDatabaseObject> avaliableSrcObjects = parentSrcObject != null ? utilities.parseToList(parentSrcObject)
		        : null;
		
		if (!isPrepared()) {
			prepare(avaliableSrcObjects, srcConn);
		}
		
		return this.getDefaultPreparedQuery()
		        .cloneAndLoadValues(null, parentSrcObject, parentSrcObject, avaliableSrcObjects, srcConn)
		        .query(engine, srcConn);
	}
	
	default void loadOwnFieldsToEtlFields(List<EtlField> etlFields, Boolean presereOriginalNames) {
		if (etlFields == null)
			throw new ForbiddenOperationException("The 'etlFields' is null");
		
		if (this instanceof MainJoiningEntity) {
			MainJoiningEntity dsAsJoining = (MainJoiningEntity) this;
			
			if (!dsAsJoining.doNotUseAsDatasource()) {
				etlFields.addAll(EtlField.converteFromDataSourceFields(this, presereOriginalNames));
			}
			
			if (dsAsJoining.hasAuxExtractTable()) {
				for (JoinableEntity j : dsAsJoining.getJoiningTable()) {
					if (!j.doNotUseAsDatasource()) {
						j.loadOwnFieldsToEtlFields(etlFields, false);
					}
				}
			}
		} else {
			etlFields.addAll(EtlField.converteFromDataSourceFields(this, presereOriginalNames));
		}
	}
	
	/**
	 * Gets the SQL query associated with this data source.
	 * <p>
	 * This query is typically used to fetch related data from the database.
	 *
	 * @return the SQL query string
	 */
	String getQuery();
	
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
	
	default void init(EtlDataConfiguration relatedParent, EtlDatabaseObject etlSchemaObject, Connection srcConn,
	        Connection dstConn) throws DBException {
		
		if (relatedParent == null)
			throw new EtlExceptionImpl("RelatedParent cannot be null!");
		
		this.setParentConf(relatedParent);
		this.tryToLoadFromTemplate();
		
		Connection conn = null;
		
		if (this instanceof EtlSrcConf) {
			conn = srcConn;
		} else if (this instanceof EtlDstConf) {
			conn = dstConn;
		} else
			throw new EtlExceptionImpl("An EtlDatasource must be either a EtlSrcConf or EtlDstConf!!!!");
		
		this.tryToLoadSchemaInfo(etlSchemaObject, conn);
	}
	
	void setParentConf(EtlDataConfiguration relatedParent);
	
	void tryToLoadSchemaInfo(EtlDatabaseObject schemaInfoSrc, Connection conn)
	        throws DBException, ForbiddenOperationException, DatabaseResourceDoesNotExists;
}
