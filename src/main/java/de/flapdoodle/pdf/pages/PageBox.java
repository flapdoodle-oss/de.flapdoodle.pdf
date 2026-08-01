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
package de.flapdoodle.pdf.pages;

import de.flapdoodle.pdf.checks.Preconditions;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import de.flapdoodle.pdf.layout.Margin;
import de.flapdoodle.pdf.tables.cells.HorizontalAlignment;
import de.flapdoodle.pdf.tables.cells.VerticalAlignment;
import de.flapdoodle.pdf.types.Dimension;

/**
 * |
 * |
 * heigh
 * |
 * left,bottom----width---
 */
public record PageBox(float left, float bottom, float width, float height) {

	public PageBox(PagePosition position, Dimension dimension) {
		this(position.x(), position.y(), dimension.width(), dimension.height());
	}

	public PageBox withMargin(float margin) {
		return new PageBox(left + margin, bottom + margin, width - margin * 2, height - margin * 2);
	}

	public PageBox withWidth(float newWidth) {
		return new PageBox(left, bottom ,newWidth , height);
	}
	public PageBox withHeight(float newHeight) {
		return new PageBox(left, bottom ,width , newHeight);
	}

	public PageBox withMargin(Margin margin) {
		return new PageBox(
			left + margin.left(),
			bottom + margin.bottom(),
			width - margin.left() - margin.right(),
			height - margin.bottom() - margin.top()
		);
	}

	public PageBox boxAt(
		Dimension dimension,
		HorizontalAlignment horizontalAlignment,
		VerticalAlignment verticalAlignment
	) {
		float leftWidth = this.width-dimension.width();
		float leftHeight = this.height-dimension.height();

		Preconditions.checkArgument(leftWidth>=0.0,"no width left");
		Preconditions.checkArgument(leftHeight>=0.0,"no height left");

		float newLeft = switch (horizontalAlignment) {
			case LEFT -> left;
			case RIGHT -> left+leftWidth;
			case CENTER -> left+this.width/2.0f-dimension.width()/2.0f;
		};

		float newBottom = switch (verticalAlignment) {
			case BOTTOM -> bottom;
			case MIDDLE -> bottom+this.height/2.0f-dimension.height()/2.0f;
			case TOP -> bottom + leftHeight;
		};

		return new PageBox(newLeft,newBottom,dimension.width(),dimension.height());
	}

	public PageBox rowAt(float height, VerticalAlignment verticalAlignment) {
		return boxAt(new Dimension(width, height), HorizontalAlignment.LEFT, verticalAlignment);
	}

	public PageBox columnAt(float width, HorizontalAlignment horizontalAlignment) {
		return boxAt(new Dimension(width, height), horizontalAlignment, VerticalAlignment.BOTTOM);
	}


	public Rectangle asRectangle() {
		return new Rectangle(left, bottom, left + width, bottom + height);
	}

	public Dimension dimension() {
		return new Dimension(width, height);
	}

	public static PageBox innerBox(Document document) {
		return new PageBox(
			document.left(),
			document.bottom(),
			document.right() - document.left(),
			document.top() - document.bottom()
		);
	}

	public static PageBox fullPageBox(Document document) {
		return asPageBox(document.getPageSize());
	}

	public static PageBox asPageBox(Rectangle rectangle) {
		return new PageBox(
			rectangle.getLeft(),
			rectangle.getBottom(),
			rectangle.getRight() - rectangle.getLeft(),
			rectangle.getTop() - rectangle.getBottom()
		);
	}

	public static void setBox(ColumnText columnText, PageBox box) {
		columnText.setSimpleColumn(
			box.left,
			box.bottom,
			box.left + box.width,
			box.bottom + box.height
		);
	}
}