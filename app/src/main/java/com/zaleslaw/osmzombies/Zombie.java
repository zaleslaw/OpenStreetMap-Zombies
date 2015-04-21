package com.zaleslaw.osmzombies;


public class Zombie {
    private String id;
    private String name;
    private String description;
    private int health;
    private int speed;
    private double lat;
    private double lon;

    public Zombie(String id, String name, String description, int health, int speed, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.health = health;
        this.speed = speed;
        this.lat = lat;
        this.lon = lon;
    }

    public Zombie(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getHealth() {
        return health;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void decreaseHealth(int i) {
        health -= i;
    }

    public void kill() {
        health = 0;
    }
}
