package io.javaalmanac.sandbox.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

	private void runCompiled(Class<?> target) throws Exception {
		String source = target.getName().replace('.', '/') + ".java";

		InMemoryCompiler compiler = new InMemoryCompiler();
		compiler.addSource(source, Java11Compat.Files.readString(Paths.get("src/test/java", source)));
		InMemoryCompiler.Result compileResult = compiler.compile();
		assertTrue(compileResult.isSuccess());

		SandboxLauncher sandbox = new SandboxLauncher(Paths.get("./target/sandbox"));

		result = sandbox.runClassFiles(target.getName(), compileResult.getClassfiles());
	}

	private void runSource(Class<?> target) throws Exception {
		String sourcefile = target.getName().replace('.', '/') + ".java";

		Map<String, byte[]> sourcefiles = Java11Compat.Map.of(sourcefile,
				Java11Compat.Files.readAllBytes(Paths.get("src/test/java", sourcefile)));

		SandboxLauncher sandbox = new SandboxLauncher(Paths.get("./target/sandbox"));

		result = sandbox.runSourceFiles(sourcefile, sourcefiles);
	}

	@Test
	void should_combine_stdout_and_stderr() throws Exception {
		runCompiled(OutErr.class);
		assertEquals(0, result.getStatus());
		assertThat(result.getOutput(), containsString("Hello from Out!"));
		assertThat(result.getOutput(), containsString("Hello from Err!"));
	}

	@Test
	@Disabled("Only available from Java 11 on")
	void should_combine_stdout_and_stderr_from_source() throws Exception {
		runSource(OutErr.class);
		System.out.println(result.getOutput());
		assertEquals(0, result.getStatus());
		assertThat(result.getOutput(), containsString("Hello from Out!"));
		assertThat(result.getOutput(), containsString("Hello from Err!"));
	}

	@Test
	void should_capture_exit_value() throws Exception {
		runCompiled(Exit.class);
		assertEquals(42, result.getStatus());
	}

	@Test
	void should_capture_vm_failure_message() throws Exception {
		runCompiled(NoMain.class);
		assertEquals(1, result.getStatus());
		System.out.println(result.getOutput());
		assertThat(result.getOutput(), containsString("Main method not found"));
	}

	@Test
	void should_cancel_after_timeout() throws Exception {
		runCompiled(Timeout.class);
		assertEquals(Result.TIMEOUT_STATUS, result.getStatus());
	}

	@Test
	void should_not_produce_additional_output() throws Exception {
		runCompiled(NoOutput.class);
		assertEquals("", result.getOutput());
	}

	@Test
	void should_run_with_preview() throws Exception {
		sandbox.enablePreview();
		runCompiled(NoOutput.class);
		assertEquals("", result.getOutput());
	}

}
