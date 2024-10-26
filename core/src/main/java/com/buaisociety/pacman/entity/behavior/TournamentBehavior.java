package com.buaisociety.pacman.entity.behavior;


import com.cjcrafter.neat.compute.Calculator;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.buaisociety.pacman.maze.Maze;
import com.buaisociety.pacman.maze.Tile;
import com.buaisociety.pacman.maze.TileState;
import com.buaisociety.pacman.sprite.DebugDrawing;
import com.cjcrafter.neat.Client;
import com.buaisociety.pacman.entity.Direction;
import com.buaisociety.pacman.entity.Entity;
import com.buaisociety.pacman.entity.PacmanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import java.util.ArrayList;
import com.buaisociety.pacman.Searcher;
import com.buaisociety.pacman.Clusters;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Random;
import java.util.function.Predicate;
import java.util.Map;


public class TournamentBehavior implements Behavior {

    private final Calculator calculator;

    private int previousScore = 0;
    private int framesSinceScoreUpdate = 0;

    private @Nullable PacmanEntity pacman;
    private int pelletCount = 0; // Track the number of pellets collected
    // Score modifiers help us maintain "multiple pools" of points.
    // This is great for training, because we can take away points from
    // specific pools of points instead of subtracting from all.
    private int scoreModifier = 0;
    private int lastDirection = 0;



    public TournamentBehavior(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Returns the desired direction that the entity should move towards.
     *
     * @param entity the entity to get the direction for
     * @return the desired direction for the entity
     */
    @NotNull
    @Override
    public Direction getDirection(@NotNull Entity entity) {
        // --- DO NOT REMOVE ---
        if (pacman == null) {
            pacman = (PacmanEntity) entity;
        }

        int newScore = pacman.getMaze().getLevelManager().getScore();
        if (previousScore != newScore) {
            previousScore = newScore;
            framesSinceScoreUpdate = 0;
        } else {
            framesSinceScoreUpdate++;
        }

        if (framesSinceScoreUpdate > 60 * 40) {
            pacman.kill();
            framesSinceScoreUpdate = 0;
        }
        // --- END OF DO NOT REMOVE ---

        // TODO: Put all your code for info into the neural network here
// We are going to use these directions a lot for different inputs. Get them all once for clarity and brevity
        Direction forward = pacman.getDirection();
        Direction left = pacman.getDirection().left();
        Direction right = pacman.getDirection().right();
        Direction behind = pacman.getDirection().behind();


        //pacman.getTilePosition().x()
        //Tile tile=pacman.getMaze().getTile(pacman.getTilePosition());
        //pacman.getMaze().getEntity()
        //entityType
        //TileState.WALL


        // Input nodes 1, 2, 3, and 4 show if the pacman can move in the forward, left, right, and behind directions
        boolean canMoveForward = pacman.canMove(forward);
        boolean canMoveLeft = pacman.canMove(left);
        boolean canMoveRight = pacman.canMove(right);
        boolean canMoveBehind = pacman.canMove(behind);

        // Calculate nearest pellets
//        if (lastDirection==0 && canMoveForward==true) return forward;
//        if (lastDirection==1 && canMoveLeft==true) return left;
//        if (lastDirection==2 && canMoveRight==true) return right;
//        if (lastDirection==3 && canMoveBehind==true) return behind;
//
//        //if (pelletCount==0) {
//        Random random = new Random();
//        int rand=random.nextInt(4);
//        if (rand==0) {
//            lastDirection=0;
//            return forward;
//        }
//        if (rand==1) {
//            lastDirection=1;
//            return left;
//        }
//        if (rand==2) {
//            lastDirection=2;
//            return right;
//        }
//        if (rand==3) {
//            lastDirection=3;
//            return behind;
//        }
        //}
//        if (pelletCount < 30) {
//                        Map<Direction, Searcher.SearchResult> nearestPellets = Searcher.findTileInAllDirections(pacman.getMaze().getTile(pacman.getTilePosition()),
//                            tile -> tile.getState() == TileState.PELLET);
//
//                        if (nearestPellets.isEmpty()) {
//                            if (canMoveForward) return forward;
//                            if (canMoveLeft) return left;
//                            if (canMoveRight) return right;
//                            if (canMoveBehind) return behind;
//                        }
//
//                    }
        Tile currentTile = pacman.getMaze().getTile(pacman.getTilePosition());
        Map<Direction, Searcher.SearchResult> nearestPellets = Searcher.findTileInAllDirections(currentTile, tile -> tile.getState() == TileState.PELLET);

        // Calculate nearest power pellets
        Map<Direction, Searcher.SearchResult> nearestPowerPellets = Searcher.findTileInAllDirections(currentTile, tile -> tile.getState() == TileState.POWER_PELLET);


        // Clusters
//
//        // Get all pellet positions in the maze
//        List<Vector2i> pelletPositions = new ArrayList<>();
//        Vector2ic dimensions = pacman.getMaze().getDimensions();
//        for (int y = 0; y < dimensions.y(); y++) {
//            for (int x = 0; x < dimensions.x(); x++) {
//                Tile tile = pacman.getMaze().getTile(x, y);
//                if (tile.getState() == TileState.PELLET) {
//                    pelletPositions.add(new Vector2i(x, y));
//                }
//            }
//        }
//        // Identify clusters of pellets
//
//        Clusters clusters = new Clusters();
//        List<List<Vector2i>> identifiedClusters = clusters.identifyClusters(pelletPositions, 4.0); // 4.0 is a distance threshold
//
//           // Initialize variables for cluster features
//            List<Vector2i> largestCluster = null;
//            List<Vector2i> nearestCluster = null;
//            double nearestDistance = Double.MAX_VALUE;
//            int largestSize = 0;
//
//            Vector2i pacmanPosition = pacman.getTilePosition();
//
//            // Analyze clusters to find the largest and nearest
//            for (List<Vector2i> cluster : identifiedClusters) {
//                double distanceToCluster = pacmanPosition.distance(cluster.get(0)); // Using the first pellet in the cluster as reference
//
//                // Check for the largest cluster
//                if (cluster.size() > largestSize) {
//                    largestSize = cluster.size();
//                    largestCluster = cluster;
//                }
//
//                // Check for the nearest cluster
//                if (distanceToCluster < nearestDistance) {
//                    nearestDistance = distanceToCluster;
//                    nearestCluster = cluster;
//                }
//            }
//
//            // Extract features for the neural network
//            double nearestClusterSize = nearestCluster != null ? nearestCluster.size() : 0;
//            double nearestClusterDistance = nearestCluster != null ? nearestDistance : Double.MAX_VALUE;
//            double largestClusterSize = largestCluster != null ? largestCluster.size() : 0;
//            double largestClusterDistance = largestCluster != null ? pacmanPosition.distance(largestCluster.get(0)) : Double.MAX_VALUE;

        // Calculate max distance for pellets
        int maxDistance = -1;
        for (Searcher.SearchResult result : nearestPellets.values()) {
            if (result != null) {
                maxDistance = Math.max(maxDistance, result.getDistance());
            }
        }

        // Calculate max distance for power pellets
        int maxDistPowerPellets = -1;
        for (Searcher.SearchResult result : nearestPowerPellets.values()) {
            if (result != null) {
                maxDistPowerPellets = Math.max(maxDistPowerPellets, result.getDistance());
            }
        }



        // Normalize distances for nearest pellets
        double nearestPelletForward = nearestPellets.get(forward) != null ? 1 - (double) nearestPellets.get(forward).getDistance() / maxDistance : 0;
        double nearestPelletLeft = nearestPellets.get(left) != null ? 1 - (double) nearestPellets.get(left).getDistance() / maxDistance : 0;
        double nearestPelletRight = nearestPellets.get(right) != null ? 1 - (double) nearestPellets.get(right).getDistance() / maxDistance : 0;
        double nearestPelletBehind = nearestPellets.get(behind) != null ? 1 - (double) nearestPellets.get(behind).getDistance() / maxDistance : 0;

        // Normalize distances for nearest power pellets
        double nearestPowerPelletForward = nearestPowerPellets.get(forward) != null ? 1 - (double) nearestPowerPellets.get(forward).getDistance() / maxDistPowerPellets : 0;
        double nearestPowerPelletLeft = nearestPowerPellets.get(left) != null ? 1 - (double) nearestPowerPellets.get(left).getDistance() / maxDistPowerPellets : 0;
        double nearestPowerPelletRight = nearestPowerPellets.get(right) != null ? 1 - (double) nearestPowerPellets.get(right).getDistance() / maxDistPowerPellets : 0;
        double nearestPowerPelletBehind = nearestPowerPellets.get(behind) != null ? 1 - (double) nearestPowerPellets.get(behind).getDistance() / maxDistPowerPellets : 0;

        // Pass the normalized distances to the neural network
        float[] outputs = calculator.calculate(new float[]{
            canMoveForward ? 1f : 0f,
            canMoveLeft ? 1f : 0f,
            canMoveRight ? 1f : 0f,
            canMoveBehind ? 1f : 0f,
            // MOVING
            (float)nearestPelletForward,
            (float)nearestPelletLeft,
            (float)nearestPelletRight,
            (float)nearestPelletBehind,
            // Include normalized power pellet distances as inputs
            (float)nearestPowerPelletForward,
            (float)nearestPowerPelletLeft,
            (float)nearestPowerPelletRight,
            (float)nearestPowerPelletBehind,
            // Cluster features
//            (float)nearestClusterSize,
//            (float)nearestClusterDistance,
//            (float)largestClusterSize,
//            (float)largestClusterDistance
        }).join();



        int index = 0;
        double max = outputs[0];
        for (int i = 1; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
                index = i;
            }
        }

        Direction newDirection = switch (index) {
            case 0 -> pacman.getDirection();
            case 1 -> pacman.getDirection().left();
            case 2 -> pacman.getDirection().right();
            case 3 -> pacman.getDirection().behind();
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };


        // Phase 3: After threshold, prioritize clusters of pellets
        /*if (lastScore >= CLUSTER_PRIORITY_SCORE_THRESHOLD) {
            List<Vector2i> pelletPositions = new ArrayList<>();
            Vector2ic dimensions = pacman.getMaze().getDimensions();
            for (int y = 0; y < dimensions.y(); y++) {
                for (int x = 0; x < dimensions.x(); x++) {
                    Tile tile = pacman.getMaze().getTile(x, y);
                    if (tile.getState() == TileState.PELLET) {
                        pelletPositions.add(new Vector2i(x, y));
                    }
                }
            }

            Clusters clusters_0 = new Clusters();
            List<List<Vector2i>> clusters = clusters_0.identifyClusters(pelletPositions, 4.0);

            List<Vector2i> largestCluster = clusters.stream()
                .max((c1, c2) -> Integer.compare(c1.size(), c2.size()))
                .orElse(null);

            if (largestCluster != null && !largestCluster.isEmpty()) {
                Vector2i target = largestCluster.get(0); // Use the first pellet as a target in the cluster
                Direction bestClusterDirection = getDirectionToTarget(currentTile, target);
                if (bestClusterDirection != null) {
                    return bestClusterDirection;
                }
            }
        }

        // Fall back to secondary direction if no cluster direction is determined
        return secondaryDirection;*/



        //client.setScore(pacman.getMaze().getLevelManager().getScore() + scoreModifier);
        return newDirection;
    }

    @Override
    public void render(@NotNull SpriteBatch batch) {
        // TODO: You can render debug information here
        /*
        if (pacman != null) {
            DebugDrawing.outlineTile(batch, pacman.getMaze().getTile(pacman.getTilePosition()), Color.RED);
            DebugDrawing.drawDirection(batch, pacman.getTilePosition().x() * Maze.TILE_SIZE, pacman.getTilePosition().y() * Maze.TILE_SIZE, pacman.getDirection(), Color.RED);
        }
         */
    }

    public void procedure (){

    }

    /*private Direction getDirectionToTarget(Tile startTile, Vector2i targetPosition) {
        Direction[] directions = Direction.values();
        for (Direction direction : directions) {
            Tile nextTile = pacman.getMaze().getTile(startTile.getPosition().add(direction.vector()));
            if (nextTile != null && nextTile.getPosition().equals(targetPosition)) {
                return direction;
            }
        }
        return null;
    }*/
}

