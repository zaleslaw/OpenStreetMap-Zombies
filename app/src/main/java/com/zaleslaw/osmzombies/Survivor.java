package com.zaleslaw.osmzombies;


public class Survivor extends Zombie {

    public static final String ME = "Me";

    public Survivor(int health, double lat, double lon) {
        super(ME, ME, ME, health, 0, lat, lon);
    }
}
