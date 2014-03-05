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

import tests.Parameter;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Kernel.EXECUTION_MODE;
import com.amd.aparapi.Range;

/* The symmetric banded matrix ( '-' indicates zero values):
 * 
 * |  a0  a1  a2   -    -  |
 * |  a1  a3  a4  a5    -  |
 * |  a2  a4  a6  a7   a8  |   
 * |  -   a5  a7  a9   a10 |
 * |  -   -   a8  a10  a11 |
 * 
 * is managed internal in the full band matrix format:
 * 
 * |   -   -    a0   a1    a2 |
 * |   -  a1    a3   a4    a5 |
 * |  a2  a4    a6   a7    a8 |   
 * |  a5  a7    a9   a10    - |
 * |  a8  a10  a11     -    - |
 * 
 * is stored as array:
 * 
 * [ -, -, a0, a1, a2,   -, a1, a3, a4, a5,    a2, a4, a6, a7, a8,   a5, a7, a9, a10, -,  a8, a10,  a11, -, - ] 
 * 
 */
final public class BandMatrixFull {

   private static final String NL = System.getProperty("line.separator");

   private static final int MAX_NUMBER_OF_ITTERATIONS = 100000;

   private final int rows;

   private final int cols;

   final long[] packedValues;

   public BandMatrixFull(final int rowsNumber, final int bandwidth) {
      rows = rowsNumber;
      cols = bandwidth;
      packedValues = new long[rows * cols];
   }

   public Vector times(final Vector b, Vector result) {

      // prepare input parameter
      final int rowStart = 0;
      final int rowEnd = rows;
      final int colMaximum = cols;
      final int rowMaximum = rows;
      final int bandwidthMid = colMaximum >> 1;

      // execute band matrix multiplication
      for (int row = rowStart; row < rowEnd; row++) {
         final int rowOffset = row * colMaximum;
         long sum = PackedDouble.pack(0.0);
         for (int col = 0; col < colMaximum; col++) {
            final int index = row - bandwidthMid + col;
            if (index < rowMaximum && index >= 0) {
               final long second = b.packedValues[index];
               final long temp = PackedDouble.multiplyPacked(packedValues[col + rowOffset], second);
               sum = PackedDouble.addPacked(sum, temp);
            }
         }
         result.packedValues[row] = sum;
      }
      return result;
   }

   private int getIndex(final int row, final int col) {
      if (row >= 0 && col >= 0 && row < rows && col < cols) {
         return col + row * cols;
      } else {
         return -1;
      }
   }

   /**
    * The complete banded-matrix is stored without zero values 
    * in the following format: 
    *
    * |  10   11    12    -     -     -    -  |    |  -    -    10   11   12 |
    * |  11   13    14    15    -     -    -  |    |  -    11   13   14   15 |
    * |  12   14    16    17    18    -    -  |    |  12   14   16   17   18 | 
    * |  -    15    17    19    20    21   -  |    |  15   17   19   20   21 |
    * |  -    -     18    20    22    23   24 |    |  18   20   22   23   24 | 
    * |  -    -     -     21    23    25   26 |    |  21   23   25   26   -  |
    * |  -    -     -     -     24    26   27 |    |  24   26   27   -    -  |
    */
   public void setValue(final int row, final int col, final double value) {
      final int indexUpperBand = getIndex(row, (cols >> 1) + col - row);
      if (indexUpperBand >= 0 && indexUpperBand < packedValues.length) {
         packedValues[indexUpperBand] = PackedDouble.pack(value);
      }
      if (row < col) {
         final int indexLowerBand = getIndex(col, (cols >> 1) - (col - row));
         if (indexLowerBand >= 0 && indexLowerBand < packedValues.length) {
            packedValues[indexLowerBand] = PackedDouble.pack(value);
         }
      }
   }

   public static Vector solveConjugateGradientStandard(BandMatrixFull A, Vector b, boolean loggingEnabled) {
      // Measure start time for logging
      final long start = System.currentTimeMillis();

      // create local variables
      int i = 0;
      double rsnew = 1.0;
      final int numberOfEquations = b.getMaxRows();
      final Vector Ap = new Vector(numberOfEquations);
      final Vector x = new Vector(numberOfEquations);

      // r = b - A * x
      final Vector r = new Vector(b);
      final Vector temp = new Vector(numberOfEquations);
      A.times(x, temp);
      b.minus(temp, r);

      // p = r
      final Vector p = new Vector(r);

      // rsold = r' * r
      double rsold = r.dotProduct(r);

      while (i++ < MAX_NUMBER_OF_ITTERATIONS && rsnew > 1e-10) {
         // Ap = A * p
         A.times(p, Ap);

         // alpha = rsold / ( p' * Ap )
         final double alpha = rsold / p.dotProduct(Ap);

         // x = x + alpha * p
         p.multi(alpha, temp);
         x.plus(temp, x);

         // r = r - alpha * Ap
         Ap.multi(alpha, temp);
         r.minus(temp, r);

         // rsnew = r' * r
         rsnew = r.dotProduct(r);

         // p = r + rsnew / rsold * p
         p.multi(rsnew / rsold, temp);
         r.plus(temp, p);

         // rsold = rsnew
         rsold = rsnew;
      }

      if (loggingEnabled) {
         final long end = System.currentTimeMillis();
         System.out.println("v3.BandMatrix Standard CG ready [" + String.format("%.5f", (float) (end - start) / i)
               + "ms/itteration, " + (end - start) + "ms, itterations=" + i + "]");
      }
      return x;
   }

   public static Vector solveConjugateGradientAparapi(BandMatrixFull A, Vector b, boolean loggingEnabled,
         EXECUTION_MODE mode) {

      // create local variables
      int i = 0;
      double rsnew = 1.0;
      final int numberOfEquations = b.getMaxRows();
      final Vector Ap = new Vector(numberOfEquations);
      final Vector x = new Vector(numberOfEquations);

      // r = b - A * x
      final Vector r = new Vector(b);
      final Vector temp = new Vector(numberOfEquations);
      A.times(x, temp);
      b.minus(temp, r);

      // p = r
      final Vector p = new Vector(r);

      // rsold = r' * r
      double rsold = r.dotProduct(r);

      // Create kernel and initialize the attributes 
      final v3.BandMatrixMultiplicatonAparapi kernel = new v3.BandMatrixMultiplicatonAparapi();
      kernel.setMatrixA(A.packedValues, A.getMaxRows(), A.getMaxCols());
      kernel.setVectorX(Ap.packedValues);
      kernel.setVectorB(p.packedValues);
      Range range = Range.create(A.getMaxRows());
      if (Kernel.EXECUTION_MODE.JTP.equals(mode)) {
         range = Range.create(A.getMaxRows(), Parameter.NUMBER_OF_POCESSORS << 1);
      }
      kernel.setExecutionMode(mode);
      kernel.execute(range);

      // Measure start time for logging (don't respect the setup time for Aparapi)
      final long start = System.currentTimeMillis();

      while (i++ < MAX_NUMBER_OF_ITTERATIONS && rsnew > 1e-10) {
         // Ap = A * p
         kernel.putVectorB();
         kernel.execute(range);
         kernel.getVectorX();

         // alpha = rsold / ( p' * Ap )
         final double alpha = rsold / p.dotProduct(Ap);

         // x = x + alpha * p
         p.multi(alpha, temp);
         x.plus(temp, x);

         // r = r - alpha * Ap
         Ap.multi(alpha, temp);
         r.minus(temp, r);

         // rsnew = r' * r
         rsnew = r.dotProduct(r);

         // p = r + rsnew / rsold * p
         p.multi(rsnew / rsold, temp);
         r.plus(temp, p);

         // rsold = rsnew
         rsold = rsnew;
      }

      if (loggingEnabled) {
         final long end = System.currentTimeMillis();
         System.out.println("v3.BandMatrix Aparapi  CG ready [" + String.format("%.5f", (float) (end - start) / i)
               + "ms/itteration, " + (end - start) + "ms, itterations=" + i + ", execution mode="
               + kernel.getExecutionMode().toString() + "]");
      }
      return x;
   }

   public int getMaxRows() {
      return rows;
   }

   public int getMaxCols() {
      return cols;
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder("v1.Matrix[");
      sb.append(NL);
      for (int row = 0; row < rows; row++) {
         sb.append('[');
         for (int col = 0; col < cols; col++) {
            sb.append(String.format("%.6E  ", PackedDouble.unpack(packedValues[getIndex(row, col)])));
         }
         sb.append(']').append(sb.append(NL));
      }
      sb.append(']');
      return sb.toString();
   }
}
