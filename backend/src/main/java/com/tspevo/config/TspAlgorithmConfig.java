package com.tspevo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tsp.algorithm")
public class TspAlgorithmConfig {
    
    private int populationSize = 100;
    private int maxGenerations = 2000;
    private double mutationRate = 0.02;
    private double crossoverRate = 0.8;
    private int tournamentSize = 5;
    private int progressInterval = 1;

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getMaxGenerations() {
        return maxGenerations;
    }

    public void setMaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public int getTournamentSize() {
        return tournamentSize;
    }

    public void setTournamentSize(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    public int getProgressInterval() {
        return progressInterval;
    }

    public void setProgressInterval(int progressInterval) {
        this.progressInterval = progressInterval;
    }
}
