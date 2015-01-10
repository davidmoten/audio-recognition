package com.github.davidmoten.ar;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class Benchmarks {

	private final Complex[] input = createInput();

	@Benchmark
	public void fft() {
		// FFT of original data
		FFT.fft(input);
	}

	private static Complex[] createInput() {
		int n = 2048;
		Complex[] x = new Complex[n];

		// original data
		for (int i = 0; i < n; i++) {
			x[i] = new Complex(i, 0);
			x[i] = new Complex(-2 * Math.random() + 1, 0);
		}
		return x;
	}

}