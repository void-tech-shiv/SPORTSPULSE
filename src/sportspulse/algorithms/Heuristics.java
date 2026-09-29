package sportspulse.algorithms;

import java.util.*;

public class Heuristics {
    
    private static final Random random = new Random();

    /**
     * Simulated Annealing for Roster Optimization.
     * Finds a boolean array representing whether a player is in the active roster to maximize fitness.
     * 
     * @param numPlayers The total number of players available.
     * @param initialTemperature The starting temperature.
     * @param coolingRate The rate at which the temperature decreases.
     * @param targetRosterSize The required number of players in the roster.
     * @return The best roster found as a boolean array.
     */
    public static boolean[] simulatedAnnealing(int numPlayers, double initialTemperature, double coolingRate, int targetRosterSize) {
        boolean[] currentSolution = generateRandomRoster(numPlayers, targetRosterSize);
        boolean[] bestSolution = currentSolution.clone();
        
        double currentEnergy = calculateRosterFitness(currentSolution);
        double bestEnergy = currentEnergy;
        
        double temperature = initialTemperature;
        
        while (temperature > 1) {
            boolean[] newSolution = generateNeighborRoster(currentSolution, targetRosterSize);
            double newEnergy = calculateRosterFitness(newSolution);
            
            // Acceptance probability
            if (acceptanceProbability(currentEnergy, newEnergy, temperature) > random.nextDouble()) {
                currentSolution = newSolution.clone();
                currentEnergy = newEnergy;
            }
            
            // Keep track of the best solution found
            if (currentEnergy > bestEnergy) {
                bestSolution = currentSolution.clone();
                bestEnergy = currentEnergy;
            }
            
            // Cool down
            temperature *= 1 - coolingRate;
        }
        
        return bestSolution;
    }

    private static boolean[] generateRandomRoster(int numPlayers, int targetSize) {
        boolean[] roster = new boolean[numPlayers];
        int count = 0;
        while (count < targetSize) {
            int idx = random.nextInt(numPlayers);
            if (!roster[idx]) {
                roster[idx] = true;
                count++;
            }
        }
        return roster;
    }

    private static boolean[] generateNeighborRoster(boolean[] current, int targetSize) {
        boolean[] neighbor = current.clone();
        // Swap one selected player with one unselected player
        int removeIdx = -1;
        int addIdx = -1;
        
        while (removeIdx == -1 || !neighbor[removeIdx]) {
            removeIdx = random.nextInt(current.length);
        }
        while (addIdx == -1 || neighbor[addIdx]) {
            addIdx = random.nextInt(current.length);
        }
        
        neighbor[removeIdx] = false;
        neighbor[addIdx] = true;
        
        return neighbor;
    }

    // Dummy fitness function for demonstration purposes
    public static double calculateRosterFitness(boolean[] roster) {
        double fitness = 0;
        for (int i = 0; i < roster.length; i++) {
            if (roster[i]) {
                // E.g., assume some players are better together, or have certain stats.
                // We'll use a deterministic pseudo-random formula for demo.
                fitness += (Math.sin(i * 12.34) * 10) + 10; 
                
                // Synergy bonus
                if (i > 0 && roster[i-1]) {
                    fitness += 5.0; // Bonus if adjacent players are picked
                }
            }
        }
        return fitness;
    }

    private static double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        // If the new solution is better, accept it
        if (newEnergy > currentEnergy) {
            return 1.0;
        }
        // If it's worse, calculate an acceptance probability
        return Math.exp((newEnergy - currentEnergy) / temperature);
    }

    /**
     * Genetic Algorithm for Advanced Scheduling.
     * Finds an array representing a sequence of matches (0 to numMatches-1) to minimize fatigue (travel/back-to-back).
     * 
     * @param numMatches The number of matches to schedule.
     * @param populationSize The size of the population.
     * @param generations Number of generations to run.
     * @param mutationRate Probability of a mutation occurring.
     * @return The best schedule found as an integer array.
     */
    public static int[] geneticAlgorithm(int numMatches, int populationSize, int generations, double mutationRate) {
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(generateRandomSchedule(numMatches));
        }
        
        int[] bestSchedule = population.get(0);
        double bestFitness = calculateScheduleFitness(bestSchedule);

        for (int generation = 0; generation < generations; generation++) {
            List<int[]> newPopulation = new ArrayList<>();
            
            // Elitism: Keep the best individual
            newPopulation.add(bestSchedule.clone());
            
            for (int i = 1; i < populationSize; i++) {
                int[] parent1 = selectParent(population);
                int[] parent2 = selectParent(population);
                
                int[] child = crossoverOrdered(parent1, parent2);
                
                if (random.nextDouble() < mutationRate) {
                    mutate(child);
                }
                
                newPopulation.add(child);
            }
            
            population = newPopulation;
            
            // Update best schedule
            for (int[] schedule : population) {
                double fitness = calculateScheduleFitness(schedule);
                if (fitness > bestFitness) {
                    bestFitness = fitness;
                    bestSchedule = schedule.clone();
                }
            }
        }
        
        return bestSchedule;
    }

    private static int[] generateRandomSchedule(int numMatches) {
        int[] schedule = new int[numMatches];
        for (int i = 0; i < numMatches; i++) {
            schedule[i] = i;
        }
        for (int i = 0; i < numMatches; i++) {
            int swapIdx = random.nextInt(numMatches);
            int temp = schedule[i];
            schedule[i] = schedule[swapIdx];
            schedule[swapIdx] = temp;
        }
        return schedule;
    }

    // Roulette wheel selection based on fitness
    private static int[] selectParent(List<int[]> population) {
        double totalFitness = 0;
        for (int[] p : population) {
            totalFitness += calculateScheduleFitness(p);
        }
        
        double r = random.nextDouble() * totalFitness;
        double runningSum = 0;
        
        for (int[] p : population) {
            runningSum += calculateScheduleFitness(p);
            if (runningSum >= r) {
                return p;
            }
        }
        return population.get(population.size() - 1);
    }

    // Ordered Crossover (OX) for permutations
    private static int[] crossoverOrdered(int[] parent1, int[] parent2) {
        int[] child = new int[parent1.length];
        Arrays.fill(child, -1);
        
        int start = random.nextInt(parent1.length);
        int end = random.nextInt(parent1.length);
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        
        for (int i = start; i <= end; i++) {
            child[i] = parent1[i];
        }
        
        int currentIdx = (end + 1) % parent1.length;
        for (int i = 0; i < parent2.length; i++) {
            int p2Idx = (end + 1 + i) % parent2.length;
            int candidate = parent2[p2Idx];
            if (!contains(child, candidate)) {
                child[currentIdx] = candidate;
                currentIdx = (currentIdx + 1) % child.length;
            }
        }
        return child;
    }

    private static boolean contains(int[] array, int val) {
        for (int v : array) {
            if (v == val) return true;
        }
        return false;
    }

    // Swap mutation
    private static void mutate(int[] schedule) {
        int idx1 = random.nextInt(schedule.length);
        int idx2 = random.nextInt(schedule.length);
        int temp = schedule[idx1];
        schedule[idx1] = schedule[idx2];
        schedule[idx2] = temp;
    }

    // Dummy fitness function for schedule (minimizing travel distance/fatigue)
    // Higher is better. We will negate a cost function.
    public static double calculateScheduleFitness(int[] schedule) {
        double penalty = 0;
        for (int i = 0; i < schedule.length - 1; i++) {
            // Assume distance between match id A and match id B is abs(A - B) * 10
            penalty += Math.abs(schedule[i] - schedule[i+1]) * 10;
        }
        return 10000.0 / (1.0 + penalty); 
    }
}
