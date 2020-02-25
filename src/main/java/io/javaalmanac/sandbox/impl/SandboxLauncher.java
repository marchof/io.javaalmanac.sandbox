package io.javaalmanac.sandbox.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Forks an new JVM from the currently used JDK installation.
 */
public class SandboxLauncher {

	public static class Result {

		public static int TIMEOUT_STATUS = -999;

		private int status;

		private String output;

		public int getStatus() {
			return status;
		}

		public boolean isTimeout() {
			return status == TIMEOUT_STATUS;
		}

		public String getOutput() {
			return output;
		}

	}

	private static final int TIMEOUT_SEC = 5;

	private static final int MAXHEAP_MB = 16;

	private static final int MAXOUTPUT_BYTES = 0x1000;

	private static final String[] JAVA_EXECUTABLE_NAME = new String[] { "java", "java.exe" };

	private final List<String> commandBase;

	public SandboxLauncher() {
		commandBase = new ArrayList<>();
		commandBase.add(getJavaExecutable());
		commandBase.add(getSandboxClassLoaderArg());
		commandBase.addAll(getInheritClassPathArgs());
		commandBase.add(getDefaultEncodingArg());
		commandBase.add(getMaxHeapArg());
		commandBase.add(getClassDataSharingArg());
		commandBase.add(getDisableOptimizationArg());
	}

	public void enablePreview() {
		commandBase.add("--enable-preview");
	}

	private String getSandboxClassLoaderArg() {
		return "-Djava.system.class.loader=" + SandboxClassLoader.class.getName();
	}

	private List<String> getInheritClassPathArgs() {
		return Arrays.asList("-cp", System.getProperty("java.class.path"));
	}

	private String getDefaultEncodingArg() {
		return "-Dfile.encoding=" + StandardCharsets.UTF_8;
	}

	private String getMaxHeapArg() {
		return "-Xmx" + MAXHEAP_MB + "m";
	}

	private String getClassDataSharingArg() {
		return "-Xshare:off";
	}

	private String getDisableOptimizationArg() {
		return "-XX:TieredStopAtLevel=1";
	}

	public Result run(String mainClass, Map<String, byte[]> classfiles) throws IOException, InterruptedException {
		List<String> cmd = new ArrayList<>(commandBase);
		cmd.add(mainClass);
		ProcessBuilder builder = new ProcessBuilder(cmd);
		builder.redirectErrorStream(true);

		Process process = builder.start();
		writeJar(classfiles, process.getOutputStream());

		Result result = new Result();
		boolean success = process.waitFor(TIMEOUT_SEC, TimeUnit.SECONDS);
		if (success) {
			result.output = new String(process.getInputStream().readNBytes(MAXOUTPUT_BYTES), StandardCharsets.UTF_8);
			result.status = process.exitValue();
		} else {
			result.output = new String(readAvailableBytes(process.getInputStream()), StandardCharsets.UTF_8);
			result.status = Result.TIMEOUT_STATUS;
			process.destroyForcibly();
		}

		return result;
	}

	private static void writeJar(Map<String, byte[]> classfiles, OutputStream out) throws IOException {
		try (JarOutputStream jar = new JarOutputStream(out)) {
			for (Map.Entry<String, byte[]> e : classfiles.entrySet()) {
				jar.putNextEntry(new ZipEntry(e.getKey()));
				jar.write(e.getValue());
			}
		}
	}

	private static String getJavaExecutable() {
		Path bin = Paths.get(System.getProperty("java.home"), "bin");
		for (String name : JAVA_EXECUTABLE_NAME) {
			Path exec = bin.resolve(name);
			if (Files.isExecutable(exec)) {
				return exec.toString();
			}
		}
		throw new IllegalStateException("No executable java runtime found.");
	}

	private byte[] readAvailableBytes(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int available = in.available();
		while ((available = in.available()) > 0 && buffer.size() < MAXOUTPUT_BYTES) {
			available = Math.min(available, MAXOUTPUT_BYTES - buffer.size());
			buffer.write(in.readNBytes(available));
		}
		return buffer.toByteArray();
	}

}
