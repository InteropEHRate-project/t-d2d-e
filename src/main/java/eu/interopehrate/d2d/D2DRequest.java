package eu.interopehrate.d2d;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class D2DRequest {	
	
	private String id = UUID.randomUUID().toString();
	private D2DRequestHeader header = new D2DRequestHeader();
	private D2DOperation operation;
	private List <D2DParameter> parameters = new ArrayList<D2DParameter>(); 
	private String body;

	public D2DRequest() {
	}
	
	
	public D2DRequest(D2DOperation operation) {
		this.setOperation(operation);
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public D2DOperation getOperation() {
		return operation;
	}


	public void setOperation(D2DOperation operation) {
		this.operation = operation;
	}


	public D2DRequestHeader getHeader() {
		return header;
	}


	public void setHeader(D2DRequestHeader header) {
		this.header = header;
	}


	public String getBody() {
		return body;
	}


	public void setBody(String body) {
		this.body = body;
	}
	

	public List<D2DParameter> getParameters() {
		return parameters;
	}

	
	public void setParameters(List<D2DParameter> parameters) {
		this.parameters = parameters;
	}
	
	
	public D2DRequest addParameter(D2DParameter parameter) {
		// the only parameters that can be duplicated are the CATEGORY and ID
		if(parameter == null) {
			return this;
		}
		
		if (this.operation == D2DOperation.SEARCH)
			addParamForSearch(parameter);
		else if (this.operation == D2DOperation.READ)
			addParamForRead(parameter);
		else if (this.operation == D2DOperation.WRITE)
			addParamForWrite(parameter);
		
		return this;
	}

	
	public D2DParameter getFirstOccurenceByName(D2DParameterName name) {
		for (D2DParameter d2dParameter : parameters) {
			if (d2dParameter.getName() == name)
				return d2dParameter;
			
		}

		return null;
	}	

	
	public Object getFirstOccurenceValueByName(D2DParameterName name) {
		D2DParameter param = getFirstOccurenceByName(name);
		return (param == null) ? null : param.getValue();
	}
	
	
	public List<D2DParameter> getAllOccurencesByName(D2DParameterName name) {
		final List <D2DParameter> occurences = new ArrayList<D2DParameter>(); 
		
		for (D2DParameter d2dParameter : parameters) {
			if (d2dParameter.getName() == name)
				occurences.add(d2dParameter);
		}
		
		return occurences;
	}
	
	
	public List<Object> getAllOccurencesValuesByName(D2DParameterName name) {
		final List <D2DParameter> occurences = getAllOccurencesByName(name);
		final List <Object> values = new ArrayList<Object>(); 
		
		for (D2DParameter d2dParameter : occurences) {
			values.add(d2dParameter.getValue());
		}
		
		return values;
	}
	
	
	private void addParamForSearch(D2DParameter parameter) {
		if (parameter.getName() == D2DParameterName.ID)
			return;
		
		D2DParameter d2dParameter;
		for (int i=0; i < parameters.size(); i++) {
			d2dParameter = parameters.get(i);
			if(d2dParameter.getName().equals(parameter.getName()) && parameter.getName() != D2DParameterName.CATEGORY) {
				parameters.remove(d2dParameter);
				break;
			}
		}
		parameters.add(parameter);
	}
	
	private void addParamForRead(D2DParameter parameter) {
		if (parameter.getName() == D2DParameterName.ID) {
			parameters.add(parameter);
		}
	}

	private void addParamForWrite(D2DParameter parameter) {
		return;
	}

	@Override
	public String toString() {
		return "D2DRequest [id=" + id + ", header=" + header + ", operation=" + operation + ", parameters=" + parameters
				+ ", body=" + body + "]";
	}

}

