package io.javaalmanac.sandbox.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.javaalmanac.sandbox.impl.SandboxLauncher.Result;

public class SandboxLauncherTest {

	private SandboxLauncher sandbox;

	private Result result;

	@BeforeEach
	void setup() {
		sandbox = new SandboxLauncher();
	}

	private void run(Class<?> main) throws Exception {
		result = sandbox.run(main.getName(), Collections.emptyMap());
	}

	@Test
	void should_combinde_stdout_and_stderr() throws Exception {
		run(OutErr.class);
		assertEquals(0, result.getStatus());
		assertThat(result.getOutput(), containsString("Hello from Out!"));
		assertThat(result.getOutput(), containsString("Hello from Err!"));
	}

	public static class OutErr {
		public static void main(String[] args) {
			System.out.println("Hello from Out!");
			System.err.println("Hello from Err!");
		}
	}

	@Test
	void should_capture_exit_value() throws Exception {
		run(Exit.class);
		assertEquals(42, result.getStatus());
	}

	public static class Exit {
		public static void main(String[] args) {
			System.exit(42);
		}
	}

	@Test
	void should_cancel_after_timeout() throws Exception {
		run(Timeout.class);
		assertEquals(Result.TIMEOUT_STATUS, result.getStatus());
	}

	public static class Timeout {
		public static void main(String[] args) throws InterruptedException {
			while (true) {
				Thread.sleep(100);
			}
		}
	}

}
