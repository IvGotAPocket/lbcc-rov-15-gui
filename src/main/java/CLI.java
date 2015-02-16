import common.Vehicle;
import common.VehicleManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import java.util.Scanner;

public class CLI {

    static Controller xbox;
    static Vehicle rov;

    public static void main(String[] args) throws Exception {

        try {
            Controllers.create(); //initialize controllers
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        findController();

        System.out.println("ROV Command Line");
        System.out.println();

        VehicleManager manager = new VehicleManager();

        System.out.println();
        System.out.println("Searching for ROVs");

        while (manager.count() < 1) {
            Thread.sleep(10);
        }

        rov = manager.getFirstVehicle();

        while (true) {
            stickControl();
            Thread.sleep(10);
        }

        /*
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            try {
                int c = Integer.parseInt(sc.nextLine());
                System.out.println("Setting new value.");
                v.set(1, c);
            } catch (NumberFormatException e) {
                System.out.println("Not a number.");
            }
        }
        */
    }



    static void findController()
    {
        Controllers.poll(); //get controller data

        for(int i=0; i< Controllers.getControllerCount(); i++) //loop through all controllers connect to PC
        {
            //System.out.println(Controllers.getController(i).getName());
            if(Controllers.getController(i).getName().equals("Controller (XBOX 360 For Windows)"))//finds xbox controller
            {
                xbox = Controllers.getController(i); //stores controller found in my xbox variable
                System.out.println("Found!- " + xbox.getName()); // shows that i found xbox controller
            }
            else if(Controllers.getController(i).getName().equals("Controller (Gamepad for Xbox 360)"))
            {
                xbox = Controllers.getController(i);
                System.out.println("Found!- " + xbox.getName());
            }
        }
    }



    static void stickControl()
    {
        xbox.poll(); //needed command to get info from controller

        double MIN_STICK = .09*500;
        double resultant, theta;
        double STOP = 1000;

        double stick_l_ud = xbox.getAxisValue(0)*500, //multiply by 500 to convert thumbstick to correct range
                stick_l_lr = xbox.getAxisValue(1)*500,
                stick_r_lr = xbox.getAxisValue(2)*500,
                stick_r_ud = xbox.getAxisValue(3)*500;

        if( (Math.abs(stick_l_ud)>MIN_STICK) || (Math.abs(stick_l_lr)>MIN_STICK) ) //Left Stick
        {
            resultant = Math.sqrt( Math.pow(stick_l_lr,2) + Math.pow(stick_l_ud,2) ); //sqrt((lr+ud)^2)
            theta = ( Math.atan(stick_l_ud / stick_l_lr) ) - Math.PI/4;

            // Adjust theta based on quadrant. Add 180 deg if angle in Q3 or Q4
            if( stick_l_ud < 0)
                theta += Math.PI;

            //System.out.println("1 "+ resultant*Math.cos(theta)*(5.0/7) + " " + stick_l_lr + " " + stick_l_ud);
            //System.out.println("2 "+ resultant*Math.sin(theta)*(5.0/7) + " " + stick_l_lr + " " + stick_l_ud);

            // Set a single value for now.
            int value = (int) Math.round(resultant*Math.cos(theta)*(5.0/7)) + 1000;
            boolean success = rov.set(1, value);
            if (!success) System.out.println("Unable to set: " + value);

        }

        // We need to decide what the right stick does, exactly.  Probably left/right turn, and up/down tilt?
        if( (Math.abs(stick_r_ud)>MIN_STICK) || (Math.abs(stick_r_lr)>MIN_STICK) ) //Right Stick
        {
            resultant = Math.sqrt( Math.pow(stick_r_lr,2) + Math.pow(stick_r_ud,2) ); //sqrt((lr+ud)^2)
            theta = ( Math.atan(stick_r_ud / stick_r_lr) ) - Math.PI/4;

            // Adjust theta based on quadrant. Add 180 deg if angle in Q3 or Q4
            if( stick_r_ud < 0)
                theta += Math.PI;
            // Multiply by 5/7 to scale down to range 500 to 1500
            //System.out.println("3 "+ resultant*Math.cos(theta)*(5.0/7) + " " + stick_r_lr + " " + stick_r_ud);
            //System.out.println("4 "+ resultant*Math.sin(theta)*(5.0/7) + " " + stick_r_lr + " " + stick_r_ud);

            //v.set(0, resultant*Math.cos(theta)+1000);
        }
    }
}
