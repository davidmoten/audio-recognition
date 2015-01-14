package com.github.davidmoten.ar;

import rx.functions.Func1;

/**
 * Applies DCT. Threadsafe.
 */
public class DiscreteCosineTransformFunction implements
		Func1<double[], double[]> {

	private final static double LOG_MINIMUM = 1e-4;

	private final int cepstrumSize;
	private final double[][] melCosine;
	private final int numMelFilters;

	public DiscreteCosineTransformFunction(int cepstrumSize, int numMelFilters) {
		this.cepstrumSize = cepstrumSize;
		this.numMelFilters = numMelFilters;
		this.melCosine = createMelCosine(cepstrumSize, numMelFilters);
	}

	@Override
	public double[] call(double[] input) {
		if (input.length == 0)
			return new double[0];
		if (input.length != numMelFilters) {
			throw new IllegalArgumentException("input should be of length "
					+ numMelFilters + " but was " + input.length);
		}
		double[] melspectrum = new double[input.length];
		for (int i = 0; i < melspectrum.length; ++i) {
			melspectrum[i] = Math.log(input[i] + LOG_MINIMUM);
		}
		return applyMelCosine(melspectrum);
	}

	private static double[][] createMelCosine(int cepstrumSize,
			int numMelFilters) {
		double[][] x = new double[cepstrumSize][numMelFilters];
		double period = 2 * numMelFilters;
		for (int i = 0; i < cepstrumSize; i++) {
			double frequency = 2 * Math.PI * i / period;
			for (int j = 0; j < numMelFilters; j++) {
				x[i][j] = Math.cos(frequency * (j + 0.5));
			}
		}
		return x;
	}

	private double[] applyMelCosine(double[] melspectrum) {
		double[] cepstrum = new double[cepstrumSize];
		double period = numMelFilters;
		double beta = 0.5;
		for (int i = 0; i < cepstrum.length; i++) {
			if (numMelFilters > 0) {
				double[] melcosine_i = melCosine[i];
				int j = 0;
				cepstrum[i] += (beta * melspectrum[j] * melcosine_i[j]);
				for (j = 1; j < numMelFilters; j++) {
					cepstrum[i] += (melspectrum[j] * melcosine_i[j]);
				}
				cepstrum[i] /= period;
			}
		}
		return cepstrum;
	}
}