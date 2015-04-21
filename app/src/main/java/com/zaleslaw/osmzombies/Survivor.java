package com.zaleslaw.osmzombies;


public class Survivor extends Zombie {

    public static final String ME = "Me";

    private int scores = 0;

    public Survivor(int health, double lat, double lon) {
        super(ME, ME, ME, health, 0, lat, lon);
    }


    public void incrementScores(int amount) {
        scores += amount;
    }

}
