import common.Vehicle;
import common.VehicleManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
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

        // Locks the thread!!
        rov = manager.waitForFirst();


        while (true) {
            stickControl();
            Thread.sleep(10);
        }


        /*
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            try {
                int c = Integer.parseInt(sc.nextLine());
                rov.set(1, 2500-c);
                rov.set(2, c);
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

        double MIN_STICK = .09*250;
        double magnitude, theta;
        int STOP = 1250;

        double stick_l_ud = xbox.getAxisValue(0)*250; // forward/backward
        double stick_l_lr = xbox.getAxisValue(1)*250; // strafe left/right

        double stick_r_lr = xbox.getAxisValue(2)*250; // turn left/right
        double stick_r_ud = xbox.getAxisValue(3)*250; // tilt forward/back

        if (Math.abs(stick_l_ud) > MIN_STICK || Math.abs(stick_l_lr) > MIN_STICK)
        {
            magnitude = Math.sqrt(Math.pow(stick_l_lr,2) + Math.pow(stick_l_ud,2));

            // Find angle of resulting vector
            theta = Math.atan(-stick_l_ud / stick_l_lr);
            if (stick_l_lr < 0) theta += Math.PI;
            if (theta < 0) theta += 2*Math.PI;

            double thetaRotated = theta - Math.PI/4;

            double sternPortVectorAngle = Math.PI * 7/4;
            double sternStarboardVectorAngle = Math.PI * 1/4;

            int chan1 = (int) Math.round(magnitude * Math.cos(thetaRotated)) + STOP;
            int chan2 = (int) Math.round(magnitude * Math.sin(thetaRotated)) + STOP;

            System.out.print("Motor 0: " + chan1);
            System.out.println("\tMotor 1: " + chan2);
//          System.out.println("Angle: " + Math.round(theta * 180 / Math.PI));

            rov.set(1, chan1);
            rov.set(2, chan2);

        } else {
            rov.set(1, STOP);
            rov.set(2, STOP);
        }
    }
}
