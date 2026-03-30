package com.quanlyduan.project_manager_api.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service ha tang quan ly viec luu tru va truy xuat tep tin (File Storage).
 * Chiu trach nhiem thuc hien cac thao tac vat ly voi file tren o dia hoac Cloud Storage.
 */
public interface FileStorageService {

    // ======================================================
    // 1. LUU TRU TEP TIN (FILE STORAGE OPERATIONS)
    // ======================================================

    /**
     * Thuc hien luu tep tin tu Client vao he thong luu tru theo tung phan loai.
     * Thuong dung cho cac loai tai nguyen nhu: Logo, Avatar, Cover Image hoac Tai lieu dinh kem.
     * * @param file Tep tin tai len tu phia nguoi dung (MultipartFile)
     * @param folderName Ten thu muc phan loai (vi du: "avatars", "company-logos")
     * @return Duong dan tuong doi hoac Key dinh danh tep de luu vao Co so du lieu
     */
    String storeFile(MultipartFile file, String folderName);

    // ======================================================
    // 2. CAC THAO TAC MO RONG (EXTENDED OPERATIONS - PLANNED)
    // ======================================================
    
    // (Ke hoach bo sung: deleteFile, loadFileAsResource, validateFileType)
}