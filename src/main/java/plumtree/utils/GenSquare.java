package plumtree.utils;

import java.io.FileWriter;
import java.io.IOException;

public class GenSquare {

    public static void main(String[] args) throws IOException {

        int side = Integer.parseInt(args[0]);
        int dist = Integer.parseInt(args[1]);

        int nNodes = side * side;

        int[][] matrix = new int[nNodes][nNodes];

        for (int i = 0; i < nNodes; i++) {
            int ix = i % side;
            int iy = i / side;
            for (int j = i; j < nNodes; j++) {
                if (i == j) {
                    matrix[i][j] = -1;
                } else {
                    int jx = j % side;
                    int jy = j / side;
                    double sqrt = Math.sqrt(Math.pow(jx - ix, 2) + Math.pow(jy - iy, 2));
                    matrix[i][j] = matrix[j][i] = (int) (sqrt * dist);
                }
            }
        }
        FileWriter fileWriter = new FileWriter("docker/latency_"  + side + "_" + dist);
        for (int i = 0; i < nNodes; i++) {
            for (int j = 0; j < nNodes; j++) {
                fileWriter.write(matrix[i][j] + "\t");
                System.out.print(matrix[i][j] + "\t");
            }
            fileWriter.write("\n");
            System.out.println();
        }
        fileWriter.flush();
        fileWriter.close();

    }
}
