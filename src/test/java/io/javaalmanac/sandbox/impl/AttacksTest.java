package io.javaalmanac.sandbox.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.javaalmanac.sandbox.attacks.StartManyThreads;
import io.javaalmanac.sandbox.attacks.TooMuchMemory;
import io.javaalmanac.sandbox.attacks.TooMuchOutput;

/**
 * Verifies that different attacks result in termination of target vm.
 */
public class AttacksTest {

	private SandboxLauncher.Result result;

	@Test
	void too_much_memory() throws Exception {
		expectException(TooMuchMemory.class, OutOfMemoryError.class);
	}

	@Test
	void start_many_threads() throws Exception {
		expectTimeout(StartManyThreads.class);
	}

	@Test
	void too_much_output() throws Exception {
		expectTimeout(TooMuchOutput.class);
		assertThat(result.getOutput(), containsString("more and more and more"));
		assertTrue(result.getOutput().length() <= 0x10_000);
	}

	private void expectException(Class<?> target, Class<?> exception) throws Exception {
		runInSandbox(target);
		assertEquals(1, result.getStatus());
		assertThat(result.getOutput(), containsString(exception.getName()));
	}

	private void expectTimeout(Class<?> target) throws Exception {
		runInSandbox(target);
		assertTrue(result.isTimeout());
	}

	private void runInSandbox(Class<?> target) throws Exception {
		String source = target.getName().replace('.', '/') + ".java";

		InMemoryCompiler compiler = new InMemoryCompiler();
		compiler.addSource(source, Files.readString(Path.of("src/test/java", source)));
		InMemoryCompiler.Result compileResult = compiler.compile();
		assertTrue(compileResult.isSuccess());

		SandboxLauncher sandbox = new SandboxLauncher(Path.of("./target/sandbox"));

		result = sandbox.run(target.getName(), compileResult.getClassfiles());
	}

}
