package eu.interopehrate.d2d;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.interopehrate.protocols.common.FHIRResourceCategory;

public class JSONTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Gson gson = new GsonBuilder().
				registerTypeAdapter(D2DParameter.class, new D2DParameterConverter())
				.setPrettyPrinting()
				.create();
		D2DRequest request = new D2DRequest();
		
		request.setOperation(D2DOperation.SEARCH);
		request.setBody("body");
		request.addParameter(new D2DParameter(D2DParameterName.CATEGORY, FHIRResourceCategory.OBSERVATION));
		request.addParameter(new D2DParameter(D2DParameterName.DATE, new Date()));
		
		System.out.println(gson.toJson(request));
		
		D2DResponse response = new D2DResponse(request);
		response.setBody("body");
		response.setStatus(200);
		response.setMessage("message");

		System.out.println(gson.toJson(response));
		
		D2DSecurityMessage secMsg = new D2DSecurityMessage();
		
		secMsg.setOperation(D2DSecurityOperation.HELLO_SEHR);
		secMsg.setBody("");
		
		System.out.println(gson.toJson(secMsg));

	}

}
