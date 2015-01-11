package com.github.davidmoten.ar;

import static org.junit.Assert.assertEquals;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import rx.functions.Action1;
import rx.functions.Func1;

public class AudioTest {

	@Test
	public void testReadUsingObservable() {
		int count = Audio
				.readSignal(
						AudioTest.class.getResourceAsStream("/alphabet.wav"))
				.count().toBlocking().single();
		assertEquals(296934, count);
	}

	@Test
	public void testReadUsingObservableAndFft() {
		final BufferedImage image = new BufferedImage(1200, 256,
				BufferedImage.TYPE_INT_ARGB);
		final int bufferSize = 256;
		Func1<List<Integer>, List<Double>> toFft = new Func1<List<Integer>, List<Double>>() {

			@Override
			public List<Double> call(List<Integer> signal) {
				if (signal.size() == bufferSize) {
					Complex[] spectrum = FFT.fft(Complex.toComplex(signal));
					List<Double> list = new ArrayList<Double>(spectrum.length);
					for (Complex c : spectrum)
						list.add(c.abs());
					return list;
				} else
					return Collections.emptyList();
			}
		};
		Audio.readSignal(AudioTest.class.getResourceAsStream("/alphabet.wav"))
		// buffer
				.buffer(bufferSize)
				// extract frequenciess
				.map(toFft)
				// get all
				.toList().doOnNext(draw(image))
				// go
				.subscribe();
		;
	}

	private static Action1<List<List<Double>>> draw(final BufferedImage image) {
		return new Action1<List<List<Double>>>() {
			@Override
			public void call(List<List<Double>> all) {
				Graphics2D g = image.createGraphics();
				Double min = null;
				Double max = null;
				for (List<Double> list : all)
					for (double d : list) {
						if (min == null || min > d)
							min = d;
						if (max == null || max < d)
							max = d;
					}
			}
		};
	}
}
