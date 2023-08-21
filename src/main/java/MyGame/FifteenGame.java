package MyGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

public class FifteenGame extends JFrame {

    private static final int BOARD_SIZE = 4;

    private JPanel gamePanel;
    private JButton[][] tiles;
    private JButton resetButton;
    private JLabel timerLabel;

    private int emptyRow;
    private int emptyCol;
    private boolean gameInProgress;
    private boolean undoAvailable;
    private int movesCount;
    private Timer timer;

    public FifteenGame() {
        setTitle("15 Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initGame();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initGame() {
        gameInProgress = false;
        undoAvailable = false;
        movesCount = 0;

        gamePanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE, 2, 2));
        tiles = new JButton[BOARD_SIZE][BOARD_SIZE];

        ActionListener moveListener = new MoveListener();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col] = new JButton();
                tiles[row][col].setFont(new Font("Arial", Font.BOLD, 24));
                tiles[row][col].addActionListener(moveListener);
                gamePanel.add(tiles[row][col]);
            }
        }

        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ResetListener());

        timerLabel = new JLabel("Time: 0");
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(resetButton);
        bottomPanel.add(timerLabel);

        add(gamePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                if (gameInProgress) {
                    timer.stop();
                }
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                if (gameInProgress) {
                    timer.start();
                }
            }
        });

        resetGame();
    }

    private void resetGame() {
        gameInProgress = false;
        undoAvailable = false;
        movesCount = 0;
        timerLabel.setText("Time: 0");
        resetButton.setEnabled(false);

        // Generate a solvable random board
        int[] numbers = new int[BOARD_SIZE * BOARD_SIZE - 1];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = i + 1;
        }

        do {
            shuffleArray(numbers);
        } while (!isSolvable(numbers));

        int index = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (index < numbers.length) {
                    tiles[row][col].setText(String.valueOf(numbers[index]));
                    tiles[row][col].setEnabled(true);
                    index++;
                } else {
                    tiles[row][col].setText("");
                    tiles[row][col].setEnabled(false);
                    emptyRow = row;
                    emptyCol = col;
                }
            }
        }

        shuffleBoard();

        resetButton.setEnabled(true);
        gameInProgress = true;
        undoAvailable = false;

        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(1000, new TimerListener());
        timer.start();
    }

    private void shuffleBoard() {
        Random random = new Random();

        for (int i = 0; i < 1000; i++) {
            int direction = random.nextInt(4); // 0: up, 1: down, 2: left, 3: right

            switch (direction) {
                case 0: // Up
                    if (emptyRow > 0) {
                        swapTiles(emptyRow, emptyCol, emptyRow - 1, emptyCol);
                        emptyRow--;
                    }
                    break;
                case 1: // Down
                    if (emptyRow < BOARD_SIZE - 1) {
                        swapTiles(emptyRow, emptyCol, emptyRow + 1, emptyCol);
                        emptyRow++;
                    }
                    break;
                case 2: // Left
                    if (emptyCol > 0) {
                        swapTiles(emptyRow, emptyCol, emptyRow, emptyCol - 1);
                        emptyCol--;
                    }
                    break;
                case 3: // Right
                    if (emptyCol < BOARD_SIZE - 1) {
                        swapTiles(emptyRow, emptyCol, emptyRow, emptyCol + 1);
                        emptyCol++;
                    }
                    break;
            }
        }
    }

    private void swapTiles(int row1, int col1, int row2, int col2) {
        String temp = tiles[row1][col1].getText();
        tiles[row1][col1].setText(tiles[row2][col2].getText());
        tiles[row2][col2].setText(temp);
        tiles[row1][col1].setEnabled(!tiles[row1][col1].getText().isEmpty());
        tiles[row2][col2].setEnabled(!tiles[row2][col2].getText().isEmpty());
    }

    private void moveTile(int row, int col) {
        if (tiles[row][col].getText().isEmpty()) {
            return; // Ignore empty tile
        }

        if (row > 0 && tiles[row - 1][col].getText().isEmpty()) {
            swapTiles(row, col, row - 1, col);
            emptyRow = row - 1;
            emptyCol = col;
            movesCount++;
            undoAvailable = true;
            checkWin();
            return;
        }

        if (row < BOARD_SIZE - 1 && tiles[row + 1][col].getText().isEmpty()) {
            swapTiles(row, col, row + 1, col);
            emptyRow = row + 1;
            emptyCol = col;
            movesCount++;
            undoAvailable = true;
            checkWin();
            return;
        }

        if (col > 0 && tiles[row][col - 1].getText().isEmpty()) {
            swapTiles(row, col, row, col - 1);
            emptyRow = row;
            emptyCol = col - 1;
            movesCount++;
            undoAvailable = true;
            checkWin();
            return;
        }

        if (col < BOARD_SIZE - 1 && tiles[row][col + 1].getText().isEmpty()) {
            swapTiles(row, col, row, col + 1);
            emptyRow = row;
            emptyCol = col + 1;
            movesCount++;
            undoAvailable = true;
            checkWin();
        }
    }

    private void checkWin() {
        boolean win = true;
        int expectedNumber = 1;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (!tiles[row][col].getText().equals(String.valueOf(expectedNumber))) {
                    win = false;
                    break;
                }
                expectedNumber++;
            }
        }

        if (win) {
            gameInProgress = false;
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You won the game in " + movesCount + " moves.", "Winner", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean isSolvable(int[] numbers) {
        int inversions = 0;

        for (int i = 0; i < numbers.length - 1; i++) {
            for (int j = i + 1; j < numbers.length; j++) {
                if (numbers[i] > numbers[j]) {
                    inversions++;
                }
            }
        }

        if (BOARD_SIZE % 2 == 0) {
            // Even-sized board
            if (emptyRow % 2 == 0) {
                // Even number of inversions and empty row is even
                return inversions % 2 == 0;
            } else {
                // Even number of inversions and empty row is odd
                return inversions % 2 != 0;
            }
        } else {
            // Odd-sized board
            return inversions % 2 == 0;
        }
    }

    private void shuffleArray(int[] array) {
        Random random = new Random();

        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    private class MoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            int row = -1;
            int col = -1;

            // Find the button's position
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (tiles[i][j] == button) {
                        row = i;
                        col = j;
                        break;
                    }
                }
            }

            moveTile(row, col);
        }
    }

    private class ResetListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(FifteenGame.this, "Are you sure you want to reset the game?", "Reset Game", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                resetGame();
            }
        }
    }

    private class TimerListener implements ActionListener {
        private int time;

        @Override
        public void actionPerformed(ActionEvent e) {
            time++;
            timerLabel.setText("Time: " + time);
        }
    }

}

