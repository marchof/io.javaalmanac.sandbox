package io.javaalmanac.sandbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Naive backports of various Java 11 APIs.
 */
public class Java11Compat {

	public static interface Set {

		@SafeVarargs
		static <E> java.util.Set<E> of(E... elements) {
			return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(elements)));
		}

	}

	public static interface List {

		@SafeVarargs
		static <E> java.util.List<E> of(E... elements) {
			return Collections.unmodifiableList(Arrays.asList(elements));
		}

	}

	public static interface Map {

		static <K, V> java.util.Map<K, V> of(K k1, V v1) {
			java.util.Map<K, V> map = new HashMap<>();
			map.put(k1, v1);
			return Collections.unmodifiableMap(map);
		}

		static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2) {
			java.util.Map<K, V> map = new HashMap<>();
			map.put(k1, v1);
			map.put(k2, v2);
			return Collections.unmodifiableMap(map);
		}

		static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
			java.util.Map<K, V> map = new HashMap<>();
			map.put(k1, v1);
			map.put(k2, v2);
			map.put(k3, v3);
			return Collections.unmodifiableMap(map);
		}

	}

	public static class Files {

		public static byte[] readAllBytes(Path path) throws IOException {
			try (java.io.InputStream in = java.nio.file.Files.newInputStream(path)) {
				return InputStream.readAllBytes(in);
			}
		}

		public static String readString(Path path) throws IOException {
			return new String(readAllBytes(path));
		}

	}

	public static class InputStream {

		public static byte[] readNBytes(java.io.InputStream in, int len) throws IOException {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int b;
			while (buffer.size() < len && (b = in.read()) != -1) {
				buffer.write(b);
			}
			return buffer.toByteArray();
		}

		public static byte[] readAllBytes(java.io.InputStream in) throws IOException {
			return readNBytes(in, Integer.MAX_VALUE);
		}

	}

}
