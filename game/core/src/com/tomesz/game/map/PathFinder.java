package com.tomesz.game.map;

import com.badlogic.gdx.math.Vector2;

import java.util.*;

public class PathFinder {
    static class Node {
        Vector2 position;
        int gCost; // koszt ruchu od początku do tego węzła
        int hCost; // heurystyczna odległość do celu
        Node parent;

        Node(Vector2 position, int gCost, int hCost, Node parent) {
            this.position = position;
            this.gCost = gCost;
            this.hCost = hCost;
            this.parent = parent;
        }

        int getFCost() {
            return gCost + hCost;
        }
    }

    public enum NeighborsMode {
        STANDARD,
        WITH_CORRECTION
    }

    public List<Vector2> findPath(Vector2 start, Vector2 end, HashMap<Vector2, Integer> map) {
        return findPath(start, end, map, NeighborsMode.WITH_CORRECTION);
    }

    public List<Vector2> findPath(Vector2 start, Vector2 end, HashMap<Vector2, Integer> map, NeighborsMode mode) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getFCost));
        HashMap<Vector2, Node> nodeMap = new HashMap<>();

        Node startNode = new Node(start, 0, heuristic(start, end), null);
        openSet.add(startNode);
        nodeMap.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.position.equals(end)) {
                return reconstructPath(current);
            }

            for (Vector2 neighborPos : getNeighbors(current.position, current.parent != null ? current.parent.position : current.position, mode)) {
                if (!map.containsKey(neighborPos) || map.get(neighborPos) == 1) {
                    continue; // pomijamy niedostępne punkty
                }

                int tentativeGCost = current.gCost + 1; // koszt ruchu do sąsiada to 1, gdyż sąsiad jest zawsze sąsiedni

                Node neighbor = nodeMap.getOrDefault(neighborPos, new Node(neighborPos, Integer.MAX_VALUE, Integer.MAX_VALUE, null));
                if (tentativeGCost < neighbor.gCost) {
                    neighbor.parent = current;
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = heuristic(neighborPos, end);
                    if (!nodeMap.containsKey(neighborPos)) {
                        openSet.add(neighbor);
                        nodeMap.put(neighborPos, neighbor);
                    }
                }
            }
        }

        return new ArrayList<>(); // jeśli nie udało się znaleźć ścieżki
    }

    private static int heuristic(Vector2 a, Vector2 b) {
        return (int)(Math.abs(a.x - b.x) + Math.abs(a.y - b.y)); // metryka Manhattan
    }

    private static List<Vector2> getNeighbors(Vector2 position, Vector2 previousPosition, NeighborsMode mode) {
        List<Vector2> neighbors = new ArrayList<>();

        if (mode == NeighborsMode.WITH_CORRECTION) {
            float deltaX = position.x - previousPosition.x;
            float deltaY = position.y - previousPosition.y;

            if (deltaX != 0) {
                neighbors.add(new Vector2(position.x, position.y + 1));
                neighbors.add(new Vector2(position.x, position.y - 1));
            } else if (deltaY != 0) {
                neighbors.add(new Vector2(position.x + 1, position.y));
                neighbors.add(new Vector2(position.x - 1, position.y));
            } else {
                neighbors.add(new Vector2(position.x + 1, position.y));
                neighbors.add(new Vector2(position.x - 1, position.y));
                neighbors.add(new Vector2(position.x, position.y + 1));
                neighbors.add(new Vector2(position.x, position.y - 1));
            }
        } else {
            neighbors.add(new Vector2(position.x + 1, position.y));
            neighbors.add(new Vector2(position.x - 1, position.y));
            neighbors.add(new Vector2(position.x, position.y + 1));
            neighbors.add(new Vector2(position.x, position.y - 1));
        }

        return neighbors;
    }

    private static List<Vector2> reconstructPath(Node node) {
        List<Vector2> path = new ArrayList<>();
        while (node != null) {
            path.add(node.position);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
