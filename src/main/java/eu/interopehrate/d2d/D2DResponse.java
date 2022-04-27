package eu.interopehrate.d2d;

import java.util.UUID;

public class D2DResponse {	
	
	private String id = UUID.randomUUID().toString();
	private D2DResponseHeader header = new D2DResponseHeader();
	private String body;
	private int status = D2DStatusCodes.SUCCESSFULL;
	private String message;
	
	public D2DResponse() {}
	
	public D2DResponse(D2DRequest request) {
		 this.header.setRequestId(request.getId());
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public D2DResponseHeader getHeader() {
		return header;
	}
	public void setHeader(D2DResponseHeader header) {
		this.header = header;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "D2DResponse [id=" + id + ", header=" + header + ", body=" + body + ", status=" + status + ", message="
				+ message + "]";
	}
	
}
