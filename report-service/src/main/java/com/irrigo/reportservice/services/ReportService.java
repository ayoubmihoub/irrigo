package com.irrigo.reportservice.services;

import com.irrigo.reportservice.clients.TaskServiceClient;
import com.irrigo.reportservice.dto.IrrigationTaskDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private TaskServiceClient taskServiceClient;

    public byte[] generatePdfReport(String email) {
        // 1. Récupération des données
        List<IrrigationTaskDTO> tasks = taskServiceClient.getRecentTasks(email);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // 2. Calculs des Statistiques
        double totalWater = tasks.stream()
                .mapToDouble(t -> t.getWaterAmount() != null ? t.getWaterAmount() : 0.0).sum();

        double totalSurface = tasks.stream()
                .mapToDouble(t -> t.getSurface() != null ? t.getSurface() : 0.0).sum();

        double debitMoyen = tasks.stream()
                .mapToDouble(t -> t.getDebit() != null ? t.getDebit() : 0.0).average().orElse(0.0);

        Map<String, Double> statsParCulture = tasks.stream()
                .filter(t -> t.getCrop() != null)
                .collect(Collectors.groupingBy(
                        IrrigationTaskDTO::getCrop,
                        Collectors.summingDouble(t -> t.getWaterAmount() != null ? t.getWaterAmount() : 0.0)
                ));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate()); // Mode paysage

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Polices
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // --- SECTION 1 : TITRE ---
            Paragraph title = new Paragraph("Rapport d'activité Irrigo", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // --- SECTION 2 : INDICATEURS CLÉS (KPIs) ---
            document.add(new Paragraph("Destinataire : " + email, headerFont));
            document.add(new Paragraph("Total des tâches effectuées : " + tasks.size(), normalFont));
            document.add(new Paragraph("Surface totale irriguée : " + String.format("%.2f m²", totalSurface), normalFont));
            document.add(new Paragraph("Consommation totale d'eau : " + String.format("%.2f L", totalWater), normalFont));
            document.add(new Paragraph("Débit moyen global : " + String.format("%.2f L/min", debitMoyen), normalFont));
            document.add(new Paragraph("\n"));

            // --- SECTION 3 : RÉSUMÉ PAR CULTURE (Celui que tu voulais garder !) ---
            document.add(new Paragraph("Résumé de la consommation d'eau par culture (L) :", headerFont));
            if (statsParCulture.isEmpty()) {
                document.add(new Paragraph("- Aucune donnée de culture disponible", normalFont));
            } else {
                for (Map.Entry<String, Double> entry : statsParCulture.entrySet()) {
                    document.add(new Paragraph("- " + entry.getKey() + " : " + String.format("%.2f", entry.getValue()) + " L", normalFont));
                }
            }
            document.add(new Paragraph("\n"));

            // --- SECTION 4 : TABLEAU DÉTAILLÉ ---
            document.add(new Paragraph("Détail complet des activités :", headerFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(7); // 7 colonnes
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f});

            addTableHeader(table, tableHeaderFont);

            for (IrrigationTaskDTO task : tasks) {
                table.addCell(new PdfPCell(new Phrase(task.getName() != null ? task.getName() : "-", normalFont)));

                // Date formatée
                String dateStr = task.getStartTime() != null ? task.getStartTime().format(formatter) : "-";
                table.addCell(new PdfPCell(new Phrase(dateStr, normalFont)));

                table.addCell(new PdfPCell(new Phrase(task.getCrop() != null ? task.getCrop() : "-", normalFont)));
                table.addCell(new PdfPCell(new Phrase(task.getStatus() != null ? task.getStatus() : "-", normalFont)));

                // Eau en L
                String water = task.getWaterAmount() != null ? String.format("%.2f L", task.getWaterAmount()) : "0 L";
                table.addCell(new PdfPCell(new Phrase(water, normalFont)));

                // Débit
                String debit = task.getDebit() != null ? String.format("%.2f L/min", task.getDebit()) : "0 L/min";
                table.addCell(new PdfPCell(new Phrase(debit, normalFont)));

                // Surface
                String surface = task.getSurface() != null ? String.format("%.2f m²", task.getSurface()) : "0 m²";
                table.addCell(new PdfPCell(new Phrase(surface, normalFont)));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, Font font) {
        String[] headers = {"Nom de la tâche", "Date", "Culture", "Statut", "Eau (L)", "Débit", "Surface"};
        for (String columnTitle : headers) {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(new Color(46, 139, 87)); // Vert forêt
            header.setPadding(5);
            header.setPhrase(new Phrase(columnTitle, font));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(header);
        }
    }
}