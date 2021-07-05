package io.javaalmanac.sandbox.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import io.javaalmanac.sandbox.Java11Compat;

/**
 * This class loader is used for the sandbox VM. It loads class definitions as a
 * JAR file from stdin and installs a restrictive security manager.
 */
public class SandboxClassLoader extends ClassLoader {

	private Map<String, byte[]> classfiles;

	public SandboxClassLoader(ClassLoader parent) throws IOException {
		super(parent);
		classfiles = new HashMap<>();
		try (JarInputStream in = new JarInputStream(System.in)) {
			JarEntry entry;
			while ((entry = in.getNextJarEntry()) != null) {
				classfiles.put(entry.getName(), Java11Compat.InputStream.readAllBytes(in));
			}
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		final byte[] bytes = classfiles.get(name.replace('.', '/') + ".class");
		return bytes == null ? super.findClass(name) : defineClass(name, bytes, 0, bytes.length);
	}

}
