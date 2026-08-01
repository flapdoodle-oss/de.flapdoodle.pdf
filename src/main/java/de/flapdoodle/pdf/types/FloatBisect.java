/*
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.pdf.types;

import de.flapdoodle.pdf.checks.Preconditions;

public record FloatBisect(
	float lower, float middle, float upper, float delta
) {

	public FloatBisect(float lower, float upper) {
		this(lower, middle(lower, upper), upper, 0.01f);
	}

	public FloatBisect(float lower, float middle, float upper) {
		this(lower, middle, upper, 0.01f);
	}

	public FloatBisect {
		Preconditions.checkArgument(lower < middle, "lower must be < middle");
		Preconditions.checkArgument(middle < upper, "middle must be < upper");
		Preconditions.checkArgument(delta > 0.f, "delta must be > 0: %s", delta);
	}

	public FloatBisect lowerHalf() {
		return new FloatBisect(lower, middle(lower, middle), middle, delta);
	}
	public FloatBisect upperHalf() {
		return new FloatBisect(middle, middle(middle, upper), upper, delta);
	}

	public boolean isCloseEnough() {
		return Floats.isNearBy(lower, upper, delta);
	}

	private static float middle(float lower, float upper) {
		return (upper - lower) / 2f + lower;
	}
}
