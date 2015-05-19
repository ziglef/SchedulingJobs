package solver;

import models.BBInstance;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.MultiNode;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class BBInstanceSolver {

    public static MultiGraph bbTree = new MultiGraph("bbTree");
    public static Integer upperBound = Integer.MAX_VALUE;
    public static boolean precise = true;

    // BBSolver Step 1: Initial Condition
    public static void BBSolver(BBInstance instance){
        // reset bbTree graph
        bbTree = new MultiGraph("bbTree");
        MultiNode newNode = bbTree.addNode("ROOT");
        newNode.addAttribute("ui.label", "ROOT");
        newNode.addAttribute("ui.style", "text-background-mode: rounded-box;");
        newNode.addAttribute("lowerBound", 0);

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

        if( omega.size() == 0 ) {
            lastInstance.setAttribute("ui.label", lastInstance.getAttribute("ui.label") + " FEASIBLE SOLUTION");
            if( getLowerBound(currInstance, true) < upperBound )
                upperBound = getLowerBound(currInstance, true);
            System.out.println("New upperBound found with value: " + upperBound);
            // currInstance.getInitialStateSimple().display();
            return;
        }

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
        MultiGraph initialState = (MultiGraph)Graphs.clone(currInstance.getInitialStateSimple());
        for( MultiNode n : omegaPrime ){
            newOmega = new ArrayList<>();
            for( MultiNode mn : omega )
                newOmega.add(mn);
            newOmega.remove(n);

            /*
            if( !lastInstance.getId().equals("ROOT") )
                System.out.println("Removing node " +" (" + ((Integer)n.getAttribute("machine")+1) + "," + ((Integer)n.getAttribute("job")+1) + ")" + " and adding it to the bbTree under " + " (" + ((Integer)lastInstance.getAttribute("machine")+1) + "," + ((Integer)lastInstance.getAttribute("job")+1) + ")");
            else
                System.out.println("Removing node " + " (" + ((Integer) n.getAttribute("machine") + 1) + "," + ((Integer) n.getAttribute("job") + 1) + ")" + " and adding it to the bbTree under " + lastInstance.getId() );
            */

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

            if( getLowerBound(newInstance, false) < upperBound /* && getLowerBound(newInstance) >= (Integer)lastInstance.getAttribute("lowerBound") */) {
                String id = n.getId() + bbTree.getNodeCount();
                bbTree.addNode(id);
                for (String s : n.getAttributeKeySet()) {
                    bbTree.getNode(id).addAttribute(s, n.getAttribute(s));
                }
                bbTree.addEdge(lastInstance.getId() + "->" + id, lastInstance.getId(), id);

                bbTree.getNode(id).addAttribute("lowerBound", getLowerBound(newInstance, false));
                bbTree.getNode(id).setAttribute("ui.label", (String) bbTree.getNode(id).getAttribute("ui.label") + " LB: " + bbTree.getNode(id).getAttribute("lowerBound"));

                // System.out.println("Exploring node: " + bbTree.getNode(id).getAttribute("ui.label"));
                BBSolver(newInstance, updatedOmega, bbTree.getNode(id));
            }

            currInstance.setInitialStateSimple(initialState);
        }
    }

    private static BBInstance updateReleaseDates( BBInstance currInstance, MultiNode from ){
        MultiGraph newState = currInstance.getInitialStateSimple();

        for( Node n : newState.getNodeSet() ){
            // n.setAttribute("releaseDate", n.getAttribute("longestPath"));
            if( !(n.getId().equals("ROOT") || n.getId().equals("SINK")) ) {
                boolean reversed = false;
                for(Edge e: n.getLeavingEdgeSet())
                    if( e.getTargetNode().getId().equals(from.getId()) )
                        reversed = true;

                if (n.getAttribute("machine").equals(from.getAttribute("machine")) && !n.getId().equals(from.getId()) && !reversed) {
                    newState
                            .addEdge(
                                    "E: (" + from.getId() + "->" + n.getId() + ")" + newState.getEdgeCount(),
                                    from.getId(),
                                    n.getId(),
                                    true);
                    newState
                            .getEdge("E: (" + from.getId() + "->" + n.getId() + ")" + (newState.getEdgeCount() - 1))
                            .addAttribute("weight", from.getAttribute("processingTime"));
                    newState
                            .getEdge("E: (" + from.getId() + "->" + n.getId() + ")" + (newState.getEdgeCount() - 1))
                            .addAttribute("type", "disjunctive");
                }
            }
        }

        currInstance.setInitialStateSimple( newState );
        if( precise )
            getLongestPath(currInstance);
        else
            getLongestPathApprox(currInstance);
        return currInstance;
    }

    private static ArrayList<Node> topologicalSort(BBInstance instance){
        // General vars
        MultiGraph graphToSort = (MultiGraph)Graphs.clone(instance.getInitialStateSimple());
        Node n;

        // Final topological sort
        ArrayList<Node> topSort = new ArrayList<>();
        // Current vertices we need to analyze
        Queue<Node> currVertexSet = new LinkedBlockingDeque<>();
        // All vertices already visited
        Set<Node> visitedVertices = new HashSet<>();
        // All vertices in the original graph
        Set<Node> allVertices = new HashSet<>();
        allVertices.addAll(graphToSort.getNodeSet());

        //Find leaf nodes
        for( Node gn : allVertices ){
            if( graphToSort.getNode(gn.getId()).getEnteringEdgeSet().size() == 0 )
                currVertexSet.add(gn);
        }

        // Visit all leaf nodes. Build result from vertices that are visited
        // for the first time. Add vertices to not visited leaf vertices currVertexSet, if
        // it contains current element n and all of its values are visited.
        while( !currVertexSet.isEmpty() ){
            // Add current vertex to the list of visited vertices and to the final sort
            n = currVertexSet.poll();
            if( n != null && !visitedVertices.contains(n)) {
                visitedVertices.add(n);
                topSort.add(n);
            } else
                continue;

            for (Node gn : graphToSort.getNodeSet()){
                // Get leaving neighbours
                ArrayList<Node> gnEnteringNeighbours = new ArrayList<>();
                for(Edge e : gn.getEnteringEdgeSet())
                    gnEnteringNeighbours.add(e.getSourceNode());

                // Get new leafs
                if( gn.getEnteringEdgeSet().size() > 0 &&
                        !visitedVertices.contains(gn) &&
                        visitedVertices.containsAll(gnEnteringNeighbours) )
                    currVertexSet.add(gn);
            }
        }

        // System.out.println("Topological sort: " + topSort);
        if( topSort.size() != graphToSort.getNodeCount() ) {
            System.out.println("Error in topSort");
        }

        return topSort;
    }

    private static void getLongestPathApprox(BBInstance currInstance) {

    }

    private static void getLongestPath(BBInstance instance){
       ArrayList<Node> topSort = topologicalSort(instance);

        // Set longest path for all nodes
        for(Node gn : topSort){
            if( gn.getEnteringEdgeSet().size() == 0 ) {
                gn.addAttribute("releaseDate", 0);
                instance.getInitialStateSimple().getNode(gn.getId()).addAttribute("releaseDate", 0);
            }else {
                Integer max = 0;
                for(Edge e : gn.getEnteringEdgeSet())
                    if( (Integer)e.getSourceNode().getAttribute("releaseDate") + (Integer)e.getSourceNode().getAttribute("processingTime") > max )
                        max = (Integer)e.getSourceNode().getAttribute("releaseDate") + (Integer)e.getSourceNode().getAttribute("processingTime");

                gn.addAttribute("releaseDate", max);
                instance.getInitialStateSimple().getNode(gn.getId()).addAttribute("releaseDate", max);
            }
        }
    }

    private static Integer getLowerBound(BBInstance instance, boolean _final) {
        if( _final )
            return instance.getInitialStateSimple().getNode("SINK").getAttribute("releaseDate");
        else
            return instance.getInitialStateSimple().getNode("SINK").getAttribute("releaseDate"); // change to enhanced value
    }
}