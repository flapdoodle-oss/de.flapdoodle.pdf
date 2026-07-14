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

import de.flapdoodle.pdf.DocumentFactory;
import de.flapdoodle.pdf.DocumentFactoryAssert;
import de.flapdoodle.pdf.blocks.Text;
import de.flapdoodle.pdf.watermark.WaterMarkContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openpdf.text.PageSize;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

class WaterMarkTest {
	@Test
	@DisplayName("render watermark")
	void renderWaterMark() throws IOException {
		var zoneId = ZoneId.of("America/New_York");
		var waterMarkContent = new WaterMarkContent("mail", ZonedDateTime.of(2019, 2, 13, 17, 33, 2, 0, zoneId));

		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4)
				.blocks(List.of(new Text("sample text")))
				.addOnPageEvents(new WaterMark(waterMarkContent))
				.build())
			.expectRendering()
			.matchesResource(getClass(),"watermark.pdf");
	}

	@Test
	@DisplayName("render watermark on landscape")
	void renderWaterMarkOnLandscape() throws IOException {
		var zoneId = ZoneId.of("America/New_York");
		var waterMarkContent = new WaterMarkContent("mail", ZonedDateTime.of(2019, 2, 13, 17, 33, 2, 0, zoneId));

		DocumentFactoryAssert.assertThat(DocumentFactory.builder()
				.pageSize(PageSize.A4.rotate())
				.blocks(List.of(new Text("sample text")))
				.addOnPageEvents(new WaterMark(waterMarkContent))
				.build())
			.expectRendering()
			.matchesResource(getClass(),"watermarkLandscape.pdf");
	}
}