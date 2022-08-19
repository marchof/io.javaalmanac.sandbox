package io.javaalmanac.sandbox.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.javaalmanac.sandbox.Java11Compat;
import io.javaalmanac.sandbox.impl.SandboxLauncher.Result;
import io.javaalmanac.sandbox.impl.targets.Exit;
import io.javaalmanac.sandbox.impl.targets.NoMain;
import io.javaalmanac.sandbox.impl.targets.NoOutput;
import io.javaalmanac.sandbox.impl.targets.OutErr;
import io.javaalmanac.sandbox.impl.targets.Timeout;

public class SandboxLauncherTest {

	private SandboxLauncher sandbox;

	private Result result;

	@BeforeEach
	void setup() {
		sandbox = new SandboxLauncher(Paths.get("./target/sandbox"));
	}

	private void run(Class<?> target) throws Exception {
		String source = target.getName().replace('.', '/') + ".java";

		InMemoryCompiler compiler = new InMemoryCompiler();
		compiler.addSource(source, Java11Compat.Files.readString(Paths.get("src/test/java", source)));
		InMemoryCompiler.Result compileResult = compiler.compile();
		assertTrue(compileResult.isSuccess());

		SandboxLauncher sandbox = new SandboxLauncher(Paths.get("./target/sandbox"));

		result = sandbox.run(target.getName(), compileResult.getClassfiles());
	}

	@Test
	void should_combine_stdout_and_stderr() throws Exception {
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
	void should_capture_vm_failure_message() throws Exception {
		run(NoMain.class);
		assertEquals(1, result.getStatus());
		System.out.println(result.getOutput());
		assertThat(result.getOutput(), containsString("Main method not found"));
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
