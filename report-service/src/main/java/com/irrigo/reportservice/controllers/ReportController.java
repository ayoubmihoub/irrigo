package com.irrigo.reportservice.controllers;

import com.irrigo.reportservice.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports") // Chemin de base
public class ReportController {

    @Autowired
    private ReportService reportService;

    // L'email fait partie du chemin ( /{email} )
    @GetMapping("/download/{email}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String email) {
        byte[] pdf = reportService.generatePdfReport(email);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
