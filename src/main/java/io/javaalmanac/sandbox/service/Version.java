package io.javaalmanac.sandbox.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.javaalmanac.sandbox.Java11Compat;

public class Version implements ActionHandler<Void, Map<String, String>> {

	@Override
	public String getName() {
		return "version";
	}

	@Override
	public Set<String> getMethods() {
		return Java11Compat.Set.of("GET", "POST");
	}

	@Override
	public Class<Void> getRequestType() {
		return Void.class;
	}

	private static final Set<String> VERSION_KEYS = Java11Compat.Set.of( //
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
	public Map<String, String> handle(Void request) {

		Map<String, String> response = new HashMap<>();

		for (String key : VERSION_KEYS) {
			response.put(key, System.getProperty(key));
		}

		return response;
	}

}
