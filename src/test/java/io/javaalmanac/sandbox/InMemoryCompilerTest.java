package io.javaalmanac.sandbox;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.javaalmanac.sandbox.InMemoryCompiler.Result;

public class InMemoryCompilerTest {

	private InMemoryCompiler compiler;

	private Result result;

	@BeforeEach
	void setup() {
		compiler = new InMemoryCompiler();
	}

	@Test
	void should_compile_single_file() throws IOException {
		compiler.addSource("foo/Foo.java", "package foo; class Foo { }");
		result = compiler.compile();
		assertThat(result.getMessages(), empty());
		assertTrue(result.isSuccess());
		assertEquals(Set.of("foo/Foo.class"), result.getClassfiles().keySet());
	}

	@Test
	void should_compile_two_files_with_cyclic_dependencies() throws IOException {
		compiler.addSource("foo/Foo.java", "package foo; class Foo { Bar bar; }");
		compiler.addSource("foo/Bar.java", "package foo; class Bar { Foo foo; }");
		result = compiler.compile();
		assertThat(result.getMessages(), empty());
		assertTrue(result.isSuccess());
		assertEquals(result.getClassfiles().keySet(), Set.of("foo/Foo.class", "foo/Bar.class"));
	}

	@Test
	void should_compile_inner_class() throws IOException {
		compiler.addSource("foo/Foo.java", "package foo; class Foo { class Inner { } }");
		result = compiler.compile();
		assertThat(result.getMessages(), empty());
		assertTrue(result.isSuccess());
		assertEquals(Set.of("foo/Foo.class", "foo/Foo$Inner.class"), result.getClassfiles().keySet());
	}

	@Test
	void should_compile_with_dependency_on_java_lib() throws IOException {
		compiler.addSource("MySet.java", "import java.util.Set; interface MySet extends Set { }");
		result = compiler.compile();
		assertThat(result.getMessages(), empty());
		assertTrue(result.isSuccess());
		assertEquals(Set.of("MySet.class"), result.getClassfiles().keySet());
	}

	@Test
	void should_fail_on_syntax_error() throws IOException {
		compiler.addSource("Foo.java", "class Foo { }}");
		result = compiler.compile();
		assertFalse(result.isSuccess());
	}

}
