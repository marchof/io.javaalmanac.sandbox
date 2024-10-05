package io.javaalmanac.sandbox.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import io.javaalmanac.sandbox.Java11Compat;
import io.javaalmanac.sandbox.api.RunRequest;
import io.javaalmanac.sandbox.api.RunResponse;
import io.javaalmanac.sandbox.api.SourceFile;
import io.javaalmanac.sandbox.impl.InMemoryCompiler;
import io.javaalmanac.sandbox.impl.SandboxLauncher;

public class CompileAndRun implements ActionHandler<RunRequest, RunResponse> {

	private Path workdir;

	CompileAndRun() {
		workdir = Paths.get(System.getenv("SANDBOX_WORK_DIR"));
	}

	@Override
	public String getName() {
		return "compileandrun";
	}

	@Override
	public Set<String> getMethods() {
		return Java11Compat.Set.of("POST");
	}

	@Override
	public Class<RunRequest> getRequestType() {
		return RunRequest.class;
	}

	public RunResponse handle(RunRequest request) {

		RunResponse response = new RunResponse();

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
				runResult = sandbox.runClassFiles(request.mainclass, cmpresult.getClassfiles());
				response.runstatus = runResult.getStatus();
				response.output += runResult.getOutput();
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		return response;
	}

}
