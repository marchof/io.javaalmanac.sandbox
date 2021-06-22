package io.javaalmanac.sandbox.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import io.javaalmanac.sandbox.api.CompileAndRunRequest;
import io.javaalmanac.sandbox.api.CompileAndRunResponse;
import io.javaalmanac.sandbox.api.SourceFile;
import io.javaalmanac.sandbox.impl.InMemoryCompiler;
import io.javaalmanac.sandbox.impl.SandboxLauncher;

public class CompileAndRun implements ActionHandler<CompileAndRunRequest, CompileAndRunResponse> {

	private Path workdir;

	CompileAndRun() {
		workdir = Path.of(System.getenv("SANDBOX_WORK_DIR"));
	}

	@Override
	public String getName() {
		return "compileandrun";
	}

	@Override
	public Set<String> getMethods() {
		return Set.of("POST");
	}

	@Override
	public Class<CompileAndRunRequest> getRequestType() {
		return CompileAndRunRequest.class;
	}

	public CompileAndRunResponse handle(CompileAndRunRequest request) {

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
			SandboxLauncher sandbox = new SandboxLauncher(workdir);
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
