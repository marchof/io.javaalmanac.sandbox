package io.javaalmanac.sandbox.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonProcessingException;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class VersionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Set<String> VERSION_KEYS = Set.of( //
			"java.class.version", //
			"java.runtime.name", //
			"java.runtime.version", //
			"java.specification.name", //
			"java.specification.version", //
			"java.version", //
			"java.version.date", //
			"java.vendor", //
			"java.vendor.url", //
			"java.vendor.url.bug", //
			"java.vendor.version", //
			"java.vm.name", //
			"java.vm.specification.version", //
			"java.vm.specification.vendor", //
			"java.vm.vendor", //
			"java.vm.version");

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		Map<String, String> response = new HashMap<>();

		for (String key : VERSION_KEYS) {
			response.put(key, System.getProperty(key));
		}

		APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
		responseEvent.setStatusCode(200);

		ObjectMapper mapper = new ObjectMapper();
		try {
			responseEvent.setBody(mapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return responseEvent;
	}

}
