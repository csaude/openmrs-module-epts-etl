package fgh.spi.changedrecordsdetector;

import java.util.List;

import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.interfaces.BaseConfiguration;

/**
 * 
 */

/**
 * Interface generica para abstrair um servico
 */
public interface GenericOperation extends BaseConfiguration {
	
	default List<Extension> getExtension() {
		return null;
	}
	
	default void setExtension(List<Extension> extension) {
	}
}
