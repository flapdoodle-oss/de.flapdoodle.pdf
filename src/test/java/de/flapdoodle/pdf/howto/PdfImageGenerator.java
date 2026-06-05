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

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

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

			ImageWriter writer = pngWriter();
			ImageWriteParam param = writer.getDefaultWriteParam();

			ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
			IIOMetadata metadata = cleanedMetaData(writer, type, param);

			try (ImageOutputStream ios = ImageIO.createImageOutputStream(imageOut)) {
				writer.setOutput(ios);
				writer.write(null, new IIOImage(image, null, metadata), param);
			} finally {
				writer.dispose();
			}

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

	private static ImageWriter pngWriter() {
		Iterator<ImageWriter> pngWriters = ImageIO.getImageWritersByFormatName("png");
		Preconditions.checkState(pngWriters.hasNext(),"no png writers found");
		ImageWriter writer = pngWriters.next();

		ImageWriteParam param = writer.getDefaultWriteParam();
		if (param.canWriteCompressed()) {
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionType("Deflate");
			param.setCompressionQuality(0.0f); // konsistent, egal welcher Wert – Hauptsache fix
		}
		return writer;
	}

	private static IIOMetadata cleanedMetaData(ImageWriter writer, ImageTypeSpecifier type, ImageWriteParam param) throws IIOInvalidTreeException {
		IIOMetadata metadata = writer.getDefaultImageMetadata(type, param);
		String formatName = metadata.getNativeMetadataFormatName(); // "javax_imageio_png_1.0"
		IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(formatName);
		removeNode(root, "tIME");
		removeNode(root, "tEXt");
		removeNode(root, "iTXt");
		removeNode(root, "zTXt");
		metadata.setFromTree(formatName, root);
		return metadata;
	}

	private static void removeNode(IIOMetadataNode root, String name) {
		var nodes = root.getElementsByTagName(name);
		for (int i = nodes.getLength() - 1; i >= 0; i--) {
			nodes.item(i).getParentNode().removeChild(nodes.item(i));
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
