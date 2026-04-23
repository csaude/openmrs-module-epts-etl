package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.AuxExtractTable;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public interface MainJoiningEntity extends TableConfiguration {
	
	static final CommonUtilities utils = CommonUtilities.getInstance();
	
	List<? extends JoinableEntity> getJoiningTable();
	
	void setJoiningTable(List<? extends JoinableEntity> joiningTable);
	
	/**
	 * Tells whether this main join entity is also joinable or not. Example of known main joinable
	 * entities are {@link AuxExtractTable}
	 * 
	 * @return
	 */
	Boolean isJoinable();
	
	Boolean doNotUseAsDatasource();
	
	/**
	 * @return return this main joining entity as {@link JoinableEntity}
	 * @throws ForbiddenOperationException if this main joining entity is not joinable
	 */
	JoinableEntity parseToJoinable() throws ForbiddenOperationException;
	
	default Boolean hasAuxExtractTable() {
		return utils.listHasElement(this.getJoiningTable());
	}
	
	default void tryToLoadAuxExtraJoinTable(EtlDatabaseObject schemaInfo, Connection conn) throws DBException {
		if (hasAuxExtractTable()) {
			for (JoinableEntity t : this.getJoiningTable()) {
				t.tryToLoadSchemaInfo(schemaInfo, conn);
				
				t.setParentConf(this);
				t.tryToGenerateTableAlias(getRelatedEtlConf());
				t.setMainExtractTable(this);
				t.setRelatedEtlConfig(this.getRelatedEtlConf());
				
				TableConfiguration fullLoadedTab = findFullConfiguredConfInAllRelatedTable(t.getFullTableName(),
				    new ArrayList<>());
				
				OpenConnection srcConn = this.getRelatedConnInfo().openConnection();
				
				try {
					if (fullLoadedTab != null) {
						t.clone(fullLoadedTab, this, null, conn);
					} else {
						t.fullLoad(srcConn);
					}
					
					if (t.useSharedPKKey()) {
						t.getSharedKeyRefInfo(conn).tryToGenerateTableAlias(getRelatedEtlConf());
						
						fullLoadedTab = findFullConfiguredConfInAllRelatedTable(t.getFullTableName(), new ArrayList<>());
						
						if (fullLoadedTab != null) {
							t.getSharedKeyRefInfo(conn).clone(fullLoadedTab, this, null, srcConn);
						} else {
							t.getSharedKeyRefInfo(conn).fullLoad(srcConn);
						}
						
						t.getSharedKeyRefInfo(conn).setParentConf(t);
					}
					
					if (t.isMainJoiningEntity() && t.parseToJoining().hasAuxExtractTable()) {
						t.parseToJoining().tryToLoadAuxExtraJoinTable(schemaInfo, conn);
					}
					
					t.loadJoinElements(schemaInfo, conn);
				}
				finally {
					srcConn.finalizeConnection();
				}
				
			}
			
		}
		
	}
	
}
