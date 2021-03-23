import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends JFrame {
    private static final int SERVER_PORT = 1234;

    public static void main(String[] args) {

        ServerSocket serverSocket;
        int numberOfPlayer = 1;
        int count = 0;

        HashMap<Long, Plane> planes = new HashMap<>();
        HashMap<Long, Enemy> enemies = new HashMap<>();
        HashMap<Long, Bullet> bullets = new HashMap<>();


        MainBoard mainBoard = new MainBoard(planes, enemies, bullets);

        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server start at port " + SERVER_PORT);

            Scanner scanner = new Scanner(System.in);

            System.out.println("Nhap so luong nguoi choi: ");
            numberOfPlayer = scanner.nextInt();
            System.out.println("Chon level: 1/2/3/4/5");
            int level = scanner.nextInt();
            switch (level) {
                case 1:
                    Enemy.speed = 1;
                    break;
                case 2:
                    Enemy.speed = 2;
                    break;
                case 3:
                    Enemy.speed = 3;
                    break;
                case 4:
                    Enemy.speed = 4;
                    break;
                case 5:
                    Enemy.speed = 5;
                    break;
            }
            scanner.nextLine();

            Socket[] sockets = new Socket[numberOfPlayer];
            System.out.println("Press enter");
            if (scanner.nextLine().equals("")) {
                while (count < numberOfPlayer) {
                    sockets[count] = serverSocket.accept();
                    ++count;
                    System.out.println("Player " + count + " ready!");
                }
                System.out.println("Start!");
                for (int i = 0; i < numberOfPlayer; ++i) {
                    Plane plane = new Plane(i * 200, 600, i+1);
                    planes.put((long) (i+1), plane);
                }
                for(int i=0; i<numberOfPlayer; ++i) {
                    PlayerThread playerThread = new PlayerThread(sockets[i], i + 1, mainBoard);
                    playerThread.start();
                }

                Timer timerMoveEnemy = new Timer();
                timerMoveEnemy.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mainBoard.moveEnemy();
                    }
                }, 1000, 20);

                Timer timerAddEnemy = new Timer();
                timerAddEnemy.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mainBoard.addEnemy();
                    }
                }, 1000,  1000);

                long t = System.currentTimeMillis();
                Timer timerCheckCollision = new Timer();
                timerCheckCollision.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mainBoard.removeBulletRedundant();
                        mainBoard.checkCollisionBulletvsEnemy();
                        int delta = (int) (System.currentTimeMillis() - t)/1000;
                    }
                }, 0, 10);

            }
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
