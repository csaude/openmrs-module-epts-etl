package org.openmrs.module.epts.etl.dbsync.model.utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Custom {@link com.fasterxml.jackson.databind.JsonSerializer} from {@link LocalDate} to String
 */
public class LocalDateSerializer extends StdSerializer<LocalDate> {
	
	private static final long serialVersionUID = -3585139508412163272L;
	
	public LocalDateSerializer() {
		this(LocalDate.class);
	}
	
	public LocalDateSerializer(Class<LocalDate> ldt) {
		super(ldt);
	}
	
	@Override
	public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
	}
	
}
