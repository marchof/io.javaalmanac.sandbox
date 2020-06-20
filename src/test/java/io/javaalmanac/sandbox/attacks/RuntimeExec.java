package io.javaalmanac.sandbox.attacks;

public class RuntimeExec {

	public static void main(String[] args) throws Exception {
		Runtime.getRuntime().exec("/bin/sh");
	}

}