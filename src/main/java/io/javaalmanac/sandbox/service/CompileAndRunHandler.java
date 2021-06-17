package io.javaalmanac.sandbox.service;

import java.io.IOException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import io.javaalmanac.sandbox.api.CompileAndRunRequest;
import io.javaalmanac.sandbox.api.CompileAndRunResponse;
import io.javaalmanac.sandbox.api.SourceFile;
import io.javaalmanac.sandbox.impl.InMemoryCompiler;
import io.javaalmanac.sandbox.impl.SandboxLauncher;

public class CompileAndRunHandler implements RequestHandler<CompileAndRunRequest, CompileAndRunResponse> {

	@Override
	public CompileAndRunResponse handleRequest(CompileAndRunRequest request, Context context) {

		CompileAndRunResponse response = new CompileAndRunResponse();

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
			SandboxLauncher.Result runResult;
			try {
				runResult = sandbox.run(request.mainclass, cmpresult.getClassfiles());
				response.runstatus = runResult.getStatus();
				response.output += runResult.getOutput();
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		return response;
	}

}
