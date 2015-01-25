package com.github.davidmoten.ar;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.fastdtw.timeseries.TimeSeries;

public class MicrophoneMain {
	public static void main(String[] args) {
		Audio.microphone()
				.window(100)
				.flatMap(
						new Func1<Observable<Integer>, Observable<TimeSeries>>() {

							@Override
							public Observable<TimeSeries> call(
									Observable<Integer> buffer) {
								return Audio.timeSeries(buffer, 256, 156, 20,
										13);
							}
						})
				// for each
				.forEach(new Action1<TimeSeries>() {

					@Override
					public void call(TimeSeries t) {
						System.out.println(t.size());
					}
				});
	}
}
