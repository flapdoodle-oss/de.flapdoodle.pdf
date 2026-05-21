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

import de.flapdoodle.pdf.pages.PageBox;

public record Box(
	float left,
	float top,
	float width,
	float height
) {
	public boolean contains(Box inner) {
		if (left <= inner.left && top <= inner.top) {
			if (left + width >= inner.left + inner.width) {
				if (top + height >= inner.top + inner.height) {
					return true;
				}
			}
		}
		return false;
	}

	public Box translate(float deltaX, float deltaY) {
		return new Box(left + deltaX, top + deltaY, width, height);
	}

	public PageBox asPageBox(PageBox base) {
		return new PageBox(base.left() + left, base.bottom() + base.height() - top - height, width, height);
	}

	public Box(Position position, Dimension dimension) {
		this(position.x(), position.y(), dimension.width(), dimension.height());
	}
}

