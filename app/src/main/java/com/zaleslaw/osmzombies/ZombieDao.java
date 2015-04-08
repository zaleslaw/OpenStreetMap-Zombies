package com.zaleslaw.osmzombies;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieDao {
    public List<Zombie> getZombies(Location location) {

        double zombieLat = location.getLatitude() * (1 - new Random().nextDouble() / 10000);
        double zombieLon = location.getLongitude() * (1 - new Random().nextDouble() / 10000);

        List<Zombie> zombieList = new ArrayList<Zombie>();
        Zombie zombie1 = new Zombie("Z1", "Old Frank", zombieLat, zombieLon);
        zombieList.add(zombie1);
        return zombieList;
    }
}
