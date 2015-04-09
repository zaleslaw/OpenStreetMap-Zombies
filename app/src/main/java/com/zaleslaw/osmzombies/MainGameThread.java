package com.zaleslaw.osmzombies;

/**
 * Game loop is based on theory provided by the next link
 * http://obviam.net/index.php/the-android-game-loop/
 */
public class MainGameThread extends Thread {


    private int record = 0;
    static final long FPS = 1;
    private ZombieMapFragment gameView;
    // flag to hold game state
    private boolean running;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public MainGameThread(ZombieMapFragment gameView) {
        super();
        this.gameView = gameView;
    }

    public void run() {
        long ticksPS = 1000 / FPS;
        long startTime;
        long sleepTime;
        while (running) {

            startTime = System.currentTimeMillis();
            gameView.updateGameState();
            gameView.displayGameState();
            sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0)
                    sleep(sleepTime);
                else
                    sleep(10);
            } catch (Exception e) {
            }
        }
    }

    public int getRecord() {
        return record;
    }

    public void setRecord(int record) {
        this.record = record;
    }
}