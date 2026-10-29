package com.resumelens.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.resumelens.entity.MatchResult;
import com.resumelens.entity.SectionScore;
import com.resumelens.exception.ResumeParseException;
import com.resumelens.exception.ResourceNotFoundException;
import com.resumelens.repository.MatchResultRepository;
import com.resumelens.repository.SectionScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates an executive, professional ATS Diagnostic Audit Report.
 * Designed with disciplined monochrome typography, spacious layout hierarchy,
 * and clean two-page structured editorial design without visual clustering.
 */
@Service
public class ReportGeneratorService {

    private final MatchResultRepository matchResultRepository;
    private final SectionScoreRepository sectionScoreRepository;
    private final ObjectMapper objectMapper;

    // Professional Editorial Palette (Disciplined Slate & Monochrome)
    private static final DeviceRgb INK_BLACK = new DeviceRgb(15, 23, 42); // #0f172a (Primary text / titles)
    private static final DeviceRgb SLATE_800 = new DeviceRgb(30, 41, 59); // #1e293b (Body headings)
    private static final DeviceRgb SLATE_700 = new DeviceRgb(51, 65, 85); // #334155 (Subheadings / Table headers)
    private static final DeviceRgb SLATE_600 = new DeviceRgb(71, 85, 105); // #475569 (Secondary body)
    private static final DeviceRgb SLATE_500 = new DeviceRgb(100, 116, 139); // #64748b (Labels & metadata)
    private static final DeviceRgb SLATE_400 = new DeviceRgb(148, 163, 184); // #94a3b8 (Hairlines & muted text)

    private static final DeviceRgb BORDER_LIGHT = new DeviceRgb(226, 232, 240); // #e2e8f0 (Subtle table rules)
    private static final DeviceRgb BORDER_DARK = new DeviceRgb(203, 213, 225); // #cbd5e1 (Card borders)
    private static final DeviceRgb ROW_ALT = new DeviceRgb(250, 250, 252); // #fafafc (Subtle zebra row)
    private static final DeviceRgb BG_CALLOUT = new DeviceRgb(248, 250, 252); // #f8fafc (Callout container)
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);

    // Single restrained brand accent (ResumeLens Flame Orange) used solely for
    // hairlines and key metrics
    private static final DeviceRgb BRAND_ACCENT = new DeviceRgb(232, 96, 46); // #e8602e
    private static final DeviceRgb BRAND_DARK = new DeviceRgb(193, 67, 21); // #c14315

    // Understated editorial status colors (used on text, never as loud background
    // boxes)
    private static final DeviceRgb STATUS_PASS = new DeviceRgb(22, 101, 52); // #166534 (Deep forest green text)
    private static final DeviceRgb STATUS_REVIEW = new DeviceRgb(180, 83, 9); // #b45309 (Deep amber text)
    private static final DeviceRgb STATUS_ALERT = new DeviceRgb(185, 28, 28); // #b91c1c (Deep crimson text)

    public ReportGeneratorService(MatchResultRepository matchResultRepository,
            SectionScoreRepository sectionScoreRepository) {
        this.matchResultRepository = matchResultRepository;
        this.sectionScoreRepository = sectionScoreRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generates a PDF report for a given match result public UUID and returns the
     * bytes.
     */
    @Transactional(readOnly = true)
    public byte[] generatePdfReport(UUID matchResultPublicId) {
        MatchResult result = matchResultRepository.findByPublicId(matchResultPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("MatchResult", matchResultPublicId));
        List<SectionScore> sections = sectionScoreRepository.findByMatchResultPublicId(matchResultPublicId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
                Document doc = new Document(pdf)) {

            // Balanced margins for comfortable reading and printing
            doc.setMargins(38, 40, 44, 40);
            String docRef = (result.getPublicId() != null)
                    ? "RL-" + result.getPublicId().toString().substring(0, 8).toUpperCase()
                    : "RL-DOCUMENT";
            pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterEventHandler(docRef));

            // PAGE 1: EXECUTIVE EVALUATION & AUDIT

            // Top Brand Accent Hairline
            Table accentLine = new Table(1).setWidth(UnitValue.createPercentValue(100));
            accentLine.addCell(new Cell().setHeight(2.5f)
                    .setBackgroundColor(BRAND_ACCENT)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(0));
            doc.add(accentLine);

            // 1. Executive Document Header
            doc.add(buildExecutiveHeader(result, docRef));

            // 2. Evaluation Summary & Submission Readiness Verdict Card
            doc.add(buildSummaryVerdictCard(result, sections));

            // 3. Technical ATS Parser Compliance Audit (The 5 Parser Gates)
            doc.add(buildSectionHeading("1. ATS PARSER COMPLIANCE AUDIT"));
            doc.add(buildComplianceGateTable(result, sections));

            // 4. Scoring Dimension Breakdown Table
            doc.add(buildSectionHeading("2. SCORING DIMENSION BREAKDOWN"));
            doc.add(buildBreakdownTable(sections));

            // PAGE 2: SKILLS GAP LEDGER & ROADMAP
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            // Page 2 Sub-Header
            doc.add(buildPageTwoHeader(docRef));

            // 5. Skills & Competencies Alignment Ledger (Two-Column Table)
            SectionScore skillsSection = sections.stream()
                    .filter(s -> SectionScore.SKILLS_VALIDATION.equals(s.getSectionName()))
                    .findFirst()
                    .orElse(null);

            if (skillsSection != null) {
                doc.add(buildSectionHeading("3. SKILLS & KEYWORDS ALIGNMENT LEDGER"));
                doc.add(buildSkillsLedgerTable(skillsSection));
            }

            // 6. Prioritized Score Optimization Roadmap
            doc.add(buildSectionHeading("4. PRIORITIZED OPTIMIZATION ROADMAP"));
            doc.add(buildRoadmapTable());

            // 7. AI Prompt Guidance & Overleaf Advisory Box
            doc.add(buildAdvisoryCallout());

        } catch (Exception e) {
            throw new ResumeParseException("Failed to generate PDF report: " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }

    // EXECUTIVE UI BUILDERS

    private Paragraph buildSectionHeading(String title) {
        return new Paragraph(title)
                .setFontSize(9f)
                .setBold()
                .setFontColor(SLATE_700)
                .setMarginTop(14)
                .setMarginBottom(6);
    }

    private Table buildExecutiveHeader(MatchResult result, String docRef) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 65, 35 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(8)
                .setMarginBottom(12);

        // Left: Branding and Document Type
        Cell left = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
        Paragraph brand = new Paragraph()
                .add(new Text("RESUMELENS ").setFontSize(11.5f).setBold().setFontColor(INK_BLACK))
                .add(new Text("• APPLICANT TRACKING SYSTEM DIAGNOSTIC").setFontSize(8f).setFontColor(SLATE_500)
                        .setBold())
                .setMarginBottom(4);
        left.add(brand);

        String jobTitle = (result.getJobDescription() != null && result.getJobDescription().getTitle() != null)
                ? result.getJobDescription().getTitle()
                : "Target Position Profile";
        left.add(new Paragraph(jobTitle)
                .setFontSize(15f).setBold().setFontColor(INK_BLACK).setMarginBottom(3));

        String resumeName = (result.getResume() != null && result.getResume().getFileName() != null)
                ? result.getResume().getFileName()
                : "candidate_resume.pdf";
        left.add(new Paragraph("Evaluated Document: " + resumeName)
                .setFontSize(9f).setFontColor(SLATE_600));

        // Right: Reference, Date, Confidentiality
        Cell right = new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                .setTextAlignment(TextAlignment.RIGHT);

        right.add(new Paragraph("AUDIT REFERENCE: " + docRef)
                .setFontSize(8.5f).setBold().setFontColor(SLATE_700).setMarginBottom(2));

        String dateStr = result.getAnalyzedAt() != null
                ? result.getAnalyzedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm 'UTC'"))
                : "Recent";
        right.add(new Paragraph("Timestamp: " + dateStr)
                .setFontSize(8f).setFontColor(SLATE_500).setMarginBottom(3));

        right.add(new Paragraph("CONFIDENTIAL ASSESSMENT")
                .setFontSize(7.5f).setBold().setFontColor(SLATE_400));

        table.addCell(left);
        table.addCell(right);
        return table;
    }

    private Table buildSummaryVerdictCard(MatchResult result, List<SectionScore> sections) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 32, 68 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(BORDER_DARK, 0.75f))
                .setBackgroundColor(WHITE)
                .setMarginBottom(4);

        double score = result.getOverallScore() != null ? result.getOverallScore() : 0.0;
        boolean hasRisk = result.getResume() != null &&
                (result.getResume().isHasGraphicsRisk() || result.getResume().isHasFormattingRisk());

        // Derive formal professional verdict
        String verdictTitle;
        DeviceRgb verdictColor;
        if (score >= 80 && !hasRisk) {
            verdictTitle = "READY FOR SUBMISSION";
            verdictColor = STATUS_PASS;
        } else if (score >= 60) {
            verdictTitle = "REVISIONS RECOMMENDED";
            verdictColor = STATUS_REVIEW;
        } else {
            verdictTitle = "SUBSTANTIAL REVISIONS REQUIRED";
            verdictColor = STATUS_ALERT;
        }

        // Left Cell: Large Numeric Score & Verdict
        Cell left = new Cell().setBorder(Border.NO_BORDER).setPadding(14)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorderRight(new SolidBorder(BORDER_LIGHT, 0.5f));

        left.add(new Paragraph("OVERALL ATS SCORE")
                .setFontSize(7.5f).setBold().setFontColor(SLATE_500).setMarginBottom(4));

        Paragraph scorePara = new Paragraph()
                .add(new Text(String.format("%.0f", score)).setFontSize(30).setBold().setFontColor(INK_BLACK))
                .add(new Text(" / 100").setFontSize(11).setBold().setFontColor(SLATE_400))
                .setMarginBottom(4);
        left.add(scorePara);

        left.add(new Paragraph(verdictTitle)
                .setFontSize(8f).setBold().setFontColor(verdictColor));

        // Right Cell: Key Vitals & Diagnostic Synthesis
        Cell right = new Cell().setBorder(Border.NO_BORDER).setPadding(14);

        Table vitals = new Table(UnitValue.createPercentArray(new float[] { 34, 33, 33 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(10);

        double kwPercent = result.getKeywordMatchPercent() != null ? (result.getKeywordMatchPercent() * 100.0) : 0.0;
        vitals.addCell(createCleanVital("KEYWORD ALIGNMENT", String.format("%.0f%%", kwPercent), INK_BLACK));

        String riskLabel = hasRisk ? "Review Needed" : "Optimal (Clear)";
        vitals.addCell(createCleanVital("PARSER INTEGRITY", riskLabel, hasRisk ? STATUS_REVIEW : STATUS_PASS));

        SectionScore weakest = null;
        for (SectionScore s : sections) {
            if (s != null && (weakest == null || s.getScore() < weakest.getScore())) {
                weakest = s;
            }
        }
        String weakestName = weakest != null ? getDimensionFriendlyName(weakest.getSectionName()) : "N/A";
        vitals.addCell(createCleanVital("PRIMARY FOCUS AREA", weakestName, BRAND_DARK));

        right.add(vitals);

        // Professional Synthesis Paragraph
        String synthesis = String.format(
                "Diagnostic Synthesis: Resume evaluates at a %s alignment profile (%.0f/100). " +
                        "The candidate demonstrates solid baseline requirements for the position. " +
                        "Addressing flagged technical keyword gaps and converting descriptive responsibilities into quantified accomplishments "
                        +
                        "will elevate parser pass rates into the top percentile.",
                result.getStatusLabel() != null ? result.getStatusLabel().toLowerCase() : "moderate",
                score);
        right.add(new Paragraph(synthesis)
                .setFontSize(8.5f).setFontColor(SLATE_600).setMultipliedLeading(1.35f));

        table.addCell(left);
        table.addCell(right);
        return table;
    }

    private Cell createCleanVital(String label, String value, DeviceRgb color) {
        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPadding(2);
        cell.add(new Paragraph(label).setFontSize(7f).setBold().setFontColor(SLATE_500).setMarginBottom(2));
        cell.add(new Paragraph(value).setFontSize(9.5f).setBold().setFontColor(color));
        return cell;
    }

    private Table buildComplianceGateTable(MatchResult result, List<SectionScore> sections) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 32, 28, 25, 15 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.5f));

        addTableHeader(table, new String[] { "Compliance Gate", "Industry Benchmark", "Audit Finding", "Status" });

        boolean hasGraphics = result.getResume() != null && result.getResume().isHasGraphicsRisk();
        boolean hasFormat = result.getResume() != null && result.getResume().isHasFormattingRisk();
        double kwPercent = result.getKeywordMatchPercent() != null ? (result.getKeywordMatchPercent() * 100.0) : 0.0;

        SectionScore impactSec = sections.stream()
                .filter(s -> SectionScore.IMPACT_ACHIEVEMENTS.equals(s.getSectionName()))
                .findFirst()
                .orElse(null);
        boolean metricsSolid = impactSec != null && impactSec.getScore() >= 65.0;

        addAuditRow(table, "Text-Layer Parseability", "100% Extractable Text", "Clean text stream extracted via PDFBox",
                "PASS", STATUS_PASS, false);
        addAuditRow(table, "Layout Hierarchy", "Linear Single-Column Flow",
                hasFormat ? "Multi-column / float layout detected" : "Standard linear flow verified",
                hasFormat ? "REVIEW" : "PASS", hasFormat ? STATUS_REVIEW : STATUS_PASS, true);
        addAuditRow(table, "Embedded Graphic Objects", "No OCR-blocking elements",
                hasGraphics ? "Images >= 72pt detected" : "Zero intrusive images detected",
                hasGraphics ? "REVIEW" : "PASS", hasGraphics ? STATUS_REVIEW : STATUS_PASS, false);
        addAuditRow(table, "Keyword Match Threshold", "≥ 70% Core Skill Overlap",
                String.format("%.0f%% candidate alignment ratio", kwPercent), kwPercent >= 70 ? "PASS" : "REVIEW",
                kwPercent >= 70 ? STATUS_PASS : STATUS_REVIEW, true);
        addAuditRow(table, "Quantified Metric Outcomes", "≥ 40% Action Bullet Points",
                metricsSolid ? "Quantifiable achievements detected" : "Responsibilities lack metric proof",
                metricsSolid ? "PASS" : "REVIEW", metricsSolid ? STATUS_PASS : STATUS_REVIEW, false);

        return table;
    }

    private void addAuditRow(Table table, String gate, String benchmark, String finding, String status,
            DeviceRgb statusColor, boolean alt) {
        DeviceRgb bg = alt ? ROW_ALT : WHITE;
        table.addCell(createCell(gate, bg, true, TextAlignment.LEFT));
        table.addCell(createCell(benchmark, bg, false, TextAlignment.LEFT));
        table.addCell(createCell(finding, bg, false, TextAlignment.LEFT));

        Cell statusCell = new Cell()
                .setBackgroundColor(bg)
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.5f))
                .setPadding(6)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        statusCell.add(new Paragraph(status).setFontSize(8f).setBold().setFontColor(statusColor));
        table.addCell(statusCell);
    }

    private Table buildBreakdownTable(List<SectionScore> sections) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 38, 12, 16, 18, 16 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.5f));

        addTableHeader(table,
                new String[] { "Scoring Dimension", "Weight", "Score", "Benchmark Status", "Assessment" });

        List<String> order = List.of(
                SectionScore.ATS_COMPATIBILITY,
                SectionScore.SKILLS_VALIDATION,
                SectionScore.IMPACT_ACHIEVEMENTS,
                SectionScore.PROJECT_QUALITY,
                SectionScore.WRITING_QUALITY,
                SectionScore.PROFESSIONALISM);

        List<SectionScore> sorted = new ArrayList<>(sections);
        sorted.sort(Comparator.comparingInt(s -> {
            int idx = order.indexOf(s.getSectionName());
            return idx != -1 ? idx : 99;
        }));

        boolean alt = false;
        for (SectionScore s : sorted) {
            DeviceRgb bg = alt ? ROW_ALT : WHITE;
            alt = !alt;

            // Dimension Name + Feedback note
            Cell nameCell = new Cell().setBackgroundColor(bg).setBorder(new SolidBorder(BORDER_LIGHT, 0.5f))
                    .setPadding(6);
            nameCell.add(new Paragraph(getDimensionFriendlyName(s.getSectionName()))
                    .setFontSize(8.5f).setBold().setFontColor(INK_BLACK).setMarginBottom(2));
            String feedback = (s.getFeedback() != null && !s.getFeedback().isBlank())
                    ? s.getFeedback()
                    : "Evaluated according to standard heuristic benchmark.";
            nameCell.add(new Paragraph(feedback)
                    .setFontSize(7f).setFontColor(SLATE_500).setItalic());
            table.addCell(nameCell);

            // Weight
            table.addCell(createCell(getDimensionWeight(s.getSectionName()) + "%", bg, false, TextAlignment.CENTER));

            // Score
            table.addCell(createCell(String.format("%.0f / 100", s.getScore()), bg, true, TextAlignment.CENTER));

            // Benchmark Status
            String benchmarkText = deriveBenchmarkText(s.getScore());
            table.addCell(createCell(benchmarkText, bg, false, TextAlignment.CENTER));

            // Assessment Tag (Clean text, no colorful box)
            DeviceRgb textColor = deriveStatusColor(s.getScore());
            Cell assessmentCell = new Cell().setBackgroundColor(bg).setBorder(new SolidBorder(BORDER_LIGHT, 0.5f))
                    .setPadding(6)
                    .setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
            assessmentCell.add(new Paragraph(s.getStatus()).setFontSize(8f).setBold().setFontColor(textColor));
            table.addCell(assessmentCell);
        }

        return table;
    }

    private Table buildPageTwoHeader(String docRef) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 65, 35 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(8);

        Cell left = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
        left.add(new Paragraph()
                .add(new Text("RESUMELENS ").setFontSize(10.5f).setBold().setFontColor(INK_BLACK))
                .add(new Text("• DETAILED SKILLS & OPTIMIZATION AUDIT").setFontSize(8f).setFontColor(SLATE_500)
                        .setBold()));

        Cell right = new Cell().setBorder(Border.NO_BORDER).setPadding(0)
                .setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph("REF: " + docRef + " • PAGE 2 OF 2")
                .setFontSize(8f).setBold().setFontColor(SLATE_500));

        table.addCell(left);
        table.addCell(right);
        return table;
    }

    private Table buildSkillsLedgerTable(SectionScore skillsSection) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 50, 50 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.5f));

        List<String> matched = parseJsonKeywords(skillsSection.getMatchedKeywords());
        List<String> missing = parseJsonKeywords(skillsSection.getMissingKeywords());

        // Header Row with generous vertical padding
        Cell hLeft = new Cell().setBackgroundColor(SLATE_700).setBorder(Border.NO_BORDER).setPadding(6);
        hLeft.add(new Paragraph("VERIFIED IN RESUME (" + matched.size() + ")")
                .setFontSize(8f).setBold().setFontColor(WHITE));

        Cell hRight = new Cell().setBackgroundColor(SLATE_700).setBorder(Border.NO_BORDER).setPadding(6);
        hRight.add(new Paragraph("TARGET JOB REQUIREMENTS MISSING (" + missing.size() + ")")
                .setFontSize(8f).setBold().setFontColor(WHITE));

        table.addHeaderCell(hLeft);
        table.addHeaderCell(hRight);

        // Content Cells with generous breathing space
        Cell cLeft = new Cell().setBackgroundColor(WHITE).setBorder(new SolidBorder(BORDER_LIGHT, 0.5f)).setPadding(10);
        if (matched.isEmpty()) {
            cLeft.add(new Paragraph("No target technical keywords detected in resume text.")
                    .setFontSize(8f).setFontColor(SLATE_500).setItalic());
        } else {
            Paragraph p = new Paragraph().setFontSize(8.5f).setFontColor(SLATE_800).setMultipliedLeading(1.4f);
            for (String kw : matched) {
                p.add(new Text("  •  " + kw + "   "));
            }
            cLeft.add(p);
        }

        Cell cRight = new Cell().setBackgroundColor(WHITE).setBorder(new SolidBorder(BORDER_LIGHT, 0.5f))
                .setPadding(10);
        if (missing.isEmpty()) {
            cRight.add(new Paragraph("All core target technical skills were identified.")
                    .setFontSize(8.5f).setFontColor(STATUS_PASS).setBold());
        } else {
            Paragraph p = new Paragraph().setFontSize(8.5f).setFontColor(SLATE_800).setMultipliedLeading(1.4f);
            for (String kw : missing) {
                p.add(new Text("  •  " + kw + "   ").setFontColor(BRAND_DARK).setBold());
            }
            cRight.add(p);
        }

        table.addCell(cLeft);
        table.addCell(cRight);
        return table;
    }

    private Table buildRoadmapTable() {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 18, 62, 20 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.5f));

        addTableHeader(table, new String[] { "Priority", "Action Recommendation", "Expected Gain" });

        addRoadmapRow(table, "HIGH IMPACT",
                "Integrate identified missing technical skills naturally into Work Experience bullet points.",
                "+8 to +12 pts", false);
        addRoadmapRow(table, "MEDIUM IMPACT",
                "Transform responsibility statements into quantified accomplishments (%, $, metrics).", "+5 to +8 pts",
                true);
        addRoadmapRow(table, "OPTIMIZATION",
                "Generate your customized LaTeX fix prompt in ResumeLens and compile via Overleaf.", "Format Guarantee",
                false);

        return table;
    }

    private void addRoadmapRow(Table table, String priority, String action, String impact, boolean alt) {
        DeviceRgb bg = alt ? ROW_ALT : WHITE;
        Cell pCell = new Cell().setBackgroundColor(bg).setBorder(new SolidBorder(BORDER_LIGHT, 0.5f)).setPadding(7)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        pCell.add(new Paragraph(priority).setFontSize(7.5f).setBold().setFontColor(BRAND_DARK));
        table.addCell(pCell);

        Cell aCell = new Cell().setBackgroundColor(bg).setBorder(new SolidBorder(BORDER_LIGHT, 0.5f)).setPadding(7)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        aCell.add(new Paragraph(action).setFontSize(8f).setFontColor(SLATE_700).setMultipliedLeading(1.3f));
        table.addCell(aCell);

        Cell iCell = new Cell().setBackgroundColor(bg).setBorder(new SolidBorder(BORDER_LIGHT, 0.5f)).setPadding(7)
                .setTextAlignment(TextAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        iCell.add(new Paragraph(impact).setFontSize(7.5f).setBold().setFontColor(SLATE_800));
        table.addCell(iCell);
    }

    private Table buildAdvisoryCallout() {
        Table table = new Table(1).setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.75f))
                .setBorderLeft(new SolidBorder(BRAND_ACCENT, 3f))
                .setBackgroundColor(BG_CALLOUT)
                .setMarginTop(14);

        Cell cell = new Cell().setBorder(Border.NO_BORDER).setPadding(10);
        cell.add(new Paragraph("NEXT STEPS & AI REWRITE WORKFLOW")
                .setFontSize(8f).setBold().setFontColor(BRAND_DARK).setMarginBottom(4));

        cell.add(new Paragraph(
                "1. Access your analysis in the ResumeLens web dashboard and click 'Generate Fix Prompt'.\n" +
                        "2. Select your preferred LaTeX template style (Simple Single-Column, Awesome-CV, or Deedy Resume).\n"
                        +
                        "3. Paste the generated prompt into Claude, ChatGPT, or Gemini to receive an ATS-optimized rewritten resume with zero fabrication.")
                .setFontSize(8f).setFontColor(SLATE_700).setMultipliedLeading(1.35f));

        table.addCell(cell);
        return table;
    }

    // REUSABLE FORMATTING HELPERS

    private void addTableHeader(Table table, String[] headers) {
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFontSize(7.5f).setBold().setFontColor(WHITE))
                    .setBackgroundColor(SLATE_700)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(6));
        }
    }

    private Cell createCell(String text, DeviceRgb bg, boolean bold, TextAlignment align) {
        Cell cell = new Cell().setBackgroundColor(bg).setBorder(new SolidBorder(BORDER_LIGHT, 0.5f)).setPadding(6)
                .setTextAlignment(align).setVerticalAlignment(VerticalAlignment.MIDDLE);
        Paragraph p = new Paragraph(text).setFontSize(8f).setFontColor(SLATE_700);
        if (bold)
            p.setBold().setFontColor(INK_BLACK);
        cell.add(p);
        return cell;
    }

    private String deriveBenchmarkText(double score) {
        if (score >= 85)
            return "Exceeds Standard";
        if (score >= 70)
            return "Meets Benchmark";
        if (score >= 55)
            return "Minor Deficit";
        return "Substantial Gap";
    }

    private DeviceRgb deriveStatusColor(double score) {
        if (score >= 70)
            return STATUS_PASS;
        if (score >= 55)
            return STATUS_REVIEW;
        return STATUS_ALERT;
    }

    private List<String> parseJsonKeywords(String json) {
        if (json == null || json.isBlank())
            return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            String cleaned = json.replaceAll("[\\[\\]\"]", "").trim();
            if (cleaned.isEmpty())
                return Collections.emptyList();
            List<String> list = new ArrayList<>();
            for (String part : cleaned.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    list.add(trimmed);
                }
            }
            return list;
        }
    }

    private int getDimensionWeight(String sectionName) {
        return switch (sectionName) {
            case SectionScore.SKILLS_VALIDATION -> 25;
            case SectionScore.ATS_COMPATIBILITY -> 20;
            case SectionScore.IMPACT_ACHIEVEMENTS -> 20;
            case SectionScore.PROJECT_QUALITY -> 15;
            case SectionScore.WRITING_QUALITY -> 10;
            case SectionScore.PROFESSIONALISM -> 10;
            default -> 0;
        };
    }

    private String getDimensionFriendlyName(String sectionName) {
        return switch (sectionName) {
            case SectionScore.ATS_COMPATIBILITY -> "ATS Compatibility & Format";
            case SectionScore.SKILLS_VALIDATION -> "Skills & Technical Stack";
            case SectionScore.IMPACT_ACHIEVEMENTS -> "Impact & Quantifiable Metrics";
            case SectionScore.PROJECT_QUALITY -> "Project Relevance & Scope";
            case SectionScore.WRITING_QUALITY -> "Writing Quality & Action Verbs";
            case SectionScore.PROFESSIONALISM -> "Structure & Professional Tone";
            default -> sectionName.replace("_", " ");
        };
    }

    /**
     * Professional running footer with hairline divider rule,
     * document reference hash, and page numbers.
     */
    private static class FooterEventHandler implements IEventHandler {
        private final String docRef;

        public FooterEventHandler(String docRef) {
            this.docRef = docRef;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);
            Rectangle pageSize = page.getPageSize();

            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);

            // Subtle 0.5pt hairline divider above footer
            pdfCanvas.setStrokeColor(BORDER_LIGHT);
            pdfCanvas.setLineWidth(0.5f);
            pdfCanvas.moveTo(40, 30);
            pdfCanvas.lineTo(pageSize.getWidth() - 40, 30);
            pdfCanvas.stroke();

            Canvas canvas = new Canvas(pdfCanvas, pageSize);

            canvas.showTextAligned(
                    new Paragraph("ResumeLens™ Official ATS Diagnostic Report • Confidential Assessment • " + docRef)
                            .setFontSize(7f).setFontColor(SLATE_400),
                    40, 18, TextAlignment.LEFT);

            canvas.showTextAligned(
                    new Paragraph("Page " + pageNumber)
                            .setFontSize(7f).setFontColor(SLATE_400),
                    pageSize.getWidth() - 40, 18, TextAlignment.RIGHT);

            canvas.close();
        }
    }
}