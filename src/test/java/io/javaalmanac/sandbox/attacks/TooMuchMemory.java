package io.javaalmanac.sandbox.attacks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TooMuchMemory {

	public static void main(String[] args) throws IOException {
		List<Object> big = new ArrayList<>();
		while (true) {
			big.add(new byte[10_000]);
		}
	}

}
