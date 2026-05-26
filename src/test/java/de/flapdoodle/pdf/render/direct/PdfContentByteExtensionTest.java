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
package de.flapdoodle.pdf.render.direct;

import com.lowagie.text.PageSize;
import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.blocks.Space;
import de.flapdoodle.pdf.blocks.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static de.flapdoodle.pdf.DocumentFactoryAssert.assertThat;

class PdfContentByteExtensionTest {
	@Test
	@DisplayName("set vertical position")
	void setVerticalPosition() {
		assertThat(DocumentFactory.builder()
			.pageSize(PageSize.A4)
			.addBlocks(
				new Text("before"),
				new Space(100f),
				new Text("after")
			)
			.build())
			.expectRendering()
			.matchesResource(getClass(),"setVerticalPosition.pdf");
	}

}