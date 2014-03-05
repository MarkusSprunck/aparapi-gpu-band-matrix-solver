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

import org.junit.Assert;
import org.junit.Test;

public class MatrixTest {

   @Test
   public void v1_times_TwoRowsAndOneCol_CorrectResult() {

      // ARRANGE
      final Matrix A = new Matrix(2, 1);
      A.setValue(0, 0, 4);
      A.setValue(1, 0, 2);
      final Matrix B = new Matrix(2, 1);
      B.setValue(0, 0, 3);
      B.setValue(1, 0, 5);

      // ACT
      final Matrix result = A.transpose().times(B);

      // ASSERT
      Assert.assertEquals(22.0, result.getValue(0, 0), 1.0E-3);
   }

   @Test
   public void v1_times_TwoRowsAndTwoCol_CorrectResult() {

      // ARRANGE
      final Matrix A = new Matrix(2, 2);
      A.setValue(0, 0, 4);
      A.setValue(1, 0, 2);
      A.setValue(0, 1, 3);
      A.setValue(1, 1, 5);
      final Matrix B = new Matrix(2, 2);
      B.setValue(0, 0, 3);
      B.setValue(1, 0, 5);
      B.setValue(0, 1, 9);
      B.setValue(1, 1, 8);

      // ACT
      final Matrix result = A.times(B);

      // ASSERT
      Assert.assertEquals(27.0, result.getValue(0, 0), 1.0E-3);
      Assert.assertEquals(60.0, result.getValue(0, 1), 1.0E-3);
      Assert.assertEquals(31.0, result.getValue(1, 0), 1.0E-3);
      Assert.assertEquals(58.0, result.getValue(1, 1), 1.0E-3);
   }

   @Test
   public void v1_plus_TwoRowsAndTwoCols_CorrectResult() {

      // ARRANGE
      final Matrix A = new Matrix(2, 1);
      A.setValue(0, 0, 7);
      A.setValue(1, 0, 1);
      final Matrix B = new Matrix(2, 1);
      B.setValue(0, 0, 0.14f);
      B.setValue(1, 0, 0.32f);

      // ACT
      final Matrix result = A.plus(B);

      // ASSERT
      Assert.assertEquals(7.14, result.getValue(0, 0), 1.0E-3);
      Assert.assertEquals(1.32, result.getValue(1, 0), 1.0E-3);
   }

   @Test
   public void v1_minus_TwoRowsAndTwoCols_CorrectResult() {

      // ARRANGE
      final Matrix A = new Matrix(2, 1);
      A.setValue(0, 0, 7);
      A.setValue(1, 0, 1);
      final Matrix B = new Matrix(2, 1);
      B.setValue(0, 0, 0.14f);
      B.setValue(1, 0, 0.32f);

      // ACT
      final Matrix result = A.minus(B);

      // ASSERT
      Assert.assertEquals(6.86, result.getValue(0, 0), 1.0E-3);
      Assert.assertEquals(0.68, result.getValue(1, 0), 1.0E-3);
   }

   @Test
   public void v1_multi_TwoRowsAndTwoCols_CorrectResult() {

      // ARRANGE
      final Matrix A = new Matrix(2, 1);
      A.setValue(0, 0, 7);
      A.setValue(1, 0, 1);

      // ACT
      final Matrix result = A.multi(0.11f);

      // ASSERT
      Assert.assertEquals(0.77, result.getValue(0, 0), 1.0E-3);
      Assert.assertEquals(0.11, result.getValue(1, 0), 1.0E-3);
   }

   @Test
   public void v1_getRow_FourRowsAndTwoCols_CorrectResult() {

      // ARRANGE
      final Matrix A = new Matrix(4, 1);

      // ACT
      final int result = A.getMaxRows();

      // ASSERT
      Assert.assertEquals(4, result, 0);
   }

   @Test
   public void v1_setValue_AllValuesOfMatrix_CorrectResult() {

      // ARRANGE
      final Matrix A = new Matrix(4, 1);

      // ACT
      for (int row = 0; row < A.getMaxRows(); row++) {
         A.setValue(row, 0, (2 * row + 1) * (3 * 1 + 1));
      }

      // ASSERT
      for (int row = 0; row < A.getMaxRows(); row++) {
         final double result = A.getValue(row, 0);
         Assert.assertEquals((2 * row + 1) * (3 * 1 + 1), result, 0);
      }
   }

}