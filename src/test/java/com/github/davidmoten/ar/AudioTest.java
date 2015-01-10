package com.github.davidmoten.ar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AudioTest {

	@Test
	public void testReadUsingObservable() {
		int count = Audio
				.read(AudioTest.class.getResourceAsStream("/alphabet.wav"))
				.count().toBlocking().single();
		System.out.println("count=" + count);
		assertEquals(296934, count);
	}

}
