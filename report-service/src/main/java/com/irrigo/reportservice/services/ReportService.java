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

        List<IrrigationTaskDTO> tasks =
                taskServiceClient.getRecentTasks(email);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        double totalWater = tasks.stream()
                .mapToDouble(t -> t.getWaterAmount() != null
                        ? t.getWaterAmount()
                        : 0.0)
                .sum();

        double totalSurface = tasks.stream()
                .mapToDouble(t -> t.getSurface() != null
                        ? t.getSurface()
                        : 0.0)
                .sum();

        double debitMoyen = tasks.stream()
                .mapToDouble(t -> t.getDebit() != null
                        ? t.getDebit()
                        : 0.0)
                .average()
                .orElse(0.0);

        Map<String, Double> statsParCulture = tasks.stream()
                .filter(t -> t.getCrop() != null)
                .collect(Collectors.groupingBy(
                        IrrigationTaskDTO::getCrop,
                        Collectors.summingDouble(
                                t -> t.getWaterAmount() != null
                                        ? t.getWaterAmount()
                                        : 0.0
                        )
                ));

        Map<String, List<IrrigationTaskDTO>> tasksParCulture =
                tasks.stream()
                        .collect(Collectors.groupingBy(
                                t -> t.getCrop() != null
                                        ? t.getCrop()
                                        : "Non définie"
                        ));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {

            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);

            Font headerFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            Font tableHeaderFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10,
                            Color.WHITE
                    );

            Font normalFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title =
                    new Paragraph("Rapport d'activité Irrigo", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);

            document.add(title);

            document.add(new Paragraph(
                    "Destinataire : " + email,
                    headerFont));

            document.add(new Paragraph(
                    "Total des tâches effectuées : " + tasks.size(),
                    normalFont));

            document.add(new Paragraph(
                    "Surface totale irriguée : "
                            + String.format("%.2f m²", totalSurface),
                    normalFont));

            document.add(new Paragraph(
                    "Consommation totale d'eau : "
                            + String.format("%.2f L", totalWater),
                    normalFont));

            document.add(new Paragraph(
                    "Débit moyen global : "
                            + String.format("%.2f L/min", debitMoyen),
                    normalFont));

            document.add(new Paragraph("\n"));

            document.add(new Paragraph(
                    "Résumé de la consommation d'eau par culture (L) :",
                    headerFont));

            if (statsParCulture.isEmpty()) {

                document.add(new Paragraph(
                        "- Aucune donnée de culture disponible",
                        normalFont));

            } else {

                for (Map.Entry<String, Double> entry :
                        statsParCulture.entrySet()) {

                    document.add(new Paragraph(
                            "- "
                                    + entry.getKey()
                                    + " : "
                                    + String.format("%.2f",
                                    entry.getValue())
                                    + " L",
                            normalFont));
                }
            }

            document.add(new Paragraph("\n"));

            document.add(new Paragraph(
                    "Détail complet des activités par culture :",
                    headerFont));

            document.add(new Paragraph("\n"));

            for (Map.Entry<String, List<IrrigationTaskDTO>> entry :
                    tasksParCulture.entrySet()) {

                String culture = entry.getKey();
                List<IrrigationTaskDTO> cultureTasks =
                        entry.getValue();

                Paragraph cultureTitle =
                        new Paragraph(
                                "Culture : " + culture,
                                headerFont);

                cultureTitle.setSpacingBefore(10);
                cultureTitle.setSpacingAfter(10);

                document.add(cultureTitle);

                PdfPTable table = new PdfPTable(7);

                table.setWidthPercentage(100);

                table.setWidths(
                        new float[]{
                                2.5f,
                                2.5f,
                                1.5f,
                                1.5f,
                                1.5f,
                                1.5f,
                                1.5f
                        });

                addTableHeader(table, tableHeaderFont);

                for (IrrigationTaskDTO task : cultureTasks) {

                    table.addCell(new PdfPCell(
                            new Phrase(
                                    task.getName() != null
                                            ? task.getName()
                                            : "-",
                                    normalFont)));

                    String dateStr =
                            task.getStartTime() != null
                                    ? task.getStartTime()
                                    .format(formatter)
                                    : "-";

                    table.addCell(new PdfPCell(
                            new Phrase(dateStr, normalFont)));

                    table.addCell(new PdfPCell(
                            new Phrase(
                                    task.getCrop() != null
                                            ? task.getCrop()
                                            : "-",
                                    normalFont)));

                    table.addCell(new PdfPCell(
                            new Phrase(
                                    task.getStatus() != null
                                            ? task.getStatus()
                                            : "-",
                                    normalFont)));

                    String water =
                            task.getWaterAmount() != null
                                    ? String.format(
                                    "%.2f L",
                                    task.getWaterAmount())
                                    : "0 L";

                    table.addCell(new PdfPCell(
                            new Phrase(water, normalFont)));

                    String debit =
                            task.getDebit() != null
                                    ? String.format(
                                    "%.2f L/min",
                                    task.getDebit())
                                    : "0 L/min";

                    table.addCell(new PdfPCell(
                            new Phrase(debit, normalFont)));

                    String surface =
                            task.getSurface() != null
                                    ? String.format(
                                    "%.2f m²",
                                    task.getSurface())
                                    : "0 m²";

                    table.addCell(new PdfPCell(
                            new Phrase(surface, normalFont)));
                }

                document.add(table);
                document.add(new Paragraph("\n"));
            }

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addTableHeader(
            PdfPTable table,
            Font font) {

        String[] headers = {
                "Nom de la Farm",
                "Date",
                "Culture",
                "Statut",
                "Eau (L)",
                "Débit",
                "Surface"
        };

        for (String columnTitle : headers) {

            PdfPCell header = new PdfPCell();

            header.setBackgroundColor(
                    new Color(46, 139, 87));

            header.setPadding(5);

            header.setPhrase(
                    new Phrase(columnTitle, font));

            header.setHorizontalAlignment(
                    Element.ALIGN_CENTER);

            table.addCell(header);
        }
    }
}