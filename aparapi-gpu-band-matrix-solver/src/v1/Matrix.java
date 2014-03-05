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

final public class Matrix {

   final int rows;

   final int cols;

   final double[][] values;

   public Matrix(final int row, final int col) {
      rows = row;
      cols = col;
      values = new double[row][col];
   }

   public Matrix(final Matrix A) {
      this(A.values);
   }

   public Matrix(final double[][] matrix) {
      rows = matrix.length;
      cols = matrix[0].length;
      values = new double[rows][cols];
      for (int i = 0; i < rows; i++) {
         System.arraycopy(matrix[i], 0, values[i], 0, cols);
      }
   }

   // return C = A + B
   public Matrix plus(final Matrix B) {
      final Matrix A = this;
      final Matrix C = new Matrix(rows, cols);
      for (int i = 0; i < rows; i++) {
         for (int j = 0; j < cols; j++) {
            C.values[i][j] = A.values[i][j] + B.values[i][j];
         }
      }
      return C;
   }

   // return C = A - B
   public Matrix minus(final Matrix B) {
      final Matrix A = this;
      final Matrix C = new Matrix(rows, cols);
      for (int i = 0; i < rows; i++) {
         for (int j = 0; j < cols; j++) {
            C.values[i][j] = A.values[i][j] - B.values[i][j];
         }
      }
      return C;
   }

   // create and return the transpose of the invoking matrix
   public Matrix transpose() {
      final Matrix A = new Matrix(cols, rows);
      for (int i = 0; i < rows; i++) {
         for (int j = 0; j < cols; j++) {
            A.values[j][i] = values[i][j];
         }
      }
      return A;
   }

   // return C = A * B
   public Matrix times(final Matrix B) {
      final Matrix A = this;
      final Matrix C = new Matrix(A.rows, B.cols);
      for (int i = 0; i < C.rows; i++) {
         for (int j = 0; j < C.cols; j++) {
            for (int k = 0; k < A.cols; k++) {
               C.values[i][j] += A.values[i][k] * B.values[k][j];
            }
         }
      }
      return C;
   }

   public Matrix multi(final double alpha) {
      final Matrix A = this;
      final Matrix C = new Matrix(rows, cols);
      for (int i = 0; i < rows; i++) {
         for (int j = 0; j < cols; j++) {
            C.values[i][j] = A.values[i][j] * alpha;
         }
      }
      return C;
   }

   public void setValue(final int row, final int col, final double value) {
      values[row][col] = value;
   }

   public double getValue(final int row, final int col) {
      return values[row][col];
   }

   public int getMaxRows() {
      return rows;
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder("v1.Matrix[");
      for (int i = 0; i < values.length; i++) {
         sb.append('[');
         for (int k = 0; k < values[i].length; k++) {
            sb.append(String.format("%.6E", values[i][k]));
            if (values[i].length > 1) {
               sb.append(' ');
            }
         }
         sb.append(']');
      }
      sb.append(']');
      return sb.toString();
   }
}