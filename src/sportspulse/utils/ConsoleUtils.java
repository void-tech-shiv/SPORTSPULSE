package sportspulse.utils;

public class ConsoleUtils {
    public static void clearScreen() {
        // Simple cross-platform clear screen using newlines for standard consoles
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printHeader(String title) {
        System.out.println("========================================================");
        System.out.println(title);
        System.out.println("========================================================");
    }
}
