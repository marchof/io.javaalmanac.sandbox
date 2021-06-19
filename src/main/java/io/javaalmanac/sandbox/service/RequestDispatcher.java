package io.javaalmanac.sandbox.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.core.JsonProcessingException;
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class RequestDispatcher implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final Map<List<String>, ActionHandler<Object, Object>> handlers = new HashMap<>();

	public RequestDispatcher() {
		addHandler(new Version());
		addHandler(new CompileAndRun());
	}

	@SuppressWarnings("unchecked")
	private void addHandler(ActionHandler<?, ?> handler) {
		for (String m : handler.getMethods()) {
			handlers.put(List.of(handler.getName(), m), (ActionHandler<Object, Object>) handler);
		}
	}

	private ActionHandler<Object, Object> getHandler(String name, String method) {
		return handlers.get(List.of(name, method));
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		String method = request.getHttpMethod();
		String action = request.getPathParameters().get("action");
		String origin = request.getHeaders().get("origin");

		ActionHandler<Object, Object> handler = getHandler(action, method);

		if (handler == null) {
			response.setStatusCode(404);
			response.setBody(String.format("No handler for %s %s", method, action));
			return response;
		}

		ObjectMapper mapper = new ObjectMapper();

		try {
			Object requestObject = null;
			if (!handler.getRequestType().equals(Void.class)) {
				requestObject = mapper.readValue(request.getBody(), handler.getRequestType());
			}

			Object responseObject = handler.handle(requestObject);

			response.setStatusCode(200);
			Map<String, String> header = new HashMap<>();
			if (origin != null) {
				header.put("Access-Control-Allow-Origin", origin);
			}
			response.setHeaders(header);

			response.setBody(mapper.writeValueAsString(responseObject));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return response;
	}

}
