package sportspulse;

import sportspulse.cli.MainMenu;
import sportspulse.utils.FileManager;

public class Main {
    public static void main(String[] args) {
        // Initialize persistence layer
        FileManager.initializeDataDirectory();
        
        // Start application
        MainMenu menu = new MainMenu();
        menu.start();
    }
}
