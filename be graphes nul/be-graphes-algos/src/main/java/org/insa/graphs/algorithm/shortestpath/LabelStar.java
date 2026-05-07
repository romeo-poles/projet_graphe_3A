package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Node;

public class LabelStar extends Label {

    private final double estimatedCost;

    public LabelStar(Node node, double estimatedCost) {
        super(node);
        this.estimatedCost = estimatedCost;
    }

    @Override
    public double getEstimatedCost() {
        return this.estimatedCost;
    }
}
