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
package de.flapdoodle.pdf.blocks;

import de.flapdoodle.pdf.DocumentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;

import java.awt.*;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class SectionTest {

	@Test
	@DisplayName("render section")
	void renderSection() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(Section.builder()
				.title("Section Title")
				.font(new Font(Font.HELVETICA, 12f, Font.NORMAL, Color.WHITE))
				.backgroundColor(new Color(160, 0, 0))
				.build())
			.build())
			.expectRendering()
			.matchesResource(getClass(), "sectionPdf.pdf");
	}

}