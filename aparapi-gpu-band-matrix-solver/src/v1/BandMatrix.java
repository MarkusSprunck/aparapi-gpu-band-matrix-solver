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
package v1;

final public class BandMatrix {

   private static final int MAX_NUMBER_OF_ITTERATIONS = 100000;

   private final int rows;

   private final int cols;

   private final double[][] values;

   public BandMatrix(int rowsNumber, int bandwidth) {
      rows = rowsNumber;
      cols = bandwidth;
      values = new double[rows][cols];
   }

   public Matrix times(Matrix b) {
      final Matrix C = new Matrix(rows, 1);
      for (int i = 0; i < rows; i++) {
         for (int k = i - cols; k - i < cols; k++) {
            if (k >= 0 && k < b.rows) {
               C.values[i][0] += getValue(i, k) * b.values[k][0];
            }
         }
      }
      return C;
   }

   public double getValue(int row, int col) {
      if (col == row) {
         return values[row][0];
      } else if (row > col) {
         return getValue(col, row);
      } else if (col - row < cols) {
         return values[row][col - row];
      }
      return 0.0;
   }

   public void setValue(int row, int col, double value) {
      if (col == row) {
         values[row][0] = value;
      } else if (col - row < cols) {
         values[row][col - row] = value;
      }
   }

   public static Matrix solveConjugateGradient(final BandMatrix A, final Matrix b) {

      Matrix x = new Matrix(b.rows, b.cols);
      Matrix r = b.minus(A.times(x));
      Matrix p = new Matrix(r);
      double rsold = r.transpose().times(r).getValue(0, 0);

      for (int i = 1; i < MAX_NUMBER_OF_ITTERATIONS; i++) {
         final Matrix Ap = A.times(p);
         final double alpha = rsold / p.transpose().times(Ap).getValue(0, 0);
         x = x.plus(p.multi(alpha));
         r = r.minus(Ap.multi(alpha));
         final double rsnew = r.transpose().times(r).getValue(0, 0);
         final double beta = rsnew / rsold;
         if (rsnew < 1e-10) {
            break;
         }
         p = r.plus(p.multi(beta));
         rsold = rsnew;
      }

      return x;
   }

}