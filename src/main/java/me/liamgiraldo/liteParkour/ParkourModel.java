package me.liamgiraldo.liteParkour;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ParkourModel {
    private String name;
    private String creator;
    private ArrayList<Location> checkpoints;
    private Location start;
    private Location end;

    private ArrayList<Player> players = new ArrayList<Player>();

    public ParkourModel(String name, String creator, ArrayList<Location> checkpoints){
        this.name = name;
        this.creator = creator;
        this.start = checkpoints.get(0);
        this.end = checkpoints.get(checkpoints.size() - 1);
        this.checkpoints = checkpoints;
    }

    public ParkourModel(String name, String creator, Location start, Location end, ArrayList<Location> checkpoints){
        this.name = name;
        this.creator = creator;
        this.start = start;
        this.end = end;
        this.checkpoints = checkpoints;
    }

    public ParkourModel(ArrayList<Location> checkpoints){
        this.checkpoints = checkpoints;
        this.name = "Unnamed";
        this.creator = "Unknown";
        this.start = checkpoints.get(0);
        this.end = checkpoints.get(checkpoints.size() - 1);
    }

    public String getName() {
        return name;
    }
    public String getCreator() {
        return creator;
    }
    public ArrayList<Location> getCheckpoints() {
        return checkpoints;
    }
    public Location getStart() {
        return start;
    }
    public Location getEnd() {
        return end;
    }
    public World getWorld() {
        return start.getWorld();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
