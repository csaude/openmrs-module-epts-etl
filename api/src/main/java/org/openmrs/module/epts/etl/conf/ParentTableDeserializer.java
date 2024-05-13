package org.openmrs.module.epts.etl.conf;

import java.io.IOException;

import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;


public class ParentTableDeserializer extends JsonDeserializer<ParentTable> {
	
	private JsonDeserializer<Object> deserializer;
	
	public ParentTableDeserializer(JsonDeserializer<Object> deserializer) {
		this.deserializer = deserializer;
	}

	@Override
	public ParentTable deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

		return new ParentTableImpl();
		//return CommonUtilities.getInstance().loadObjectFormJSON(ParentTableImpl.class, ctxt.g);
	}
	
}
