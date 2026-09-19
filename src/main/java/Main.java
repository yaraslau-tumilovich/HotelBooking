import service.Database;
import ui.ConsoleApp;

public class Main {

    public static void main(String[] args) {

        Database.initialize();

        ConsoleApp.main(args);

    }

}