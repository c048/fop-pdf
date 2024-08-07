package com.ravago.fop.render.pdf.pdfbox;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;

import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageConverter;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * Image converter implementation to convert PDF pages into Java2D images.
 */
public class ImageConverterPDF2G2D extends AbstractImageConverter {

    /** {@inheritDoc} */
    public Image convert(Image src, Map hints) throws ImageException, IOException {
        checkSourceFlavor(src);
        ImagePDF imgPDF = (ImagePDF)src;

        final int selectedPage = ImageUtil.needPageIndexFromURI(src.getInfo().getOriginalURI());

        PDDocument pddoc = imgPDF.getPDDocument();
        PDPage page = (PDPage)pddoc.getDocumentCatalog().getAllPages().get(selectedPage);

        Graphics2DImagePainter painter = new Graphics2DImagePainterPDF(page);
        return new ImageGraphics2D(src.getInfo(), painter);
    }

    /** {@inheritDoc} */
    public ImageFlavor getSourceFlavor() {
        return ImagePDF.PDFBOX_IMAGE;
    }

    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return ImageFlavor.GRAPHICS2D;
    }

    /** {@inheritDoc} */
    public int getConversionPenalty() {
        return 1000; //Use only if no native embedding is possible
    }

    private static class Graphics2DImagePainterPDF implements Graphics2DImagePainter {

        private final PDPage page;

        public Graphics2DImagePainterPDF(PDPage page) {
            this.page = page;
        }

        /** {@inheritDoc} */
        public Dimension getImageSize() {
            PDRectangle mediaBox = page.findMediaBox();
            int wmpt = (int)Math.ceil(mediaBox.getWidth() * 1000);
            int hmpt = (int)Math.ceil(mediaBox.getHeight() * 1000);
            return new Dimension(wmpt, hmpt);
        }

        /** {@inheritDoc} */
        public void paint(Graphics2D g2d, Rectangle2D area) {
            try {
                PDRectangle mediaBox = page.findMediaBox();
                Dimension pageDimension = mediaBox.createDimension();

                AffineTransform at = new AffineTransform();
                at.translate(area.getX(), area.getY());
                at.scale(area.getWidth() / pageDimension.width,
                        area.getHeight() / pageDimension.height);
                g2d.transform(at);

                PageDrawer drawer = new PageDrawer();
                drawer.drawPage(g2d, page, pageDimension);
            } catch (IOException ioe) {
                //TODO Better exception handling
                throw new RuntimeException("I/O error while painting PDF page", ioe);
            }
        }
    }

}
