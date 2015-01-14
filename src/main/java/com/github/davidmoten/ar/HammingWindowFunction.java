package com.github.davidmoten.ar;

import rx.functions.Func1;

public class HammingWindowFunction implements Func1<double[], double[]> {

	private double alpha;

	public HammingWindowFunction(double alpha) {
		this.alpha = alpha;
	}

	@Override
	public double[] call(double[] x) {
		if (x.length == 0)
			return new double[0];
		else {
			double[] y = new double[x.length];
			for (int i = 0; i < x.length; i++) {
				y[i] = x[i]
						* ((1 - alpha) - alpha
								* Math.cos(2 * Math.PI * i / (x.length - 1)));
			}
			return y;
		}
	}

}
