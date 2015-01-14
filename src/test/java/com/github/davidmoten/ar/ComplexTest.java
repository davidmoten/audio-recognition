package com.github.davidmoten.ar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ComplexTest {

    private static final double PRECISION = 0.00001;
    private static Complex a = new Complex(5.0, 6.0);
    private static Complex b = new Complex(-3.0, 4.0);

    // System.out.println("a            = " + a);
    // System.out.println("b            = " + b);
    // System.out.println("Re(a)        = " + a.re());
    // System.out.println("Im(a)        = " + a.im());
    // System.out.println("b + a        = " + b.plus(a));
    // System.out.println("a - b        = " + a.minus(b));
    // System.out.println("a * b        = " + a.times(b));
    // System.out.println("b * a        = " + b.times(a));
    // System.out.println("a / b        = " + a.divides(b));
    // System.out.println("(a / b) * b  = " + a.divides(b).times(b));
    // System.out.println("conj(a)      = " + a.conjugate());
    // System.out.println("|a|          = " + a.abs());
    // System.out.println("tan(a)       = " + a.tan());

    @Test
    public void testConstructor() {
        assertEquals(5.0, a.re(), PRECISION);
        assertEquals(6.0, a.im(), PRECISION);
    }

    @Test
    public void testAddition() {
        Complex c = a.plus(b);
        assertEquals(2.0, c.re(), PRECISION);
        assertEquals(10.0, c.im(), PRECISION);
    }

    @Test
    public void testSubtraction() {
        Complex c = a.minus(b);
        assertEquals(8.0, c.re(), PRECISION);
        assertEquals(2.0, c.im(), PRECISION);
    }

    @Test
    public void testMultiplication() {
        Complex c = a.times(b);
        assertEquals(-39.0, c.re(), PRECISION);
        assertEquals(2.0, c.im(), PRECISION);
    }

    @Test
    public void testDivision() {
        Complex c = a.times(b).divides(b);
        assertEquals(5.0, c.re(), PRECISION);
        assertEquals(6.0, c.im(), PRECISION);
    }

    @Test
    public void testConjugate() {
        Complex c = a.conjugate();
        assertEquals(5.0, c.re(), PRECISION);
        assertEquals(-6.0, c.im(), PRECISION);
    }

    @Test
    public void testAbsoluteValue() {
        assertEquals(Math.sqrt(25 + 36), a.abs(), PRECISION);
    }

    @Test
    public void testTanValue() {
        Complex c = b.tan();
        assertEquals(0.00018734, c.re(), PRECISION);
        assertEquals(0.99935598738, c.im(), PRECISION);
    }

}
