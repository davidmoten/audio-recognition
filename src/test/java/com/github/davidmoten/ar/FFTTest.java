package com.github.davidmoten.ar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FFTTest {

	private static final double PRECISION = 0.00000001;

	@Test
	public void testFft() {
		double[] values = new double[] { -0.0348042583933070,
				0.07910192950176387, 0.7233322451735928, 0.1659819820667019 };
		Complex[] a = Complex.toComplex(values);
		Complex[] b = FFT.fft(a);
		assertEquals(0.9336118983487516, b[0].re(), PRECISION);
		assertEquals(0, b[0].im(), PRECISION);
		assertEquals(-0.7581365035668999, b[1].re(), PRECISION);
		assertEquals(0.08688005256493803, b[1].im(), PRECISION);
		assertEquals(0.44344407521182005, b[2].re(), PRECISION);
		assertEquals(0, b[2].im(), PRECISION);
		assertEquals(-0.7581365035668999, b[3].re(), PRECISION);
		assertEquals(-0.08688005256493803, b[3].im(), PRECISION);
	}

}
