import common.VehicleManager;

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
    }
}
