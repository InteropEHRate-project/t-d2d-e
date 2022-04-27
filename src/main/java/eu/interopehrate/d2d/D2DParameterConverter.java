package eu.interopehrate.d2d;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import eu.interopehrate.protocols.common.DocumentCategory;
import eu.interopehrate.protocols.common.FHIRResourceCategory;
import eu.interopehrate.protocols.common.ResourceCategory;

public class D2DParameterConverter extends TypeAdapter<D2DParameter> {
	private transient SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public D2DParameter read(JsonReader reader) throws IOException {
		D2DParameter d2dParam = new D2DParameter();
		String fieldname  = null;
		String tmp  = null;
		ResourceCategory cat = null;
		
	    reader.beginObject(); 
	    
	    while (reader.hasNext()) { 
	    	JsonToken token = reader.peek();   
	    	
	         if (token.equals(JsonToken.NAME)) {     
	             //get the current token 
	             fieldname = reader.nextName(); 
	         }
	          
	         if ("name".equals(fieldname)) {       
	             //move to next token 
	             token = reader.peek(); 
	             d2dParam.setName(D2DParameterName.valueOf(reader.nextString()));
	         } else if ("value".equals(fieldname)) { 
	             token = reader.peek();
	     		if (d2dParam.getName() == D2DParameterName.DATE) {
	    			try {
	    				tmp = reader.nextString();
						d2dParam.setValue(dateFormatter.parse(tmp));
					} catch (ParseException e) {
						throw new IOException("Received Parameter contains an invalid date: " + tmp);
					}
	     		} else if (d2dParam.getName() == D2DParameterName.MOST_RECENT)
	    			d2dParam.setValue(reader.nextInt());	             
	    		else if (d2dParam.getName() == D2DParameterName.SUMMARY)
	    			d2dParam.setValue(reader.nextBoolean());	             
	    		else if (d2dParam.getName() == D2DParameterName.CATEGORY) {
	    			tmp = reader.nextString();
	    			try {
	    				cat = FHIRResourceCategory.valueOf(tmp);
	    			} catch (IllegalArgumentException iae) {}
	    			
	    			if (cat == null) {
		    			try {
		    				cat = DocumentCategory.valueOf(tmp);
		    			} catch (IllegalArgumentException iae) {}
	    			}
	    			
	    			if (cat != null)
	    				d2dParam.setValue(cat);
	    			else
	    				throw new IOException("Received Parameter contains an invalid CATEGORY: " + tmp);	             
	    		} else
	    			d2dParam.setValue(reader.nextString());	             

	         }
	    }
	    
	    reader.endObject();
		
		return d2dParam;
	}

	@Override
	public void write(JsonWriter writer, D2DParameter d2dParam) throws IOException {
		writer.beginObject(); 
		writer.name("name");
		D2DParameterName name = d2dParam.getName();
		writer.value(name.name());
		writer.name("value");
		
		if (name == D2DParameterName.DATE)
			writer.value(dateFormatter.format((Date)d2dParam.getValue()));
		else if (name == D2DParameterName.MOST_RECENT)
			writer.value((Integer)d2dParam.getValue());
		else if (name == D2DParameterName.SUMMARY)
			writer.value((Boolean)d2dParam.getValue());
		else
			writer.value(d2dParam.getValue().toString());
		
	    writer.endObject();
	}

}
