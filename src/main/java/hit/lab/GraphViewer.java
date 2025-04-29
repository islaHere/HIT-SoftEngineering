package hit.lab;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.awt.image.BufferedImage;
import com.mxgraph.util.mxCellRenderer;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.io.File;

public class GraphViewer {

    public static void showGraph(Graph<String, DefaultWeightedEdge> graph) {
        mxGraph mxGraph = new mxGraph();
        Object parent = mxGraph.getDefaultParent();

        mxGraph.getModel().beginUpdate();
        try {
            Map<String, Object> vertexMap = new HashMap<>();
            for (String v : graph.vertexSet()) {
                vertexMap.put(v, mxGraph.insertVertex(parent, null, v, 0, 0, 80, 30));
            }
            for (DefaultWeightedEdge e : graph.edgeSet()) {
                String source = graph.getEdgeSource(e);
                String target = graph.getEdgeTarget(e);
                double weight = graph.getEdgeWeight(e);
                mxGraph.insertEdge(parent, null, weight, vertexMap.get(source), vertexMap.get(target));
            }
        } finally {
            mxGraph.getModel().endUpdate();
        }

        mxCircleLayout layout = new mxCircleLayout(mxGraph);
        layout.execute(mxGraph.getDefaultParent());

        JFrame frame = new JFrame("有向图可视化");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new mxGraphComponent(mxGraph));
        frame.setVisible(true);

        layout.execute(mxGraph.getDefaultParent());
        saveGraphImage(mxGraph, "graphs");  // 保存到项目目录下 graph_images 文件夹

    }

    public static void saveGraphImage(mxGraph graph, String directoryPath) {
        // 确保目录存在
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 获取目录下已有的图片编号
        int maxIndex = 0;
        File[] files = dir.listFiles((d, name) -> name.matches("graph_\\d+\\.png"));
        if (files != null) {
            for (File f : files) {
                String name = f.getName();
                int index = Integer.parseInt(name.replaceAll("[^0-9]", ""));
                if (index > maxIndex) {
                    maxIndex = index;
                }
            }
        }

        // 新文件名
        String fileName = "graph_" + (maxIndex + 1) + ".png";
        File imgFile = new File(dir, fileName);

        // 渲染保存
        BufferedImage image = mxCellRenderer.createBufferedImage(
                graph, null, 2, Color.WHITE, true, null);

        try {
            ImageIO.write(image, "PNG", imgFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("图像已保存到：" + imgFile.getAbsolutePath());
    }

}

