package hit.lab;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GraphBuilder builder = new GraphBuilder();

        // 读取文本文件，生成有向图
//        System.out.print("请输入文本文件路径：");
//        String filePath = scanner.nextLine();
        String filePath = "/Users/isla_chen/IdeaProjects/Lab1/src/main/java/hit/lab/EasyTest.txt";
        builder.buildGraphFromFile(filePath);

        // 图形化展示
        builder.showDirectedGraph();

        // 功能菜单
        while (true) {
            System.out.println("\n功能菜单：");
            System.out.println("1. 查询桥接词");
            System.out.println("2. 生成新文本");
            System.out.println("3. 计算最短路径");
            System.out.println("4. 计算PageRank");
            System.out.println("5. 随机游走");
            System.out.println("0. 退出");

            System.out.print("请选择功能编号：");

            while (!scanner.hasNextInt()) {
                System.out.println("输入无效！请输入数字编号！");
                scanner.nextLine(); // 清空错误输入
                System.out.print("请选择功能编号：");
            }

            int choice = scanner.nextInt();
            scanner.nextLine(); // 吃掉换行符

            switch (choice) {
                case 1 -> {
                    System.out.print("请输入两个单词：");
                    String w1 = scanner.next();
                    String w2 = scanner.next();
                    String result = builder.queryBridgeWords(w1, w2);
                    System.out.println(result);
                }
                case 2 -> {
                    System.out.print("请输入新文本：");
                    String text = scanner.nextLine();
                    String newText = builder.generateNewText(text);
                    System.out.println("生成的新文本：" + newText);
                }
                case 3 -> {
                    System.out.print("请输入一个单词（或两个单词，空格分隔）：");
                    String line = scanner.nextLine();
                    String[] words = line.toLowerCase().split("\\s+");

                    if (words.length == 1) {
                        System.out.print("想查看几条随机路径？输入个数字：");
                        int count = scanner.nextInt();
                        scanner.nextLine();
                        String result = builder.calcPathsToRandomTargets(words[0], count);
                        System.out.println(result);
                    } else if (words.length == 2) {
                        String result = builder.calcShortestPath(words[0], words[1]);
                        System.out.println(result);
                    } else {
                        System.out.println("输入格式不正确！");
                    }
                }
                case 4 -> {
                    builder.calcPageRank();
                    builder.calcTFPageRank();
                }
                case 5 -> {
                    String walkResult = builder.randomWalk();
                    System.out.println(walkResult);
                }
                case 0 -> {
                    System.out.println("程序结束。");
                    return;
                }
                default -> System.out.println("输入错误，请重新选择！");
            }
        }
    }
}

