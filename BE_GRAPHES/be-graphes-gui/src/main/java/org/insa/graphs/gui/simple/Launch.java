package org.insa.graphs.gui.simple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.algorithm.shortestpath.AStarAlgorithm;
import org.insa.graphs.algorithm.shortestpath.DijkstraAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.ShortestPathSolution;
import org.insa.graphs.gui.drawing.Drawing;
import org.insa.graphs.gui.drawing.components.BasicDrawing;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.io.BinaryGraphReader;
import org.insa.graphs.model.io.GraphReader;

public class Launch {

    private static final int ORIGIN = 111111;
    private static final int DESTINATION = 333333;

    /**
     * Create a new Drawing inside a JFrame and return it.
     *
     * @return The created drawing.
     * @throws Exception if something wrong happens when creating the graph.
     */
    public static Drawing createDrawing() throws Exception {
        final BasicDrawing drawing = new BasicDrawing();

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("BE Graphes - Launch Belgium");
                frame.setLayout(new BorderLayout());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                frame.setSize(new Dimension(900, 700));
                frame.setContentPane(drawing);
                frame.validate();
            }
        });

        return drawing;
    }

    private static String findBelgiumMap() {
        String[] candidates = {
                "Maps/belgium.mapgr",
                "../Maps/belgium.mapgr",
                "../../Maps/belgium.mapgr"
        };

        for (String candidate: candidates) {
            if (new File(candidate).exists()) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Impossible de trouver Maps/belgium.mapgr. "
                + "Place le dossier Maps à la racine du projet BE-Graphes."
        );
    }

    private static double computeLength(Path path) {
        double length = 0.0;

        for (Arc arc: path.getArcs()) {
            length += arc.getLength();
        }

        return length;
    }

    private static double computeMinimumTravelTime(Path path) {
        double time = 0.0;

        for (Arc arc: path.getArcs()) {
            time += arc.getMinimumTravelTime();
        }

        return time;
    }

    private static void printSolution(String name, ShortestPathSolution solution) {
        System.out.println();
        System.out.println(name);
        System.out.println("Statut : " + solution.getStatus());

        if (solution.getPath() == null) {
            System.out.println("Aucun chemin trouvé.");
        }
        else {
            System.out.println("Longueur : " + computeLength(solution.getPath()) + " m");
            System.out.println("Temps minimal : " + computeMinimumTravelTime(solution.getPath()) + " s");
            System.out.println("Nombre d'arcs : " + solution.getPath().getArcs().size());
        }
    }

    public static void main(String[] args) throws Exception {

        final String mapName = findBelgiumMap();

        final Graph graph;

        // Create a graph reader.
        try (final GraphReader reader = new BinaryGraphReader(new DataInputStream(
                new BufferedInputStream(new FileInputStream(mapName))))) {

            // Read the graph.
            graph = reader.read();
        }

        System.out.println("Carte chargée : " + mapName);
        System.out.println("Origine : " + ORIGIN);
        System.out.println("Destination : " + DESTINATION);

        // Filter 3 = fastest path for pedestrian.
        final ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(0);

        final ShortestPathData data = new ShortestPathData(
                graph,
                graph.get(ORIGIN),
                graph.get(DESTINATION),
                inspector
        );

        // Run Dijkstra.
        final ShortestPathSolution dijkstraSolution = new DijkstraAlgorithm(data).run();

        // Run A*.
        final ShortestPathSolution aStarSolution = new AStarAlgorithm(data).run();

        printSolution("Dijkstra", dijkstraSolution);
        printSolution("A*", aStarSolution);

        // Create the drawing.
        final Drawing drawing = createDrawing();

        // Draw the graph.
        drawing.drawGraph(graph);

        // Draw the paths if they exist.
        if (dijkstraSolution.getPath() != null) {
            drawing.drawPath(dijkstraSolution.getPath(), Color.BLUE);
        }

        if (aStarSolution.getPath() != null) {
            drawing.drawPath(aStarSolution.getPath(), Color.GREEN);
        }
    }
}