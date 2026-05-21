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
package de.flapdoodle.pdf.layout;

import org.immutables.value.Value;

@Value.Immutable(singleton = true)
public interface Margin {
	@Value.Default
	default float top() { return 0; }
	@Value.Default
	default float left() { return 0; }
	@Value.Default
	default float right() { return 0; }
	@Value.Default
	default float bottom() { return 0; }

	static Margin of(float top, float left, float right, float bottom) {
		return ImmutableMargin.builder().left(left).top(top).right(right).bottom(bottom).build();
	}

	static Margin none() {
		return ImmutableMargin.of();
	}
}
