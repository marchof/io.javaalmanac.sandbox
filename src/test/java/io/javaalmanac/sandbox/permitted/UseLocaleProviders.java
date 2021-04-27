package io.javaalmanac.sandbox.permitted;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class UseLocaleProviders {

	public static void main(String[] args) throws IOException {
		NumberFormat.getInstance(Locale.US).format(123.456);
	}

}
