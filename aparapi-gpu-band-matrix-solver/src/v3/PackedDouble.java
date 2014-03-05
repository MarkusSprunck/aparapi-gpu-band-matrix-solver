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

package v3;

/**
 * This class calculates simple floating point operations up to 16 digits
 * without using double values. Floating points are represented by a long 
 * value with packed mantissa and exponent. The motivation is that for GPU
 * computations quite often FP64 is not available and some algorithms just 
 * need a better precision than float. 
 * 
 */
public class PackedDouble {

   /**
    * Two digits are reserved for exponent [0..99] 
    */
   private final static int MIN_EXP = -49;

   /**
    * Number of used digits in mantissa
    */
   private final static byte DIGITS = 16;

   /**
    * Used to split the double value to mantissa and exponent. 
    * The mantissa scaled to use maximal 17 digits. These are 
    * MANTISSA_DIGITS plus 1 for overflows in add operation
    */
   final static long SPLIT_EXP = 100000000000000000L;

   /**
    * Used to split the mantissa to a higher and lower integer. 
    */
   private final static long SPLIT_INT = 100000000L;

   private final static int POW_OFFSET = 500;
   private static final double[] POW_10_DOUBLE = new double[POW_OFFSET << 1];
   static {
      for (int i = 0; i < POW_OFFSET << 1; i++) {
         POW_10_DOUBLE[i] = Math.pow(10, i - POW_OFFSET);
      }
   }

   private static final long[] POW_10_LONG = new long[POW_OFFSET];
   static {
      POW_10_LONG[0] = 1;
      for (int i = 1; i < 100; i++) {
         if (i < 19) {
            POW_10_LONG[i] = 10 * POW_10_LONG[i - 1];
         } else {
            POW_10_LONG[i] = POW_10_LONG[18];
         }
      }
   }

   /**
    * Approximation of log is good enough to determine the exponent.
    * Source: http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c
    * 
    * public static double fastlog(double x) {
    *    final double y = (Double.doubleToLongBits(x) >> 32);
    *    return (y - 1072632447) / 1512775;
    * }
    *
    * public static double fastabs(double x) {
    *    final long y = Double.doubleToLongBits(x);
    *    return Double.longBitsToDouble((y << 1) >>> 1);
    * }
    * 
    */
   public static long pack(double value) {
      int exponent = (int) (((Double.doubleToRawLongBits(value) << 1 >>> 33) - 1072632447L) / 3483293L);
      exponent = (exponent < MIN_EXP) ? MIN_EXP : exponent;
      final long mantissa = (long) (value * POW_10_DOUBLE[POW_OFFSET + DIGITS - exponent - 1]);
      return (exponent - MIN_EXP) * SPLIT_EXP * (mantissa >> 63 | -mantissa >>> 63) + mantissa;
   }

   public static double unpack(long value) {
      final long exponent_fraction = value / SPLIT_EXP;
      final long unpacked_mantissa = value - exponent_fraction * SPLIT_EXP;
      final long unpacked_exponent = exponent_fraction * (exponent_fraction >> 63 | -exponent_fraction >>> 63)
            + MIN_EXP;
      return unpacked_mantissa * POW_10_DOUBLE[(int) (POW_OFFSET + unpacked_exponent - DIGITS + 1)];
   }

   public static long multiplyPacked(long multiplicand, long multiplier) {

      final long md_mantissa = multiplicand - multiplicand / SPLIT_EXP * SPLIT_EXP;
      final long md_hi = md_mantissa / SPLIT_INT;
      final long md_lo = md_mantissa % SPLIT_INT;

      final long mr_mantissa = multiplier - multiplier / SPLIT_EXP * SPLIT_EXP;
      final long mr_hi = mr_mantissa / SPLIT_INT;
      final long mr_lo = mr_mantissa % SPLIT_INT;

      final long product_mantissa = md_hi * mr_hi + md_lo * mr_hi / SPLIT_INT + md_hi * mr_lo / SPLIT_INT;
      final long product_exponent = (multiplicand >> 63 | -multiplicand >>> 63) * (multiplicand / SPLIT_EXP)
            + (multiplier >> 63 | -multiplier >>> 63) * (multiplier / SPLIT_EXP) + 2 * MIN_EXP + 1;

      return (product_exponent - MIN_EXP) * SPLIT_EXP * (product_mantissa >> 63 | -product_mantissa >>> 63)
            + product_mantissa;
   }

   public static long addPacked(long augend, long addend) {

      long augend_exponent = ((augend >> 63 | -augend >>> 63) * (augend / SPLIT_EXP) + MIN_EXP);
      long addend_exponent = ((addend >> 63 | -addend >>> 63) * (addend / SPLIT_EXP) + MIN_EXP);

      if (augend_exponent < addend_exponent) {

         // Swap values
         augend = augend ^ addend;
         addend = addend ^ augend;
         augend = augend ^ addend;

         final long value = augend / SPLIT_EXP;
         augend_exponent = (value * (value >> 63 | -value >>> 63)) + MIN_EXP;
         final long value1 = addend / SPLIT_EXP;
         addend_exponent = (value1 * (value1 >> 63 | -value1 >>> 63)) + MIN_EXP;

         final long addend_mantissa = addend - addend / SPLIT_EXP * SPLIT_EXP;
         final long augend_mantissa = augend - augend / SPLIT_EXP * SPLIT_EXP;
         final long sum_mantissa = augend_mantissa + addend_mantissa
               / POW_10_LONG[(int) (augend_exponent - addend_exponent)];
         return (augend_exponent - MIN_EXP) * SPLIT_EXP * (sum_mantissa >> 63 | -sum_mantissa >>> 63) + sum_mantissa;
      } else {
         final long addend_mantissa = addend - addend / SPLIT_EXP * SPLIT_EXP;
         final long augend_mantissa = augend - augend / SPLIT_EXP * SPLIT_EXP;
         final long sum_mantissa = augend_mantissa + addend_mantissa
               / POW_10_LONG[(int) (augend_exponent - addend_exponent)];
         return (augend_exponent - MIN_EXP) * SPLIT_EXP * (sum_mantissa >> 63 | -sum_mantissa >>> 63) + sum_mantissa;
      }
   }

}