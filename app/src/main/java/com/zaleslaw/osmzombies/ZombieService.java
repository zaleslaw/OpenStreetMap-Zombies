package com.zaleslaw.osmzombies;


import android.location.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ZombieService {

    private int zombieCounter = 1;

    // TODO : singleton
    private Survivor survivor;

    final private List<Zombie> currentGeneration = new ArrayList<>();
    final private ZombieDao dao = new ZombieDao();


    public List<Zombie> getCurrentGeneration() {

        return currentGeneration;
    }

    public List<Zombie> getFirstGeneration(Location gamerLocation) {
        return dao.getZombies(gamerLocation);
    }

    public void kickZombie(String uid) {
        for (Zombie z : currentGeneration) {
            if (z.getId().equals(uid)) {
                z.decreaseHealth(1);
            }
        }
    }

    public void killZombie(String uid) {
        for (Zombie z : currentGeneration) {
            if (z.getId().equals(uid)) {
                z.kill();
            }
        }
    }

    public Survivor createSurvivor(int health, double latitude, double longitude) {
        survivor = new Survivor(health, latitude, longitude);
        return survivor;
    }

    public Survivor getSurvivor() {
        return survivor;
    }

    public void updateSurvivorPosition(Location gamerLocation) {
        survivor.setLat(gamerLocation.getLatitude());
        survivor.setLon(gamerLocation.getLongitude());
    }

    public boolean verifyEndGameCondition() {
        return false;
    }

    public void filterAliveZombies() {
        for (Iterator<Zombie> i = currentGeneration.iterator(); i.hasNext(); ) {
            Zombie z = i.next();
            if (z.getHealth() <= 0) {
                i.remove();
            }
        }
    }

    public void updateZombieLocations() {
        double sLat = survivor.getLat();
        double sLon = survivor.getLon();

        for (Zombie z : currentGeneration) {
            double zombieLat = z.getLat() + z.getSpeed() * (sLat - z.getLat()) / 10000;
            double zombieLon = z.getLon() + z.getSpeed() * (sLon - z.getLon()) / 10000;
            z.setLat(zombieLat);
            z.setLon(zombieLon);
        }
    }

    public void generateNewZombies() {
        for (Zombie z : currentGeneration) {
            if (new Random().nextDouble() * z.getHealth() > 2.5) {
                zombieCounter++;
                String newbyId = "Z" + zombieCounter;

                double zombieLat = z.getLat() * (1 - new Random().nextDouble() / 10000);
                double zombieLon = z.getLon() * (1 - new Random().nextDouble() / 10000);

                // New zombies are more healthy and quickly than their parents
                Zombie newby = new Zombie(newbyId, newbyId, newbyId, z.getHealth() + 1, z.getSpeed() + 1, zombieLat, zombieLon);
                currentGeneration.add(newby);
            }
        }
    }
}
