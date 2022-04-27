package eu.interopehrate.d2d;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.interopehrate.protocols.common.DocumentCategory;
import eu.interopehrate.protocols.common.FHIRResourceCategory;
import eu.interopehrate.protocols.common.ResourceCategory;

public class D2DParameter {

	private D2DParameterName name;
	private Object value;
	
	public D2DParameter() {
	}
	
	public D2DParameter(D2DParameterName name, Object value) {
		this.name  = name;
		setValue(value);
	}
	
	public D2DParameterName getName() {
		return name;
	}

	public D2DParameter setName(D2DParameterName name) {
		this.name = name;
		return this;
	}

	public Object getValue() {
		return value;
	}

	public D2DParameter setValue(Object value) {
		if (name == D2DParameterName.DATE) {
			if (!(value instanceof Date))
				throw new IllegalArgumentException(String.format("Type mismatch: provided value '%s' must be a java.util.Date", value));
			// this.value = dateFormatter.format((Date)value);
			// return this;
		} else if (name == D2DParameterName.MOST_RECENT) {
			if (!(value instanceof Number))
				throw new IllegalArgumentException(String.format("Type mismatch: provided value '%s' must be a subclass of java.lang.Number", value));
		} else if (name == D2DParameterName.SUMMARY) {
			if (!(value instanceof Boolean))
				throw new IllegalArgumentException(String.format("Type mismatch: provided value '%s' must be a java.lang.Boolean", value));
		} else if (name == D2DParameterName.CATEGORY) {
			if (!(value instanceof ResourceCategory))
				throw new IllegalArgumentException(String.format("Type mismatch: provided value '%s' must implement ResourceCategory", value));
			// this.value = value.toString();
			// return this;
		} else if (!(value instanceof String))
			throw new IllegalArgumentException(String.format("Type mismatch: provided value '%s' must be a java.lang.String", value));
		
		this.value = value;
		return this;
	}

	@Override
	public String toString() {
		return "D2DParameter [name=" + name + ", value=" + value + "]";
	}
	
}

