package io.javaalmanac.sandbox.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Forks an new JVM from the currently used JDK installation.
 */
public class SandboxLauncher {

	private static final String[] JAVA_EXECUTABLE_NAME = new String[] { "java", "java.exe" };

	private final List<String> commandBase;

	public SandboxLauncher() {
		commandBase = new ArrayList<>();
		commandBase.add(getJavaExecutable());
	}

	public void setSandboxClassLoader() {
		commandBase.add("-Djava.system.class.loader=" + SandboxClassLoader.class.getName());
	}

	public void setClassPath(String path) {
		commandBase.add("-cp");
		commandBase.add(path);
	}

	public void inheritClassPath() {
		setClassPath(System.getProperty("java.class.path"));
	}

	public void setDefaultEncoding(Charset encoding) {
		commandBase.add("-Dfile.encoding=" + encoding);
	}

	public void setMaxHeap(int megabyte) {
		commandBase.add("-Xmx" + megabyte + "m");
	}

	public void addParam(String param) {
		commandBase.add(param);
	}

	public Process run(String mainClass) throws IOException {
		List<String> cmd = new ArrayList<>(commandBase);
		cmd.add(mainClass);
		ProcessBuilder builder = new ProcessBuilder(cmd);
		return builder.start();
	}

	public static boolean waitFor(Process process, long timeout, TimeUnit unit) throws InterruptedException {
		boolean success = process.waitFor(timeout, unit);
		if (!success) {
			process.destroyForcibly();
		}
		return success;
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

}
