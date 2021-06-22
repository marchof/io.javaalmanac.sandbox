package io.javaalmanac.sandbox.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Exec {

	public static void main(String[] args) throws Exception {
		ProcessBuilder builder = new ProcessBuilder(List.of("/bin/bash", "-c", "ls -al /"));
		Process process = builder.start();

		process.waitFor(3, TimeUnit.SECONDS);
		System.out.println(new String(process.getInputStream().readNBytes(4000), StandardCharsets.UTF_8));
	}

}
