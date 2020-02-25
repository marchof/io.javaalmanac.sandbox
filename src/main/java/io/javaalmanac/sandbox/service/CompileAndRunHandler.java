package io.javaalmanac.sandbox.service;

import io.javaalmanac.sandbox.api.CompileAndRunReponse;
import io.javaalmanac.sandbox.api.CompileAndRunRequest;
import io.javaalmanac.sandbox.api.SourceFile;
import io.javaalmanac.sandbox.impl.InMemoryCompiler;
import io.javaalmanac.sandbox.impl.SandboxLauncher;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class CompileAndRunHandler implements Handler {

	@Override
	public void handle(Context ctx) throws Exception {

		CompileAndRunRequest request = ctx.bodyAsClass(CompileAndRunRequest.class);
		CompileAndRunReponse response = new CompileAndRunReponse();

		InMemoryCompiler compiler = new InMemoryCompiler();
		if (request.preview) {
			compiler.enablePreview();
		}
		for (SourceFile s : request.sourcefiles) {
			compiler.addSource(s.name, s.content);
		}
		InMemoryCompiler.Result cmpresult = compiler.compile();

		response.compilesuccess = cmpresult.isSuccess();
		response.output = cmpresult.getMessagesAsText();

		if (cmpresult.isSuccess()) {
			SandboxLauncher sandbox = new SandboxLauncher();
			if (request.preview) {
				sandbox.enablePreview();
			}
			SandboxLauncher.Result runResult = sandbox.run(request.mainclass, cmpresult.getClassfiles());
			response.runstatus = runResult.getStatus();
			response.output += runResult.getOutput();
		}

		ctx.json(response);
	}

}
