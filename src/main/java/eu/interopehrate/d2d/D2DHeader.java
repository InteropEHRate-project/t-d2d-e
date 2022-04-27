package eu.interopehrate.d2d;

import java.time.Instant;
import java.util.UUID;

public class D2DHeader {
	
	private String timeStamp;
	private String agent;
	private String protocol = "D2D";
	private String version = "1";
	
	public D2DHeader() {
		timeStamp = Instant.now().toString();
		agent = "JRE " + System.getProperty("java.version") + 
				" - " + System.getProperty("os.name") + 
				" " + System.getProperty("os.version");
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "D2DHeader [timeStamp=" + timeStamp + ", agent=" + agent + ", protocol=" + protocol + ", version="
				+ version + "]";
	}

}
