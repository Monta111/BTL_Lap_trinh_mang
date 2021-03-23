public class Plane extends Square{

    private int dx;


    public int player;

    public Plane(int x, int y, int player) {
        super(x, y);
        this.player = player;
        initPlane();
    }


    private void initPlane() {
        loadImage("image/plane"+player+".png");
        getImageDimension();
    }

            private void getImageDimension() {
        width = 50;
        height = 50;
    }

}
