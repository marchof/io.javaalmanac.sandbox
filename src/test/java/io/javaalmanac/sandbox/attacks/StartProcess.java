package io.javaalmanac.sandbox.attacks;

public class StartProcess {

	public static void main(String[] args) throws Exception {
		new ProcessBuilder().command("/bin/sh").start();
	}

}
