package io.javaalmanac.sandbox.attacks;

import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFile {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		new FileOutputStream("out.txt");
	}

}
