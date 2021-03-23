package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainBoard extends JPanel {

    public int player;

    private Plane plane;
    private HashMap<Long, Plane> planes;
    private HashMap<Long, Enemy> enemies;
    private HashMap<Long, Bullet> bullets;

    private boolean isPlaying;
    private int point = 0;

    private BufferedWriter os;
    private BufferedReader is;

    private Timer updateTimer;


    public MainBoard(BufferedWriter os, BufferedReader is, int player, HashMap<Long, Plane> planes
            , HashMap<Long, Bullet> bullets, HashMap<Long, Enemy> enemies) throws IOException, ClassNotFoundException {
        this.player = player;
        this.planes = planes;
        this.enemies = enemies;
        this.bullets = bullets;
        this.os = os;
        this.is = is;
        initBoard();
    }

    private void initBoard() {
        isPlaying = true;
        point = 0;
        plane = planes.get((long) player);

        setFocusable(true);
        setBackground(Color.WHITE);
        setDoubleBuffered(true);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                plane.mouseMoved(e);
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                fireBullet();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                        movePlane();
                        moveBullet();
                        removeEnemy();
                        sendInfo();
                        receiveInfo();
                        repaint();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }, 0, 10);

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isPlaying) {
            drawObject(g);
        } else {
            updateTimer.cancel();
            drawGameOver(g);
        }
    }

    private void drawObject(Graphics g) {
        g.drawString(String.valueOf(point), 50, 50);
        planes.forEach((aLong, plane) -> g.drawImage(plane.getImage(), plane.x, plane.y, MainBoard.this));
        enemies.forEach((aLong, enemy) -> g.drawImage(enemy.getImage(), enemy.x, enemy.y, MainBoard.this));
        bullets.forEach((aLong, bullet) -> g.drawImage(bullet.getImage(), bullet.x, bullet.y, MainBoard.this));
    }

    private void drawGameOver(Graphics g) {
        Font font = new Font("Helvetica", Font.BOLD, 20);
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString("Score: " + point, 500, 300);
    }

    private synchronized void movePlane() {
        plane.move();
    }

    private synchronized void moveBullet() {
        ArrayList<Long> deleteBullet = new ArrayList<>();
        bullets.forEach((aLong, bullet) -> {
            if(bullet.y >= -20)
                bullet.move();
            else
                deleteBullet.add(aLong);
        });
        for (Long key : deleteBullet) {
            bullets.remove(key);
        }
    }

    private synchronized void removeEnemy() {
        ArrayList<Long> deleteEnemy = new ArrayList<>();
        enemies.forEach((aLong, enemy) -> {
            if(enemy.y >= 650)
                deleteEnemy.add(aLong);
        });
        for(Long key : deleteEnemy)
            enemies.remove(key);
    }


    private synchronized void fireBullet() {
        Bullet bullet = new Bullet(plane.x + 15, plane.y - 25, player);
        bullets.put(System.currentTimeMillis(), bullet);
    }


    private synchronized void sendInfo() throws IOException{
        StringBuilder s = new StringBuilder();
        //Gui thong tin may bay hien tai
        s.append("Start_send_info\n");
        s.append(plane.player);
        s.append(",");
        s.append(plane.x);
        s.append(",");
        s.append(plane.y);
        s.append("#");

        //Gui thong tin dan ban tu may bay
        AtomicBoolean hasBullet = new AtomicBoolean(false);
        if(bullets.size() > 0) {
            bullets.forEach((aLong, bullet) -> {
                if (bullet.player == player) {
                    hasBullet.set(true);
                    s.append(aLong);
                    s.append(",");
                    s.append(bullet.player);
                    s.append(",");
                    s.append(bullet.x);
                    s.append(",");
                    s.append(bullet.y);
                    s.append("@");
                }
            });
        }
        if(!hasBullet.get())
            s.append("Nothing");
        s.append("#");

        s.append("\n");
        s.append("End_send_info\n");
        os.write(s.toString());
        os.flush();
    }

    private synchronized void receiveInfo() throws IOException {
        String s;
        while ((s=is.readLine()) != null) {
            if(s.equals("End_send_info"))
                break;
            if(!s.equals("Start_send_info")) {
                String[] parts = s.split("#");

                //Thong tin cua cac may bay khac
                if(!parts[0].equals("Nothing")) {
                    String[] planePart = parts[0].split("@");
                    for (String value : planePart) {
                        String[] singlePlane = value.split(",");
                        Plane temp = planes.get(Long.parseLong(singlePlane[0]));
                        if(temp != null) {
                        temp.x = Integer.parseInt(singlePlane[1]);
                        temp.y = Integer.parseInt(singlePlane[2]);
                        }
                    }
                }

                //Thong tin cua cac dan khac
                if(!parts[1].equals("Nothing")) {
                    String[] bulletPart = parts[1].split("@");
                    for(String value : bulletPart) {
                        if(!value.equals("")) {
                            String[] singleBullet = value.split(",");
                            Long key = Long.parseLong(singleBullet[0]);
                            if (bullets.containsKey(key)) {
                                Bullet temp = bullets.get(key);
                                temp.x = Integer.parseInt(singleBullet[2]);
                                temp.y = Integer.parseInt(singleBullet[3]);
                            } else
                                bullets.put(key, new Bullet(Integer.parseInt(singleBullet[2]),
                                        Integer.parseInt(singleBullet[3]),
                                        Integer.parseInt(singleBullet[1])));
                        }
                    }
                }

                //Thong tin may bay dich
                if(!parts[2].equals("Nothing")) {
                    String[] enemyPart = parts[2].split("@");
                    for(String value : enemyPart) {
                        String[] singleEnemy = value.split(",");
                        Long key = Long.parseLong(singleEnemy[0]);
                        if(enemies.containsKey(key)) {
                            Enemy temp = enemies.get(key);
                            temp.x = Integer.parseInt(singleEnemy[1]);
                            temp.y = Integer.parseInt(singleEnemy[2]);
                        }
                        else
                            enemies.put(key, new Enemy(Integer.parseInt(singleEnemy[1]),
                                    Integer.parseInt(singleEnemy[2])));
                    }
                }

                //Con choi hay khong
                isPlaying = Integer.parseInt(parts[3]) == 1;

                //Diem
                point = Integer.parseInt(parts[4]);

                //Dich bi ban ha
                if(!parts[5].equals("Nothing")) {
                    String[] deleteEnemy = parts[5].split(",");
                    for(String key : deleteEnemy) {
                        enemies.remove(Long.parseLong(key));
                    }
                }

                if(!parts[6].equals("Nothing")) {
                    String[] deleteBullet = parts[6].split(",");
                    for(String key : deleteBullet) {
                        bullets.remove(Long.parseLong(key));
                    }
                }

                if(!parts[7].equals("Nothing")) {
                    String[] deletePlane = parts[7].split(",");
                    for(String key : deletePlane) {
                        planes.remove(Long.parseLong(key));
                    }
                }
            }
        }
    }

}
