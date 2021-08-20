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

package schrodinger

import org.apache.commons.rng.core.source64.SplitMix64
import org.scalacheck.{Arbitrary, Gen}
import schrodinger.rng.Rng

package object random:
  given Arbitrary[SplitMix64] =
    Arbitrary(Gen.long.map(new SplitMix64(_)))

  given Rng[SplitMix64] with
    extension (s: SplitMix64)
      override def unsafeNextInt(): Int = s.nextInt()
      override def unsafeNextLong(): Long = s.nextLong()
      override def copy: SplitMix64 =
        val s1 = new SplitMix64(0)
        s1.restoreState(s.saveState())
        s1
