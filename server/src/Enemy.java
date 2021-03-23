public class Enemy extends Square{

    public static int speed = 3;

    public Enemy(int x, int y) {
        super(x, y);
        initEnemy();
    }

    private void initEnemy() {
        loadImage("image/enemy.png");
        getImageDimension();
    }

    public void move() {
        y += speed;
    }

    
            private void getImageDimension() {
        width = 50;
        height = 50;
    }
}
