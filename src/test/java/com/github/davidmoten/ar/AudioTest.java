package com.github.davidmoten.ar;

public class AudioTest {

	public static void main(String[] args) {
		int count = Audio
				.read(AudioTest.class.getResourceAsStream("/alphabet.wav"))
				.count().toBlocking().single();
		System.out.println("count=" + count);
	}

}
