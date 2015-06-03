package utils;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

public class GraphUtil {

    public static MultiGraph clone( MultiGraph mg ){
        MultiGraph newMG = new MultiGraph( mg.getId() );

        for( String s : mg.getAttributeKeySet() )
            newMG.addAttribute(s, mg.getAttribute(s));

        for( Node n : mg.getNodeSet() ) {
            newMG.addNode(n.getId());

            for( String s : n.getAttributeKeySet() )
                newMG.getNode(n.getId()).addAttribute( s, n.getAttribute(s));
        }

        for( Edge e : mg.getEdgeSet() ) {
            newMG.addEdge(e.getId(), e.getSourceNode().getId(), e.getTargetNode().getId(), true);

            for( String s : e.getAttributeKeySet() )
                newMG.getEdge(e.getId()).addAttribute(s, e.getAttribute(s));
        }

        return newMG;
    }
}
