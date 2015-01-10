package com.github.davidmoten.ar;

public class AudioTest {

	public static void main(String[] args) {
		Audio.play(AudioTest.class.getResourceAsStream("/alphabet.wav"));
	}

}
