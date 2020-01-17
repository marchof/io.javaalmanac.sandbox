package io.javaalmanac.sandbox.attacks;

import java.io.FileInputStream;
import java.io.IOException;

public class ReadFile {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		new FileInputStream("secret");
	}

}
