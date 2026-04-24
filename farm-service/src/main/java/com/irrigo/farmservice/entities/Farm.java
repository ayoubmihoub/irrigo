package com.irrigo.farmservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "farms")
@Data // <--- CRUCIAL : Génère automatiquement setIrrigationActive()
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String crop;
    private String location;
    private Double surface;

    @Enumerated(EnumType.STRING)
    private ESoilType soilProfile;

    // --- CHAMPS IOT MANQUANTS ---

    /**
     * Stocke l'humidité réelle envoyée par l'ESP32 (0-100)
     */
    private Integer currentMoisture;

    /**
     * Flag utilisé par l'ESP32 pour savoir s'il doit allumer la LED.
     * C'est ce champ qui génère la méthode setIrrigationActive().
     */
    private boolean irrigationActive = false;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String image;

    private String userEmail;

    @Column(columnDefinition = "TEXT")
    private String parcelJson;
}