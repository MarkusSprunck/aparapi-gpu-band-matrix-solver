/*
 * Copyright (C) 2014, Markus Sprunck <sprunck.markus@gmail.com>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package v4;

import org.junit.Assert;
import org.junit.Test;

public class PackedDoubleTest {

   private static final String NL = System.getProperty("line.separator");

   private static final int NUMBER_OF_RANDOM_TESTS = 100000;

   @Test
   public void pack_ValidNumbersUpper_Correct() {

      // positive exponent
      long value = PackedDouble.pack(1E+3);
      Assert.assertEquals(1E+3, PackedDouble.unpack(value), 1E-8);

      value = PackedDouble.pack(1E+38);
      Assert.assertEquals(1.0000000000000002E38, PackedDouble.unpack(value), 0.0);

      value = PackedDouble.pack(1E+39);
      Assert.assertEquals(1.0000000000000001E39, PackedDouble.unpack(value),  0.0);

      value = PackedDouble.pack(1E+41);
      Assert.assertEquals(1.0E+41, PackedDouble.unpack(value), 1E-8);

      // negative exponent
      value = PackedDouble.pack(1E-5);
      Assert.assertEquals(1E-5, PackedDouble.unpack(value), 1E-8);

      value = PackedDouble.pack(1E-38);
      Assert.assertEquals(1.0000000000000001E-38, PackedDouble.unpack(value), 0.0);

      value = PackedDouble.pack(1E-39);
      Assert.assertEquals(1.0000000000000001E-39, PackedDouble.unpack(value), 0.0);

      value = PackedDouble.pack(1E-46);
      Assert.assertEquals(1E-46, PackedDouble.unpack(value), 0.0);

   }

   @Test
   public void add_SomeSpecialCases_Correct() {
      // ARRANGE
      boolean failed = false;
      // ACT
      failed |= isRunAddTestSuccessful(-3.836464871262581000e+13, +2.965381412521720000e-05);
      failed |= isRunAddTestSuccessful(0.0, 0.1);
      failed |= isRunAddTestSuccessful(0.0, 0.0);
      failed |= isRunAddTestSuccessful(0.1, 0.0);
      failed |= isRunAddTestSuccessful(9.999999999999999E-20, -9.999999999999999E-20);
      failed |= isRunAddTestSuccessful(9.999999999999999E-20, +9.999999999999999E+20);
      failed |= isRunAddTestSuccessful(9.999999999999999E+20, 9.999999999999999E-20);
      failed |= isRunAddTestSuccessful(9.999999999999999E+20, 9.999999999999999E+20);

      // CHECK
      Assert.assertFalse(failed);
   }

   @Test
   public void multiply_SomeSpecialCases_Correct() {
      // ARRANGE
      boolean failed = false;
      // ACT
      failed |= isRunMultiplyTestSuccessful(0.0, 0.1);
      failed |= isRunMultiplyTestSuccessful(0.0, 0.0);
      failed |= isRunMultiplyTestSuccessful(0.1, 0.0);
      failed |= isRunMultiplyTestSuccessful(9.999999999999999E-20, -9.999999999999999E-20);
      failed |= isRunMultiplyTestSuccessful(9.999999999999999E+20, +9.999999999999999E+20);

      // CHECK
      Assert.assertFalse(failed);
   }

   @Test
   public void add_ManyRandomNumbers_Correct() {
      boolean failed = false;
      for (int i = 0; i < NUMBER_OF_RANDOM_TESTS; i++) {
         // ARRANGE
         final double a = createRandomNumber();
         final double b = createRandomNumber();

         // ACT
         failed |= isRunAddTestSuccessful(a, b);
      }

      // CHECK
      Assert.assertFalse(failed);
   }

   @Test
   public void multiply_ManyRandomNumbers_Correct() {
      boolean failed = false;

      for (int i = 0; i < NUMBER_OF_RANDOM_TESTS; i++) {
         // ARRANGE
         final double a = createRandomNumber();
         final double b = createRandomNumber();

         // ACT
         failed |= isRunMultiplyTestSuccessful(a, b);
      }

      // CHECK
      Assert.assertFalse(failed);
   }

   //////////////////////////////////////////////////////////////////////////////////

   private boolean isRunAddTestSuccessful(double a, double b) {
      boolean failed = false;

      // ACT
      final long first = PackedDouble.pack(a);
      final long second = PackedDouble.pack(b);
      final long result = PackedDouble.addPacked(first, second);
      final double actual = PackedDouble.unpack(result);

      // CHECK
      final double expected = a + b;
      final int length_of_mantissa = mantissaLengthFromPackedValue(result);
      if (Math.abs(expected) > Double.MIN_VALUE) {

         final double relativeError = actual / expected - 1.0d;
         if (Math.abs(relativeError) > Math.pow(10, 2 - length_of_mantissa)) {
            final StringBuilder message = new StringBuilder("ERROR add").append(NL);
            message.append("a            = ").append(String.format("%+.18e", a)).append(NL);
            message.append("b            = ").append(String.format("%+.18e", b)).append(NL);
            message.append("a+b expected = ").append(String.format("%+.18e", expected)).append(NL);
            message.append("a+b actual   = ").append(String.format("%+.18e", actual)).append(NL);
            message.append("len mantissa = ").append(String.format("%d", length_of_mantissa)).append(NL);
            message.append("relativeError= ").append(String.format("%+.18e", relativeError)).append(NL);
            System.out.println(message.toString());

            failed = true;
         }
      } else {
         if (Math.abs(actual) > Double.MIN_VALUE) {

            final StringBuilder message = new StringBuilder("ERROR add").append(NL);
            message.append("a            = ").append(String.format("%+.18e", a)).append(NL);
            message.append("b            = ").append(String.format("%+.18e", b)).append(NL);
            message.append("a+b expected = ").append(String.format("%+.18e", expected)).append(NL);
            message.append("a+b actual   = ").append(String.format("%+.18e", actual)).append(NL);
            message.append("len mantissa = ").append(String.format("%d", length_of_mantissa)).append(NL);
            message.append("error        = ").append(String.format("%+.18e", expected)).append(NL);
            System.out.println(message.toString());

            failed = true;
         }
      }
      return failed;
   }

   private boolean isRunMultiplyTestSuccessful(double a, double b) {
      boolean failed = false;

      // ACT 
      final long first = PackedDouble.pack(a);
      final long second = PackedDouble.pack(b);
      final long result = PackedDouble.multiplyPacked(first, second);
      final double actual = PackedDouble.unpack(result);

      // CHECK
      final double expected = a * b;
      final int length_of_mantissa = mantissaLengthFromPackedValue(result);
      if (Math.abs(expected) > Double.MIN_VALUE) {

         final double relativeError = actual / expected - 1;
         if (Math.abs(relativeError) > Math.pow(10, 2 - length_of_mantissa)) {
            final StringBuilder message = new StringBuilder("ERROR multiply").append(NL);
            message.append("a            = ").append(String.format("%+.18e", a)).append(NL);
            message.append("b            = ").append(String.format("%+.18e", b)).append(NL);
            message.append("a*b expected = ").append(String.format("%+.18e", expected)).append(NL);
            message.append("a*b actual   = ").append(String.format("%+.18e", actual)).append(NL);
            message.append("len mantissa = ").append(String.format("%d", length_of_mantissa)).append(NL);
            message.append("relativeError= ").append(String.format("%+.18e", relativeError)).append(NL);
            System.out.println(message.toString());

            failed = true;
         }
      } else {
         if (Math.abs(actual) > Double.MIN_VALUE) {

            final StringBuilder message = new StringBuilder("ERROR multiply").append(NL);
            message.append("a            = ").append(String.format("%+.18e", a)).append(NL);
            message.append("b            = ").append(String.format("%+.18e", b)).append(NL);
            message.append("a*b expected = ").append(String.format("%+.18e", expected)).append(NL);
            message.append("a*b actual   = ").append(String.format("%+.18e", actual)).append(NL);
            message.append("len mantissa = ").append(String.format("%d", length_of_mantissa)).append(NL);
            message.append("error        = ").append(String.format("%+.18e", expected)).append(NL);
            System.out.println(message.toString());

            failed = true;
         }
      }
      return failed;
   }

   protected static int mantissaLengthFromPackedValue(long value) {
      final long mantissa = value - value / PackedDouble.SPLIT_EXP * PackedDouble.SPLIT_EXP;
      return (int) Math.log10(Math.abs(mantissa));
   }

   private double createRandomNumber() {
      return 2.0 * (Math.random() - 0.5) * Math.pow(10, 5 * (Math.random() - 0.5));
   }

   public static void main(String[] args) {

      final PackedDoubleTest packedDoubleTest = new PackedDoubleTest();
      do {
         packedDoubleTest.add_ManyRandomNumbers_Correct();
         packedDoubleTest.multiply_ManyRandomNumbers_Correct();
      } while (true);

   }
}
