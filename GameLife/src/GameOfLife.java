import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import javax.swing.*;


public class GameOfLife extends JFrame {
    private static final Object lock = new Object();
    private static final int CELL_SIZE = 10;
    private boolean[][] field;
    private int rows;
    private int cols;
    private volatile boolean running;


    public GameOfLife(int rows, int cols) {
        super("Игра-симулятор «Жизнь»");
        this.rows = rows;
        this.cols = cols;
        field = new boolean[rows][cols];

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(cols * CELL_SIZE, rows * CELL_SIZE);
        setLocationRelativeTo(null);
        setResizable(false);
        running = false;
    }

    private void draw() {
        SwingUtilities.invokeLater(() -> {
            Scene panel = new Scene(field, rows, cols);
            setContentPane(panel);
            setVisible(true);
        });
    }

    private boolean updateField() {
        boolean[][] newField = new boolean[rows][cols];
        boolean hasChanges = false;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int aliveNeighbours = countAliveNeighbours(row, col);
                if (field[row][col]) {
                    newField[row][col] = (aliveNeighbours == 2 || aliveNeighbours == 3);
                } else {
                    newField[row][col] = (aliveNeighbours == 3);
                }
                if (newField[row][col] != field[row][col]) {
                    hasChanges = true;
                }
            }
        }
        field = newField;
        return hasChanges;
    }


    private int countAliveNeighbours(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                int neighbourRow = (row + i + rows) % rows;
                int neighbourCol = (col + j + cols) % cols;
                if (!(i == 0 && j == 0) && field[neighbourRow][neighbourCol]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void loadInitialConfig(String filePath, int startX, int startY) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner scanner = new Scanner(file);

        int currentRow = startX;
        int currentCol = startY;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            for (char c : line.toCharArray()) {
                field[currentRow][currentCol++] = (c == '1');
            }
            currentRow++;
            currentCol = startY;
        }

        scanner.close();
    }

    public void stop() {
        running = false;
    }
    public void start() {
        if (running) {
            System.out.println("Игра уже запущена.");
            return;
        }
        running = true;
        draw();

        HashSet<String> configurations = new HashSet<>();

        Thread gameThread = new Thread(() -> {
            while (running) {
                draw();
                boolean hasChanges = updateField();

                if (!hasChanges || configurations.contains(getFieldConfigAsString())) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setVisible(false);
                    System.exit(0);
                    break;
                }

                configurations.add(getFieldConfigAsString());

                try {
                    Thread.sleep(90);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameThread.start();
    }

    private String getFieldConfigAsString() {
        StringBuilder builder = new StringBuilder();

        for (boolean[] row : field) {
            for (boolean cell : row) {
                builder.append(cell ? "1" : "0");
            }
            builder.append("\n");
        }

        return builder.toString();
    }


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Введите путь файла исходной колонии: ");
        String file = in.nextLine();
        System.out.println("Введите количество рядов: ");
        int rows = in.nextInt();
        System.out.println("Введите количество столбцов: ");
        int cols = in.nextInt();
        System.out.println("Введите координату x: ");
        int x = in.nextInt();
        System.out.println("Введите координату y: ");
        int y = in.nextInt();
        GameOfLife game = new GameOfLife(rows, cols);
        try {
            game.loadInitialConfig(file, x, y);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        in.nextLine();
        game.draw();

        Thread consoleThread = new Thread(() -> {
            while (true) {
                System.out.println("Введите команду (start/stop/exit): ");
                String command = in.nextLine();

                synchronized (lock) {
                    switch (command.toLowerCase()) {
                        case "start":
                            game.start();
                            break;
                        case "stop":
                            game.stop();
                            break;
                        case "exit":
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Неизвестная команда.");
                            break;
                    }
                }
            }
        });

        consoleThread.start();
    }
}
