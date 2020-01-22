package io.javaalmanac.sandbox.service;

import io.javalin.Javalin;

public class Server {

	public static void main(String[] args) {
		Javalin app = Javalin.create().start(8000);
		app.post("/compileandrun", new CompileAndRunHandler());
	}

}
