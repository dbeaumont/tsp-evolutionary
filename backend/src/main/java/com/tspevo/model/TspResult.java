package com.tspevo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TspResult {
    private List<City> optimalRoute;
    private double totalDistance;
    private int generations;
    private long executionTimeMs;
}
