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

import java.util.concurrent.ForkJoinPool;

import tests.Parameter;

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
public final class BandMatrixFull {

   // create thread pool
   public static final ForkJoinPool POOL = new ForkJoinPool(Parameter.NUMBER_OF_POCESSORS << 1);

   private static final int MAX_NUMBER_OF_ITTERATIONS = 100000;

   private final int rows;

   private final int cols;

   protected final double[] values;

   public BandMatrixFull(final int rowsNumber, final int bandwidth) {
      rows = rowsNumber;
      cols = bandwidth;
      values = new double[rows * cols];
   }

   public void times(final Vector b, final Vector result) {

      // prepare input parameter
      final int rowStart = 0;
      final int rowEnd = rows;
      final int colMaximum = cols;
      final int rowMaximum = rows;
      final int bandwidthMid = colMaximum >> 1;

      // execute band matrix multiplication
      int index = 0;
      int rowOffset = 0;
      double sum = 0.0;
      for (int row = rowStart; row < rowEnd; row++) {
         rowOffset = row * colMaximum;
         sum = 0.0;
         for (int col = 0; col < colMaximum; col++) {
            index = row - bandwidthMid + col;
            if (index < rowMaximum && index >= 0) {
               sum += values[col + rowOffset] * b.values[index];
            }
         }
         result.values[row] = sum;
      }
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
      if (indexUpperBand >= 0 && indexUpperBand < values.length) {
         values[indexUpperBand] = value;
      }
      if (row < col) {
         final int indexLowerBand = getIndex(col, (cols >> 1) - (col - row));
         if (indexLowerBand >= 0 && indexLowerBand < values.length) {
            values[indexLowerBand] = value;
         }
      }
   }

   public static Vector solveConjugateGradient(final BandMatrixFull A, final Vector b, boolean loggingEnabled) {
      // Measure start time for logging
      final long start = System.currentTimeMillis();

      // create local variables
      int i = -1;
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
         System.out.print("\t" + (end - start));
      }
      return x;
   }

   public static Vector solveConjugateGradientForkAndJoin(BandMatrixFull A, Vector b) {

      // create local variables
      int i = -1;
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
         final v3.BandMatrixMultiplicatonTask task = new v3.BandMatrixMultiplicatonTask(0, A.getMaxRows(), A, p, Ap);
         POOL.invoke(task);

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

      return x;
   }

   public int getMaxRows() {
      return rows;
   }

   public int getMaxCols() {
      return cols;
   }

}
