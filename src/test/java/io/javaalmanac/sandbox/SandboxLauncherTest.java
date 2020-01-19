package io.javaalmanac.sandbox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SandboxLauncherTest {

	private SandboxLauncher sandbox;

	@BeforeEach
	void setup() {
		sandbox = new SandboxLauncher();
		sandbox.inheritClassPath();
		sandbox.setDefaultEncoding(StandardCharsets.UTF_8);
	}

	private String run(Class<?> main) throws IOException {
		Process run = sandbox.run(main.getName());
		return new String(run.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
				+ new String(run.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
	}

	@Test
	void run_should_execute_main() throws IOException {
		String out = run(Main.class);
		assertThat(out, containsString("Hello from the sandbox!"));
	}

	public static class Main {
		public static void main(String[] args) {
			System.out.println("Hello from the sandbox!");
		}
	}

}
