package io.javaalmanac.sandbox.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class VersionHandler implements RequestHandler<Void, Map<String, String>> {

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
	public Map<String, String> handleRequest(Void input, Context context) {
		Map<String, String> response = new HashMap<>();

		for (String key : VERSION_KEYS) {
			response.put(key, System.getProperty(key));
		}

		return response;
	}

}
