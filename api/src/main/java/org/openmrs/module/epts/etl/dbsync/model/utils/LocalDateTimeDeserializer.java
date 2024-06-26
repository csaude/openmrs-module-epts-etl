package org.openmrs.module.epts.etl.dbsync.model.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom {@link com.fasterxml.jackson.databind.JsonDeserializer} from String {@link LocalDateTime}
 */
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
	
	private static final long serialVersionUID = 4612950453295451630L;
	
	public LocalDateTimeDeserializer() {
		this(LocalDateTime.class);
	}
	
	public LocalDateTimeDeserializer(Class<LocalDateTime> ldt) {
		super(ldt);
	}
	
	@Override
	public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		return ZonedDateTime.parse(p.getText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
		        .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
	}
	
}
