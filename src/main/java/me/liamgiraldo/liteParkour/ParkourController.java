package me.liamgiraldo.liteParkour;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class ParkourController implements Listener {
    private LiteParkour liteParkour;
    private ArrayList<ParkourModel> parkours = new ArrayList<ParkourModel>();
    public ParkourController(LiteParkour liteParkour) {
        this.liteParkour = liteParkour;
        parkours = loadParkours(liteParkour);
    }

    private ArrayList<ParkourModel> loadParkours(LiteParkour liteParkour) {
        // Load parkours from file
        FileConfiguration config = liteParkour.getConfig();
        if(config.contains("parkours")) {
            for(String parkourName: config.getConfigurationSection("parkours").getKeys(false)) {
                String worldName = config.getString("parkours." + parkourName + ".world");
                String[] startCoords = config.getString("parkours." + parkourName + ".start").split(",");
                String[] endCoords = config.getString("parkours." + parkourName + ".end").split(",");
                ArrayList<String> checkpointStrings = new ArrayList<String>();
                if(config.contains("parkours." + parkourName + ".checkpoints")) {
                    for(String checkpoint: config.getConfigurationSection("parkours." + parkourName + ".checkpoints").getKeys(false)) {
                        checkpointStrings.add(config.getString("parkours." + parkourName + ".checkpoints." + checkpoint));
                    }
                }
                ArrayList<Location> checkpoints = new ArrayList<Location>();
                for(String checkpoint: checkpointStrings) {
                    String[] coords = checkpoint.split(",");
                    checkpoints.add(new Location(liteParkour.getServer().getWorld(worldName), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])));
                }
                parkours.add(new ParkourModel(parkourName, worldName, new Location(liteParkour.getServer().getWorld(worldName), Integer.parseInt(startCoords[0]), Integer.parseInt(startCoords[1]), Integer.parseInt(startCoords[2])), new Location(liteParkour.getServer().getWorld(worldName), Integer.parseInt(endCoords[0]), Integer.parseInt(endCoords[1]), Integer.parseInt(endCoords[2])), checkpoints));
            }
        }
        return new ArrayList<ParkourModel>();
    }

    public void saveParkours() {
        // Save parkours to file
        FileConfiguration config = liteParkour.getConfig();
        for(ParkourModel parkour: parkours){
            saveParkour(parkour);
        }
    }

    public void saveParkour(ParkourModel parkour) {
        // Save parkour to file
        FileConfiguration config = liteParkour.getConfig();
        if(config.contains("parkours." + parkour.getName())) {
            config.set("parkours." + parkour.getName(), null);
            /**
             * This is roughly the format of a config file
             *
             * parkours:
             *     Parkour1:
             *         world: world
             *         start: 0,0,0
             *         end: 10,10,10
             *         checkpoints:
             *         - 1,1,1
             *         - 2,2,2
             *         - 3,3,3
             *         - 4,4,4
             *         - 5,5,5
             *         - 6,6,6
             *         - 7,7,7
             *         - 8,8,8
             *         - 9,9,9
             *         - 10,10,10
             * */

            config.set("parkours." + parkour.getName() + ".world", parkour.getWorld().getName());
            config.set("parkours." + parkour.getName() + ".start", parkour.getStart().getBlockX() + "," + parkour.getStart().getBlockY() + "," + parkour.getStart().getBlockZ());
            config.set("parkours." + parkour.getName() + ".end", parkour.getEnd().getBlockX() + "," + parkour.getEnd().getBlockY() + "," + parkour.getEnd().getBlockZ());
            for(int i = 0; i < parkour.getCheckpoints().size(); i++) {
                config.set("parkours." + parkour.getName() + ".checkpoints." + i, parkour.getCheckpoints().get(i).getBlockX() + "," + parkour.getCheckpoints().get(i).getBlockY() + "," + parkour.getCheckpoints().get(i).getBlockZ());
            }
        }
        else{
            System.out.println("Parkour " + parkour.getName() + " not found in config");
            //add parkour to config
            config.set("parkours." + parkour.getName(), parkour.getName());
            config.set("parkours." + parkour.getName() + ".world", parkour.getWorld().getName());
            config.set("parkours." + parkour.getName() + ".start", parkour.getStart().getBlockX() + "," + parkour.getStart().getBlockY() + "," + parkour.getStart().getBlockZ());
            config.set("parkours." + parkour.getName() + ".end", parkour.getEnd().getBlockX() + "," + parkour.getEnd().getBlockY() + "," + parkour.getEnd().getBlockZ());
            for(int i = 0; i < parkour.getCheckpoints().size(); i++) {
                config.set("parkours." + parkour.getName() + ".checkpoints." + i, parkour.getCheckpoints().get(i).getBlockX() + "," + parkour.getCheckpoints().get(i).getBlockY() + "," + parkour.getCheckpoints().get(i).getBlockZ());
            }
        }
    }

    public void addParkour(ParkourModel parkour) {
        parkours.add(parkour);
        saveParkour(parkour);
    }

    private void removePlayerFromParkour(Player player) {
        for(ParkourModel parkour: parkours) {
            if(parkour.getPlayers().contains(player)) {
                player.sendMessage("You left the" + parkour.getName() +  " parkour...");
                parkour.removePlayer(player);
            }
        }
    }

    private void addPlayerToParkour(Player player, ParkourModel parkour) {
        parkour.addPlayer(player);
        player.sendMessage("You joined the " + parkour.getName() + " parkour!");
    }

    private boolean isPlayerInParkour(Player player) {
        for(ParkourModel parkour: parkours) {
            if(parkour.getPlayers().contains(player)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    private void onPlayerTouchStart(PlayerMoveEvent e) {
        //check if the player touches a start checkpoint, and assign them to that parkour
        for(ParkourModel parkour: parkours) {
            if(e.getTo().getBlock().getLocation().equals(parkour.getStart())) {
                Player player = e.getPlayer();
                removePlayerFromParkour(player);
                addPlayerToParkour(player, parkour);
            }
        }
    }

    @EventHandler
    private void onPlayerTouchEnd(PlayerMoveEvent e) {
        //check if the player touches an end checkpoint, and remove them from that parkour
        for(ParkourModel parkour: parkours) {
            if(e.getTo().getBlock().getLocation().equals(parkour.getEnd())) {
                Player player = e.getPlayer();
                player.sendMessage("You completed the " + parkour.getName() + " parkour!");
                removePlayerFromParkour(player);
            }
        }
    }
}
