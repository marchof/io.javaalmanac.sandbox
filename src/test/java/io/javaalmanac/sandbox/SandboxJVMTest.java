package io.javaalmanac.sandbox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SandboxJVMTest {

	private SandboxJVM sandbox;

	@BeforeEach
	void setup() {
		sandbox = new SandboxJVM();
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

	@Test
	void setMaxHeap_should_limit_heap() throws IOException, InterruptedException {
		sandbox.setMaxHeap(4);
		String out = run(BigHeap.class);
		assertThat(out, containsString("java.lang.OutOfMemoryError"));
	}

	public static class BigHeap {
		public static void main(String[] args) throws IOException {
			List<Object> big = new ArrayList<>();
			while (true) {
				big.add(new byte[10_000]);
			}
		}
	}

	@Test
	void setDenyAllSecurityPolicy_should_disallow_system_property_access() throws IOException, InterruptedException {
		sandbox.setDenyAllSecurityPolicy();
		String out = run(SystemPropertyAccess.class);
		assertThat(out, containsString("java.security.AccessControlException"));
		assertThat(out, containsString("PropertyPermission"));
	}

	public static class SystemPropertyAccess {
		public static void main(String[] args) {
			System.getProperty("user.home");
		}
	}

	@Test
	void setDenyAllSecurityPolicy_should_disallow_file_access() throws IOException, InterruptedException {
		sandbox.setDenyAllSecurityPolicy();
		String out = run(FileAccess.class);
		assertThat(out, containsString("java.security.AccessControlException"));
		assertThat(out, containsString("FilePermission"));
	}

	public static class FileAccess {
		@SuppressWarnings("resource")
		public static void main(String[] args) throws IOException {
			new FileInputStream("secret.txt");
		}
	}

	@Test
	void setDenyAllSecurityPolicy_should_disallow_http_requests() throws IOException, InterruptedException {
		sandbox.setDenyAllSecurityPolicy();
		String out = run(HttpRequest.class);
		assertThat(out, containsString("java.security.AccessControlException"));
		assertThat(out, containsString("SocketPermission"));
	}

	public static class HttpRequest {
		public static void main(String[] args) throws IOException {
			new URL("http://javaalmanac.io/").getContent();
		}
	}

}
