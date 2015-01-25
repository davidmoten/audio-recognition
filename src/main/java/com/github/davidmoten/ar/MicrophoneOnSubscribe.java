package com.github.davidmoten.ar;

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import rx.Observable.OnSubscribe;
import rx.Subscriber;

public class MicrophoneOnSubscribe implements OnSubscribe<byte[]> {
	// see
	// http://www.developer.com/java/other/article.php/1572251/Java-Sound-Getting-Started-Part-1-Playback.htm#Complete%20Program%20Listings

	private final int bufferSize;
	private final AudioFormat format;

	public MicrophoneOnSubscribe(int bufferSize, AudioFormat format) {
		this.bufferSize = bufferSize;
		this.format = format;
	}

	@Override
	public void call(final Subscriber<? super byte[]> child) {

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
			byte[] buffer = new byte[bufferSize];
			int count = 0;
			while (!child.isUnsubscribed() && (line.isOpen())
					&& (count = line.read(buffer, 0, bufferSize)) != -1) {
				if (count > 0)
					child.onNext(Arrays.copyOf(buffer, count));
			}
			child.onCompleted();
			line.close();
		} catch (LineUnavailableException e) {
			child.onError(e);
		}
	}

}
