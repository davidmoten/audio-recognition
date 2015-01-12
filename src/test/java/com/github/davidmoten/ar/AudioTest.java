package com.github.davidmoten.ar;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

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

	private static final int pixelsPerVerticalCell = 1;
	private static final int pixelsPerHorizontalCell = 8;

	@Test
	public void testReadUsingObservableAndFft() {
		final int bufferSize = 256;
		final BufferedImage image = new BufferedImage(
				1200 * pixelsPerHorizontalCell, bufferSize
						* pixelsPerVerticalCell, BufferedImage.TYPE_INT_ARGB);
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

	private static Color toColor(double d) {
		return Color.getHSBColor(1f - (float) d, 1f, (float) d * 1.0f);
	}

	private static Action1<List<List<Double>>> draw(final BufferedImage image) {
		return new Action1<List<List<Double>>>() {
			@Override
			public void call(List<List<Double>> all) {
				Graphics2D g = image.createGraphics();
				Double min = null;
				Double max = null;
				for (List<Double> list : all) {
					boolean isFirst = true;
					for (double d : list) {
						if (!isFirst) {
							double dlog = Math.max(0, Math.log10(d));
							if (min == null || min > dlog)
								min = dlog;
							if (max == null || max < dlog)
								max = dlog;
						}
						isFirst = false;
					}
				}
				int sample = 0;

				for (List<Double> list : all) {
					int freq = 0;
					for (double d : list) {
						double prop = Math.max(
								0,
								Math.min(1.0, (Math.log10(d) - min)
										/ (max - min)));
						Color color = toColor(prop);
						g.setColor(color);
						g.fillRect(sample * pixelsPerHorizontalCell,
								(freq + list.size() / 2) % list.size()
										* pixelsPerVerticalCell,
								pixelsPerHorizontalCell, pixelsPerVerticalCell);
						freq++;
					}
					sample += 1;
				}
				try {
					ImageIO.write(image, "png", new File("target/image.png"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
