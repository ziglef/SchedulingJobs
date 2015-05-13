package solver;

import models.BBInstance;
import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.MultiNode;

import java.util.ArrayList;
import java.util.Collections;

public class BBInstanceSolver {

    // BBSolver Step 1: Initial Condition
    public static void BBSolver(BBInstance instance){
        // get the graph
        MultiGraph G = instance.getInitialState();
        ArrayList<MultiNode> omega = new ArrayList<>();

        // get all nodes that are directly connected to from SOURCE
        for(Edge n : G.getNode("SOURCE").getLeavingEdgeSet()){
            n.getTargetNode().changeAttribute("releaseTime", 0);
            omega.add( (MultiNode)n.getTargetNode() );
        }

        // call step 2 of the B&B alg
        BBSolver( instance, omega );
    }

    // BBSolver Step 2: Machine Selection
    public static void BBSolver(BBInstance instance, ArrayList<MultiNode> omega){
        ArrayList<Integer> tOmega = new ArrayList<>();
        ArrayList<Integer> mOmega = new ArrayList<>();

        // for each node get its machine and the sum between its processingTime and its releaseDate
        for ( MultiNode n : omega ){
            mOmega.add( (Integer)n.getAttribute("machine") );
            tOmega.add( (Integer)n.getAttribute("processingTime") + (Integer)n.getAttribute("releaseDate") );
        }
/*
        // for debug purposes
        for (MultiNode anOmega : omega) {
            System.out.println("omega[i]: " + anOmega.getId() +
                    " machine: " + anOmega.getAttribute("machine") +
                    " pTime: " + anOmega.getAttribute("processingTime") +
                    " rDate: " + anOmega.getAttribute("releaseDate"));
        }
*/

        // find the minimum { rij + pij }
        int minTOmega = Collections.min(tOmega);

        // get all machines on which this minimum occurs
        ArrayList<Integer> minMachines = new ArrayList<>();
        for (int i = 0; i < tOmega.size(); i++) {
            if( tOmega.get(i) == minTOmega )
                minMachines.add( mOmega.get(i) );
        }
/*
        // for debug purposes
        System.out.println("min{rij + pij}: " + minTOmega);
        for (Integer minMachine : minMachines) {
            System.out.println("As verified on machine: " + minMachine);
            BBSolver(instance, omega, minTOmega, minMachine);
        }
*/
    }

    // BBSolver Step 3: Branching
    public static void BBSolver(BBInstance instance, ArrayList<MultiNode> omega, Integer minTOmega, Integer minMachine){
        // find all tasks on omega that uses machine minMachine and has its rij < minTOmega
        // for each of those delete them from omega and add its follower to omega and go back to step 2
    }

}
