import common.Vehicle;
import common.VehicleManager;

import java.text.ParseException;
import java.util.Scanner;

public class CLI {

    public static void main(String[] args) throws Exception {

        System.out.println("ROV Command Line");
        System.out.println();

        VehicleManager manager = new VehicleManager();

        System.out.println();
        System.out.println("Searching for ROVs");

        while (manager.count() < 1) {
            Thread.sleep(10);
        }

        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            try {
                int c = Integer.parseInt(sc.nextLine());
                System.out.println("Setting new value.");
                Vehicle v = manager.getFirstVehicle();
                v.set(1, c);
            } catch (NumberFormatException e) {
                System.out.println("Not a number.");
            }
        }




    }
}
