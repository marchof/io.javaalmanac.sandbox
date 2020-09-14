package io.javaalmanac.sandbox.permitted;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ListDirectory {

	public static void main(String[] args) throws IOException {
		Files.list(Paths.get("/")).count();
	}

}
