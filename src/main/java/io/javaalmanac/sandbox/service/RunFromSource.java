package io.javaalmanac.sandbox.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.javaalmanac.sandbox.Java11Compat;
import io.javaalmanac.sandbox.api.RunRequest;
import io.javaalmanac.sandbox.api.RunResponse;
import io.javaalmanac.sandbox.api.SourceFile;
import io.javaalmanac.sandbox.impl.SandboxLauncher;

public class RunFromSource implements ActionHandler<RunRequest, RunResponse> {

	private Path workdir;

	RunFromSource() {
		workdir = Paths.get(System.getenv("SANDBOX_WORK_DIR"));
	}

	@Override
	public String getName() {
		return "runfromsource";
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

		Map<String, byte[]> sources = new HashMap<String, byte[]>();
		for (SourceFile s : request.sourcefiles) {
			sources.put(s.name, s.content.getBytes(StandardCharsets.UTF_8));
		}

		RunResponse response = new RunResponse();
		response.compilesuccess = true;

		SandboxLauncher sandbox = new SandboxLauncher(workdir);
		if (request.preview) {
			sandbox.enablePreview();
		}
		SandboxLauncher.Result runResult;
		try {
			runResult = sandbox.runClassFiles(request.mainclass, sources);
			response.runstatus = runResult.getStatus();
			response.output = runResult.getOutput();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		return response;
	}

}
