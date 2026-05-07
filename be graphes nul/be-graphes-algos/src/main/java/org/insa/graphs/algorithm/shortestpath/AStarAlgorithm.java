package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.algorithm.AbstractInputData.Mode;
import org.insa.graphs.model.GraphStatistics;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Point;

public class AStarAlgorithm extends DijkstraAlgorithm {

    public AStarAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
    protected Label createLabel(Node node) {
        final ShortestPathData data = getInputData();
        final Node destination = data.getDestination();

        if (node.getPoint() == null || destination.getPoint() == null) {
            return new LabelStar(node, 0.0);
        }

        final double birdFlightDistance = Point.distance(node.getPoint(), destination.getPoint());
        double estimatedCost = birdFlightDistance;

        if (data.getMode() == Mode.TIME) {
            final int maxSpeed = data.getMaximumSpeed();
            if (maxSpeed == GraphStatistics.NO_MAXIMUM_SPEED || maxSpeed <= 0) {
                estimatedCost = 0.0;
            }
            else {
                estimatedCost = birdFlightDistance * 3600.0 / (maxSpeed * 1000.0);
            }
        }

        return new LabelStar(node, estimatedCost);
    }

}
