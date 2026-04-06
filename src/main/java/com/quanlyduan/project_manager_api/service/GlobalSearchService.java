package com.quanlyduan.project_manager_api.service;

import com.quanlyduan.project_manager_api.dto.response.GlobalSearchResponse;

public interface GlobalSearchService {

    /**
     * Perform a global search across Projects, Epics, and Tasks.
     * The results are restricted to items belonging to projects the current user is a member of.
     *
     * @param keyword The search keyword.
     * @return A categorized list of search results.
     */
    GlobalSearchResponse searchAll(String keyword);
}