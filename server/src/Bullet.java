import javax.swing.*;

public class Bullet extends Square{

    public int player;

    public Bullet(int x, int y, int player) {
        super(x, y);
        this.player = player;
        initBullet();
    }

    private void initBullet() {
        ImageIcon imageIcon = new ImageIcon("image/bullet.png");
        image = imageIcon.getImage();
        getImageDimension();
    }

    
            private void getImageDimension() {
        width = 20;
        height = 20;
    }
}
