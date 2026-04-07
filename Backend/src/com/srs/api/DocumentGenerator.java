package com.srs.api;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DocumentGenerator {

    public static byte[] generateIEEEReport(String projectName, String author, String content) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Document document = new Document(PageSize.A4, 72, 72, 72, 72);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 20);
        Font sectionFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 14);
        Font bodyFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11);

        Paragraph title = new Paragraph("Software Requirements Specification", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(100);
        document.add(title);

        Paragraph forProj = new Paragraph("for\n" + projectName, sectionFont);
        forProj.setAlignment(Element.ALIGN_CENTER);
        forProj.setSpacingBefore(20);
        document.add(forProj);

        Paragraph authorInfo = new Paragraph("\n\nPrepared by: " + author, bodyFont);
        authorInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(authorInfo);

        document.newPage(); 

        Paragraph sec1 = new Paragraph("1. Introduction", sectionFont);
        sec1.setSpacingBefore(10);
        document.add(sec1);

        Paragraph text = new Paragraph(content, bodyFont);
        text.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(text);

        document.close();
        return out.toByteArray();
    }
}