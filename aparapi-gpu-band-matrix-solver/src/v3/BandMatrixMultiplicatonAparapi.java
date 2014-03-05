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

import com.amd.aparapi.Kernel;

/**
 * This class performs a band matrix multiplication (A x B = X)
 *
 */
public class BandMatrixMultiplicatonAparapi extends Kernel {

   long[] vectorB = null;

   long[] matrixA = null;

   long[] vectorX = null;

   int[] bandwidthMid = new int[1];

   int[] colMaximum = new int[1];

   int[] rowMaximum = new int[1];

   long[] POW_10_LONG = new long[100];

   public BandMatrixMultiplicatonAparapi() {
      setExplicit(true);

      POW_10_LONG[0] = 1;
      for (int i = 1; i < 100; i++) {
         if (i < 19) {
            POW_10_LONG[i] = 10 * POW_10_LONG[i - 1];
         } else {
            POW_10_LONG[i] = POW_10_LONG[18];
         }
      }
      this.put(POW_10_LONG);
   }

   @Override
   public void run() {

      // prepare input parameter
      final int row = getGlobalId();

      // execute band matrix multiplication (for one row)
      long sum = -MIN_EXP * SPLIT_EXP;
      int index = 0;
      final int rowOffset = row * colMaximum[0];
      for (int col = 0; col < colMaximum[0]; col++) {
         index = row - bandwidthMid[0] + col;
         if (index < rowMaximum[0] && index >= 0) {
            sum = addPacked(sum, multiplyPacked(matrixA[col + rowOffset], vectorB[index]));
         }
      }
      vectorX[row] = sum;
   }

   public void setMatrixA(long[] values, final int rowNumber, final int bandwidth) {
      if (null == matrixA) {
         matrixA = values;
      }
      vectorB = new long[rowNumber];
      vectorX = new long[rowNumber];

      bandwidthMid[0] = bandwidth >> 1;
      colMaximum[0] = bandwidth;
      rowMaximum[0] = rowNumber;
   }

   public void setVectorB(long[] values) {
      vectorB = values;
      this.put(vectorB);
   }

   public void putVectorB() {
      this.put(vectorB);
   }

   public void setVectorX(long[] values) {
      vectorX = values;
      this.put(vectorX);
   }

   public void getVectorX() {
      this.get(vectorX);
   }

   //////////////////////////////////////////////////////////////
   // Copied from PackedDecimalUtil | start
   //////////////////////////////////////////////////////////////

   /**
    * Two digits are reserved for exponent [0..99] 
    */
   private static final int MIN_EXP = -49;

   /**
    * Used to split the double value to mantissa and exponent. 
    * The mantissa scaled to use maximal 17 digits. These are 
    * MANTISSA_DIGITS plus 1 for overflows in add operation
    */
   private static final long SPLIT_EXP = 100000000000000000L;

   /**
    * Used to split the mantissa to a higher and lower integer. 
    */
   private static final long SPLIT_INT = 100000000L;

   public long multiplyPacked(long multiplicand, long multiplier) {

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

   public long addPacked(long augend, long addend) {

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
