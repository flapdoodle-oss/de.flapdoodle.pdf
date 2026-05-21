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
package de.flapdoodle.pdf.watermark;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public record WaterMarkContent(
	String email,
	ZonedDateTime downloadDate
) {

	private static final DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
	private static final String FILLER = "-";

	public String repeatUntilLen(int minLengthForContent) {
		var part = "- "+email+" - "+downloadDate.format(formatter)+" -";

		StringBuilder ret = new StringBuilder(part);
		while (ret.length() < minLengthForContent) {
			ret.append(part);
		}
		return ret.toString();
	}

	public String asString(int minLengthForContent) {
		var text = " "+email+" "+FILLER+FILLER+" "+downloadDate.format(formatter);
		var neededFillerLength = (minLengthForContent - text.length()) / 2;

		return FILLER.repeat(neededFillerLength) + text + FILLER.repeat(neededFillerLength);
	}

}
