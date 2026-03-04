package com.tspevo.service;

import com.tspevo.config.TspAlgorithmConfig;
import com.tspevo.model.City;
import com.tspevo.model.TspResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Service
public class EvolutionaryAlgorithm {
    
    private static final Logger logger = LoggerFactory.getLogger(EvolutionaryAlgorithm.class);
    private static final Random random = new Random();
    
    @Autowired
    private TspAlgorithmConfig config;

    public void setConfig(TspAlgorithmConfig config) {
        this.config = config;
    }
    
    public TspResult solve(List<City> cities) {
        return solve(cities, this::defaultDistance, null);
    }
    
    public TspResult solve(List<City> cities, BiFunction<City, City, Double> distanceFunction) {
        return solve(cities, distanceFunction, null);
    }
    
    public TspResult solve(List<City> cities, BiFunction<City, City, Double> distanceFunction, Consumer<TspProgress> progressCallback) {
        if (cities.size() < 2) {
            return new TspResult(cities, 0.0, 0, 0);
        }
        
        long startTime = System.currentTimeMillis();
        
        List<List<City>> population = initializePopulation(cities, config.getPopulationSize());
        List<City> bestIndividual = null;
        double bestFitness = Double.MAX_VALUE;
        int generation = 0;
        
        for (int gen = 0; gen < config.getMaxGenerations(); gen++) {
            Collections.sort(population, Comparator.comparingDouble(route -> calculateDistance(route, distanceFunction)));
            
            double currentBest = calculateDistance(population.get(0), distanceFunction);
            if (currentBest < bestFitness) {
                bestFitness = currentBest;
                bestIndividual = new ArrayList<>(population.get(0));
                generation = gen + 1;
            }
            
            if (progressCallback != null && gen % config.getProgressInterval() == 0) {
                logger.debug("Generation {} - Best distance: {}", gen + 1, bestFitness);
                progressCallback.accept(new TspProgress(gen + 1, new ArrayList<>(bestIndividual), bestFitness));
            }
            
            List<List<City>> newPopulation = new ArrayList<>();
            newPopulation.add(new ArrayList<>(population.get(0)));
            
            while (newPopulation.size() < config.getPopulationSize()) {
                List<City> parent1 = tournamentSelection(population, distanceFunction);
                List<City> parent2 = tournamentSelection(population, distanceFunction);
                
                List<City> child;
                if (random.nextDouble() < config.getCrossoverRate()) {
                    child = crossover(parent1, parent2);
                } else {
                    child = new ArrayList<>(random.nextBoolean() ? parent1 : parent2);
                }
                
                if (random.nextDouble() < config.getMutationRate()) {
                    mutate(child);
                }
                
                newPopulation.add(child);
            }
            
            population = newPopulation;
        }
        
        long endTime = System.currentTimeMillis();
        
        if (bestIndividual == null) {
            bestIndividual = population.get(0);
            generation = config.getMaxGenerations();
        }
        
        return new TspResult(bestIndividual, bestFitness, generation, endTime - startTime);
    }
    
    private double defaultDistance(City c1, City c2) {
        return haversineDistance(c1, c2);
    }
    
    private List<List<City>> initializePopulation(List<City> cities, int size) {
        List<List<City>> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<City> individual = new ArrayList<>(cities);
            Collections.shuffle(individual);
            population.add(individual);
        }
        return population;
    }
    
    private List<City> tournamentSelection(List<List<City>> population, BiFunction<City, City, Double> distanceFunction) {
        List<City> best = null;
        for (int i = 0; i < config.getTournamentSize(); i++) {
            List<City> individual = population.get(random.nextInt(population.size()));
            if (best == null || calculateDistance(individual, distanceFunction) < calculateDistance(best, distanceFunction)) {
                best = new ArrayList<>(individual);
            }
        }
        return best;
    }
    
    private List<City> crossover(List<City> parent1, List<City> parent2) {
        int start = random.nextInt(parent1.size());
        int end = start + random.nextInt(parent1.size() - start);
        
        List<City> child = new ArrayList<>(Collections.nCopies(parent1.size(), null));
        
        for (int i = start; i <= end; i++) {
            child.set(i, parent1.get(i));
        }
        
        for (City city : parent2) {
            if (!child.contains(city)) {
                for (int i = 0; i < child.size(); i++) {
                    if (child.get(i) == null) {
                        child.set(i, city);
                        break;
                    }
                }
            }
        }
        
        return child;
    }
    
    private void mutate(List<City> individual) {
        int i = random.nextInt(individual.size());
        int j = random.nextInt(individual.size());
        
        City temp = individual.get(i);
        individual.set(i, individual.get(j));
        individual.set(j, temp);
    }
    
    private double calculateDistance(List<City> route, BiFunction<City, City, Double> distanceFunction) {
        double distance = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            distance += distanceFunction.apply(route.get(i), route.get(i + 1));
        }
        if (route.size() > 1) {
            distance += distanceFunction.apply(route.get(route.size() - 1), route.get(0));
        }
        return distance;
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
