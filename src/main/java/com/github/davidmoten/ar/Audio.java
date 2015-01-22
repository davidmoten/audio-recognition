package com.github.davidmoten.ar;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.timeseries.TimeSeriesBase.Builder;

public class Audio {

	public static Observable<Integer> readSignal(final InputStream is) {

		return Observable.create(new OnSubscribe<Integer>() {

			@Override
			public void call(Subscriber<? super Integer> sub) {
				try {
					// Load the Audio Input Stream from the file
					AudioInputStream audioInputStream = AudioSystem
							.getAudioInputStream(is);

					// Get Audio Format information
					AudioFormat audioFormat = audioInputStream.getFormat();

					// log details
					printAudioDetails(audioInputStream, audioFormat);

					// Write the sound to an array of bytes
					int bytesRead;
					byte[] data = new byte[8192];
					while (!sub.isUnsubscribed()
							&& (bytesRead = audioInputStream.read(data, 0,
									data.length)) != -1) {
						// Determine the original Endian encoding format
						boolean isBigEndian = audioFormat.isBigEndian();
						int n = bytesRead / 2;
						// convert each pair of byte values from the byte
						// array to an Endian value
						for (int i = 0; i < n * 2; i += 2) {
							int value = valueFromTwoBytesEndian(data[i],
									data[i + 1], isBigEndian);
							if (sub.isUnsubscribed())
								return;
							else
								sub.onNext(value);
						}
					}
					sub.onCompleted();
				} catch (Exception e) {
					sub.onError(e);
				}
			}

		});
	}

	private static int valueFromTwoBytesEndian(int b1, int b2,
			boolean isBigEndian) {
		if (b1 < 0)
			b1 += 0x100;
		if (b2 < 0)
			b2 += 0x100;
		if (!isBigEndian)
			return (b1 << 8) + b2;
		else
			return b1 + (b2 << 8);
	}

	private static void printAudioDetails(AudioInputStream audioInputStream,
			AudioFormat audioFormat) {
		// Calculate the sample rate
		float sample_rate = audioFormat.getSampleRate();
		System.out.println("sample rate = " + sample_rate);

		// Calculate the length in seconds of the sample
		float T = audioInputStream.getFrameLength()
				/ audioFormat.getFrameRate();
		System.out
				.println("T = " + T + " (length of sampled sound in seconds)");

		// Calculate the number of equidistant points in time
		int num = (int) (T * sample_rate) / 2;
		System.out.println("n = " + num + " (number of equidistant points)");

		// Calculate the time interval at each equidistant point
		float h = (T / num);
		System.out.println("h = " + h
				+ " (length of each time interval in seconds)");
	}

	private static final int BUFFER_SIZE = 1024;

	public static void play(InputStream is) {

		// Load the Audio Input Stream from the file
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(is);
		} catch (UnsupportedAudioFileException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Get Audio Format information
		AudioFormat audioFormat = audioInputStream.getFormat();

		// Handle opening the line
		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
		} catch (LineUnavailableException e) {
			throw new RuntimeException(e);
		}

		// Start playing the sound
		line.start();

		// Write the sound to an array of bytes
		int nBytesRead = 0;
		byte[] abData = new byte[BUFFER_SIZE];
		while (nBytesRead != -1) {
			try {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (nBytesRead >= 0) {
				line.write(abData, 0, nBytesRead);
			}
		}

		// close the line
		line.drain();
		line.close();

	}

	public static Observable<TimeSeries> timeSeries(InputStream wave,
			int frameSize, int numTriFilters, int numMfcCoefficients) {
		return readSignal(wave)
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
				// make a list of the frame mfccs
				.toList()
				// to TimeSeries
				.map(TO_TIME_SERIES);
	}

	public static Observable<TimeSeries> timeSeries(InputStream wave) {
		return timeSeries(wave, 256, 26, 13);
	}

	public static Func1<double[], double[]> toFft() {
		return new Func1<double[], double[]>() {

			@Override
			public double[] call(double[] signal) {
				return FFT.fftMagnitude(signal);
			}
		};
	}

	public static final Func1<List<double[]>, TimeSeries> TO_TIME_SERIES = new Func1<List<double[]>, TimeSeries>() {

		@Override
		public TimeSeries call(List<double[]> list) {
			Builder builder = TimeSeriesBase.builder();
			int time = 0;
			for (double[] mfccs : list) {
				builder = builder.add(time, mfccs);
				time += 1;
			}
			return builder.build();
		}
	};
}
