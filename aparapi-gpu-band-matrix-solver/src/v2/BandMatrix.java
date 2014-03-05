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
package v2;

/* The symmetric banded matrix ( '-' indicates zero values):
 * 
 * |  a0  a1  a2   0    0  |
 * |  a1  a3  a4  a5    0  |
 * |  a2  a4  a6  a7   a8  |   
 * |  0   a5  a7  a9   a10 |
 * |  0   0   a8  a10  a11 |
 * 
 * is managed in the half band matrix format (just upper part):
 * 
 * |  a0   a1  a2 |
 * |  a3   a4  a5 |
 * |  a6   a7  a8 |   
 * |  a9   a10  0 |
 * |  a11  0    0 |
 * 
 * is stored as array:
 * 
 * [ a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, 0, a11, 0, 0 ] 
 * 
 */

final public class BandMatrix {

   private static final int MAX_NUMBER_OF_ITTERATIONS = 100000;

   private final int rows;

   private final int cols;

   private final double[] values;

   public BandMatrix(final int rowsNumber, final int bandwidth) {
      rows = rowsNumber;
      cols = bandwidth;
      values = new double[rows * cols];
   }

   public Vector times(final Vector b) {
      final Vector C = new Vector(rows);
      for (int row = 0; row < rows; row++) {
         final int start = Math.max(0, row - cols + 1);
         final int end = Math.min(rows, row + cols);

         for (int col = start; col < Math.min(end, row); col++) {
            C.addValue(row, getValue(col, row) * b.getValue(col));
         }

         for (int col = Math.max(start, row); col < end; col++) {
            C.addValue(row, getValue(row, col) * b.getValue(col));
         }
      }
      return C;
   }

   public double getValue(final int row, final int col) {
      return values[getIndex(row, col - row)];
   }

   private int getIndex(final int row, final int col) {
      return col + row * cols;
   }

   /**
    * The upper band and diagonal values are stored in the following 
    * half-banded-matrix format: 
    * 
    * |  10   11    12    -     -     -    -  |    |  10   11   12 |
    * |  11   13    14    15    -     -    -  |    |  13   14   15 |
    * |  12   14    16    17    18    -    -  |    |  16   17   18 | 
    * |  -    15    17    19    20    21   -  |    |  19   20   21 |
    * |  -    -     18    20    22    23   24 |    |  22   23   24 | 
    * |  -    -     -     21    23    25   26 |    |  25   26   -  |
    * |  -    -     -     -     24    26   27 |    |  27   -    -  |
    */
   public void setValue(final int row, final int col, final double value) {
      values[getIndex(row, col - row)] = value;
   }

   public static Vector solveConjugateGradient(final BandMatrix A, final Vector b, boolean loggingEnabled) {
      final long start = System.currentTimeMillis();

      Vector x = new Vector(b.getMaxRows());
      Vector r = b.minus(A.times(x));
      Vector p = new Vector(r);
      double rsold = r.dotProduct(r);
      int i = 1;
      for (i = 1; i < MAX_NUMBER_OF_ITTERATIONS; i++) {
         final Vector Ap = A.times(p);
         final double alpha = rsold / p.dotProduct(Ap);
         x = x.plus(p.multi(alpha));
         r = r.minus(Ap.multi(alpha));
         final double rsnew = r.dotProduct(r);
         final double beta = rsnew / rsold;
         if (rsnew < 1e-10) {
            break;
         }
         p = r.plus(p.multi(beta));
         rsold = rsnew;
      }

      if (loggingEnabled) {
         final long end = System.currentTimeMillis();
         System.out.println("v2.BandMatrix Standard CG ready [" + String.format("%.5f", (float) (end - start) / i)
               + "ms/itteration, " + +(end - start) + "ms, itterations=" + i + "]");
      }

      return x;
   }
 
}
