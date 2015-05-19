package models;

import org.graphstream.graph.implementations.Graphs;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.ArrayList;

public class BBInstance {

    private MultiGraph initialState;
    private MultiGraph initialStateSimple;
    private MultiGraph initialStateSimpleReset;
    private ArrayList<Job> jobs;

    public BBInstance(ArrayList<Job> jobs, int noMachines){
        // Save the job list
        this.jobs = jobs;

        // Create the "HashTable" for tasks using the same machine
        ArrayList<ArrayList<String>> sameMachineTasks = new ArrayList<>();
        for (int i = 0; i < noMachines; i++) {
            sameMachineTasks.add(new ArrayList<String>());
        }

        // Create a multigraph and add the ROOT and SINK nodes
        initialState = new MultiGraph("Current Instance");
        initialState.addNode("ROOT");
        initialState
                .getNode("ROOT")
                .addAttribute("ui.label", "ROOT");
        initialState
                .getNode("ROOT")
                .addAttribute("ui.style", "text-background-mode: rounded-box;");
        initialState
                .getNode("ROOT")
                .addAttribute("processingTime", 0);

        initialState.addNode("SINK");
        initialState
                .getNode("SINK")
                .addAttribute("ui.style", "text-background-mode: rounded-box;");
        initialState
                .getNode("SINK")
                .addAttribute("ui.label", "SINK");

        // Parse the jobs and add the tasks to the graph
        for (Job j : jobs){
            ArrayList<Integer> jMachines = j.getMachineOrder();
            ArrayList<Integer> jTimes = j.getProcessingTimes();
            // System.out.println("Job " + j.getId() + " has " + jMachines.size() + " machines.");
            for (int i = 0; i < jMachines.size(); i++) {
                // System.out.println("i: " + i);
                // Add the task to the "HashTable"
                sameMachineTasks.get(jMachines.get(i)).add("(" + j.getId() + "," + jMachines.get(i) + ")");

                // Add the new Task to the graph
                initialState.addNode("(" + j.getId() + "," + jMachines.get(i) + ")");
                // Attributes //
                initialState
                        .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                        .addAttribute("job", j.getId());
                initialState
                        .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                        .addAttribute("machine", jMachines.get(i));
                initialState
                        .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                        .addAttribute("processingTime", jTimes.get(i));
                // Style //
                initialState
                        .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                        .addAttribute("ui.label", "(" + (jMachines.get(i)+1) + "," + (j.getId()+1) + ")");
                initialState
                        .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                        .addAttribute("ui.style", "text-background-mode: rounded-box;");

                // Setup edge from the ROOT to the new node
                if( i == 0 ){
                    initialState
                            .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                            .addAttribute("releaseDate", 0);

                    initialState
                            .addEdge("E: (ROOT ->" + "(" + j.getId() + "," + jMachines.get(i) + "))",
                                    "ROOT",
                                    "(" + j.getId() + "," + jMachines.get(i) + ")",
                                    true);

                    initialState
                            .getEdge("E: (ROOT ->" + "(" + j.getId() + "," + jMachines.get(i) + "))")
                            .addAttribute("weight", 0);
                    initialState
                            .getEdge("E: (ROOT ->" + "(" + j.getId() + "," + jMachines.get(i) + "))")
                            .addAttribute("type", "conjunctive");
                // Setup edge from previous task to actual task
                } else {
                    int Rij = j.getProcessingTimes(i) + (Integer)initialState.getNode("(" + j.getId() + "," + jMachines.get(i-1) + ")").getAttribute("releaseDate");
                    initialState
                            .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                            .addAttribute("releaseDate", Rij);

                    initialState
                            .addEdge(
                                    "E: ((" + j.getId() + "," + jMachines.get(i - 1) + ")->(" + j.getId() + "," + jMachines.get(i) + "))",
                                    "(" + j.getId() + "," + jMachines.get(i - 1) + ")",
                                    "(" + j.getId() + "," + jMachines.get(i) + ")",
                                    true);

                    initialState
                            .getEdge("E: ((" + j.getId() + "," + jMachines.get(i - 1) + ")->(" + j.getId() + "," + jMachines.get(i) + "))")
                            .addAttribute("weight", jTimes.get(i - 1));
                    initialState
                            .getEdge("E: ((" + j.getId() + "," + jMachines.get(i - 1) + ")->(" + j.getId() + "," + jMachines.get(i) + "))")
                            .addAttribute("type", "conjunctive");
                }
                // Setup edge from last task to sink
                if( i == jMachines.size() -1 ){
                    initialState
                            .addEdge(
                                    "E: ((" + j.getId() + "," + jMachines.get(i) + ")->" + "SINK)",
                                    "(" + j.getId() + "," + jMachines.get(i) + ")",
                                    "SINK",
                                    true);

                    initialState
                            .getEdge("E: ((" + j.getId() + "," + jMachines.get(i) + ")->" + "SINK)")
                            .addAttribute("weight", jTimes.get(i));
                    initialState
                            .getEdge("E: ((" + j.getId() + "," + jMachines.get(i) + ")->" + "SINK)")
                            .addAttribute("type", "conjunctive");
                }
            }
        }

        this.initialStateSimple = (MultiGraph)Graphs.clone(initialState);
        this.initialStateSimpleReset = (MultiGraph)Graphs.clone(initialState);

        // Parse the hashtable and add the edges between tasks that share a machine
        for ( ArrayList<String> machines : sameMachineTasks ){
            for ( String node_s : machines ){
                for ( String node_d : machines ) {
                    if (!(node_s.equals(node_d))) {
                        Integer jobIndex = Integer.valueOf(node_s.substring(1, node_s.indexOf(",")));
                        Integer machine = Integer.valueOf(node_s.substring(node_s.indexOf(",")+1).replace(')', ' ').trim());

                        int machineIndex = 0;
                        Job job = jobs.get(jobIndex);
                        for (int i = 0; i < job.getMachineOrder().size(); i++) {
                            if( machine.equals(job.getMachineOrder(i)) ) {
                                machineIndex = i;
                                break;
                            }
                        }

                        initialState
                                .addEdge(
                                        "E: (" + node_s + "->" + node_d + ")",
                                        node_s,
                                        node_d,
                                        true);

                        initialState
                                .getEdge("E: (" + node_s + "->" + node_d + ")")
                                .addAttribute("weight", job.getProcessingTimes(machineIndex));
                        initialState
                                .getEdge("E: (" + node_s + "->" + node_d + ")")
                                .addAttribute("type", "disjunctive");
                    }
                }
            }
        }
        // initialState.display();
    }

    public MultiGraph getInitialState() {
        return initialState;
    }

    public MultiGraph getInitialStateSimple() {
        return initialStateSimple;
    }

    public void initialStateSimpleReset() {
        this.initialStateSimple = (MultiGraph)Graphs.clone(initialStateSimpleReset);
    }

    public void setInitialState(MultiGraph initialState) {
        this.initialState = (MultiGraph)Graphs.clone(initialState);
    }

    public void setInitialStateSimple(MultiGraph initialStateSimple) {
        this.initialStateSimple = (MultiGraph)Graphs.clone(initialStateSimple);
    }

    public ArrayList<Job> getJobs() {
        return jobs;
    }

    public Job getJob(int i) {
        return jobs.get(i);
    }
}
