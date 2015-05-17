package cli;

import models.BBInstance;
import models.Job;
import models.Machine;
import solver.BBInstanceSolver;
import utils.InstanceParser;

import java.util.ArrayList;

public class Main {

    private static final String FILEPATH = "instances/";

    public static void main(String[] args) {
        System.out.println("This is what in time will be a scheduler for batch production!");

        // Setup system var for graphstream
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        ArrayList<Job> jobs = new ArrayList<>();
        ArrayList<Machine> machines = new ArrayList<>();
        String instance = "instance-0.txt";

        InstanceParser.parseInstance(jobs, machines, FILEPATH, instance);

        BBInstance bbInstance = new BBInstance(jobs, machines.size());
        BBInstanceSolver.BBSolver(bbInstance);
        System.out.println("Minimum makespan: " + BBInstanceSolver.upperBound);

        BBInstanceSolver.bbTree.display();
    }
}
