package com.hotel.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Small helper for exporting simple, polished PDFs without pulling UI logic into controllers.
 */
public final class PdfExportUtil {
    private static final float PAGE_MARGIN = 48f;
    private static final float TITLE_SIZE = 20f;
    private static final float SECTION_SIZE = 12f;
    private static final float BODY_SIZE = 10.5f;
    private static final float LINE_GAP = 6f;

    private PdfExportUtil() {
    }

    public static void exportDocument(Path outputPath, String title, List<String> introLines, List<String> bodyLines)
        throws IOException {
        Files.createDirectories(outputPath.getParent());

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - PAGE_MARGIN;
                y = writeTitle(content, title, y);
                y = writeMutedLines(content, introLines, y);
                writeBody(content, bodyLines, y, page);
            }

            document.save(outputPath.toFile());
        }
    }

    private static float writeTitle(PDPageContentStream content, String title, float y) throws IOException {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, TITLE_SIZE);
        content.setNonStrokingColor(new Color(34, 49, 63));
        content.newLineAtOffset(PAGE_MARGIN, y);
        content.showText(title);
        content.endText();

        content.setStrokingColor(new Color(52, 152, 219));
        content.setLineWidth(1.2f);
        content.moveTo(PAGE_MARGIN, y - 10f);
        content.lineTo(PDRectangle.LETTER.getWidth() - PAGE_MARGIN, y - 10f);
        content.stroke();
        return y - 28f;
    }

    private static float writeMutedLines(PDPageContentStream content, List<String> lines, float y) throws IOException {
        if (lines == null || lines.isEmpty()) {
            return y;
        }

        content.setNonStrokingColor(new Color(95, 106, 106));
        for (String line : lines) {
            for (String wrapped : wrap(line, 95)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, BODY_SIZE);
                content.newLineAtOffset(PAGE_MARGIN, y);
                content.showText(wrapped);
                content.endText();
                y -= BODY_SIZE + LINE_GAP;
            }
        }
        return y - 8f;
    }

    private static void writeBody(PDPageContentStream content, List<String> bodyLines, float startY, PDPage page)
        throws IOException {
        float y = startY;
        for (String rawLine : bodyLines) {
            if (rawLine == null) {
                continue;
            }

            boolean section = rawLine.startsWith("## ");
            String line = section ? rawLine.substring(3) : rawLine;
            List<String> wrapped = wrap(line, section ? 85 : 95);

            for (int i = 0; i < wrapped.size(); i++) {
                if (y < PAGE_MARGIN + 20f) {
                    break;
                }
                content.beginText();
                content.setNonStrokingColor(section ? new Color(44, 62, 80) : Color.DARK_GRAY);
                content.setFont(section ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, section ? SECTION_SIZE : BODY_SIZE);
                content.newLineAtOffset(PAGE_MARGIN, y);
                content.showText(wrapped.get(i));
                content.endText();
                y -= (section ? SECTION_SIZE : BODY_SIZE) + LINE_GAP;
            }
            y -= section ? 4f : 1f;
        }
    }

    private static List<String> wrap(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.isEmpty()) {
                current.append(word);
                continue;
            }
            if (current.length() + 1 + word.length() > maxChars) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current.append(' ').append(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }
}
