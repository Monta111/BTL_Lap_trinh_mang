package game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainGame extends JFrame {

    public MainGame(MainBoard mainBoard) {
        add(mainBoard);
        setTitle("Client");
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(Color.WHITE);
        setVisible(true);
    }
}
