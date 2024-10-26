package com.buaisociety.pacman;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class Clusters {

    /**
     * Identifies clusters of points (pellets) within a given distance threshold.
     *
     * @param points           List of points (Vector2i) representing pellet positions.
     * @param distanceThreshold The maximum distance to consider points as part of the same cluster.
     * @return A list of clusters, where each cluster is a list of Vector2i points.
     */
    public List<List<Vector2i>> identifyClusters(List<Vector2i> points, double distanceThreshold) {
        List<List<Vector2i>> clusters = new ArrayList<>();
        boolean[] visited = new boolean[points.size()];

        for (int i = 0; i < points.size(); i++) {
            if (!visited[i]) {
                List<Vector2i> cluster = new ArrayList<>();
                findCluster(points, i, visited, cluster, distanceThreshold);
                clusters.add(cluster);
            }
        }
        return clusters;
    }

    /**
     * Recursively finds all points in the same cluster as the given point.
     *
     * @param points           List of points (Vector2i) to check.
     * @param index            The index of the point to check.
     * @param visited          Array to track visited points.
     * @param cluster          List to accumulate points in the current cluster.
     * @param distanceThreshold The maximum distance to consider points as part of the same cluster.
     */
    private void findCluster(List<Vector2i> points, int index, boolean[] visited, List<Vector2i> cluster, double distanceThreshold) {
        visited[index] = true;
        cluster.add(points.get(index));

        Vector2i currentPoint = points.get(index);
        for (int i = 0; i < points.size(); i++) {
            if (!visited[i]) {
                Vector2i neighborPoint = points.get(i);
                if (currentPoint.distance(neighborPoint) <= distanceThreshold) {
                    findCluster(points, i, visited, cluster, distanceThreshold);
                }
            }
        }
    }
}
