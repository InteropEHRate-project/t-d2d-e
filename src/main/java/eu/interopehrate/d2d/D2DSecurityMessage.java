package eu.interopehrate.d2d;

public class D2DSecurityMessage {
	
	private D2DHeader header = new D2DHeader();
	private D2DSecurityOperation operation;
	private String body;
	
	public D2DSecurityOperation getOperation() {
		return operation;
	}
	public void setOperation(D2DSecurityOperation operation) {
		this.operation = operation;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public D2DHeader getHeader() {
		return header;
	}
	public void setHeader(D2DHeader header) {
		this.header = header;
	}
	@Override
	public String toString() {
		return "D2DSecurityMessage [operation=" + operation + ", body=" + body + ", header=" + header + "]";
	}

}
