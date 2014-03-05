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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import tests.Parameter;

import com.amd.aparapi.Kernel;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BandMatrixFullTest {

   private static final BandMatrixFull A = new BandMatrixFull(Parameter.ROW_NUMBER, Parameter.BAND_WIDTH);

   private static final Vector B = new Vector(Parameter.ROW_NUMBER);

   @BeforeClass
   public static void v3_setup() {
      v2.BandMatrixTest.v2_setup();
      for (int row = 0; row < Parameter.ROW_NUMBER; row++) {
         for (int col = row; col < row + Parameter.BAND_WIDTH && col < Parameter.ROW_NUMBER; col++) {
            A.setValue(row, col, v2.BandMatrixTest.A.getValue(row, col));
         }
         B.setValue(row, v2.BandMatrixTest.B.getValue(row));
      }

   }

   @Test
   public void v3_times_5x5_SymetricBandMatrix_CorrectResult() {

      // ARRANGE
      final int rowsNumber = 5;
      final int bandwidth = 5;

      /*
       * |  10   11    12    -    - |
       * |  11   13    14   15    - |
       * |  12   14    16   17   18 |   
       * |  -    15    17   19   20 |
       * |  -    -     18   20   21 |
       */
      final BandMatrixFull A = new BandMatrixFull(rowsNumber, bandwidth);
      A.setValue(0, 0, 10.0f);
      A.setValue(0, 1, 11.0f);
      A.setValue(0, 2, 12.0f);

      A.setValue(1, 1, 13.0f);
      A.setValue(1, 2, 14.0f);
      A.setValue(1, 3, 15.0f);

      A.setValue(2, 2, 16.0f);
      A.setValue(2, 3, 17.0f);
      A.setValue(2, 4, 18.0f);

      A.setValue(3, 3, 19.0f);
      A.setValue(3, 4, 20.0f);

      A.setValue(4, 4, 21.0f);

      final Vector b = new Vector(rowsNumber);
      b.setValue(0, 1);
      b.setValue(1, 2);
      b.setValue(2, 3);
      b.setValue(3, 4);
      b.setValue(4, 5);

      // ACT
      final Vector x = new Vector(rowsNumber);
      A.times(b, x);

      // CHECK
      Assert.assertEquals(68.0f, x.getValue(0), 0.0000000000001f);
      Assert.assertEquals(139.0f, x.getValue(1), 0.0000000000001f);
      Assert.assertEquals(246.0f, x.getValue(2), 0.0000000000001f);
      Assert.assertEquals(257.0f, x.getValue(3), 0.0000000000001f);
      Assert.assertEquals(239.0f, x.getValue(4), 0.0000000000001f);
   }

   @Test
   public void v3_times_5x5_SymetricBandMatrixNullVector_CorrectResult() {

      // ARRANGE
      final int rowsNumber = 5;
      final int bandwidth = 5;

      /*
       * |  10   11    12    -    - |
       * |  11   13    14   15    - |
       * |  12   14    16   17   18 |   
       * |  -    15    17   19   20 |
       * |  -    -     18   20   21 |
       */
      final BandMatrixFull A = new BandMatrixFull(rowsNumber, bandwidth);
      A.setValue(0, 0, 10.0f);
      A.setValue(0, 1, 11.0f);
      A.setValue(0, 2, 12.0f);

      A.setValue(1, 1, 13.0f);
      A.setValue(1, 2, 14.0f);
      A.setValue(1, 3, 15.0f);

      A.setValue(2, 2, 16.0f);
      A.setValue(2, 3, 17.0f);
      A.setValue(2, 4, 18.0f);

      A.setValue(3, 3, 19.0f);
      A.setValue(3, 4, 20.0f);

      A.setValue(4, 4, 21.0f);

      final Vector b = new Vector(rowsNumber);
      b.setValue(0, 0);
      b.setValue(1, 0);
      b.setValue(2, 0);
      b.setValue(3, 0);
      b.setValue(4, 0);

      // ACT
      final Vector x = new Vector(rowsNumber);
      A.times(b, x);

      // CHECK
      Assert.assertEquals(0.0d, x.getValue(0), 0.0000000000001f);
      Assert.assertEquals(0.0d, x.getValue(1), 0.0000000000001f);
      Assert.assertEquals(0.0d, x.getValue(2), 0.0000000000001f);
      Assert.assertEquals(0.0d, x.getValue(3), 0.0000000000001f);
      Assert.assertEquals(0.0d, x.getValue(4), 0.0000000000001f);
   }

   @Test
   public void v3_times_7x5_SymetricBandMatrix_CorrectResult() {

      // ARRANGE
      final int rowsNumber = 7;
      final int bandwidth = 5;

      /*
       * |  10   11    12    -     -     -    -  |    
       * |  11   13    14    15    -     -    -  |    
       * |  12   14    16    17    18    -    -  |   
       * |  -    15    17    19    20    21   -  |  
       * |  -    -     18    20    22    23   24 |    
       * |  -    -     -     21    23    25   26 |   
       * |  -    -     -     -     24    26   27 |   
       */

      final BandMatrixFull A = new BandMatrixFull(rowsNumber, bandwidth);
      A.setValue(0, 0, 10.0f);
      A.setValue(0, 1, 11.0f);
      A.setValue(0, 2, 12.0f);

      A.setValue(1, 1, 13.0f);
      A.setValue(1, 2, 14.0f);
      A.setValue(1, 3, 15.0f);

      A.setValue(2, 2, 16.0f);
      A.setValue(2, 3, 17.0f);
      A.setValue(2, 4, 18.0f);

      A.setValue(3, 3, 19.0f);
      A.setValue(3, 4, 20.0f);
      A.setValue(3, 5, 21.0f);

      A.setValue(4, 4, 22.0f);
      A.setValue(4, 5, 23.0f);
      A.setValue(4, 6, 24.0f);

      A.setValue(5, 5, 25.0f);
      A.setValue(5, 6, 26.0f);

      A.setValue(6, 6, 27.0f);

      final Vector b = new Vector(rowsNumber);
      b.setValue(0, 1);
      b.setValue(1, 2);
      b.setValue(2, 3);
      b.setValue(3, 4);
      b.setValue(4, 5);
      b.setValue(5, 6);
      b.setValue(6, 7);

      // ACT
      final Vector x = new Vector(rowsNumber);
      A.times(b, x);

      // CHECK
      Assert.assertEquals(68.0f, x.getValue(0), 0.0000000000001f);
      Assert.assertEquals(139.0f, x.getValue(1), 0.0000000000001f);
      Assert.assertEquals(246.0f, x.getValue(2), 0.0000000000001f);
      Assert.assertEquals(383.0f, x.getValue(3), 0.0000000000001f);
      Assert.assertEquals(550.0f, x.getValue(4), 0.0000000000001f);
      Assert.assertEquals(531.0f, x.getValue(5), 0.0000000000001f);
      Assert.assertEquals(465.0f, x.getValue(6), 0.0000000000001f);
   }

   @Test
   public void v3_solveConjugateGradient_1_Standard_LargeRandomBandMatrix_Solved() {

      // ARRANGE

      // ACT
      //
      // solve linear equation
      final Vector x = BandMatrixFull.solveConjugateGradientStandard(A, B, true);
      //
      // all elements of result should be zero 
      final Vector temp = new Vector(B.getMaxRows());
      A.times(x, temp);
      final Vector actual = new Vector(B.getMaxRows());
      temp.minus(B, actual);

      // CHECK
      for (int i = 0; i < Parameter.ROW_NUMBER; i++) {
         Assert.assertEquals(0.0d, actual.getValue(i), 1E-4);
      }
   }

   @Test
   public void v3_solveConjugateGradient_2_Aparapi_JTP_LargeRandomBandMatrix_Solved() {

      // ARRANGE

      // ACT
      //
      // solve linear equation
      final Vector x = BandMatrixFull.solveConjugateGradientAparapi(A, B, true, Kernel.EXECUTION_MODE.JTP);
      //
      // all elements of result should be zero 
      final Vector temp = new Vector(B.getMaxRows());
      A.times(x, temp);
      final Vector actual = new Vector(B.getMaxRows());
      temp.minus(B, actual);

      // CHECK
      for (int i = 0; i < Parameter.ROW_NUMBER; i++) {
         Assert.assertEquals(0.0d, actual.getValue(i), 1E-4);
      }
   }

//   @Test
//   public void v3_solveConjugateGradient_2_Aparapi_GPU_LargeRandomBandMatrix_Solved() {
//
//      // ARRANGE
//
//      // ACT
//      //
//      // solve linear equation
//      final Vector x = BandMatrixFull.solveConjugateGradientAparapi(A, B, true, Kernel.EXECUTION_MODE.GPU);
//      //
//      // all elements of result should be zero 
//      final Vector temp = new Vector(B.getMaxRows());
//      A.times(x, temp);
//      final Vector actual = new Vector(B.getMaxRows());
//      temp.minus(B, actual);
//
//      // CHECK
//      for (int i = 0; i < Parameter.ROW_NUMBER; i++) {
//         Assert.assertEquals(0.0d, actual.getValue(i), 1E-4);
//      }
//   }

}