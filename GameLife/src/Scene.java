import javax.swing.*;
import java.awt.*;

public class Scene extends JPanel {

    private static final int CELL_SIZE = 10;
    private static final Color DEAD_COLOR = Color.WHITE;
    private static final Color ALIVE_COLOR = Color.BLACK;

    private boolean[][] field;
    private int rows;
    private int cols;

    public Scene(boolean[][] field, int rows, int cols) {
        this.field = field;
        this.rows = rows;
        this.cols = cols;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < cols; ++col) {
                Color color = field[row][col] ? ALIVE_COLOR : DEAD_COLOR;
                g.setColor(color);
                g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }
}