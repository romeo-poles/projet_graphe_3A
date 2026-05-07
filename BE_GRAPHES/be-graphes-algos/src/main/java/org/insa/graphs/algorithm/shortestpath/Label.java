package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;

public class Label implements Comparable<Label> {

    private final Node node;
    private boolean marked;
    private double cost;
    private Arc predecessor;

    public Label(Node node) {
        this.node = node;
        this.marked = false;
        this.cost = Double.POSITIVE_INFINITY;
        this.predecessor = null;
    }

    public Node getNode() {
        return node;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public double getRealizedCost() {
        return cost;
    }

    public void setRealizedCost(double cost) {
        this.cost = cost;
    }

    public Arc getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(Arc predecessor) {
        this.predecessor = predecessor;
    }

    public double getEstimatedCost() {
        return 0.0;
    }

    public double getTotalCost() {
        return this.getRealizedCost() + this.getEstimatedCost();
    }

    @Override
    public int compareTo(Label other) {
        int cmp = Double.compare(this.getTotalCost(), other.getTotalCost());
        if (cmp != 0) {
            return cmp;
        }

        cmp = Double.compare(this.getEstimatedCost(), other.getEstimatedCost());
        if (cmp != 0) {
            return cmp;
        }

        return Integer.compare(this.node.getId(), other.node.getId());
    }
}
