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
package tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import v2.BandMatrix;
import v2.Vector;

public class TestRunner {

   public static boolean initNeeded = true;

   private static final Object HANDLER_INIT_LOCK = new Object();

   public static BandMatrix A;

   public static Vector B;

   public static void main(String[] args) {

      System.out.println("#rows\t#cols\tv1\tv3a\tv3b\tv4a\tv4b\tv4c");

      while (Parameter.ROW_NUMBER < Parameter.ROW_NUMBER_MAX && Parameter.BAND_WIDTH < Parameter.BAND_WIDTH_MAX) {

         System.out.print(Parameter.ROW_NUMBER + "\t");
         System.out.print(Parameter.BAND_WIDTH);

         final Result result = JUnitCore.runClasses(v1.BandMatrixTest.class, /* v2.BandMatrixTest.class, */
               v3.BandMatrixFullTest.class, v4.BandMatrixFullTest.class);

         if (!result.getFailures().isEmpty()) {
            for (final Failure failure : result.getFailures()) {
               System.out.println(failure.toString());
            }
         }
         System.out.println("");

         Parameter.ROW_NUMBER += 128;
         Parameter.BAND_WIDTH = Parameter.ROW_NUMBER / 10;
         Parameter.BAND_WIDTH = ((Parameter.BAND_WIDTH % 2) == 0) ? Parameter.BAND_WIDTH + 1 : Parameter.BAND_WIDTH;
      }
   }

   public static void setupTestData() {
      synchronized (HANDLER_INIT_LOCK) {
         if (initNeeded) {
            A = new v2.BandMatrix(Parameter.ROW_NUMBER_MAX, Parameter.BAND_WIDTH_MAX);
            B = new v2.Vector(Parameter.ROW_NUMBER_MAX);
            for (int row = 0; row < Parameter.ROW_NUMBER_MAX; row++) {
               for (int col = 0; col <= (Parameter.BAND_WIDTH_MAX >> 1); col++) {
                  A.setValue(row, row + col, createRandomNumber());
               }
               B.setValue(row, createRandomNumber());
            }
            initNeeded = false;
         }
      }
   }

   private static double createRandomNumber() {
      return 10 * (Math.random() - 0.5);
   }
}
