package com.tspevo.service;

import com.tspevo.config.TspAlgorithmConfig;
import com.tspevo.model.City;
import com.tspevo.model.TspResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvolutionaryAlgorithmTest {
    
    private EvolutionaryAlgorithm evolutionaryAlgorithm;
    private TspAlgorithmConfig config;
    private List<City> testCities;
    
    @BeforeEach
    void setUp() {
        config = new TspAlgorithmConfig();
        config.setPopulationSize(50);
        config.setMaxGenerations(100);
        config.setMutationRate(0.02);
        config.setCrossoverRate(0.8);
        config.setTournamentSize(5);
        config.setProgressInterval(10);
        
        evolutionaryAlgorithm = new EvolutionaryAlgorithm();
        evolutionaryAlgorithm.setConfig(config);
        
        testCities = new ArrayList<>();
        testCities.add(new City("A", 0.0, 0.0));
        testCities.add(new City("B", 0.0, 10.0));
        testCities.add(new City("C", 10.0, 0.0));
        testCities.add(new City("D", 10.0, 10.0));
    }
    
    @Test
    void solve_shouldReturnValidResult() {
        TspResult result = evolutionaryAlgorithm.solve(testCities);
        
        assertNotNull(result);
        assertNotNull(result.getOptimalRoute());
        assertEquals(testCities.size(), result.getOptimalRoute().size());
        assertTrue(result.getTotalDistance() > 0);
        assertTrue(result.getGenerations() > 0);
        assertTrue(result.getExecutionTimeMs() >= 0);
    }
    
    @Test
    void solve_withEmptyList_shouldReturnEmptyResult() {
        List<City> emptyCities = new ArrayList<>();
        TspResult result = evolutionaryAlgorithm.solve(emptyCities);
        
        assertNotNull(result);
        assertNotNull(result.getOptimalRoute());
        assertEquals(0, result.getOptimalRoute().size());
    }
    
    @Test
    void solve_withSingleCity_shouldReturnCity() {
        List<City> singleCity = new ArrayList<>();
        singleCity.add(new City("A", 0.0, 0.0));
        
        TspResult result = evolutionaryAlgorithm.solve(singleCity);
        
        assertNotNull(result);
        assertEquals(1, result.getOptimalRoute().size());
    }
    
    @Test
    void solve_withTwoCities_shouldReturnTwoCities() {
        List<City> twoCities = new ArrayList<>();
        twoCities.add(new City("A", 0.0, 0.0));
        twoCities.add(new City("B", 10.0, 10.0));
        
        TspResult result = evolutionaryAlgorithm.solve(twoCities);
        
        assertNotNull(result);
        assertEquals(2, result.getOptimalRoute().size());
    }
    
    @Test
    void solve_shouldContainAllCities() {
        TspResult result = evolutionaryAlgorithm.solve(testCities);
        
        List<City> route = result.getOptimalRoute();
        assertEquals(testCities.size(), route.size());
        
        for (City city : testCities) {
            assertTrue(route.contains(city), "Route should contain city: " + city.getName());
        }
    }
    
    @Test
    void solve_withProgressCallback_shouldCallCallback() {
        final int[] callbackCount = {0};
        
        evolutionaryAlgorithm.solve(testCities, (c1, c2) -> haversineDistance(c1, c2), progress -> {
            callbackCount[0]++;
            assertNotNull(progress.getBestRoute());
            assertTrue(progress.getDistance() >= 0);
            assertTrue(progress.getGeneration() > 0);
        });
        
        assertTrue(callbackCount[0] > 0, "Callback should have been called");
    }
    
    private double haversineDistance(City c1, City c2) {
        final double R = 6371.0;
        double lat1 = Math.toRadians(c1.getX());
        double lat2 = Math.toRadians(c2.getX());
        double lon1 = Math.toRadians(c1.getY());
        double lon2 = Math.toRadians(c2.getY());
        
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
