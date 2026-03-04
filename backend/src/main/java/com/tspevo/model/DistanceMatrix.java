package com.tspevo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "distance_matrix", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source_city_id", "target_city_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceMatrix {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "source_city_id", nullable = false)
    private City sourceCity;
    
    @ManyToOne
    @JoinColumn(name = "target_city_id", nullable = false)
    private City targetCity;
    
    @Column(nullable = false)
    private double distanceKm;
    
    public DistanceMatrix(City sourceCity, City targetCity, double distanceKm) {
        this.sourceCity = sourceCity;
        this.targetCity = targetCity;
        this.distanceKm = distanceKm;
    }
}
