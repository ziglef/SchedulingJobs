package models;

import org.graphstream.graph.implementations.MultiGraph;

import java.util.ArrayList;

public class Instance {

    private MultiGraph initialState;

    public Instance( ArrayList<Job> jobs ){
        // Create the "HashTable" for tasks using the same machine
        ArrayList<ArrayList<String>> sameMachineTasks = new ArrayList<>();
        for (int i = 0; i < jobs.size(); i++) {
            sameMachineTasks.add(new ArrayList<String>());
        }

        // Create a multigraph and add the SOURCE and SINK nodes
        initialState = new MultiGraph("Current Instance");
        initialState.addNode("SOURCE");
        initialState.addNode("SINK");

        // Parse the jobs and add the tasks to the graph
        for (Job j : jobs){
            ArrayList<Integer> jMachines = j.getMachineOrder();
            ArrayList<Integer> jTimes = j.getProcessingTimes();

            for (int i = 0; i < jMachines.size(); i++) {
                // Add the task to the "HashTable"
                sameMachineTasks.get(jMachines.get(i)).add("(" + j.getId() + "," + jMachines.get(i) + ")");

                // Add the new Task to the graph
                initialState.addNode("(" + j.getId() + "," + jMachines.get(i) + ")");
                initialState
                        .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                        .addAttribute("job", j.getId());
                initialState
                        .getNode("(" + j.getId() + "," + jMachines.get(i) + ")")
                        .addAttribute("machine", jMachines.get(i));

                // Setup edge from the source to the new node
                if( i == 0 ){
                    initialState
                            .addEdge("E: (SOURCE ->" + "(" + j.getId() + "," + jMachines.get(i) + "))",
                                    "SOURCE",
                                    "(" + j.getId() + "," + jMachines.get(i) + ")",
                                    true);

                    initialState
                            .getEdge("E: (SOURCE ->" + "(" + j.getId() + "," + jMachines.get(i) + "))")
                            .addAttribute("weight", 0);
                // Setup edge from previous task to actual task
                } else {
                    initialState
                            .addEdge(
                                    "E: ((" + j.getId() + "," + jMachines.get(i - 1) + ")->(" + j.getId() + "," + jMachines.get(i) + "))",
                                    "(" + j.getId() + "," + jMachines.get(i - 1) + ")",
                                    "(" + j.getId() + "," + jMachines.get(i) + ")",
                                    true);

                    initialState
                            .getEdge("E: ((" + j.getId() + "," + jMachines.get(i - 1) + ")->(" + j.getId() + "," + jMachines.get(i) + "))")
                            .addAttribute("weight", jTimes.get(i - 1));
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
                }
            }
        }

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
                    }
                }
            }
        }

        initialState.display();
    }
}
