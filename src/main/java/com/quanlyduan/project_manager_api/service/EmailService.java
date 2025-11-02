package com.quanlyduan.project_manager_api.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}