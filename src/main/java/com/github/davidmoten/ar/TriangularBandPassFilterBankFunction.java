package com.github.davidmoten.ar;

import rx.functions.Func1;

public class TriangularBandPassFilterBankFunction implements
		Func1<double[], double[]> {

	private final TriangularBandPassFilterFunction[] filters;
	private final int inputLength;

	public TriangularBandPassFilterBankFunction(double minFreq, double maxFreq,
			int numberFilters, int sampleRate, int inputLength) {
		this.inputLength = inputLength;
		int numberFftPoints = (inputLength - 1) << 1;
		this.filters = createFilters(numberFftPoints, numberFilters, minFreq,
				maxFreq, sampleRate);
	}

	public TriangularBandPassFilterBankFunction(int inputLength) {
		this(130, 6800, 40, 16000, inputLength);
	}

	private static TriangularBandPassFilterFunction[] createFilters(
			int numberFftPoints, int numberFilters, double minFreq,
			double maxFreq, int sampleRate) {
		if (numberFilters < 1)
			throw new IllegalArgumentException("Number of filters illegal: "
					+ numberFilters);
		if (numberFftPoints == 0)
			throw new IllegalArgumentException("Number of FFT points is zero");

		TriangularBandPassFilterFunction[] filters = new TriangularBandPassFilterFunction[numberFilters];

		double[] leftEdge = new double[numberFilters];
		double[] centerFreq = new double[numberFilters];
		double[] rightEdge = new double[numberFilters];

		double deltaFreq = (double) sampleRate / numberFftPoints;
		double minFreqMel = linearToMelFrequency(minFreq);
		double maxFreqMel = linearToMelFrequency(maxFreq);
		double deltaFreqMel = (maxFreqMel - minFreqMel) / (numberFilters + 1);
		leftEdge[0] = nearestFrequencyBucket(minFreq, deltaFreq);
		double nextEdgeMel = minFreqMel;
		double nextEdge;
		for (int i = 0; i < numberFilters; i++) {
			nextEdgeMel += deltaFreqMel;
			nextEdge = melToLinearFrequency(nextEdgeMel);
			centerFreq[i] = nearestFrequencyBucket(nextEdge, deltaFreq);
			if (i > 0)
				rightEdge[i - 1] = centerFreq[i];
			if (i < numberFilters - 1)
				leftEdge[i + 1] = centerFreq[i];
		}
		nextEdgeMel = nextEdgeMel + deltaFreqMel;
		nextEdge = melToLinearFrequency(nextEdgeMel);
		rightEdge[numberFilters - 1] = nearestFrequencyBucket(nextEdge,
				deltaFreq);
		for (int i = 0; i < numberFilters; i++) {
			double initialFreqBin = nearestFrequencyBucket(leftEdge[i],
					deltaFreq);
			if (initialFreqBin < leftEdge[i])
				initialFreqBin += deltaFreq;
			filters[i] = new TriangularBandPassFilterFunction(leftEdge[i],
					centerFreq[i], rightEdge[i], initialFreqBin, deltaFreq);
		}
		return filters;
	}

	@Override
	public double[] call(double[] input) {
		if (input.length != inputLength)
			throw new IllegalArgumentException("Window size is incorrect");

		double[] output = new double[filters.length];
		for (int i = 0; i < output.length; i++) {
			output[i] = filters[i].call(input);
		}
		return output;
	}

	private static double linearToMelFrequency(double f) {
		return (2595.0 * (Math.log(1.0 + f / 700.0) / Math.log(10.0)));
	}

	private static double melToLinearFrequency(double f) {
		return (700.0 * (Math.pow(10.0, (f / 2595.0)) - 1.0));
	}

	private static double nearestFrequencyBucket(double inFreq, double stepFreq) {
		if (stepFreq == 0) {
			throw new IllegalArgumentException("stepFreq is zero");
		}
		return stepFreq * Math.round(inFreq / stepFreq);
	}

}