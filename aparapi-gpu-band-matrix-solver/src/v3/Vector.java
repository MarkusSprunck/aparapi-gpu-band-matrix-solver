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

public class Vector {

   protected final long[] packedValues;

   public Vector(final int length) {
      packedValues = new long[length];
   }

   public Vector(final Vector A) {
      this(A.packedValues);
   }

   public Vector(final long[] values) {
      packedValues = new long[values.length];
      System.arraycopy(values, 0, packedValues, 0, values.length);
   }

   // return C = A + B
   public Vector plus(final Vector B, final Vector result) {
      for (int i = 0; i < packedValues.length; i++) {
         result.packedValues[i] = PackedDouble.addPacked(packedValues[i], B.packedValues[i]);
      }
      return result;
   }

   // return C = A - B
   public Vector minus(final Vector B, final Vector result) {
      for (int i = 0; i < packedValues.length; i++) {
         result.packedValues[i] = PackedDouble.pack(PackedDouble.unpack(packedValues[i])
               - PackedDouble.unpack(B.packedValues[i]));
      }
      return result;
   }

   // return C = A o B 
   public double dotProduct(final Vector B) {
      double result = 0.0;
      for (int i = 0; i < packedValues.length; i++) {
         result += PackedDouble.unpack(PackedDouble.multiplyPacked(packedValues[i], B.packedValues[i]));
      }
      return result;
   }

   // return C = A * alpha
   public Vector multi(final double alpha, final Vector result) {
      for (int i = 0; i < packedValues.length; i++) {
         result.packedValues[i] = PackedDouble.pack(PackedDouble.unpack(packedValues[i]) * alpha);
      }
      return result;
   }

   public void setValue(final int index, final double value) {
      packedValues[index] = PackedDouble.pack(value);
   }

   public double getValue(final int index) {
      return PackedDouble.unpack(packedValues[index]);
   }

   public int getMaxRows() {
      return packedValues.length;
   }

   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder("v5.Vector [");
      for (int i = 0; i < packedValues.length; i++) {
         sb.append(String.format("%.6E", PackedDouble.unpack(packedValues[i]))).append("  ");
      }
      sb.append(']');
      return sb.toString();
   }
}