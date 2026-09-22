package com.resumelens.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.resumelens.entity.MatchResult;
import com.resumelens.entity.SectionScore;
import com.resumelens.exception.ResourceNotFoundException;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.SectionScoreRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportGeneratorService {

    private final MatchResultRepository matchResultRepository;
    private final SectionScoreRepository sectionScoreRepository;

    public ReportGeneratorService(MatchResultRepository matchResultRepository,
                                  SectionScoreRepository sectionScoreRepository) {
        this.matchResultRepository = matchResultRepository;
        this.sectionScoreRepository = sectionScoreRepository;
    }

    /** Generates a PDF report for a given match result and returns the bytes. */
    public byte[] generatePdfReport(Long matchResultId) {
        MatchResult result = matchResultRepository.findById(matchResultId)
            .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchResultId));
        List<SectionScore> sections = sectionScoreRepository.findByMatchResultId(matchResultId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
             Document doc = new Document(pdf)) {

            // Title
            doc.add(new Paragraph("ATS Analysis Report")
                .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Job: " + result.getJobDescription().getTitle())
                .setFontSize(12).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(" "));

            // Overall score
            doc.add(new Paragraph("Overall Score: " + result.getOverallScore() + " / 100  —  " + result.getStatusLabel())
                .setBold().setFontSize(14).setFontColor(scoreColor(result.getOverallScore())));
            doc.add(new Paragraph(" "));

            // Dimension scores table
            doc.add(new Paragraph("Score Breakdown").setBold().setFontSize(13));
            Table table = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100));
            addHeaderCell(table, "Section");
            addHeaderCell(table, "Score");
            addHeaderCell(table, "Status");
            for (SectionScore s : sections) {
                table.addCell(s.getSectionName().replace("_", " "));
                table.addCell(String.valueOf(s.getScore()));
                table.addCell(s.getStatus());
            }
            doc.add(table);
            doc.add(new Paragraph(" "));

            // Missing keywords
            sections.stream()
                .filter(s -> s.getMissingKeywords() != null && !s.getMissingKeywords().isBlank())
                .findFirst()
                .ifPresent(s -> {
                    doc.add(new Paragraph("Missing Keywords").setBold().setFontSize(13));
                    doc.add(new Paragraph(s.getMissingKeywords()).setFontSize(10));
                    doc.add(new Paragraph(" "));
                });

            // Improvement prompt note
            doc.add(new Paragraph("Improvement Prompt").setBold().setFontSize(13));
            doc.add(new Paragraph(
                "An AI-improvement prompt has been generated for this analysis. " +
                "Copy it from the API and paste it into your preferred AI assistant " +
                "(Claude, ChatGPT, Gemini, or any other LLM) to receive a rewritten resume.")
                .setFontSize(10).setItalic());

        }
        return baos.toByteArray();
    }

    private void addHeaderCell(Table table, String text) {
        table.addHeaderCell(new Cell().add(new Paragraph(text).setBold())
            .setBackgroundColor(ColorConstants.LIGHT_GRAY));
    }

    private com.itextpdf.kernel.colors.Color scoreColor(double score) {
        if (score >= 70) return ColorConstants.DARK_GRAY;
        if (score >= 40) return ColorConstants.ORANGE;
        return ColorConstants.RED;
    }
}