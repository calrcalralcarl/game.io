/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package javaapplication1;

/**
 *
 * @author cjmej
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class JavaApplication1 extends JFrame {

    public JavaApplication1(String playerName) {
        initUI(playerName);
    }

    private void initUI(String playerName) {
        setTitle("Bombaclat");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        Board board = new Board(playerName);
        add(board);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog(null, "Enter your name:", "Enter Name", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Your Love";
            JavaApplication1 game = new JavaApplication1(name.trim());
            game.setVisible(true);
        });
    }
}

class Board extends JPanel implements ActionListener {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int CELL_SIZE = 26;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private Shape curPiece;
    private Tetrominoes[] board;
    private int curX = 0;
    private int curY = 0;
    private String playerName;
    private int linesCleared = 0;

    private final int TARGET_LINES = 1;

    public Board(String name) {
        setFocusable(true);
        playerName = name;
        curPiece = new Shape();
        timer = new Timer(400, this);
        timer.start();
        board = new Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
        clearBoard();
        addKeyListener(new TAdapter());
        start();
    }

    public void start() {
        isStarted = true;
        isFallingFinished = false;
        linesCleared = 0;
        clearBoard();
        newPiece();
        timer.start();
    }

    private void pause() {
        if (!isStarted) return;
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BOARD_WIDTH * CELL_SIZE, BOARD_HEIGHT * CELL_SIZE);
    }

    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }
    private Tetrominoes shapeAt(int x, int y) {
        return board[y * BOARD_WIDTH + x];
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_WIDTH * BOARD_HEIGHT; ++i)
            board[i] = Tetrominoes.NoShape;
    }

    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1)) break;
            --newY;
        }
        pieceDropped();
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1))
            pieceDropped();
    }

    private void pieceDropped() {
        for (Point p : curPiece.getCoords()) {
            int x = curX + p.x;
            int y = curY - p.y;
            if (y >= 0 && x >= 0 && x < BOARD_WIDTH && y < BOARD_HEIGHT)
                board[y * BOARD_WIDTH + x] = curPiece.getShape();
        }
        removeFullLines();
        if (!isFallingFinished) newPiece();
    }

    private void newPiece() {
        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Tetrominoes.NoShape);
            timer.stop();
            isStarted = false;
            JOptionPane.showMessageDialog(this, "Game Over! Lines cleared: " + linesCleared);
            System.exit(0);
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (Point p : newPiece.getCoords()) {
            int x = newX + p.x;
            int y = newY - p.y;
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
                return false;
            if (shapeAt(x, y) != Tetrominoes.NoShape)
                return false;
        }
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;

        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j)
                        board[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                }
                for (int j = 0; j < BOARD_WIDTH; ++j)
                    board[(BOARD_HEIGHT - 1) * BOARD_WIDTH + j] = Tetrominoes.NoShape;
                --i;
            }
        }

        if (numFullLines > 0) {
            linesCleared += numFullLines;
            isFallingFinished = true;
            curPiece.setShape(Tetrominoes.NoShape);
            repaint();

            if (linesCleared >= TARGET_LINES) {
                timer.stop();
                isStarted = false;
                showLoveLetter();
            }
        }
    }

    private void showLoveLetter() {
        String letter = createLoveLetter(playerName, linesCleared);
        JTextArea area = new JTextArea(letter);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font("Serif", Font.PLAIN, 16));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scroll, "A Letter For You ❤️", JOptionPane.PLAIN_MESSAGE);

        System.exit(0);
    }

private String createLoveLetter(String name, int lines) {
    return 
        "Happy monthsary babyy, as you said time flies by fast. Thank you for all the moments that we're doing, " +
        "the away, laugh, pikunan, tampuhan and many more. I wanted to thank you for all of it babyy. Thank you for being in my life " +
        "and hoping that it would end by still being us at the end, that's my only wish this Christmas and many Christmas to come.\n\n" +

        "I'm sorry for the ones that I've made you cry, angry and make that cute face that you always do with your eyebrows hehe. " +
        "I know I'm hard to deal with sometimes, matigas kasi ulo. But still after all that you're still being patient with me, " +
        "and for that I'm very very thankful. Don’t worry, I'm always here to be patient with you and be understanding whenever you have problems.\n\n" +

        "And for that I just wanna say, we're down with the last one to make it a 1 year 🙌🏾. This is still new to me tbf, I can't imagine that my crush " +
        "and I will make it to 1 year of relationship. God really did hear my prayers. Little story, when kayo pa ng ex mo, naalala ko na prayer ko was: " +
        "if ever na maghihiwalay, sana sakin mapadpad. Since alam ko na ganyang mukha tas di mo pa hihiwalayan, loyalty na e. " +
        "But God didn’t think that he was the ending, and now I'm hoping that I'll be the one to be it. I'm still praying don't worry 🤞🏾.\n\n" +

        "And a fun little story nung di pa tayo masyado magkakilala — I had an ipon worth 7k or 5k, then nung lumalabas na tayo nung talking stage or ligawan, " +
        "taena naubos lahat HAHAHAHAH. I always thought money would bring me happiness, and it did if I’m spending it good — but especially when I'm spending it " +
        "with my special someone, and that's you. Kaya whenever we're going outside to eat, I always remember that you're the person I would spend my whole money on " +
        "just to make you smile and satisfy your cravings.\n\n" +

        "I love you very much, you don’t need to question yourself 'cause it's just a fact 🙌🏾🙌🏾. I love you today, tomorrow, and always and forever be. " +
        "Kahit langgam ka mamahalin kita — bibigyan pa kita matcha na milktea, isang drop HAHAHAHAHAHA.\n\n" +

        "Happy 11th month to us loveyyy. I love you always. More years and blessings to come. Mwaa mwaa 💖\n\n" +

        "— With all my love, " + name + " ❤️\n";
}

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Tetrominoes.NoShape)
                    drawSquare(g, j * CELL_SIZE, i * CELL_SIZE, shape);
            }
        }

        if (curPiece.getShape() != Tetrominoes.NoShape) {
            for (Point p : curPiece.getCoords()) {
                int x = curX + p.x;
                int y = curY - p.y;
                int drawX = x * CELL_SIZE;
                int drawY = (BOARD_HEIGHT - 1 - y) * CELL_SIZE;
                drawSquare(g, drawX, drawY, curPiece.getShape());
            }
        }

        g.setColor(Color.WHITE);
        g.drawString("Lines: " + linesCleared + " / " + TARGET_LINES, 5, 15);
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
        Color color;
        switch (shape) {
            case ZShape: color = Color.RED; break;
            case SShape: color = Color.GREEN; break;
            case LineShape: color = Color.CYAN; break;
            case TShape: color = Color.MAGENTA; break;
            case SquareShape: color = Color.YELLOW; break;
            case LShape: color = Color.ORANGE; break;
            case MirroredLShape: color = Color.PINK; break;
            default: color = Color.GRAY; break;
        }
        g.setColor(color);
        g.fillRect(x, y, CELL_SIZE - 1, CELL_SIZE - 1);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, CELL_SIZE - 1, CELL_SIZE - 1);
    }

    class TAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (!isStarted || curPiece.getShape() == Tetrominoes.NoShape) return;

            int keycode = e.getKeyCode();
            if (keycode == KeyEvent.VK_P) {
                pause();
                return;
            }
            if (isPaused) return;

            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    tryMove(curPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(curPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_DOWN:
                    oneLineDown();
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_X:
                    tryMove(curPiece.rotateRight(), curX, curY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
            }
        }
    }
}

enum Tetrominoes { NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape }

class Shape {
    private Tetrominoes pieceShape;
    private Point[] coords;

    public Shape() {
        coords = new Point[4];
        for (int i = 0; i < 4; i++) coords[i] = new Point();
        setShape(Tetrominoes.NoShape);
    }

    public void setShape(Tetrominoes shape) {
        Point[][][] table = shapeTable();
        pieceShape = shape;

        if (shape == Tetrominoes.NoShape) return;

        Point[] base = table[shape.ordinal() - 1][0];
        for (int i = 0; i < 4; i++) {
            coords[i].x = base[i].x;
            coords[i].y = base[i].y;
        }
    }

    public void setRandomShape() {
        Random r = new Random();
        int x = r.nextInt(7) + 1;
        setShape(Tetrominoes.values()[x]);
    }

    public Tetrominoes getShape() { return pieceShape; }
    public Point[] getCoords() { return coords; }

    public Shape rotateRight() {
        if (pieceShape == Tetrominoes.SquareShape) return this;
        Shape result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; ++i) {
            result.coords[i].x = coords[i].y;
            result.coords[i].y = -coords[i].x;
        }
        return result;
    }

    public int minY() {
        int m = coords[0].y;
        for (int i = 1; i < 4; i++) m = Math.min(m, coords[i].y);
        return m;
    }

    private Point[][][] shapeTable() {
        Point[][][] table = new Point[7][][];
        table[0] = new Point[][] { { new Point(0,0), new Point(-1,0), new Point(0,1), new Point(1,1) } };
        table[1] = new Point[][] { { new Point(0,0), new Point(1,0), new Point(0,1), new Point(-1,1) } };
        table[2] = new Point[][] { { new Point(-1,0), new Point(0,0), new Point(1,0), new Point(2,0) } };
        table[3] = new Point[][] { { new Point(-1,0), new Point(0,0), new Point(1,0), new Point(0,1) } };
        table[4] = new Point[][] { { new Point(0,0), new Point(1,0), new Point(0,1), new Point(1,1) } };
        table[5] = new Point[][] { { new Point(-1,0), new Point(0,0), new Point(1,0), new Point(1,1) } };
        table[6] = new Point[][] { { new Point(-1,1), new Point(-1,0), new Point(0,0), new Point(1,0) } };
        return table;
    }
}
