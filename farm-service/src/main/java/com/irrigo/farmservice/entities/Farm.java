package com.irrigo.farmservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "farms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String crop;
    private String location; // Nom de la ville ou description
    private Double surface;

    @Enumerated(EnumType.STRING)
    private ESoilType soilProfile;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String image; // Stockage en Base64 ou URL

    private String userEmail; // Pour la sécurité

    @Column(columnDefinition = "TEXT")
    private String parcelJson; // Le polygone GeoJSON de la carte
}