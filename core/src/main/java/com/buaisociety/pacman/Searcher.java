package com.buaisociety.pacman;

import com.buaisociety.pacman.entity.Direction;
import com.buaisociety.pacman.maze.Maze;
import com.buaisociety.pacman.maze.Tile;
import com.buaisociety.pacman.maze.TileState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.Map;

public class Searcher {

    /**
     * Stores the result of the BFS search, including the found tile and the distance from the start tile.
     */
    public static class SearchResult {
        private final Tile tile;
        private final int distance;

        public SearchResult(Tile tile, int distance) {
            this.tile = tile;
            this.distance = distance;
        }

        public Tile getTile() {
            return tile;
        }

        public int getDistance() {
            return distance;
        }
    }

    /**
     * Performs a BFS search in each of the four directions to find tiles matching the predicate.
     *
     * @param startTile The starting tile for the BFS.
     * @param predicate The predicate to test each tile.
     * @return A Map containing the first matching tile and distance for each direction.
     */
    public static Map<Direction, SearchResult> findTileInAllDirections(@NotNull Tile startTile, @NotNull Predicate<Tile> predicate) {
        Map<Direction, SearchResult> results = new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            SearchResult result = findTileWithBFS(startTile, predicate, direction);
            if (result != null) {
                results.put(direction, result);
            }
        }

        return results;
    }

    /**
     * Performs a BFS to find the closest tile in the specified direction that matches the predicate.
     *
     * @param startTile The starting tile for the BFS.
     * @param predicate The predicate to test each tile.
     * @param direction The initial direction for the search.
     * @return The SearchResult containing the tile and distance, or null if no matching tile is found.
     */
    private static SearchResult findTileWithBFS(@NotNull Tile startTile, @NotNull Predicate<Tile> predicate, @NotNull Direction direction) {
        Queue<Vector2ic> queue = new ArrayDeque<>();
        Set<Vector2ic> visited = new HashSet<>();
        Queue<Integer> distances = new ArrayDeque<>();

        Vector2i startPosition = new Vector2i(startTile.getPosition()).add(getDirectionOffset(direction));
        queue.add(startPosition);
        visited.add(startPosition);
        distances.add(1);

        while (!queue.isEmpty()) {
            Vector2ic currentPos = queue.poll();
            int currentDistance = distances.poll();
            Tile currentTile = startTile.getMaze().getTile(new Vector2i(currentPos));

            if (predicate.test(currentTile)) {
                return new SearchResult(currentTile, currentDistance);
            }

            // Enqueue neighboring tiles only in the initial search direction
            for (Vector2i neighbor : getNeighborsInDirection(new Vector2i(currentPos), direction)) {
                Tile neighborTile = startTile.getMaze().getTile(neighbor);
                if (neighborTile.getState() == TileState.WALL) continue;

                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    distances.add(currentDistance + 1);
                }
            }
        }

        return null;  // No tile matching the predicate was found
    }

    /**
     * Returns the offset vector based on the initial direction.
     *
     * @param direction The direction for the offset.
     * @return The vector offset for moving in that direction.
     */
    private static Vector2i getDirectionOffset(Direction direction) {
        return switch (direction) {
            case UP -> new Vector2i(0, 1);
            case DOWN -> new Vector2i(0, -1);
            case LEFT -> new Vector2i(-1, 0);
            case RIGHT -> new Vector2i(1, 0);
        };
    }

    /**
     * Returns neighboring tiles based on the given initial direction.
     *
     * @param position The tile position for which to get neighbors.
     * @param direction The primary direction for the search.
     * @return A list of neighboring tile positions limited to the specified direction.
     */
    private static List<Vector2i> getNeighborsInDirection(Vector2i position, Direction direction) {
        List<Vector2i> neighbors = new ArrayList<>();
        Vector2i offset = getDirectionOffset(direction);
        neighbors.add(new Vector2i(position.x + offset.x, position.y + offset.y));
        return neighbors;
    }
}