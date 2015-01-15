package com.github.davidmoten.ar;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

import rx.functions.Action1;
import rx.functions.Func1;

public class AudioTest {

	private static Func1<double[], double[]> toFft() {
		return new Func1<double[], double[]>() {

			@Override
			public double[] call(double[] signal) {
				return FFT.fftMagnitude(signal);
			}
		};
	}

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
		final int frameSize = 256;
		final BufferedImage image = new BufferedImage(
				1200 * pixelsPerHorizontalCell, frameSize
						* pixelsPerVerticalCell, BufferedImage.TYPE_INT_ARGB);
		Audio.readSignal(AudioTest.class.getResourceAsStream("/alphabet.wav"))
		// buffer
				.buffer(frameSize)
				// full frames only
				.filter(Util.<Integer> hasSize(frameSize))
				// to double arrays
				.map(Util.TO_DOUBLE_ARRAY)
				// extract frequencies
				.map(toFft())
				// to list
				.map(Util.TO_LIST)
				// get all
				.toList()
				// draw image
				.doOnNext(draw(image))
				// go
				.subscribe();
	}

	@Test
	public void testExtractMFCCs() {
		final int frameSize = 256;
		int numTriFilters = 26;
		int numMfcCoefficients = 13;
		Audio.readSignal(AudioTest.class.getResourceAsStream("/alphabet.wav"))
		// get frames
				.buffer(frameSize)
				// full frames only
				.filter(Util.<Integer> hasSize(frameSize))
				// as array of double
				.map(Util.TO_DOUBLE_ARRAY)
				// emphasize higher frequencies
				.map(new PreEmphasisFunction())
				// apply filter to handle discontinuities at start and end
				.map(new HammingWindowFunction())
				// extract frequencies
				.map(toFft())
				// tri bandpass filter
				.map(new TriangularBandPassFilterBankFunction(numTriFilters,
						frameSize))
				// DCT
				.map(new DiscreteCosineTransformFunction(numMfcCoefficients,
						numTriFilters))
				// to list of double MFCCs
				.map(Util.TO_LIST)
				// print mfcc values
				.doOnNext(println())
				// go
				.subscribe();
	}

	private <T> Action1<T> println() {
		return new Action1<T>() {

			@Override
			public void call(T t) {
				System.out.println(t);
			}
		};
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

	private static Action1<List<List<Double>>> draw2(final BufferedImage image) {
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
							if (min == null || min > d)
								min = d;
							if (max == null || max < d)
								max = d;
						}
						isFirst = false;
					}
				}
				int sample = 0;

				for (List<Double> list : all) {
					int freq = 0;
					for (double d : list) {
						double prop = Math.max(0,
								Math.min(1.0, (d - min) / (max - min)));
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
					ImageIO.write(image, "png", new File("target/image2.png"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
