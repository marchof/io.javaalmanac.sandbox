package io.javaalmanac.sandbox.service;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import io.javaalmanac.sandbox.Java11Compat;

public class RequestDispatcher implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Set<String> ALLOWED_ORIGINS_HOSTS = Java11Compat.Set.of( //
			"localhost", //
			"javaalmanac.io", //
			"www.javaalmanac.io", //
			"horstmann.com", //
			"www.horstmann.com", //
			"jkost.github.io");

	private final ObjectMapper mapper = new ObjectMapper();

	private final Map<String, ActionHandler<Object, Object>> handlers = new HashMap<>();

	public RequestDispatcher() {
		addHandler(new Version());
		addHandler(new CompileAndRun());
		addHandler(new RunFromSource());
	}

	@SuppressWarnings("unchecked")
	private void addHandler(ActionHandler<?, ?> handler) {
		handlers.put(handler.getName(), (ActionHandler<Object, Object>) handler);
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		try {

			String action = request.getPathParameters().get("action");
			ActionHandler<Object, Object> handler = handlers.get(action);
			if (handler == null) {
				return errorResponse(404, "No handler for " + action);
			}

			String method = request.getHttpMethod();
			APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
			addCORSHeaders(handler, request, response);

			if ("OPTIONS".equals(method)) {
				response.setStatusCode(200);
				return response;
			}

			if (!handler.getMethods().contains(method)) {
				return errorResponse(405, "Handler for " + action + " does not support " + method);
			}

			Object requestObject = null;
			if (!handler.getRequestType().equals(Void.class)) {
				requestObject = mapper.readValue(request.getBody(), handler.getRequestType());
			}

			Object responseObject = handler.handle(requestObject);

			response.setBody(mapper.writeValueAsString(responseObject));
			response.setStatusCode(200);
			return response;

		} catch (Exception e) {
			context.getLogger().log(e.toString());
			try {
				return errorResponse(500, e.toString());
			} catch (IOException ee) {
				throw new RuntimeException(ee);
			}
		}
	}

	private APIGatewayProxyResponseEvent errorResponse(int code, String message) throws IOException {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		Map<String, Map<String, Object>> body = Java11Compat.Map.of( //
				"error", Java11Compat.Map.of( //
						"status", code, //
						"message", message));
		response.setStatusCode(code);
		response.setBody(mapper.writeValueAsString(body));
		return response;
	}

	private void addCORSHeaders(ActionHandler<Object, Object> handler, APIGatewayProxyRequestEvent request,
			APIGatewayProxyResponseEvent response) {
		String origin = getHeader(request, "origin");
		if (origin == null) {
			return;
		}
		if (ALLOWED_ORIGINS_HOSTS.contains(URI.create(origin).getHost())) {
			response.setHeaders(Java11Compat.Map.of( //
					"Access-Control-Allow-Origin", origin, //
					"Access-Control-Allow-Headers", "content-type", //
					"Access-Control-Allow-Methods", String.join(", ", handler.getMethods())));
		}
	}

	private String getHeader(APIGatewayProxyRequestEvent request, String key) {
		Map<String, String> headers = request.getHeaders();
		if (headers == null) {
			return null;
		}
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			if (entry.getKey().toLowerCase().equals(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

}
