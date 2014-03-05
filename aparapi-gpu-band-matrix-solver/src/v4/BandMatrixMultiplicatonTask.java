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

import java.util.concurrent.RecursiveTask;

import tests.Parameter;

public class BandMatrixMultiplicatonTask extends RecursiveTask<Long> {

   private static final long serialVersionUID = 1L;

   private final Vector b;
   private final BandMatrixFull valuesMatrix;
   private final Vector result;
   private final int colMaximum;
   private final int rowStart;
   private final int rowEnd;

   public BandMatrixMultiplicatonTask(int rowIndexStart, int rowIndexEnd, BandMatrixFull valuesBandMatrix,
         final Vector vectorP, final Vector result) {
      rowStart = rowIndexStart;
      rowEnd = rowIndexEnd;
      b = vectorP;
      valuesMatrix = valuesBandMatrix;
      colMaximum = valuesBandMatrix.getMaxCols();
      this.result = result;
   }

   @Override
   public Long compute() {

      if (rowEnd - rowStart < b.getMaxRows() / Parameter.NUMBER_OF_POCESSORS) {
         calculateMatrixMultiplication();
      } else {
         final int mid = (rowEnd + rowStart) >> 1;

         final BandMatrixMultiplicatonTask firstWorker = new BandMatrixMultiplicatonTask(rowStart, mid, valuesMatrix,
               b, result);
         firstWorker.fork();

         final BandMatrixMultiplicatonTask secondWorker = new BandMatrixMultiplicatonTask(mid, rowEnd, valuesMatrix, b,
               result);
         secondWorker.compute();
         firstWorker.join();
      }
      return 0L;
   }

   private void calculateMatrixMultiplication() {
      // prepare input parameter
      final int bandwidthMid = colMaximum >> 1;
      final int rowMaximum = b.values.length;
      final double[] values = valuesMatrix.values;

      // execute band matrix multiplication
      int index;
      int rowOffset;
      double sum;
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

}
