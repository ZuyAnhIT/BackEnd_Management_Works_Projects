package com.quanlyduan.project_manager_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quanlyduan.project_manager_api.dto.response.ApiResponse;
import com.quanlyduan.project_manager_api.dto.response.GlobalSearchResponse;
import com.quanlyduan.project_manager_api.service.GlobalSearchService;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final GlobalSearchService globalSearchService;

    public SearchController(GlobalSearchService globalSearchService) {
        this.globalSearchService = globalSearchService;
    }

    /**
     * Endpoint for global search navbar.
     * Expects a minimum of 2 characters.
     * URL Example: /api/v1/search/global?q=WEB-12
     */
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<GlobalSearchResponse>> searchGlobal(@RequestParam("q") String keyword) {
        GlobalSearchResponse data = globalSearchService.searchAll(keyword);
        return ResponseEntity.ok(ApiResponse.success("Global search completed successfully", data));
    }
}
