package com.github.davidmoten.ar;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.fastdtw.timeseries.TimeSeries;

public class MicrophoneMain {
	public static void main(String[] args) {
		Audio.microphone().buffer(100)
				.flatMap(new Func1<List<Integer>, Observable<TimeSeries>>() {

					@Override
					public Observable<TimeSeries> call(List<Integer> buffer) {
						return Audio.timeSeries(Observable.from(buffer), 256,
								156, 20, 13);
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
