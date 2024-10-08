package io.javaalmanac.sandbox.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.javaalmanac.sandbox.Java11Compat;

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

	private static final int MAXHEAP_MB = 64;

	private static final int MAXOUTPUT_BYTES = 0x10_000;

	private static final String[] JAVA_EXECUTABLE_NAME = new String[] { "java", "java.exe" };

	private Path workdir;

	private final List<String> commandBase;

	public SandboxLauncher(Path workdir) {
		this.workdir = workdir;
		commandBase = new ArrayList<>();
		commandBase.add(getJavaExecutable());
		commandBase.add(getFileEncodingArg());
		commandBase.add(getStdoutEncodingArg());
		commandBase.add(getStderrEncodingArg());
		commandBase.add(getMaxHeapArg());
		commandBase.add(getClassDataSharingArg());
		commandBase.add(getDisableOptimizationArg());
	}

	public void enablePreview() {
		commandBase.add("--enable-preview");
	}

	private String getFileEncodingArg() {
		return "-Dfile.encoding=" + UTF_8;
	}

	private String getStdoutEncodingArg() {
		return "-Dstdout.encoding=" + UTF_8;
	}

	private String getStderrEncodingArg() {
		return "-Dstderr.encoding=" + UTF_8;
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

	public Result runClassFiles(String mainClass, Map<String, byte[]> classfiles)
			throws IOException, InterruptedException {
		List<String> cmd = new ArrayList<>(commandBase);
		cmd.add(mainClass);

		prepareWorkingDirectory();
		writeFiles(classfiles);

		return runJava(cmd);
	}

	public Result runSourceFiles(String mainSource, Map<String, byte[]> sourcefiles)
			throws IOException, InterruptedException {
		List<String> cmd = new ArrayList<>(commandBase);
		cmd.add(mainSource);

		prepareWorkingDirectory();
		writeFiles(sourcefiles);

		return runJava(cmd);
	}

	private Result runJava(List<String> cmd) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(cmd);
		// Do not inherit environment variables
		builder.environment().clear();
		builder.directory(workdir.toFile());
		builder.redirectErrorStream(true);

		Process process = builder.start();

		Result result = new Result();
		boolean success = process.waitFor(TIMEOUT_SEC, TimeUnit.SECONDS);
		if (success) {
			result.output = new String(Java11Compat.InputStream.readNBytes(process.getInputStream(), MAXOUTPUT_BYTES),
					UTF_8);
			result.status = process.exitValue();
		} else {
			result.output = new String(readAvailableBytes(process.getInputStream()), UTF_8);
			result.status = Result.TIMEOUT_STATUS;
			process.destroyForcibly();
		}
		return result;
	}

	private void writeFiles(Map<String, byte[]> files) throws IOException {
		for (Map.Entry<String, byte[]> e : files.entrySet()) {
			Path file = workdir.resolve(e.getKey());
			Files.createDirectories(file.getParent());
			Files.write(file, e.getValue());
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
			buffer.write(Java11Compat.InputStream.readNBytes(in, available));
		}
		return buffer.toByteArray();
	}

	private void prepareWorkingDirectory() throws IOException {
		if (Files.exists(workdir)) {
			Files.walk(workdir).sorted(Comparator.reverseOrder()).forEach(this::delete);
		}
		Files.createDirectories(workdir);
	}

	private void delete(Path p) {
		try {
			Files.delete(p);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
