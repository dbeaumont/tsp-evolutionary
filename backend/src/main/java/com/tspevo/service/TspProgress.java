package com.tspevo.service;

import com.tspevo.model.City;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TspProgress {
    private int generation;
    private List<City> bestRoute;
    private double distance;
}
