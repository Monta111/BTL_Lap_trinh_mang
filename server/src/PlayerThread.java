import java.io.*;
import java.net.Socket;
import java.util.*;

public class PlayerThread extends Thread {
    private final Socket socket;

    private final int player;

    private MainBoard mainBoard;

    public PlayerThread(Socket socket, int player,
                        MainBoard mainBoard) {
        this.socket = socket;
        this.player = player;
        this.mainBoard = mainBoard;
    }


    @Override
    public void run() {
        try {
            BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os.write("Start\n");
            os.write(player + "\n");
            os.flush();

            mainBoard.sendInfoInitPlane(os);

            Timer task = new Timer();
            task.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        mainBoard.receiveInfo(is, player);
                        mainBoard.checkCollisionPlanevsEnemy(player);
                        mainBoard.sendInfo(os, player);
                        if(mainBoard.getIsPlaying()[player-1] ==0)
                            task.cancel();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 10);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
