package io.javaalmanac.sandbox.attacks;

public class StartManyThreads {

	public static void main(String[] args) {
		for (int i = 0; true; i++) {
			new Thread(String.valueOf(i)).start();
		}
	}

}
