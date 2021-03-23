package game;

public class Enemy extends Square{

    public Enemy(int x, int y) {
        super(x, y);
        initEnemy();
    }

    private void initEnemy() {
        loadImage("image/enemy.png");
        getImageDimension();
    }

    private void getImageDimension() {
        width = 50;
        height = 50;
    }
}
