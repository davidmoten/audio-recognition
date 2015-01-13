package com.github.davidmoten.ar;

public class TriangularBandPassFilter {

	private final double[] weight;
	private final int initialFrequencyIndex;

	public TriangularBandPassFilter(double lowestFrequency, double centreFrequency,
			double highestFrequency, double startFrequency,
			double deltaFrequency) {

		Preconditions.checkArgument(deltaFrequency > 0);
		Preconditions.checkArgument(Math.round(highestFrequency
				- lowestFrequency) > 0);
		Preconditions.checkArgument(Math.round(centreFrequency
				- lowestFrequency) > 0);
		Preconditions.checkArgument(Math.round(highestFrequency
				- centreFrequency) > 0);
		int numberElementsWeightField = (int) Math
				.round((highestFrequency - lowestFrequency) / deltaFrequency
						+ 1);
		Preconditions.checkArgument(numberElementsWeightField > 0);

		double[] weight = new double[numberElementsWeightField];
		double filterHeight = 2.0f / (highestFrequency - lowestFrequency);
		double leftGradient = filterHeight
				/ (centreFrequency - lowestFrequency);
		double rightGradient = filterHeight
				/ (centreFrequency - highestFrequency);

		double currentFrequency;
		int indexFilterWeight;
		// compute the weight for each frequency bin
		for (currentFrequency = startFrequency, indexFilterWeight = 0; currentFrequency <= highestFrequency; currentFrequency += deltaFrequency, indexFilterWeight++) {
			if (currentFrequency < centreFrequency) {
				weight[indexFilterWeight] = leftGradient
						* (currentFrequency - lowestFrequency);
			} else {
				weight[indexFilterWeight] = filterHeight + rightGradient
						* (currentFrequency - centreFrequency);
			}
		}
		this.weight = weight;
		this.initialFrequencyIndex = (int) Math.round(startFrequency
				/ deltaFrequency);
	}

	/**
	 * Returns the weighted average of power for the frequencies within the
	 * confines of this triangular filter pass band.
	 *
	 * @parameter signal the power spectrum to be filtered
	 * @return the weighted average of power
	 */
	public double apply(double[] signal) {
		double result = 0.0;

		for (int i = 0; i < this.weight.length; i++) {
			int indexSignal = this.initialFrequencyIndex + i;
			if (indexSignal < signal.length) {
				result += signal[indexSignal] * this.weight[i];
			}
		}
		return result;
	}
}