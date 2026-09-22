package com.resumelens.controller;

import com.resumelens.dto.response.ApiResponse;
import com.resumelens.dto.response.HistoryItemResponse;
import com.resumelens.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HistoryItemResponse>>> getHistory(
            Authentication authentication) {
        List<HistoryItemResponse> history = historyService.getHistoryForUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}