package com.tspevo.service;

import com.tspevo.model.City;
import com.tspevo.model.TspResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

@Service
public class EvolutionaryAlgorithm {
    
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATIONS = 2000;
    private static final double MUTATION_RATE = 0.02;
    private static final double CROSSOVER_RATE = 0.8;
    private static final int TOURNAMENT_SIZE = 5;
    private static final Random random = new Random();
    
    public TspResult solve(List<City> cities) {
        return solve(cities, null);
    }
    
    public TspResult solve(List<City> cities, Consumer<TspProgress> progressCallback) {
        if (cities.size() < 2) {
            return new TspResult(cities, 0.0, 0, 0);
        }
        
        long startTime = System.currentTimeMillis();
        
        List<List<City>> population = initializePopulation(cities, POPULATION_SIZE);
        List<City> bestIndividual = null;
        double bestFitness = Double.MAX_VALUE;
        int generation = 0;
        
        for (int gen = 0; gen < MAX_GENERATIONS; gen++) {
            Collections.sort(population, Comparator.comparingDouble(this::calculateDistance));
            
            double currentBest = calculateDistance(population.get(0));
            if (currentBest < bestFitness) {
                bestFitness = currentBest;
                bestIndividual = new ArrayList<>(population.get(0));
                generation = gen + 1;
            }
            
            if (progressCallback != null && gen % 5 == 0) {
                progressCallback.accept(new TspProgress(gen + 1, new ArrayList<>(population.get(0)), bestFitness));
            }
            
            List<List<City>> newPopulation = new ArrayList<>();
            newPopulation.add(new ArrayList<>(population.get(0)));
            
            while (newPopulation.size() < POPULATION_SIZE) {
                List<City> parent1 = tournamentSelection(population);
                List<City> parent2 = tournamentSelection(population);
                
                List<City> child;
                if (random.nextDouble() < CROSSOVER_RATE) {
                    child = crossover(parent1, parent2);
                } else {
                    child = new ArrayList<>(random.nextBoolean() ? parent1 : parent2);
                }
                
                if (random.nextDouble() < MUTATION_RATE) {
                    mutate(child);
                }
                
                newPopulation.add(child);
            }
            
            population = newPopulation;
        }
        
        long endTime = System.currentTimeMillis();
        
        if (bestIndividual == null) {
            bestIndividual = population.get(0);
            generation = MAX_GENERATIONS;
        }
        
        return new TspResult(bestIndividual, bestFitness, generation, endTime - startTime);
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
    
    private List<City> tournamentSelection(List<List<City>> population) {
        List<City> best = null;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            List<City> individual = population.get(random.nextInt(population.size()));
            if (best == null || calculateDistance(individual) < calculateDistance(best)) {
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
    
    private double calculateDistance(List<City> route) {
        double distance = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            distance += euclideanDistance(route.get(i), route.get(i + 1));
        }
        if (route.size() > 1) {
            distance += euclideanDistance(route.get(route.size() - 1), route.get(0));
        }
        return distance;
    }
    
    private double euclideanDistance(City c1, City c2) {
        double dx = c1.getX() - c2.getX();
        double dy = c1.getY() - c2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
