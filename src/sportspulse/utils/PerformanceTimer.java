package sportspulse.utils;

public class PerformanceTimer {
    private long startTime;
    private long endTime;

    public void start() {
        startTime = System.nanoTime();
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    public double getExecutionTimeMs() {
        return (endTime - startTime) / 1_000_000.0;
    }

    public void printExecutionTime(String algorithmName, int inputSize, String timeComplexity, String spaceComplexity) {
        System.out.println("\n--- ALGORITHM PERFORMANCE ---");
        System.out.println("Algorithm: " + algorithmName);
        System.out.println("Input Size: " + inputSize);
        System.out.printf("Execution Time: %.3f ms\n", getExecutionTimeMs());
        System.out.println("Time Complexity: " + timeComplexity);
        System.out.println("Space Complexity: " + spaceComplexity);
        System.out.println("-----------------------------\n");
    }
}
