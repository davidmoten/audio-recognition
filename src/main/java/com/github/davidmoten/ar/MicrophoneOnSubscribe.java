package com.github.davidmoten.ar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MicrophoneOnSubscribe implements OnSubscribe<byte[]> {

	private static PublishSubject<byte[]> subject = PublishSubject.create();
	private static Observable<byte[]> shared = subject.share();

	private final int bufferSize;

	public MicrophoneOnSubscribe(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@Override
	public void call(final Subscriber<? super byte[]> child) {
		final Type fileType = AudioFileFormat.Type.WAVE;
		AudioFormat format = createDefaultAudioFormat();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

		// checks if system supports the data line
		if (!AudioSystem.isLineSupported(info)) {
			child.onError(new RuntimeException("line not supported for format "
					+ format));
			return;
		}

		try {
			TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			System.out.println("Starting capture...");
			line.start();
			final AudioInputStream ais = new AudioInputStream(line);
			final OutputStream os = createOutputStream(subject, bufferSize);

			final CountDownLatch latch = new CountDownLatch(1);
			shared.doOnSubscribe(new Action0() {

				@Override
				public void call() {
					latch.countDown();
				}
			}).subscribeOn(Schedulers.io()).subscribe(child);

			Schedulers.io().createWorker().schedule(new Action0() {

				@Override
				public void call() {
					try {
						// wait for subscribe
						latch.await(60, TimeUnit.SECONDS);
						System.out.println("Starting recording...");
						// start recordings
						AudioSystem.write(ais, fileType, os);
					} catch (IOException e) {
						child.onError(e);
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			});

		} catch (LineUnavailableException e) {
			child.onError(e);
		}
	}

	private static OutputStream createOutputStream(
			final PublishSubject<byte[]> subject, final int bufferSize) {
		return new OutputStream() {
			final byte[] buffer = new byte[bufferSize];
			int length = 0;

			@Override
			public void write(int b) throws IOException {
				buffer[length] = (byte) b;
				length++;
				if (length == bufferSize) {
					subject.onNext(Arrays.copyOfRange(buffer, 0, length));
					length = 0;
				}
			}

			@Override
			public void close() {
				if (length > 0) {
					subject.onNext(Arrays.copyOfRange(buffer, 0, length));
					length = 0;
				}
			}

		};
	}

	private static AudioFormat createDefaultAudioFormat() {
		float sampleRate = 16000;
		int sampleSizeInBits = 8;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = true;
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
				channels, signed, bigEndian);
		return format;
	}

}
