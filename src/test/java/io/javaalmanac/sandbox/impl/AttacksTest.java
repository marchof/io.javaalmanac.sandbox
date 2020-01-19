package io.javaalmanac.sandbox.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessControlException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.javaalmanac.sandbox.attacks.GetSystemProperties;
import io.javaalmanac.sandbox.attacks.OpenUrl;
import io.javaalmanac.sandbox.attacks.ReadFile;
import io.javaalmanac.sandbox.attacks.TooMuchMemory;
import io.javaalmanac.sandbox.attacks.WriteSystemProperty;
import io.javaalmanac.sandbox.impl.InMemoryCompiler;
import io.javaalmanac.sandbox.impl.InMemoryCompiler.Result;

/**
 * Verifies that different attacks result in termination of target vm.
 */
public class AttacksTest {

	private Process process;

	private String output;

	@Test
	void get_system_properties() throws Exception {
		expectAccessControlException(GetSystemProperties.class, "PropertyPermission");
	}

	@Test
	void write_system_properties() throws Exception {
		expectAccessControlException(WriteSystemProperty.class, "PropertyPermission");
	}

	@Test
	void read_file() throws Exception {
		expectAccessControlException(ReadFile.class, "FilePermission");
	}

	@Test
	void open_url() throws Exception {
		expectAccessControlException(OpenUrl.class, "SocketPermission");
	}

	@Test
	void too_much_memory() throws Exception {
		expectException(TooMuchMemory.class, OutOfMemoryError.class);
	}

	private void expectAccessControlException(Class<?> target, String permission) throws Exception {
		expectException(target, AccessControlException.class);
		assertThat(output, containsString(permission));
	}

	private void expectException(Class<?> target, Class<?> exception) throws Exception {
		runInSandbox(target);
		assertEquals(1, process.exitValue());
		assertThat(output, containsString(exception.getName()));
	}

	private void runInSandbox(Class<?> target) throws Exception {
		String source = target.getName().replace('.', '/') + ".java";

		InMemoryCompiler compiler = new InMemoryCompiler();
		compiler.addSource(source, Files.readString(Path.of("src/test/java", source)));
		Result result = compiler.compile();
		assertTrue(result.isSuccess());

		SandboxLauncher sandbox = new SandboxLauncher();
		sandbox.setMaxHeap(4);
		sandbox.inheritClassPath();
		sandbox.setSandboxClassLoader();

		process = sandbox.run(target.getName());
		result.writeJar(process.getOutputStream());

		output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
				+ new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

		SandboxLauncher.waitFor(process, 2, TimeUnit.SECONDS);
	}

}
