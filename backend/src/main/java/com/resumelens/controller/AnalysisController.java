package com.resumelens.controller;

import com.resumelens.dto.request.AnalysisRequest;
import com.resumelens.dto.response.AnalysisResponse;
import com.resumelens.dto.response.ApiResponse;
import com.resumelens.service.AnalysisService;
import com.resumelens.service.PromptBuilderService;
import com.resumelens.service.ReportGeneratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ReportGeneratorService reportGeneratorService;
    private final PromptBuilderService promptBuilderService;

    public AnalysisController(AnalysisService analysisService,
                               ReportGeneratorService reportGeneratorService,
                               PromptBuilderService promptBuilderService) {
        this.analysisService = analysisService;
        this.reportGeneratorService = reportGeneratorService;
        this.promptBuilderService = promptBuilderService;
    }

    @PostMapping("/match")
    public ResponseEntity<ApiResponse<AnalysisResponse>> match(
            @Valid @RequestBody AnalysisRequest request) {
        AnalysisResponse response = analysisService.runAnalysis(
            request.resumeId(), request.jobDescriptionId(),
            request.rawJobDescription(), request.jobTitle());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{matchResultId}")
    public ResponseEntity<ApiResponse<AnalysisResponse>> getResult(
            @PathVariable Long matchResultId) {
        return ResponseEntity.ok(ApiResponse.success(analysisService.getResult(matchResultId)));
    }

    @GetMapping("/{matchResultId}/report")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long matchResultId) {
        byte[] pdf = reportGeneratorService.generatePdfReport(matchResultId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"ats-report-" + matchResultId + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    @GetMapping("/{matchResultId}/fix-prompt")
    public ResponseEntity<ApiResponse<String>> getFixPrompt(
            @PathVariable Long matchResultId,
            @RequestParam(value = "templateStyle", defaultValue = "Simple Single-Column") String templateStyle) {
        String prompt = promptBuilderService.buildFixPrompt(matchResultId, templateStyle);
        return ResponseEntity.ok(ApiResponse.success(prompt));
    }
}