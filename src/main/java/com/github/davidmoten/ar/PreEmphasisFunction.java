package com.github.davidmoten.ar;

import rx.functions.Func1;

/**
 * <p>
 * This function emphasizes higher frequencies in an audio signal represented by
 * a an array of double. This has the effect of sharpening speech in particular.
 * </p>
 * 
 * <p>
 * Audio samples <a href=
 * "http://mirlab.org/jang/books/audiosignalprocessing/speechFeatureMfcc.asp?title=12-2%20MFCC"
 * >here</a>.
 * </p>
 * 
 */
public class PreEmphasisFunction implements Func1<double[], double[]> {

    private final double alpha;

    public PreEmphasisFunction(double alpha) {
        this.alpha = alpha;
    }

    public PreEmphasisFunction() {
        this(0.95);
    }

    @Override
    public double[] call(double[] x) {
        if (x.length == 0)
            return new double[0];
        else {
            double[] y = new double[x.length];
            y[0] = x[0];
            for (int i = 1; i < x.length; i++) {
                y[i] = x[i] - alpha * x[i - 1];
            }
            return y;
        }
    }

}
