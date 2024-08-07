/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package com.ravago.fop.render.pdf.pdfbox;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFormXObject;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.pdf.PDFContentGenerator;
import org.apache.fop.render.pdf.PDFRenderingContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

/**
 * Abstract base class for implementation of FOP's image handler interfaces (old and new)
 * which can use PDFBox to parse an existing PDF file and write that to the
 * target PDF as a Form XObject.
 */
public abstract class AbstractPDFBoxHandler implements ImageHandler {

	private static final ImageFlavor[] FLAVORS = new ImageFlavor[]{ImagePDF.PDFBOX_IMAGE};
	private static Map objectCaches = Collections.synchronizedMap(new java.util.WeakHashMap());

	protected static Log log = LogFactory.getLog(AbstractPDFBoxHandler.class);

	public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
		PDFRenderingContext pdfContext = (PDFRenderingContext) context;
		PDFContentGenerator generator = pdfContext.getGenerator();
		ImagePDF pdfImage = (ImagePDF) image;

		PDFFormXObject form = createFormForPDF(pdfImage, pdfContext.getPage());
		if (form != null) {
			float x = (float) pos.getX() / 1000f;
			float y = (float) pos.getY() / 1000f;
			float w = (float) pos.getWidth() / 1000f;
			float h = (float) pos.getHeight() / 1000f;
			generator.placeImage(x, y, w, h, form);
		}
	}

	protected PDFFormXObject createFormForPDF(ImagePDF image, PDFPage targetPage) throws IOException {
		final int selectedPage = ImageUtil.needPageIndexFromURI(image.getInfo().getOriginalURI());

		PDDocument pddoc = image.getPDDocument();
		float pdfVersion = pddoc.getDocument().getVersion();
		if (pdfVersion > 1.4f) {
			log.warn("The version of the loaded PDF is " + pdfVersion + ". PDF Versions beyond 1.4 might not create correct results.");
		}

		//Encryption test
		if (pddoc.isEncrypted() && pddoc.getCurrentAccessPermission().isOwnerPermission()) {
			log.error("PDF to be embedded must not be encrypted! Alternative: provide authentication via interceptor.");
			return null;
		}

		PDFDocument pdfDoc = targetPage.getDocument();
		//Warn about potential problems with PDF/A and PDF/X
		if (pdfDoc.getProfile().isPDFAActive()) {
			log.warn("PDF/A mode is active. Embedding a PDF file may result in a non-compliant file!");
		}
		if (pdfDoc.getProfile().isPDFXActive()) {
			log.warn("PDF/X mode is active. Embedding a PDF file may result in a non-compliant file!");
		}

		PDPage page = (PDPage) pddoc.getDocumentCatalog().getAllPages().get(selectedPage);

		//Only has an effect if PDDocuments are reused for multiple page which is currently not
		//the case. Code remains in place in case this can be improved in the future.
		MapKey key = new MapKey(pddoc, pdfDoc);
		Map objectCache = (Map) objectCaches.get(key);
		if (objectCache == null) {
			//Object cache itself doesn't need to be cached as FOP is not multi-threaded
			objectCache = new java.util.HashMap();
			objectCaches.put(key, objectCache);
		}

		PDFBoxAdapter adapter = new PDFBoxAdapter(targetPage, objectCache);
		PDFFormXObject form = adapter.createFormFromPDFBoxPage(pddoc, page, image.getInfo().getOriginalURI());
		return form;
	}

	public boolean isCompatible(RenderingContext targetContext, Image image) {
		return (image == null || image instanceof ImagePDF) && targetContext instanceof PDFRenderingContext;
	}

	public int getPriority() {
		//Before built-in handlers in case someone implements a PDF -> Graphics2D converter
		return 50;
	}

	public ImageFlavor[] getSupportedImageFlavors() {
		return FLAVORS;
	}

	public Class getSupportedImageClass() {
		return ImagePDF.class;
	}

	private static final class MapKey {

		private PDDocument sourceDocument;
		private PDFDocument targetDocument;

		public MapKey(PDDocument source, PDFDocument target) {
			this.sourceDocument = source;
			this.targetDocument = target;
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + sourceDocument.hashCode();
			result = prime * result + targetDocument.hashCode();
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			MapKey other = (MapKey) obj;
			return sourceDocument == other.sourceDocument
				   && targetDocument == other.targetDocument;
		}
	}
}
