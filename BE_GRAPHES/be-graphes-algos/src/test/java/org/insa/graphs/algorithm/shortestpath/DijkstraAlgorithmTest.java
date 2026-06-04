package org.insa.graphs.algorithm.shortestpath;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.io.BinaryGraphReader;
import org.insa.graphs.model.io.GraphReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class DijkstraAlgorithmTest {

    private static final double EPSILON = 1e-6;
    private static final double MAP_TOLERANCE = 1.0;

    private static final int ORIGIN = 1000;
    private static final int DESTINATION = 50000;

    private static Graph graph;

    @BeforeClass
    public static void initAll() throws Exception {
        try (final GraphReader reader = new BinaryGraphReader(new DataInputStream(
                new BufferedInputStream(new FileInputStream(findBelgiumMap()))))) {
            graph = reader.read();
        }
    }

    private static String findBelgiumMap() {
        final Path[] candidates = new Path[] { Paths.get("Maps", "belgium.mapgr"),
                Paths.get("..", "Maps", "belgium.mapgr"),
                Paths.get("..", "..", "Maps", "belgium.mapgr") };

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toString();
            }
        }
        throw new IllegalStateException("Impossible de trouver Maps/belgium.mapgr. "
                + "Lancez les tests depuis la racine BE-Graphes ou depuis be-graphes-algos.");
    }

    private ShortestPathSolution runDijkstra(int origin, int destination,
            ArcInspector inspector) {
        ShortestPathData data = new ShortestPathData(graph, graph.get(origin),
                graph.get(destination), inspector);
        return new DijkstraAlgorithm(data).run();
    }

    private static boolean isPathValid(org.insa.graphs.model.Path path) {
        if (path == null) {
            return false;
        }
        List<Arc> arcs = path.getArcs();
        if (path.getOrigin() == null) {
            return arcs.isEmpty();
        }
        if (arcs.isEmpty()) {
            return true;
        }
        if (!arcs.get(0).getOrigin().equals(path.getOrigin())) {
            return false;
        }
        for (int i = 0; i < arcs.size() - 1; ++i) {
            if (!arcs.get(i).getDestination().equals(arcs.get(i + 1).getOrigin())) {
                return false;
            }
        }
        return true;
    }

    private static Node getPathDestination(org.insa.graphs.model.Path path) {
        List<Arc> arcs = path.getArcs();
        if (arcs.isEmpty()) {
            return path.getOrigin();
        }
        return arcs.get(arcs.size() - 1).getDestination();
    }

    private static double computeLength(org.insa.graphs.model.Path path) {
        double length = 0.0;
        for (Arc arc : path.getArcs()) {
            length += arc.getLength();
        }
        return length;
    }

    private static double computeMinimumTravelTime(org.insa.graphs.model.Path path) {
        double travelTime = 0.0;
        for (Arc arc : path.getArcs()) {
            travelTime += arc.getMinimumTravelTime();
        }
        return travelTime;
    }

    private static double computeCost(org.insa.graphs.model.Path path,
            ArcInspector inspector) {
        double cost = 0.0;
        for (Arc arc : path.getArcs()) {
            cost += inspector.getCost(arc);
        }
        return cost;
    }

    private static void assertOptimalValidPath(ShortestPathSolution solution) {
        assertEquals(Status.OPTIMAL, solution.getStatus());
        assertNotNull(solution.getPath());
        assertTrue(isPathValid(solution.getPath()));
    }

    @Test
    public void testBelgiumMapIsLoadedFromMapsFolder() {
        assertEquals("BE", graph.getMapId());
        assertEquals("Belgique", graph.getMapName());
        assertEquals(1038329, graph.size());
    }

    @Test
    public void testDijkstraFindsShortestPathOnBelgiumInDistanceMode() {
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(0);
        ShortestPathSolution solution = runDijkstra(ORIGIN, DESTINATION, inspector);

        assertOptimalValidPath(solution);
        assertEquals(graph.get(ORIGIN), solution.getPath().getOrigin());
        assertEquals(graph.get(DESTINATION), getPathDestination(solution.getPath()));
        assertEquals(13191.041, computeLength(solution.getPath()), MAP_TOLERANCE);
        assertEquals(computeLength(solution.getPath()),
                computeCost(solution.getPath(), inspector), MAP_TOLERANCE);
    }

    @Test
    public void testDijkstraFindsFastestPathOnBelgiumInTimeMode() {
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(2);
        ShortestPathSolution solution = runDijkstra(ORIGIN, DESTINATION, inspector);

        assertOptimalValidPath(solution);
        assertEquals(graph.get(ORIGIN), solution.getPath().getOrigin());
        assertEquals(graph.get(DESTINATION), getPathDestination(solution.getPath()));
        assertEquals(19660.322, computeLength(solution.getPath()), MAP_TOLERANCE);
        assertEquals(862.108, computeMinimumTravelTime(solution.getPath()),
                MAP_TOLERANCE);
        assertEquals(computeMinimumTravelTime(solution.getPath()),
                computeCost(solution.getPath(), inspector), MAP_TOLERANCE);
    }

    

    @Test
    public void testDijkstraHandlesSameOriginAndDestinationOnBelgiumMap() {
        ShortestPathSolution solution =
                runDijkstra(ORIGIN, ORIGIN, ArcInspectorFactory.getAllFilters().get(0));

        assertOptimalValidPath(solution);
        assertEquals(graph.get(ORIGIN), solution.getPath().getOrigin());
        assertEquals(graph.get(ORIGIN), getPathDestination(solution.getPath()));
        assertEquals(0, solution.getPath().getArcs().size());
        assertEquals(0.0, computeLength(solution.getPath()), EPSILON);
    }

    @Test
    public void testDijkstraPedestrianFastestPathBetween3420And3421IsInfeasible() {
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(3);
        ShortestPathSolution solution = runDijkstra(3420, 3421, inspector);

        assertEquals(Status.INFEASIBLE, solution.getStatus());
        assertNull(solution.getPath());
    }
}
