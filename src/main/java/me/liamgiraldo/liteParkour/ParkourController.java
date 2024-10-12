package me.liamgiraldo.liteParkour;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class ParkourController implements Listener, CommandExecutor {
    private LiteParkour liteParkour;
    private ArrayList<ParkourModel> parkours = new ArrayList<ParkourModel>();
    private ArrayList<ProgressModel> progressModels = new ArrayList<ProgressModel>();
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

            liteParkour.saveConfig();
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
        if(isPlayerInParkour(player)) {
            ParkourModel parkour = getPlayerParkour(player);
            parkour.removePlayer(player);
            player.sendMessage("You left the " + parkour.getName() + " parkour!");
            progressModels.remove(getProgressModel(player));
        }
    }


    private void addPlayerToParkour(Player player, ParkourModel parkour) {
        if(isPlayerInParkour(player)) {
            player.sendMessage("You are already in a parkour");
            removePlayerFromParkour(player);
        }
        parkour.addPlayer(player);
        player.sendMessage("You joined the " + parkour.getName() + " parkour!");
        progressModels.add(new ProgressModel(parkour, player));
    }

    private boolean isPlayerInParkour(Player player) {
        for(ProgressModel progressModel: progressModels) {
            if(progressModel.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    private ParkourModel getPlayerParkour(Player player) {
        for(ProgressModel progressModel: progressModels) {
            if(progressModel.getPlayer().equals(player)) {
                return progressModel.getParkour();
            }
        }
        return null;
    }

    private boolean isPlayerInThisParkour(Player player, ParkourModel parkour) {
        return parkour.getPlayers().contains(player);
    }

    @EventHandler
    private void onPlayerTouchStart(PlayerMoveEvent e) {
        //check if the player touches a start checkpoint, and assign them to that parkour
        for(ParkourModel parkour: parkours) {
            if(e.getTo().getBlock().getLocation().equals(parkour.getStart())) {
                Player player = e.getPlayer();
                //if the player is in the same parkour, don't add them again
                if(isPlayerInThisParkour(player, parkour)) {
                    //you would also want to restart the player's progress in the parkour, but that hasn't been implemented
                    //TODO: restart player's progress in parkour
                    getProgressModel(player).restartProgress();
                    return;
                }
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
//                if(parkour.getPlayers().contains(e.getPlayer())) {
//                    e.getPlayer().sendMessage("You completed the " + parkour.getName() + " parkour!");
//                    removePlayerFromParkour(e.getPlayer());
//                }
                if(isPlayerInThisParkour(e.getPlayer(), parkour)) {
                    e.getPlayer().sendMessage("You completed the " + parkour.getName() + " parkour!");
                    ProgressModel progressModel = getProgressModel(e.getPlayer());
                    progressModel.setCompleted(true);
                    saveTime(progressModel);
                    removePlayerFromParkour(e.getPlayer());
                }
            }
        }
    }

    private void saveTime(ProgressModel progressModel) {
        FileConfiguration config = liteParkour.getTimeConfig();
        if(config.contains(progressModel.getPlayer().getUniqueId().toString())) {
            if(config.contains(progressModel.getPlayer().getUniqueId().toString() + "." + progressModel.getParkour().getName())) {
                int time = config.getInt(progressModel.getPlayer().getUniqueId().toString() + "." + progressModel.getParkour().getName());
                if(progressModel.getTime() < time) {
                    config.set(progressModel.getPlayer().getUniqueId().toString() + "." + progressModel.getParkour().getName(), progressModel.getTime());
                }
            }
            else{
                config.set(progressModel.getPlayer().getUniqueId().toString() + "." + progressModel.getParkour().getName(), progressModel.getTime());
            }
        }
        else{
            config.set(progressModel.getPlayer().getUniqueId().toString(), progressModel.getPlayer().getUniqueId().toString());
            config.set(progressModel.getPlayer().getUniqueId().toString() + "." + progressModel.getParkour().getName(), progressModel.getTime());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            //the available commands are
            // /parkour leave - leave the parkour you are in
            // /parkour list - list all parkours
            // /parkour restart - restart the parkour you are in
            // /parkour top - list the top 10 players in the parkour you are in

            if(args.length > 0) {
                if(args[0].equalsIgnoreCase("leave")) {
                    removePlayerFromParkour(player);
                    return true;
                }
                else if(args[0].equalsIgnoreCase("list")) {
                    for(ParkourModel parkour: parkours) {
                        player.sendMessage(parkour.getName() + " in world " + parkour.getWorld().getName());
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("restart")) {
                    //TODO: restart player's progress in parkour
                    ProgressModel playerProgress = getProgressModel(player);
                    if(playerProgress != null) {
                        playerProgress.restartProgress();
                    }
                    else{
                        player.sendMessage("You are not in a parkour");
                    }
                    return true;

                }
                else if(args[0].equalsIgnoreCase("top")) {
                    //get the top 10 times for the parkour the player is in
                    HashMap<Player, Integer> times = getTimes();
                    HashMap<Player, Integer> topTimes = getTopTimes(times);
                    for(Player topPlayer: topTimes.keySet()) {
                        player.sendMessage(topPlayer.getName() + " - " + topTimes.get(topPlayer));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private ProgressModel getProgressModel(Player player) {
        for(ProgressModel progressModel: progressModels) {
            if(progressModel.getPlayer().equals(player)) {
                return progressModel;
            }
        }
        return null;
    }

    //reads from the time config file and returns the top 10 times for the parkour the player is in
    private HashMap<Player, Integer> getTimes(){
        /**
         * This is roughly what the times file would look like
         * UUID:
         *   Parkour1: 608
         *   Parkour2: 609
         *   Parkour3: 610
         * UUID2:
         *   Parkour1: 611
         *   Parkour2: 612
         *   Parkour3: 613
         * */
        HashMap<Player, Integer> times = new HashMap<Player, Integer>();
        FileConfiguration config = liteParkour.getTimeConfig();
        for(String playerUUID: config.getKeys(false)) {
            if (playerUUID.equals("UUID")) {
                for (String parkourName : config.getConfigurationSection(playerUUID).getKeys(false)) {
                    for (ParkourModel parkour : parkours) {
                        if (parkour.getName().equals(parkourName)) {
                            times.put(liteParkour.getServer().getPlayer(playerUUID), config.getInt(playerUUID + "." + parkourName));
                        }
                    }
                }
            }
        }
        return times;
    }

    private HashMap<Player, Integer> getTopTimes(HashMap<Player, Integer> times){
        //sort the times and return the top 10
        HashMap<Player, Integer> topTimes = new HashMap<Player, Integer>();
        int i = 0;
        for(Player player: times.keySet()) {
            if(i < 10) {
                topTimes.put(player, times.get(player));
                i++;
            }
        }
        return topTimes;
    }
}
