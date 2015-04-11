package com.zaleslaw.osmzombies;

import android.os.Handler;

/**
 * Game loop is based on theory provided by the next link
 * http://obviam.net/index.php/the-android-game-loop/
 */
public class MainGameThread extends Thread {


    private int record = 0;
    static final long FPS = 10;
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
        long ticksPS = 10000 / FPS; // not real FPS
        long startTime;
        long sleepTime;
        Handler handler = new Handler(gameView.getActivity().getMainLooper());
        while (running) {

            startTime = System.currentTimeMillis();
            if (!gameView.isLBSrequested()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        gameView.requestLocationServicesEnabling();
                    }
                });

            } else if (!gameView.isMapInitialized()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        gameView.initializeMap();
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        gameView.updateGameState();
                        gameView.displayGameState();
                    }
                });
            }

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
