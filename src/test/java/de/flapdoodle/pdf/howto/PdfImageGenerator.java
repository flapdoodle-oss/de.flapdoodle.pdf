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
package de.flapdoodle.pdf.howto;

import com.google.common.base.Preconditions;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class PdfImageGenerator implements AutoCloseable {
	private final PDDocument document;
	private final PDFRenderer renderer;

	public PdfImageGenerator(byte[] buffer) {
		try {
			this.document = Loader.loadPDF(buffer);
			renderer = new PDFRenderer(document);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
//		try (PDDocument document = Loader.loadPDF(pdfFile)) {
//			PDFRenderer renderer = new PDFRenderer(document);
//
//			for (int page = 0; page < document.getNumberOfPages(); page++) {
//				// 300 DPI gives print-quality output; 72 is screen resolution
//				BufferedImage image = renderer.renderImageWithDPI(page, 300, ImageType.RGB);
//
//				String filename = String.format("page-%d.png", page + 1);
//				ImageIO.write(image, "png", new File(filename));
//			}
//		}
	}

	public int pages() {
		return document.getNumberOfPages();
	}

	private BufferedImage page2BufferedImage(int page) {
		try {
			return renderer.renderImageWithDPI(page, 75, ImageType.RGB);
		}
		catch (IOException e) {
			throw new RuntimeException("could not render page: "+page,e);
		}
	}

	public byte[] renderPageAsPng(int pageNum) {
		Preconditions.checkArgument(pageNum >= 0, "pageNum must be >= 0");
		Preconditions.checkArgument(pageNum < pages(), "pageNum must be < pages");

		try (ByteArrayOutputStream imageOut = new ByteArrayOutputStream()) {
			BufferedImage image = page2BufferedImage(pageNum);
			ImageIO.write(image, "png", imageOut);
			return imageOut.toByteArray();
		} catch (IOException iox) {
			throw new RuntimeException(iox);
		}
	}

	public static byte[] renderPageAsPng(byte[] page, int pageNum) {
		try(PdfImageGenerator imageGenerator = new PdfImageGenerator(page)) {
			return imageGenerator.renderPageAsPng(pageNum);
		}
	}


	@Override
	public void close() {
		try {
			document.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
