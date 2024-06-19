package org.openmrs.module.epts.etl.dbsync.model.utils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Custom {@link com.fasterxml.jackson.databind.JsonSerializer} from {@link LocalDateTime} to String
 */
public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
	
	private static final long serialVersionUID = -7881492162164082655L;
	
	public LocalDateTimeSerializer() {
		this(LocalDateTime.class);
	}
	
	public LocalDateTimeSerializer(Class<LocalDateTime> ldt) {
		super(ldt);
	}
	
	@Override
	public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(value.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	}
	
}
