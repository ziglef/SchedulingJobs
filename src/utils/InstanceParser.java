package utils;

import models.Job;
import models.Machine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public final class InstanceParser {

    public static void parseInstance(ArrayList<Job> jobs, ArrayList<Machine> machines, String FILEPATH, String filename){
        Scanner scanner = null;
        int noJobs, noMachines;

        try {
            scanner = new Scanner(new File(FILEPATH+filename));
        } catch (FileNotFoundException e) {
            System.out.println("The file specified does not exist or is in the wrong location!");
            e.printStackTrace();
            return;
        }

        noJobs = scanner.nextInt();
        noMachines = scanner.nextInt();

        for (int i = 0; i < noJobs; i++) {
            ArrayList<Integer> tempMachines = new ArrayList<>();
            ArrayList<Integer> tempTimes = new ArrayList<>();
            for (int j = 0; j < noMachines; j++) {
                // Initialize machine array //
                if( i==0 && j % 2 == 0)
                    machines.add(new Machine(j));

                // Gather information for the creation of the new job //
                tempMachines.add(scanner.nextInt());
                tempTimes.add(scanner.nextInt());
                // System.out.println("Just added (m, pt): (" + tempMachines.get(tempMachines.size()-1) + "," + tempTimes.get(tempTimes.size()-1) + ")");
            }
            jobs.add(new Job(i, tempMachines, tempTimes));
        }
    }

}
