package solver;

import models.BBInstance;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.MultiNode;

import java.util.ArrayList;
import java.util.Collections;

public class BBInstanceSolver {

    public static MultiGraph bbTree = new MultiGraph("bbTree");
    public static Integer upperBound = Integer.MAX_VALUE;

    // BBSolver Step 1: Initial Condition
    public static void BBSolver(BBInstance instance){
        // reset bbTree graph
        bbTree = new MultiGraph("bbTree");
        MultiNode newNode = bbTree.addNode("ROOT");
        newNode.addAttribute("ui.label", "ROOT");
        newNode.addAttribute("ui.style", "text-background-mode: rounded-box;");

        // get the graph
        MultiGraph G = instance.getInitialState();
        ArrayList<MultiNode> omega = new ArrayList<>();

        // get all nodes that are directly connected to from ROOT
        for(Edge e : G.getNode("ROOT").getLeavingEdgeSet()){
            e.getTargetNode().changeAttribute("releaseDate", 0);
            omega.add( (MultiNode)e.getTargetNode() );
        }

        // call step 2 of the B&B alg
        BBSolver( instance, omega, bbTree.getNode("ROOT") );
    }

    // BBSolver Step 2: Machine Selection
    public static void BBSolver(BBInstance currInstance, ArrayList<MultiNode> omega, Node lastInstance){
        /*System.out.print("Omega: {");
        for( MultiNode n : omega ){
            System.out.print(" (" + ((Integer)n.getAttribute("machine")+1) + "," + ((Integer)n.getAttribute("job")+1) + ")");
        }
        System.out.println(" }");*/

        ArrayList<Integer> tOmega = new ArrayList<>();
        ArrayList<Integer> mOmega = new ArrayList<>();

        // for each node get its machine and the sum between its processingTime and its releaseDate
        for ( MultiNode n : omega ){
            if( !n.getId().equals("SINK") || !n.getId().equals("ROOT") ){
                mOmega.add( (Integer)n.getAttribute("machine") );
                tOmega.add( (Integer)n.getAttribute("processingTime") + (Integer)n.getAttribute("releaseDate") );
            }
        }

        /*// for debug purposes
        int k = 0;
        for (MultiNode anOmega : omega) {
            System.out.println("omega[" + k + "]: " + anOmega.getId() +
                    " machine: " + anOmega.getAttribute("machine") +
                    " pTime: " + anOmega.getAttribute("processingTime") +
                    " rDate: " + anOmega.getAttribute("releaseDate"));
            k++;
        }*/

        if( tOmega.size() == 0 ) {
            if( (Integer)lastInstance.getAttribute("lowerBound") < upperBound )
                upperBound = lastInstance.getAttribute("lowerBound");
            return;
        }
        // find the minimum { rij + pij }
        int minTOmega = Collections.min(tOmega);

        // get all machines on which this minimum occurs
        ArrayList<Integer> minMachines = new ArrayList<>();
        for (int i = 0; i < tOmega.size(); i++) {
            if( tOmega.get(i) == minTOmega )
                minMachines.add( mOmega.get(i) );
        }

        for (Integer minMachine : minMachines) {
            //System.out.println("Solving for minTOmega: " + minTOmega + " and minMachine: " + minMachine);
            BBSolver(currInstance, omega, minTOmega, minMachine, lastInstance);
        }
    }

    // BBSolver Step 3: Branching
    public static void BBSolver(BBInstance currInstance, ArrayList<MultiNode> omega, Integer minTOmega, Integer minMachine, Node lastInstance){
        // find all tasks on omega that uses machine minMachine and has its rij < minTOmega
        ArrayList<MultiNode> omegaPrime = new ArrayList<>();
        ArrayList<MultiNode> newOmega;

        for( MultiNode n : omega )
            if( n.getAttribute("machine") == minMachine && (Integer)n.getAttribute("releaseDate") < minTOmega )
                omegaPrime.add( n );

        /*// for debug purposes
        int i=0;
        for ( MultiNode n : omegaPrime ){
            System.out.println("Adding omega[" + i + "]: " + n.getId() +
                    " machine: " + n.getAttribute("machine") +
                    " pTime: " + n.getAttribute("processingTime") +
                    " rDate: " + n.getAttribute("releaseDate"));
            i++;
        }

        // debug
        System.out.print("Omega Prime: {");
        for( MultiNode n : omegaPrime ){
            System.out.print(" (" + ((Integer)n.getAttribute("machine")+1) + "," + ((Integer)n.getAttribute("job")+1) + ")");
        }
        System.out.println(" }");*/

        // for each of those delete them from omega and add its follower to omega and go back to step 2
        for( MultiNode n : omegaPrime ){
            newOmega = new ArrayList<>();
            for( MultiNode mn : omega )
                newOmega.add(mn);
            newOmega.remove(n);

            String id = n.getId() + bbTree.getNodeCount();

            /*
            if( !lastInstance.getId().equals("ROOT") )
                System.out.println("Removing node " +" (" + ((Integer)n.getAttribute("machine")+1) + "," + ((Integer)n.getAttribute("job")+1) + ")" + " and adding it to the bbTree under " + " (" + ((Integer)lastInstance.getAttribute("machine")+1) + "," + ((Integer)lastInstance.getAttribute("job")+1) + ")");
            else
                System.out.println("Removing node " + " (" + ((Integer) n.getAttribute("machine") + 1) + "," + ((Integer) n.getAttribute("job") + 1) + ")" + " and adding it to the bbTree under " + lastInstance.getId() );
            */

            bbTree.addNode(id);
            for( String s : n.getAttributeKeySet() ){
                bbTree.getNode(id).addAttribute(s, n.getAttribute(s));
            }
            bbTree.addEdge(lastInstance.getId() + "->" + id, lastInstance.getId(), id);

            for( Edge e : n.getEdgeSet() ) {
                if ( e.getAttribute("type").equals("conjunctive") && e.getSourceNode().equals(n) && !e.getTargetNode().getId().equals("SINK") ) {
                    newOmega.add((MultiNode) e.getTargetNode());
                    //System.out.println("Added " + e.getTargetNode().getId() + " to newOmega");
                    break;
                }
            }

            BBInstance newInstance = updateReleaseDates(currInstance, n);
            ArrayList<MultiNode> updatedOmega = new ArrayList<>();

            for ( MultiNode mn : newOmega ){
                updatedOmega.add( (MultiNode)newInstance.getInitialStateSimple().getNode(mn.getId()) );
            }

            bbTree.getNode(id).addAttribute("lowerBound", getLowerBound(newInstance));
            if( (Integer)bbTree.getNode(id).getAttribute("lowerBound") < upperBound )
                BBSolver(newInstance, updatedOmega, bbTree.getNode(id));
            newInstance.initialStateSimpleReset();
        }
    }

    // TODO: FIX ME!
    private static BBInstance updateReleaseDates( BBInstance currInstance, MultiNode from ){
        MultiGraph newState = currInstance.getInitialStateSimple();

        for( Node n : newState.getNodeSet() ){
            if( !(n.getId().equals("ROOT") || n.getId().equals("SINK")) )
                if( n.getAttribute("machine").equals(from.getAttribute("machine")) && !n.getId().equals(from.getId()) ){
                    newState
                            .addEdge(
                                    "E: (" + from.getId() + "->" + n.getId() + ")" + newState.getEdgeCount(),
                                    from.getId(),
                                    n.getId(),
                                    true);
                    newState
                            .getEdge("E: (" + from.getId() + "->" + n.getId() + ")" + (newState.getEdgeCount()-1))
                            .addAttribute("weight", from.getAttribute("processingTime"));
                    newState
                            .getEdge("E: (" + from.getId() + "->" + n.getId() + ")" + (newState.getEdgeCount()-1))
                            .addAttribute("type", "disjunctive");

                    n.setAttribute("releaseDate", (Integer)from.getAttribute("releaseDate") + (Integer)from.getAttribute("processingTime") );
                }
        }

        currInstance.setInitialStateSimple( newState );
        return currInstance;
    }

    private static Integer getLowerBound(BBInstance instance) {
        ArrayList<Integer> times = new ArrayList<>();
        Node root = instance.getInitialStateSimple().getNode("ROOT");
        Node current = root;

        // process conjunctives times
        for (int i = 0; i < instance.getJobs().size(); i++) {
            // get the first node related to this job
            for(Edge e : root.getLeavingEdgeSet())
                if( e.getTargetNode().getAttribute("job").equals(i) )
                    current = e.getTargetNode();

            // add another time to our array equal to the releaseDate of the job
            times.add((Integer)current.getAttribute("releaseDate"));

            // get the sum of the processing times from there to the sink
            do{
                Integer currTime = current.getAttribute("processingTime");

                for(Edge e : current.getLeavingEdgeSet() ){
                    if( e.getAttribute("type").equals("conjunctive") )
                        current = e.getTargetNode();
                }

                times.set(i, times.get(i) + currTime);
            }while( !current.getId().equals("SINK") );
        }

        // System.out.println("Returning " + Collections.max(times) + " as the lower bound!");
        return Collections.max(times);
    }
}