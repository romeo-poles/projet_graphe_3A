package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Collections;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.algorithm.utils.ElementNotFoundException;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;

public class DijkstraAlgorithm extends ShortestPathAlgorithm {

    public DijkstraAlgorithm(ShortestPathData data) {
        super(data);
    }

    protected Label createLabel(Node node) {
        return new Label(node);
    }

    @Override
    protected ShortestPathSolution doRun() {

        final ShortestPathData data = getInputData();
        final Graph graph = data.getGraph();

        final Label[] labels = new Label[graph.size()];
        for (int i = 0; i < graph.size(); ++i) {
            labels[i] = createLabel(graph.get(i));
        }

        final Label originLabel = labels[data.getOrigin().getId()];
        originLabel.setRealizedCost(0.0);

        notifyOriginProcessed(data.getOrigin());

        if (data.getOrigin().equals(data.getDestination())) {
            notifyDestinationReached(data.getDestination());
            return new ShortestPathSolution(data, Status.OPTIMAL,
                    new Path(graph, data.getOrigin()));
        }

        final BinaryHeap<Label> heap = new BinaryHeap<>();
        heap.insert(originLabel);

        final Label destinationLabel = labels[data.getDestination().getId()];

        while (!heap.isEmpty() && !destinationLabel.isMarked()) {
            final Label current = heap.deleteMin();

            if (current.isMarked()) {
                continue;
            }

            current.setMarked(true);
            notifyNodeMarked(current.getNode());

            for (Arc arc: current.getNode().getSuccessors()) {
                if (!data.isAllowed(arc)) {
                    continue;
                }

                final Label successor = labels[arc.getDestination().getId()];
                if (successor.isMarked()) {
                    continue;
                }

                final double oldCost = successor.getRealizedCost();
                final double newCost = current.getRealizedCost() + data.getCost(arc);

                if (newCost < oldCost) {
                    if (Double.isFinite(oldCost)) {
                        try {
                            heap.remove(successor);
                        }
                        catch (ElementNotFoundException ignored) {
                            // The label may not be in the heap any more if it has been
                            // removed by a previous update. In that case, we simply
                            // reinsert the updated label below.
                        }
                    }
                    else {
                        notifyNodeReached(successor.getNode());
                    }

                    successor.setRealizedCost(newCost);
                    successor.setPredecessor(arc);
                    heap.insert(successor);
                }
            }
        }

        if (destinationLabel.getPredecessor() == null) {
            return new ShortestPathSolution(data, Status.INFEASIBLE);
        }

        notifyDestinationReached(data.getDestination());

        final ArrayList<Arc> arcs = new ArrayList<>();
        Arc arc = destinationLabel.getPredecessor();
        while (arc != null) {
            arcs.add(arc);
            arc = labels[arc.getOrigin().getId()].getPredecessor();
        }
        Collections.reverse(arcs);

        return new ShortestPathSolution(data, Status.OPTIMAL, new Path(graph, arcs));
    }

}
