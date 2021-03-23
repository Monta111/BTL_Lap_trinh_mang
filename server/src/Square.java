import javax.swing.*;
import java.awt.*;

public class Square {
    public int x;
    public int y;
    protected int width;
    protected int height;
    protected Image image;


    public Square(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Square() {

    }

    public void loadImage(String path) {
        ImageIcon imageIcon = new ImageIcon(path);
        image = imageIcon.getImage();
    }

    public Rectangle getBound() {
        return new Rectangle(x, y, width, height);
    }
}

