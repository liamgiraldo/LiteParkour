package me.liamgiraldo.liteParkour;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ProgressModel {
    private ParkourModel parkour;
    private int checkpointIndex;
    //time in seconds for the progress of this parkour
    private int time;
    private boolean completed;
    //the player that this progress is for
    private Player player;
    private BukkitRunnable timer;
    public ProgressModel(ParkourModel parkour, Player player) {
        this.parkour = parkour;
        this.player = player;
        this.checkpointIndex = 0;
        this.time = 0;
        this.completed = false;
    }

    public void start() {
        this.timer = new BukkitRunnable() {
            @Override
            public void run() {
                time++;
            }
        };
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.timer.cancel();
    }
    public void setCheckpointIndex(int checkpointIndex) {
        this.checkpointIndex = checkpointIndex;
    }
    public int getCheckpointIndex() {
        return checkpointIndex;
    }
    public int getTime() {
        return time;
    }
    public Player getPlayer() {
        return player;
    }
    public ParkourModel getParkour() {
        return parkour;
    }

    public void restartProgress() {
        this.checkpointIndex = 0;
        this.time = 0;
        this.completed = false;
    }

    public void restartAtCheckpoint(int checkpointIndex) {
        this.checkpointIndex = checkpointIndex;
        this.completed = false;
    }
}
