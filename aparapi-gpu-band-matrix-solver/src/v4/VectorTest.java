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

public class VectorTest {

   @Test
   public void v4_times_TwoRows_CorrectResult() {

      // ARRANGE
      final Vector A = new Vector(2);
      A.setValue(0, 4);
      A.setValue(1, 2);
      final Vector B = new Vector(2);
      B.setValue(0, 3);
      B.setValue(1, 5);

      // ACT
      final double result = A.dotProduct(B);

      // ASSERT
      Assert.assertEquals(22.0, result, 0.0);
   }

   @Test
   public void v4_plus_TwoRows_CorrectResult() {

      // ARRANGE
      final Vector A = new Vector(2);
      A.setValue(0, 7);
      A.setValue(1, 1);
      final Vector B = new Vector(2);
      B.setValue(0, 0.14);
      B.setValue(1, 0.32);

      // ACT
      final Vector result = new Vector(2);
      A.plus(B, result);

      // ASSERT
      Assert.assertEquals(7.14, result.getValue(0), 1.0E-12);
      Assert.assertEquals(1.32, result.getValue(1), 1.0E-12);
   }

   @Test
   public void v4_minus_TwoRows_CorrectResult() {

      // ARRANGE
      final Vector A = new Vector(2);
      A.setValue(0, 7);
      A.setValue(1, 1);
      final Vector B = new Vector(2);
      B.setValue(0, 0.14);
      B.setValue(1, 0.32);

      // ACT
      final Vector result = new Vector(2);
      A.minus(B, result);

      // ASSERT
      Assert.assertEquals(6.86, result.getValue(0), 1.0E-15);
      Assert.assertEquals(0.68, result.getValue(1), 1.0E-15);
   }

   @Test
   public void v4_multi_TwoRows_CorrectResult() {

      // ARRANGE
      final Vector A = new Vector(2);
      A.setValue(0, 7);
      A.setValue(1, 1);

      // ACT
      final Vector result = new Vector(2);
      A.multi(0.11, result);

      // ASSERT
      Assert.assertEquals(0.77, result.getValue(0), 1.0E-15);
      Assert.assertEquals(0.11, result.getValue(1), 1.0E-15);
   }

   @Test
   public void v4_getRow_FourRows_CorrectResult() {

      // ARRANGE
      final Vector A = new Vector(4);

      // ACT
      final int result = A.getMaxRows();

      // ASSERT
      Assert.assertEquals(4, result, 0);
   }

   @Test
   public void v4_setValue_AllValuesOfMatrix_CorrectResult() {

      // ARRANGE
      final Vector A = new Vector(10);

      // ACT
      for (int row = 0; row < A.getMaxRows(); row++) {
         A.setValue(row, (2 * row + 1) * (3 * 1 + 1));
      }

      // ASSERT
      for (int row = 0; row < A.getMaxRows(); row++) {
         final double result = A.getValue(row);
         Assert.assertEquals((2 * row + 1) * (3 * 1 + 1), result, 0);
      }
   }

   @Test
   public void v4_copyConstructor_AllValuesOfMatrix_CorrectResult() {

      // ARRANGE
      final Vector B = new Vector(10);
      for (int row = 0; row < B.getMaxRows(); row++) {
         B.setValue(row, (2 * row + 1) * (3 * 1 + 1));
      }

      // ACT
      final Vector A = new Vector(B);

      // ASSERT
      for (int row = 0; row < A.getMaxRows(); row++) {
         final double result = A.getValue(row);
         Assert.assertEquals((2 * row + 1) * (3 * 1 + 1), result, 0);
      }
   }

}