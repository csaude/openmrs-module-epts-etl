package org.openmrs.module.epts.etl.dbsync.model.utils;

import java.io.IOException;

import org.openmrs.module.epts.etl.dbsync.model.BaseModel;
import org.openmrs.module.epts.etl.dbsync.model.SyncModel;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Overrides deserialize method to get the model class from the parent TransferObject
 */
public class BaseModelDeserializer extends StdDeserializer<BaseModel> {
	
	public BaseModelDeserializer() {
		super(BaseModel.class);
	}
	
	@Override
	public BaseModel deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
		ObjectCodec codec = p.getCodec();
		
		Class<? extends BaseModel> type = ((SyncModel) p.getParsingContext().getParent().getCurrentValue())
		        .getTableToSyncModelClass();
		
		JsonNode node = codec.readTree(p);
		
		return codec.treeToValue(node, type);
	}
}
