package solver;

import models.BBInstance;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.MultiNode;
import utils.GraphUtil;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

public class BBInstanceSolver {

    public static MultiGraph bbTree = new MultiGraph("bbTree");
    public static Integer upperBound = Integer.MAX_VALUE;
    public static MultiGraph upperBoundGraph = null;
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
        BBSolver( instance, omega, bbTree.getNode("ROOT"), topologicalSort(instance) );
    }

    // BBSolver Step 2: Machine Selection
    public static void BBSolver(BBInstance currInstance, ArrayList<MultiNode> omega, Node lastInstance, ArrayList<Node> previousSort){
        /*System.out.print("Omega: {");
        for( MultiNode n : omega ){
            System.out.print(" (" + ((Integer)n.getAttribute("machine")+1) + "," + ((Integer)n.getAttribute("job")+1) + ")");
        }
        System.out.println(" }");*/

        if( omega.size() == 0 ) {
            lastInstance.setAttribute("ui.label", lastInstance.getAttribute("ui.label") + " FEASIBLE SOLUTION");
            if( getLowerBound(currInstance) < upperBound ) {
                upperBound = getLowerBound(currInstance);
                upperBoundGraph = currInstance.getInitialStateSimple();
                System.out.println("New upperBound found with value: " + upperBound);
            }
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
            BBSolver(currInstance, omega, minTOmega, minMachine, lastInstance, previousSort);
        }
    }

    // BBSolver Step 3: Branching
    public static void BBSolver(BBInstance currInstance, ArrayList<MultiNode> omega, Integer minTOmega, Integer minMachine, Node lastInstance, ArrayList<Node> previousSort){
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
        /* OLD SOLUTION
        MultiGraph initialState = (MultiGraph)Graphs.clone(currInstance.getInitialStateSimple());
        */
        MultiGraph initialState = GraphUtil.clone(currInstance.getInitialStateSimple());
        ArrayList<Node> topSort = new ArrayList<>(previousSort);
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

            BBInstance newInstance = updateReleaseDates(currInstance, n, topSort);
            ArrayList<MultiNode> updatedOmega = new ArrayList<>();
            for ( MultiNode mn : newOmega ){
                updatedOmega.add( (MultiNode)newInstance.getInitialStateSimple().getNode(mn.getId()) );
            }

            if( getLowerBound(newInstance) < upperBound /* && getLowerBound(newInstance) >= (Integer)lastInstance.getAttribute("lowerBound") */) {
                String id = n.getId() + bbTree.getNodeCount();
                bbTree.addNode(id);
                for (String s : n.getAttributeKeySet()) {
                    bbTree.getNode(id).addAttribute(s, n.getAttribute(s));
                }
                bbTree.addEdge(lastInstance.getId() + "->" + id, lastInstance.getId(), id);

                bbTree.getNode(id).addAttribute("lowerBound", getLowerBound(newInstance));
                bbTree.getNode(id).setAttribute("ui.label", bbTree.getNode(id).getAttribute("ui.label") + " LB: " + bbTree.getNode(id).getAttribute("lowerBound"));

                // System.out.println("Exploring node: " + bbTree.getNode(id).getAttribute("ui.label"));
                BBSolver(newInstance, updatedOmega, bbTree.getNode(id), topSort);
            }

            currInstance.setInitialStateSimple(initialState);
            topSort = new ArrayList<>(previousSort);
        }
    }

    private static BBInstance updateReleaseDates( BBInstance currInstance, MultiNode from, ArrayList<Node> previousSort ){
        /* OLD SOLUTION
        MultiGraph newState = (MultiGraph) Graphs.clone(currInstance.getInitialStateSimple());
        */
        MultiGraph newState = GraphUtil.clone(currInstance.getInitialStateSimple());
        ArrayList<Node> topSort = previousSort;

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

                    currInstance.setInitialStateSimple( newState );
                    ArrayList<Node> tempSort = new ArrayList<>();
                    for (Node tsn : topSort ){
                        tempSort.add(newState.getNode(tsn.getId()));
                    }
                    topSort = topologicalSort(tempSort, from, n);
                }
            }
        }

        currInstance.setInitialStateSimple( newState );
        // System.out.println("topSort before: " + topSort);
        ArrayList<Node> tempSort = new ArrayList<>();
        for (Node tsn : topSort ){
            tempSort.add(newState.getNode(tsn.getId()));
        }
        topSort = topologicalSort(tempSort, from, newState.getNode("SINK"));
        // System.out.println("topSort after: " + topSort);
        if( precise )
            getLongestPath(currInstance, topSort);
        else
            getLongestPath(currInstance, topSort);

        return currInstance;
    }

    private static int findNodeIndex(ArrayList<Node> array, Node n){
        int index = -1;

        for(int i=0; i<array.size(); i++){
            if( array.get(i).getId().equals(n.getId()) ) {
                return i;
            }
        }

        return index;
    }

    private static ArrayList<Node> reorderTopSort(ArrayList<Node> oldTopSort, Set<Node> fSet, Set<Node> bSet) {
        ArrayList<Node> newTopSort = new ArrayList<>(oldTopSort);
        ArrayList<Node> setsMerge = new ArrayList<>();
        ArrayList<Node> fSet_a = new ArrayList<>(fSet);
        ArrayList<Node> bSet_a = new ArrayList<>(bSet);
        ArrayList<Integer> newOrder = new ArrayList<>();

/*
        System.out.println("OldTopSort: " + oldTopSort);
        System.out.println("Old fSet: " + fSet);
        System.out.println("Old fSet_a: " + fSet_a);
        System.out.println("Old bSet: " + bSet);
        System.out.println("Old bSet_a: " + bSet_a);
*/
        if( fSet_a.size() > 1 ) {
            // System.out.println("Asking to sort: " + fSet_a.size() + " elements!");
            fSet_a = nodeSort(oldTopSort, fSet_a);
        }
        if( bSet_a.size() > 1 ) {
            // System.out.println("Asking to sort: " + bSet_a.size() + " elements!");
            bSet_a = nodeSort(oldTopSort, bSet_a);
        }
/*
        System.out.println("New fSet_a: " + fSet_a);
        System.out.println("New bSet_a: " + bSet_a);
*/

        for(Node n : bSet_a) {
            newOrder.add(findNodeIndex(oldTopSort, n));
            setsMerge.add(n);
        }

        for(Node n : fSet_a) {
            newOrder.add(findNodeIndex(oldTopSort, n));
            setsMerge.add(n);
        }

        for(int i=0; i<newOrder.size(); i++){
            newTopSort.set(newOrder.get(i), setsMerge.get(i));
        }

        return newTopSort;
    }

    private static ArrayList<Node> nodeSort(ArrayList<Node> oldTopSort, ArrayList<Node> set) {
        if( set == null || set.size() == 0 )
            return null;

        return nodeSort(oldTopSort, set, 0, set.size()-1);
    }

    private static ArrayList<Node> nodeSort(ArrayList<Node> oldTopSort, ArrayList<Node> set, int low, int high) {
        ArrayList<Node> sortedList = set;
        Node pivot = sortedList.get(low+(high-low)/2);
        int pivotIndex = findNodeIndex(oldTopSort, pivot);
        int i = low;
        int j = high;

        while( i <= j ){
            while( findNodeIndex(oldTopSort, sortedList.get(i)) < pivotIndex )
                i++;

            while( findNodeIndex(oldTopSort, sortedList.get(j)) > pivotIndex )
                j--;

            if( i <= j ){
                Collections.swap(sortedList, i, j);
                i++;
                j--;
            }
        }

        if( low < j )
            sortedList = nodeSort(oldTopSort, sortedList, low, j);
        if( i < high )
            sortedList = nodeSort(oldTopSort, sortedList, i, high);

        return sortedList;
    }

    private static Set<Node> dfs_f(ArrayList<Node> oldTopSort, Node to, int fromIndex, Set<Node> visitedVertices){
        Set<Node> fSet = new HashSet<>();

        visitedVertices.add(to);
        fSet.add(to);

        for(Edge e : to.getLeavingEdgeSet()){
            Node w = e.getTargetNode();
            int wIndex = findNodeIndex(oldTopSort, w);
            /*
            System.out.println("w: " + w);
            System.out.println("ord[w]: " + wIndex);
            System.out.println("Checking if w is already visited: " + visitedVertices.contains(w));
            System.out.println("Checking if ord[w] < ord[x]: " + (wIndex < fromIndex));
            */
            if( !visitedVertices.contains(w) && wIndex < fromIndex )
                fSet.addAll(dfs_f(oldTopSort, w, wIndex, visitedVertices));
        }

        return fSet;
    }

    private static Set<Node> dfs_b(ArrayList<Node> oldTopSort, Node from, int toIndex, Set<Node> visitedVertices){
        Set<Node> bSet = new HashSet<>();

        visitedVertices.add(from);
        bSet.add(from);

        for(Edge e : from.getEnteringEdgeSet()){
            Node w = e.getSourceNode();
            int wIndex = findNodeIndex(oldTopSort, w);
            /*
            System.out.println("w: " + w);
            System.out.println("ord[w]: " + wIndex);
            System.out.println("Checking if w is already visited: " + visitedVertices.contains(w));
            System.out.println("Checking if ord[y] < ord[w]: " + (toIndex < wIndex));
            */
            if( !visitedVertices.contains(w) && toIndex < wIndex )
                bSet.addAll(dfs_b(oldTopSort, w, wIndex, visitedVertices));
        }

        return bSet;
    }

    private static ArrayList<Node> topologicalSort(ArrayList<Node> oldTopSort, Node from, Node to){
        ArrayList<Node> newTopSort;
        int fromIndex = findNodeIndex(oldTopSort, from);
        int toIndex = findNodeIndex(oldTopSort, to);

        if( toIndex > fromIndex ) {
            newTopSort = new ArrayList<>(oldTopSort);
        } else { // Dynamic topSort
            /*
            System.out.println("CHANGES!");
            System.out.println("From: " + from + " ord[x]: " + fromIndex);
            System.out.println("To: " + to + " ord[y]: " + toIndex);
            System.out.println("OldTopSort: " + oldTopSort);
            */
            Set<Node> fSet = dfs_f(oldTopSort, to, fromIndex, new HashSet<Node>());
            Set<Node> bSet = dfs_b(oldTopSort, from, toIndex, new HashSet<Node>());
            /*
            System.out.println("dfs_f: " + fSet);
            System.out.println("dfs_b: " + bSet);
            */
            newTopSort = reorderTopSort(oldTopSort, fSet, bSet);
            // System.out.println("NewTopSort: " + newTopSort);
        }

        // System.out.println("TopSort: " + newTopSort);
        return newTopSort;
    }

    private static ArrayList<Node> topologicalSort(BBInstance instance){
        // General vars
        /* OLD SOLUTION
        MultiGraph graphToSort = (MultiGraph) Graphs.clone(instance.getInitialStateSimple());
        */
        MultiGraph graphToSort = GraphUtil.clone(instance.getInitialStateSimple());
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

    private static void getLongestPath(BBInstance instance, ArrayList<Node> topSort){
        // Set longest path for all nodes
        //System.out.println("TopSort: " + topSort);
        for(Node gn : topSort){
            // System.out.println("Calculating releaseDate for node: " + gn);
            // System.out.println("Which has " + gn.getEnteringEdgeSet().size() + " entering edges!");
            if( gn.getEnteringEdgeSet().size() == 0 ) {
                gn.addAttribute("releaseDate", 0);
                instance.getInitialStateSimple().getNode(gn.getId()).addAttribute("releaseDate", 0);
            } else {
                Integer max = 0;
                for(Edge e : gn.getEnteringEdgeSet()) {
                    //System.out.println("Getting releaseDate and processingTime for node: " + e.getSourceNode().getId());
                    //System.out.println("ReleaseDate: " + e.getSourceNode().getAttribute("releaseDate"));
                    //System.out.println("ProcessingTime: " + e.getSourceNode().getAttribute("processingTime"));
                    if ((Integer) e.getSourceNode().getAttribute("releaseDate") + (Integer) e.getSourceNode().getAttribute("processingTime") > max)
                        max = (Integer) e.getSourceNode().getAttribute("releaseDate") + (Integer) e.getSourceNode().getAttribute("processingTime");
                }
                gn.addAttribute("releaseDate", max);
                instance.getInitialStateSimple().getNode(gn.getId()).addAttribute("releaseDate", max);
            }
        }
    }

    private static Integer getLowerBound(BBInstance instance) {
        return instance.getInitialStateSimple().getNode("SINK").getAttribute("releaseDate");
    }
}