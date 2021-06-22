package io.javaalmanac.sandbox.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.javaalmanac.sandbox.impl.SandboxLauncher.Result;
import io.javaalmanac.sandbox.impl.targets.Exit;
import io.javaalmanac.sandbox.impl.targets.NoOutput;
import io.javaalmanac.sandbox.impl.targets.OutErr;
import io.javaalmanac.sandbox.impl.targets.Timeout;

public class SandboxLauncherTest {

	private SandboxLauncher sandbox;

	private Result result;

	@BeforeEach
	void setup() {
		sandbox = new SandboxLauncher(Path.of("./target/sandbox"));
	}

	private void run(Class<?> target) throws Exception {
		String source = target.getName().replace('.', '/') + ".java";

		InMemoryCompiler compiler = new InMemoryCompiler();
		compiler.addSource(source, Files.readString(Path.of("src/test/java", source)));
		InMemoryCompiler.Result compileResult = compiler.compile();
		assertTrue(compileResult.isSuccess());

		SandboxLauncher sandbox = new SandboxLauncher(Path.of("./target/sandbox"));

		result = sandbox.run(target.getName(), compileResult.getClassfiles());
	}

	@Test
	void should_combinde_stdout_and_stderr() throws Exception {
		run(OutErr.class);
		assertEquals(0, result.getStatus());
		assertThat(result.getOutput(), containsString("Hello from Out!"));
		assertThat(result.getOutput(), containsString("Hello from Err!"));
	}

	@Test
	void should_capture_exit_value() throws Exception {
		run(Exit.class);
		assertEquals(42, result.getStatus());
	}

	@Test
	void should_cancel_after_timeout() throws Exception {
		run(Timeout.class);
		assertEquals(Result.TIMEOUT_STATUS, result.getStatus());
	}

	@Test
	void should_not_produce_additional_output() throws Exception {
		run(NoOutput.class);
		assertEquals("", result.getOutput());
	}

	@Test
	void should_run_with_preview() throws Exception {
		sandbox.enablePreview();
		run(NoOutput.class);
		assertEquals("", result.getOutput());
	}

}
