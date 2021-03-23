import java.awt.*;
import java.io.*;
import java.util.*;

public class MainBoard {

    private HashMap<Long, Plane> planes;
    private HashMap<Long, Enemy> enemies;
    private HashMap<Long, Bullet> bullets;

    private ArrayList<Long> deleteEnemies;
    private ArrayList<Long> deletePlanes;
    private ArrayList<Long> deleteBullets;

    public int[] points;
    public int[] isPlaying;

    public MainBoard(HashMap<Long, Plane> planes,
                     HashMap<Long, Enemy> enemies,
                     HashMap<Long, Bullet> bullets) {
        this.planes = planes;
        this.enemies = enemies;
        this.bullets = bullets;
        points = new int[4];
        isPlaying = new int[4];
        Arrays.fill(isPlaying, 1);
        deleteEnemies = new ArrayList<>();
        deletePlanes = new ArrayList<>();
        deleteBullets = new ArrayList<>();
    }

    public synchronized void addEnemy() {
        enemies.put(System.currentTimeMillis(), new Enemy(new Random().nextInt(1151), 0));
    }

    public synchronized void moveEnemy() {
        enemies.forEach((aLong, enemy) -> {
            if (enemy.y <= 650)
                enemy.move();
            else
                deleteEnemies.add(aLong);
        });
    }

    public synchronized void removeBulletRedundant() {
        ArrayList<Long> deleteBullet = new ArrayList<>();
        bullets.forEach((aLong, bullet) -> {
            if (bullet.y <= -20)
                deleteBullet.add(aLong);
        });
        for (Long key : deleteBullet) {
            bullets.remove(key);
        }
    }

    public synchronized void sendInfoInitPlane(BufferedWriter os) throws IOException {
        //long t = System.currentTimeMillis();
        StringBuilder s = new StringBuilder();
        s.append("Init_plane\n");
        planes.forEach((aLong, plane) -> {
            s.append(aLong);
            s.append(",");
            s.append(plane.x);
            s.append(",");
            s.append(plane.y);
            s.append("@");
        });
        s.append("\n");
        s.append("End_init_plane\n");
        os.write(s.toString());
        os.flush();
        //System.out.println(System.currentTimeMillis() - t);
    }

    public synchronized void sendInfo(BufferedWriter os, int player) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append("Start_send_info\n");

        //Gui thong tin cac may bay khac
        if (planes.size() > 1) {
            planes.forEach((aLong, plane) -> {
                if (plane.player != player) {
                    s.append(plane.player);
                    s.append(",");
                    s.append(plane.x);
                    s.append(",");
                    s.append(plane.y);
                    s.append("@");
                }
            });
        } else
            s.append("Nothing");
        s.append("#");

        //Gui thong tin dan cua cac may bay khac
        if (bullets.size() > 0 && planes.size() > 1) {
            bullets.forEach((aLong, bullet) -> {
                if (bullet.player != player) {
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
        } else
            s.append("Nothing");

        s.append("#");

        //Gui thong tin may bay dich
        if (enemies.size() > 0) {
            enemies.forEach((aLong, enemy) -> {
                s.append(aLong);
                s.append(",");
                s.append(enemy.x);
                s.append(",");
                s.append(enemy.y);
                s.append("@");
            });
        } else
            s.append("Nothing");
        s.append("#");

        //Gui thong tin thang/thua
        s.append(isPlaying[player - 1]);
        s.append("#");

        //Gui thong tin diem so
        s.append(points[player - 1]);
        s.append("#");

        //Gui thong tin may bay dich bi xoa
        if (deleteEnemies.size() > 0) {
            for (Long key : deleteEnemies) {
                s.append(key);
                s.append(",");
            }
        } else
            s.append("Nothing");
        s.append("#");

        //Gui thong tin dan bi xoa
        if (deleteBullets.size() > 0) {
            for (Long key : deleteBullets) {
                s.append(key);
                s.append(",");
            }
        } else
            s.append("Nothing");
        s.append("#");

        //Gui thong tin may bay bi xoa
        if (deletePlanes.size() > 0) {
            for (Long key : deletePlanes) {
                s.append(key);
                s.append(",");
            }
        } else
            s.append("Nothing");
        s.append("#");

        s.append("\n");
        s.append("End_send_info\n");
        os.write(s.toString());
        os.flush();
    }

    public synchronized void receiveInfo(BufferedReader is, int player) throws IOException {
        if (isPlaying[player - 1] == 1) {
            String s;
            while ((s = is.readLine()) != null) {
                if (s.equals("End_send_info"))
                    break;
                if (!s.equals("Start_send_info")) {
                    String[] parts = s.split("#");
                    //part[0] may bay gui tu client
                    String[] planePart = parts[0].split(",");
                    Plane temp1 = planes.get(Long.parseLong(planePart[0]));
                    temp1.x = Integer.parseInt(planePart[1]);
                    temp1.y = Integer.parseInt(planePart[2]);

                    if (!parts[1].equals("Nothing")) {
                        //part[1] dan gui tu client
                        String[] bulletPart = parts[1].split("@");
                        for (String value : bulletPart) {
                            String[] bulletSingle = value.split(",");
                            Long key = Long.parseLong(bulletSingle[0]);
                            if (bullets.containsKey(key)) {
                                Bullet temp2 = bullets.get(key);
                                temp2.x = Integer.parseInt(bulletSingle[2]);
                                temp2.y = Integer.parseInt(bulletSingle[3]);
                            } else {
                                bullets.put(key, new Bullet(Integer.parseInt(bulletSingle[2]),
                                        Integer.parseInt(bulletSingle[3]),
                                        Integer.parseInt(bulletSingle[1])));
                            }
                        }
                    }
                }
            }
        }
    }

    public synchronized void checkCollisionPlanevsEnemy(int player) {
        Rectangle planeRec = planes.get((long) player).getBound();
        if (planeRec != null) {
            for (Map.Entry<Long, Enemy> entry : enemies.entrySet()) {
                Rectangle enemyRec = entry.getValue().getBound();
                if (planeRec.intersects(enemyRec)) {
                    isPlaying[player - 1] = 0;
                    deletePlanes.add((long) player);
                    deleteEnemies.add(entry.getKey());
                    break;
                }
            }
        }
    }

    public synchronized void checkCollisionBulletvsEnemy() {
        for (Map.Entry<Long, Enemy> entry : enemies.entrySet()) {
            Rectangle enemy = entry.getValue().getBound();
            for (Map.Entry<Long, Bullet> entry1 : bullets.entrySet()) {
                Bullet bullet = entry1.getValue();
                Rectangle bulletRec = bullet.getBound();
                if (enemy.intersects(bulletRec)) {
                    ++points[bullet.player - 1];
                    deleteEnemies.add(entry.getKey());
                    deleteBullets.add(entry1.getKey());
                    break;
                }
            }
        }
        for (Long key : deleteEnemies) {
            enemies.remove(key);
        }

        for (Long key : deleteBullets)
            bullets.remove(key);

        for (Long key : deletePlanes)
            planes.remove(key);
    }

    public int[] getIsPlaying() {
        return isPlaying;
    }
}
