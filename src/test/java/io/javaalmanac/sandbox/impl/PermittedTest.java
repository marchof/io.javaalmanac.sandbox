package io.javaalmanac.sandbox.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.javaalmanac.sandbox.permitted.ListDirectory;

/**
 * Verifies that certain actions are still allowed in the target vm.
 */
public class PermittedTest {

	private SandboxLauncher.Result result;

	@Test
	void read_file() throws Exception {
		expectExecutionWithoutException(ListDirectory.class);
	}

	private void expectExecutionWithoutException(Class<?> target) throws Exception {
		runInSandbox(target);
		assertEquals(0, result.getStatus());
		assertEquals("", result.getOutput());
	}

	private void runInSandbox(Class<?> target) throws Exception {
		String source = target.getName().replace('.', '/') + ".java";

		InMemoryCompiler compiler = new InMemoryCompiler();
		compiler.addSource(source, Files.readString(Path.of("src/test/java", source)));
		InMemoryCompiler.Result compileResult = compiler.compile();
		assertTrue(compileResult.isSuccess());

		SandboxLauncher sandbox = new SandboxLauncher();

		result = sandbox.run(target.getName(), compileResult.getClassfiles());
	}

}