package io.javaalmanac.sandbox.attacks;

import java.io.IOException;
import java.net.URL;

public class OpenUrl {

	public static void main(String[] args) throws IOException {
		new URL("http://javaalmanac.io/").getContent();
	}

}
