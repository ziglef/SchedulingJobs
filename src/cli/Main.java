package cli;

import models.Job;
import models.Machine;
import utils.InstanceParser;

import java.util.ArrayList;

public class Main {

    private static final String FILEPATH = "instances/";

    public static void main(String[] args) {
        System.out.println("This is what in time will be a scheduler for batch production!");

        ArrayList<Job> jobs = new ArrayList<>();
        ArrayList<Machine> machines = new ArrayList<>();
        String instance = "instance-1.txt";

        InstanceParser.parseInstance(jobs, machines, FILEPATH, instance);
    }
}
