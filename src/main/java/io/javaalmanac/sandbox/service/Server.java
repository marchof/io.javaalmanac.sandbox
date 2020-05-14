package io.javaalmanac.sandbox.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;

public class Server {

	private static final Logger LOG = LoggerFactory.getLogger("access");

	public static void main(String[] args) {
		int port = Integer.parseInt(System.getenv("PORT"));
		Javalin app = Javalin.create(config -> {
			config.enableCorsForOrigin("http://localhost", "https://javaalmanac.io/", "https://www.javaalmanac.io/");
			config.requestLogger((ctx, ms) -> {
				LOG.info("{} \"{} {} {}\" {} in {}ms", //
						ctx.ip(), ctx.method(), ctx.path(), ctx.protocol(), ctx.res.getStatus(), ms);
			});
		}).start(port);
		app.get("/health", ctx -> ctx.result("ok"));
		app.get("/version", ctx -> ctx.result(System.getProperty("java.vm.version")));
		app.post("/compileandrun", new CompileAndRunHandler());
	}

}
