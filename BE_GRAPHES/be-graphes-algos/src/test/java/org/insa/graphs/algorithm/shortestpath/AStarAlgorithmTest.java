package org.insa.graphs.algorithm.shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.GraphStatistics;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.Point;
import org.insa.graphs.model.RoadInformation;
import org.insa.graphs.model.RoadInformation.RoadType;
import org.junit.BeforeClass;
import org.junit.Test;

public class AStarAlgorithmTest {

    private static Graph graph;
    private static Node[] nodes;
    private static Arc a2b, b2d, a2c, c2d;

    @BeforeClass
    public static void initAll() {
        nodes = new Node[] {
                new Node(0, new Point(1.0f, 43.0f)),
                new Node(1, new Point(1.0001f, 43.0f)),
                new Node(2, new Point(1.0f, 43.0001f)),
                new Node(3, new Point(1.0001f, 43.0001f))
        };

        RoadInformation speed30 = new RoadInformation(RoadType.RESIDENTIAL, null, true, 30, "r30");
        RoadInformation speed90 = new RoadInformation(RoadType.MOTORWAY, null, true, 90, "r90");

        a2b = Node.linkNodes(nodes[0], nodes[1], 100, speed30, null);
        b2d = Node.linkNodes(nodes[1], nodes[3], 100, speed30, null);
        a2c = Node.linkNodes(nodes[0], nodes[2], 140, speed90, null);
        c2d = Node.linkNodes(nodes[2], nodes[3], 40, speed90, null);

        GraphStatistics.BoundingBox bb = new GraphStatistics.BoundingBox(
                new Point(0.9999f, 43.0002f), new Point(1.0002f, 42.9999f));
        GraphStatistics stats = new GraphStatistics(bb, 4, 0, 130, 140);

        graph = new Graph("test", "test", Arrays.asList(nodes), stats);
    }

    @Test
    public void testAStarMatchesDijkstraInLengthMode() {
        ShortestPathData data = new ShortestPathData(graph, nodes[0], nodes[3],
                ArcInspectorFactory.getAllFilters().get(0));

        ShortestPathSolution dijkstra = new DijkstraAlgorithm(data).run();
        ShortestPathSolution astar = new AStarAlgorithm(data).run();

        assertEquals(Status.OPTIMAL, dijkstra.getStatus());
        assertEquals(dijkstra.getStatus(), astar.getStatus());
        assertNotNull(dijkstra.getPath());
        assertNotNull(astar.getPath());
        assertEquals(dijkstra.getPath().getLength(), astar.getPath().getLength(), 1e-6);
        assertEquals(dijkstra.getPath().getArcs().size(), astar.getPath().getArcs().size());
    }

    @Test
    public void testAStarMatchesDijkstraInTimeMode() {
        ShortestPathData data = new ShortestPathData(graph, nodes[0], nodes[3],
                ArcInspectorFactory.getAllFilters().get(2));

        ShortestPathSolution dijkstra = new DijkstraAlgorithm(data).run();
        ShortestPathSolution astar = new AStarAlgorithm(data).run();

        assertEquals(Status.OPTIMAL, dijkstra.getStatus());
        assertEquals(dijkstra.getStatus(), astar.getStatus());
        assertNotNull(dijkstra.getPath());
        assertNotNull(astar.getPath());
        assertEquals(dijkstra.getPath().getMinimumTravelTime(),
                astar.getPath().getMinimumTravelTime(), 1e-6);
        assertEquals(dijkstra.getPath().getArcs().size(), astar.getPath().getArcs().size());
    }
}
