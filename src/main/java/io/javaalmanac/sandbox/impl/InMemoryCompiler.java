package io.javaalmanac.sandbox.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.ToolProvider;

/**
 * Simple compiler to compile a set of Strings to classes without any file
 * system access.
 */
public class InMemoryCompiler {

	private final List<JavaFileObject> compilationUnits = new ArrayList<>();

	public void addSource(String filename, String content) {
		compilationUnits.add(new MemoryFile(Kind.SOURCE, filename, content));
	}

	public Result compile() {
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		List<Diagnostic<? extends JavaFileObject>> messages = new ArrayList<Diagnostic<? extends JavaFileObject>>();
		final MemoryFileManager fileManager = new MemoryFileManager(compiler);
		boolean success = compiler.getTask(null, fileManager, messages::add, null, null, compilationUnits).call();
		return new Result(success, messages, fileManager.output);
	}

	public static class Result {

		private boolean success;
		private List<Diagnostic<? extends JavaFileObject>> messages;
		private Map<String, byte[]> classfiles;

		Result(boolean success, List<Diagnostic<? extends JavaFileObject>> messages, Map<String, MemoryFile> output) {
			this.success = success;
			this.messages = messages;
			this.classfiles = new HashMap<>();
			output.forEach((name, file) -> classfiles.put(name, file.content.toByteArray()));
		}

		public boolean isSuccess() {
			return success;
		}

		public List<Diagnostic<? extends JavaFileObject>> getMessages() {
			return messages;
		}

		public String getMessagesAsText() {
			StringBuilder result = new StringBuilder();
			messages.forEach(d -> result.append(d).append('\n'));
			return result.toString();
		}

		public Map<String, byte[]> getClassfiles() {
			return classfiles;
		}

		@Override
		public String toString() {
			final StringWriter result = new StringWriter();
			final PrintWriter printer = new PrintWriter(result);
			messages.forEach(printer::println);
			printer.println(success ? "SUCCESS" : "FAILURE");
			return result.toString();
		}
	}

	private static class MemoryFile implements JavaFileObject {

		private static final Charset ENCODING = StandardCharsets.UTF_8;

		private final Kind kind;
		private String name;
		private final ByteArrayOutputStream content;

		MemoryFile(Kind kind, String name) {
			this.kind = kind;
			this.name = name;
			this.content = new ByteArrayOutputStream();
		}

		MemoryFile(Kind kind, String name, String content) {
			this(kind, name);
			this.content.writeBytes(content.getBytes(ENCODING));
		}

		@Override
		public Kind getKind() {
			return kind;
		}

		@Override
		public URI toUri() {
			try {
				return new URI("memory", name, null);
			} catch (URISyntaxException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public InputStream openInputStream() {
			return new ByteArrayInputStream(content.toByteArray());
		}

		@Override
		public Reader openReader(boolean ignoreEncodingErrors) {
			return new InputStreamReader(openInputStream(), ENCODING);
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return new String(content.toByteArray(), ENCODING);
		}

		@Override
		public OutputStream openOutputStream() {
			content.reset();
			return content;
		}

		@Override
		public Writer openWriter() {
			return new OutputStreamWriter(openOutputStream(), ENCODING);
		}

		@Override
		public boolean isNameCompatible(String simpleName, Kind kind) {
			return name.endsWith(simpleName + kind.extension);
		}

		@Override
		public long getLastModified() {
			return 0;
		}

		@Override
		public boolean delete() {
			return false;
		}

		@Override
		public NestingKind getNestingKind() {
			return null;
		}

		@Override
		public Modifier getAccessLevel() {
			return null;
		}

		@Override
		public String toString() {
			return "MemoryFile[" + name + "]";
		}

	}

	private static class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {

		private final Map<String, MemoryFile> output;

		protected MemoryFileManager(JavaCompiler compiler) {
			super(compiler.getStandardFileManager(null, null, null));
			output = new HashMap<>();
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
				throws IOException {
			final String name = className.replace('.', '/') + kind.extension;
			final MemoryFile file = new MemoryFile(kind, name);
			output.put(name, file);
			return file;
		}

	}

}
