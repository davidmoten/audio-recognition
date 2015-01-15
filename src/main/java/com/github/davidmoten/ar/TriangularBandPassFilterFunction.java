package com.github.davidmoten.ar;

import rx.functions.Func1;

import com.github.davidmoten.util.Preconditions;

public class TriangularBandPassFilterFunction implements Func1<double[], Double> {

    private final double[] weight;
    private final int initialFrequencyIndex;

    public TriangularBandPassFilterFunction(double lowestFrequency, double centreFrequency,
            double highestFrequency, double startFrequency, double deltaFrequency) {
        Preconditions.checkArgument(deltaFrequency > 0);
        Preconditions.checkArgument(Math.round(highestFrequency - lowestFrequency) > 0);
        Preconditions.checkArgument(Math.round(centreFrequency - lowestFrequency) > 0);
        Preconditions.checkArgument(Math.round(highestFrequency - centreFrequency) > 0);
        int numBuckets = (int) Math
                .round((highestFrequency - lowestFrequency) / deltaFrequency + 1);
        Preconditions.checkArgument(numBuckets > 0);

        double[] weight = new double[numBuckets];
        double filterHeight = 2.0 / (highestFrequency - lowestFrequency);
        double leftGradient = filterHeight / (centreFrequency - lowestFrequency);
        double rightGradient = filterHeight / (centreFrequency - highestFrequency);

        double f;
        int bucketIndex = 0;
        // compute the weight for each frequency bucket
        for (f = startFrequency; f <= highestFrequency; f += deltaFrequency) {
            if (f < centreFrequency)
                weight[bucketIndex] = leftGradient * (f - lowestFrequency);
            else
                weight[bucketIndex] = filterHeight + rightGradient * (f - centreFrequency);
            bucketIndex++;
        }
        this.weight = weight;
        this.initialFrequencyIndex = (int) Math.round(startFrequency / deltaFrequency);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rx.functions.Func1#call(java.lang.Object)
     * 
     * Returns the weighted average of power for the frequencies within the
     * confines of this triangular filter pass band.
     * 
     * @parameter signal the power spectrum to be filtered
     * 
     * @return the weighted average of power
     */
    @Override
    public Double call(double[] signal) {
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