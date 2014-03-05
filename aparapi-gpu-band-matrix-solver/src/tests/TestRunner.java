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

public class TestRunner {

   public static void main(String[] args) {

      for (int i = 0; i < 8; i++) {

         System.out.println("NUMBER_OF_POCESSORS = " + Parameter.NUMBER_OF_POCESSORS);
         System.out.println("BAND_WIDTH          = " + Parameter.BAND_WIDTH);
         System.out.println("ROW_NUMBER          = " + Parameter.ROW_NUMBER);

         System.out.println("");
         final Result result = JUnitCore.runClasses(v1.BandMatrixTest.class, v2.BandMatrixTest.class,
               v3.BandMatrixFullTest.class, v4.BandMatrixFullTest.class, v1.MatrixTest.class, v2.VectorTest.class,
               v3.VectorTest.class, v4.VectorTest.class);

         if (!result.getFailures().isEmpty()) {
            for (final Failure failure : result.getFailures()) {
               System.out.println(failure.toString());
            }
         }
         System.out.println("");
         System.out.println("**************************************************************************");

         Parameter.BAND_WIDTH = ((Parameter.BAND_WIDTH - 1) << 1) + 1;
         Parameter.ROW_NUMBER = Parameter.ROW_NUMBER << 1;
         v2.BandMatrixTest.initNeeded = true;

      }

   }

}
