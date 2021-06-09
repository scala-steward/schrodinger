/*
 * Copyright 2021 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package schrodinger.random

import cats.Monad
import cats.syntax.all._
import cats.mtl.Stateful
import schrodinger.kernel.{Gaussian, Uniform}

object gaussian extends GaussianInstances

final case class CachedGaussian[A](value: A) extends AnyVal

trait GaussianInstances {

  def schrodingerRandomGaussianForDouble[F[_]: Monad: Uniform[*[_], Unit, Double]](
      implicit state: Stateful[F, CachedGaussian[Double]]): Gaussian[F, Double] = {

    final case class BoxMuller(x: Double, y: Double, s: Double)
    object BoxMuller {
      def apply(x: Double, y: Double): BoxMuller =
        BoxMuller(x, y, x * x + y * y)
    }

    new Gaussian[F, Double] {
      private val CachedNaN = CachedGaussian(Double.NaN)

      override def apply(mean: Double, standardDeviation: Double): F[Double] =
        standard.map(_ * standardDeviation + mean)

      override val standard: F[Double] = state.get.flatMap { cached =>
        if (cached.value.isNaN) for {
          (x, y) <- pair
          _ <- state.set(CachedGaussian(y))
        } yield x
        else state.set(CachedNaN).as(cached.value)
      }

      private val boxMuller =
        Uniform.standard.map2(Uniform.standard) { (x, y) => BoxMuller(x * 2 - 1, y * 2 - 1) }

      private val pair = boxMuller.iterateWhile(bm => bm.s >= 1.0 | bm.s == 0.0).map { bm =>
        import bm._
        val scale = math.sqrt(-2.0 * math.log(s) / s)
        (scale * x, scale * y)
      }

    }
  }
}
