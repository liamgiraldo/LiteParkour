package me.liamgiraldo.liteParkour;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class ParkourCreator implements Listener, CommandExecutor {
    private LiteParkour liteParkour;
    private ParkourController parkourController;
    private Player commandUser;

    private ArrayList<Location> checkpoints = new ArrayList<Location>();
    private String parkourName;

    public ParkourCreator(LiteParkour liteParkour, ParkourController parkourController) {
        this.liteParkour = liteParkour;
        this.parkourController = parkourController;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
            player.sendMessage(Arrays.toString(args));
            if(args.length > 0) {
                if(args[0].equalsIgnoreCase("create")) {

                    if(commandUser == null) {
                        startParkourCreation(player);
                        return true;
                    }
                    else if(commandUser == player) {
                        cancelParkourCreation(player);
                        return true;
                    }
                }
                else if(args[0].equalsIgnoreCase("cancel")) {
                    if(commandUser == player) {
                        cancelParkourCreation(player);
                        return true;
                    }
                }
                else if(args[0].equalsIgnoreCase("name")) {
                    if(commandUser == player) {
                        if(args.length > 1) {
                            parkourName = args[1];
                            player.sendMessage("Parkour name set to " + parkourName);
                            return true;
                        }
                    }
                }
                else if(args[0].equalsIgnoreCase("save")) {
                    if (commandUser == player) {
                        if (checkpoints.size() > 1 || parkourName != null) {
                            ParkourModel parkour = new ParkourModel(parkourName, player.getName(),checkpoints);
                            this.parkourController.addParkour(parkour);
                            commandUser = null;
                            this.parkourName = null;
                            checkpoints.clear();
                            player.sendMessage("Parkour saved");
                            return true;
                        } else {
                            player.sendMessage("You need at least 2 checkpoints to save a parkour");
                            player.sendMessage("You also need to name the parkour");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e) {
        //if the player is creating a parkour, check if they are placing a gold pressure plate
        if(isSomeoneCreatingParkour() && this.commandUser == e.getPlayer() && e.getBlock().getType().toString().contains("GOLD_PLATE")) {
            Block block = e.getBlock();
            Player player = e.getPlayer();
            if(checkpoints.isEmpty()) {
                checkpoints.add(block.getLocation());
                player.sendMessage("Start checkpoint created");
            }
            else {
                checkpoints.add(block.getLocation());
                player.sendMessage("Checkpoint " + checkpoints.size() + " created");
            }
        }
    }

    @EventHandler
    public void onBreakBlock(BlockPlaceEvent e) {
        //if the player is creating a parkour, check if they are breaking a gold pressure plate
        if(isSomeoneCreatingParkour() && this.commandUser == e.getPlayer() && e.getBlock().getType().toString().contains("GOLD_PLATE")) {
            Block block = e.getBlock();
            Player player = e.getPlayer();
            if(checkpoints.contains(block.getLocation())) {
                checkpoints.remove(block.getLocation());
                player.sendMessage("Checkpoint removed");
            }
        }
    }

    public boolean isSomeoneCreatingParkour() {
        return commandUser != null;
    }

    public void cancelParkourCreation(Player player) {
        commandUser = null;
        this.parkourName = null;
        this.checkpoints.clear();
        player.sendMessage("Parkour creation cancelled");
    }

    public void startParkourCreation(Player player) {
        commandUser = player;
        this.checkpoints.clear();
        this.parkourName = null;
        player.sendMessage("Parkour creator enabled");
        player.sendMessage("Place gold pressure plates to create checkpoints");
        player.sendMessage("Type /parkour cancel to cancel");
        player.sendMessage("Type /parkour save to save");
        player.sendMessage("Before you save, type /parkour name <name> to name your parkour");
    }
}
