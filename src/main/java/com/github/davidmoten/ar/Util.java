package com.github.davidmoten.ar;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Func1;

public class Util {

	public static final Func1<double[], List<Double>> TO_LIST = new Func1<double[], List<Double>>() {

		@Override
		public List<Double> call(double[] values) {
			List<Double> list = new ArrayList<Double>(values.length);
			for (double value : values)
				list.add(value);
			return list;
		}
	};

	public static Func1<List<? extends Number>, double[]> TO_DOUBLE_ARRAY = new Func1<List<? extends Number>, double[]>() {

		@Override
		public double[] call(List<? extends Number> list) {
			double[] x = new double[list.size()];
			for (int i = 0; i < x.length; i++) {
				x[i] = list.get(0).doubleValue();
			}
			return x;
		}
	};

	public static <T> Func1<List<T>, Boolean> hasSize(final int size) {
		return new Func1<List<T>, Boolean>() {

			@Override
			public Boolean call(List<T> list) {
				return list.size() == size;
			}
		};
	}

	static int valueFromTwoBytesEndian(int b1, int b2, boolean isBigEndian) {
		if (b1 < 0)
			b1 += 0x100;
		if (b2 < 0)
			b2 += 0x100;
		if (!isBigEndian)
			return (b1 << 8) + b2;
		else
			return b1 + (b2 << 8);
	}

}
