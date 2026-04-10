package com.irrigo.reportservice.controllers;

import com.irrigo.reportservice.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder; // Import indispensable
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * L'URL devient simplement /api/reports/download
     * L'email est extrait du Token JWT envoyé dans le Header Authorization
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport() {
        // Extraction de l'email de l'utilisateur connecté depuis le SecurityContext
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Appel du service avec l'email sécurisé
        byte[] pdf = reportService.generatePdfReport(currentUserEmail);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport_irrigo.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}