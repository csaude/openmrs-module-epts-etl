package org.openmrs.module.epts.etl.etl.model;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.EtlItemSrcConf;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;

public class EtlDynamicItemSearchParams extends EtlDatabaseObjectSearchParams {
	
	EtlItemSrcConf relatedItem;
	
	public EtlDynamicItemSearchParams(EtlItemSrcConf relatedItem) {
		super(null, null);
		
		/*
		if (!relatedItem.isDynamic()) {
			throw new ForbiddenOperationException("This item [" + relatedItem.getConfigCode() + " Is not dynamic!!!");
		}
		*/
		
		this.relatedItem = relatedItem;
	}
	
	@Override
	public SrcConf getSrcConf() {
		return relatedItem;
	}
	
	@Override
	public EtlConfiguration getRelatedEtlConf() {
		return relatedItem.getRelatedEtlConf();
	}
	
}
