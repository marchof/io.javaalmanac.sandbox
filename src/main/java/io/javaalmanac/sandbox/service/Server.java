package io.javaalmanac.sandbox.service;

import io.javalin.Javalin;

public class Server {

	public static void main(String[] args) {
		int port = Integer.parseInt(System.getenv("PORT"));
		Javalin app = Javalin.create(config -> {
			config.enableCorsForOrigin("http://localhost", "https://javaalmanac.io/", "https://www.javaalmanac.io/");
		}).start(port);
		app.get("/health", ctx -> ctx.result("ok"));
		app.get("/version", ctx -> ctx.result(System.getProperty("java.vm.version")));
		app.post("/compileandrun", new CompileAndRunHandler());
	}

}
