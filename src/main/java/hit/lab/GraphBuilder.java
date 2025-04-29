package hit.lab;

import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class GraphBuilder {

    private final Graph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
    private final Random random = new Random();
    private final Map<String, Integer> wordFrequency = new HashMap<>();


    public void buildGraphFromFile(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            List<String> words = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase().replaceAll("[^a-zA-Z ]", " ");
                String[] tokens = line.split("\\s+");
                for (String word : tokens) {
                    if (!word.isEmpty()) {
                        words.add(word);
                        wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                    }
                }
            }
            for (int i = 0; i < words.size() - 1; i++) {
                String from = words.get(i);
                String to = words.get(i + 1);
                graph.addVertex(from);
                graph.addVertex(to);
                DefaultWeightedEdge edge = graph.getEdge(from, to);
                if (edge == null) {
                    edge = graph.addEdge(from, to);
                    graph.setEdgeWeight(edge, 1);
                } else {
                    graph.setEdgeWeight(edge, graph.getEdgeWeight(edge) + 1);
                }
            }
            System.out.println("图构建完成！顶点数：" + graph.vertexSet().size() + "，边数：" + graph.edgeSet().size());
        } catch (FileNotFoundException e) {
            System.out.println("文件读取失败：" + e.getMessage());
        }
    }

    public void showDirectedGraph() {
        System.out.println("\n有向图结构：");
        for (String v : graph.vertexSet()) {
            Set<DefaultWeightedEdge> edges = graph.outgoingEdgesOf(v);
            for (DefaultWeightedEdge e : edges) {
                String target = graph.getEdgeTarget(e);
                double weight = graph.getEdgeWeight(e);
                System.out.println(v + " --(" + weight + ")--> " + target);
            }
        }
        GraphViewer.showGraph(graph);  // 调用图形化显示
    }

    public String queryBridgeWords(String word1, String word2) {
        if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) {
            return "No \"" + word1 + "\" or \"" + word2 + "\" in the graph!";
        }
        Set<String> bridgeWords = new HashSet<>();
        for (DefaultWeightedEdge e : graph.outgoingEdgesOf(word1)) {
            String mid = graph.getEdgeTarget(e);
            if (graph.containsEdge(mid, word2)) {
                bridgeWords.add(mid);
            }
        }
        if (bridgeWords.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        }
        return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: " + bridgeWords;
    }

    public String generateNewText(String inputText) {
        String[] tokens = inputText.toLowerCase().replaceAll("[^a-zA-Z ]", " ").split("\\s+");
        StringBuilder result = new StringBuilder(tokens[0]);
        for (int i = 0; i < tokens.length - 1; i++) {
            String word1 = tokens[i];
            String word2 = tokens[i + 1];

            // 如果word1或word2不存在，跳过查桥接词，直接拼接
            if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) {
                result.append(" ").append(word2);
                continue;
            }

            Set<String> bridgeWords = new HashSet<>();
            for (DefaultWeightedEdge e : graph.outgoingEdgesOf(word1)) {
                String mid = graph.getEdgeTarget(e);
                if (graph.containsEdge(mid, word2)) {
                    bridgeWords.add(mid);
                }
            }

            // 如果有桥接词，随机选一个/都连上
            if (!bridgeWords.isEmpty()) {
                int index = random.nextInt(bridgeWords.size());
                String bridge  = bridgeWords.stream().toList().get(index);
                result.append(" ").append(bridge);
//                List<String> bridges = bridgeWords.stream().toList();
//                for(String bridge:bridges){
//                    result.append(" ").append(bridge);
//                }
            }

            result.append(" ").append(word2);
        }
        return result.toString();
    }


    public String calcShortestPath(String word1, String word2) {
        if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) {
            return "No \"" + word1 + "\" or \"" + word2 + "\" in the graph!";
        }
        DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(graph);
        var path = dijkstra.getPath(word1, word2);
        if (path == null) {
            return "No path from \"" + word1 + "\" to \"" + word2 + "\".";
        }
        return "最短路径：" + path.getVertexList() + "，路径长度：" + path.getWeight();
    }

    public String calcPathsToRandomTargets(String startWord, int count) {
        if (!graph.containsVertex(startWord)) {
            return "图中不存在单词：" + startWord;
        }

        List<String> vertices = new ArrayList<>(graph.vertexSet());
        vertices.remove(startWord);
        if (vertices.isEmpty()) return "图中没有其他单词可达。";

        Collections.shuffle(vertices, random);
        count = Math.min(count, vertices.size());

        DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(graph);
        StringBuilder result = new StringBuilder("从 \"" + startWord + "\" 出发到任意单词的最短路径：\n");

        for (int i = 0; i < count; i++) {
            String target = vertices.get(i);
            var path = dijkstra.getPath(startWord, target);
            if (path == null) {
                result.append(startWord).append(" → ").append(target).append(" ：不可达\n");
            } else {
                result.append(path.getVertexList())
                        .append("，路径长度：").append(path.getWeight()).append("\n");
            }
        }
        return result.toString();
    }


    public Map<String, Double> getInitialPageRank() {
        Map<String, Double> initialPR = new HashMap<>();
        int totalFreq = wordFrequency.values().stream().mapToInt(Integer::intValue).sum();
        for (String word : graph.vertexSet()) {
            int freq = wordFrequency.getOrDefault(word, 1); // 没出现的默认1
            initialPR.put(word, (double) freq / totalFreq);
        }
        return initialPR;
    }


    public void calcPageRank() {
        PageRank<String, DefaultWeightedEdge> pr = new PageRank<>(graph, 0.85);
        System.out.println("\n【不带初始词频权重的PageRank】");
        for (String v : graph.vertexSet()) {
            System.out.printf("%-15s : %.4f\n", v, pr.getVertexScore(v));
        }
    }

    public void calcTFPageRank() {
        double d = 0.85;
        double epsilon = 0.0001;
        int maxIterations = 100;
        Map<String, Double> initialPR = getInitialPageRank();
        Map<String, Double> pr = new HashMap<>(initialPR);

        for (int iter = 0; iter < maxIterations; iter++) {
            Map<String, Double> newPr = new HashMap<>();
            double diff = 0.0;

            for (String v : graph.vertexSet()) {
                double rank = (1 - d) * initialPR.get(v);
                for (DefaultWeightedEdge e : graph.incomingEdgesOf(v)) {
                    String src = graph.getEdgeSource(e);
                    double weight = graph.getEdgeWeight(e);
                    double outWeight = graph.outgoingEdgesOf(src).stream()
                            .mapToDouble(graph::getEdgeWeight).sum();
                    rank += d * pr.get(src) * (weight / outWeight);
                }
                newPr.put(v, rank);
                diff += Math.abs(rank - pr.get(v));
            }

            pr = newPr;
            if (diff < epsilon) break;
        }

        // 排序打印
        System.out.println("\n【带初始词频权重的PageRank】：");
        pr.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> System.out.printf("%-15s : %.4f\n", entry.getKey(), entry.getValue()));

    }


    public String randomWalk() {
        List<String> vertices = new ArrayList<>(graph.vertexSet());
        StringBuilder walk = new StringBuilder();
        Set<DefaultWeightedEdge> visitedEdges = new HashSet<>();

        String current = vertices.get(random.nextInt(vertices.size()));
        walk.append(current);
        while (true) {
            Set<DefaultWeightedEdge> edges = graph.outgoingEdgesOf(current);
            List<DefaultWeightedEdge> availableEdges = edges.stream().filter(e -> !visitedEdges.contains(e)).toList();
            if (availableEdges.isEmpty()) break;

            DefaultWeightedEdge chosen = availableEdges.get(random.nextInt(availableEdges.size()));
            visitedEdges.add(chosen);
            current = graph.getEdgeTarget(chosen);
            walk.append(" -> ").append(current);
        }
        return "随机游走路径：" + walk;
    }

    public Graph<String, DefaultWeightedEdge> getGraph() {
        return graph;
    }
}
