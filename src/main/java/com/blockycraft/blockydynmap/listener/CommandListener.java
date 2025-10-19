package com.blockycraft.blockydynmap.listener;

import com.blockycraft.blockyclaim.data.Claim;
import com.blockycraft.blockydynmap.BlockyDynmap;
import com.blockycraft.blockyfactions.data.Faction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandListener implements Listener {

    private final BlockyDynmap plugin;

    public CommandListener(BlockyDynmap plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        String[] args = event.getMessage().toLowerCase().split(" ");
        String command = args[0].replace("/", "");

        // A lógica é executada aqui para capturar o estado da facção ANTES da mudança.
        handleCommand(command, args, player);
    }

    private void handleCommand(String command, String[] args, Player player) {
        // --- Gatilhos do BlockyClaim ---
        if (command.equals("claim")) {
            if (args.length > 1) {
                String subCommand = args[1];
                if (subCommand.equals("confirm") || subCommand.equals("buy") || subCommand.equals("occupy")) {
                    // Adia a verificação para garantir que o claim já foi processado pelo outro plugin
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            Claim claim = plugin.getBlockyClaim().getClaimManager().getClaimAt(player.getLocation());
                            if (claim != null) {
                                plugin.getDynmapManager().createOrUpdateClaimMarker(claim);
                            }
                        }
                    }, 1L);
                }
            }
        }
        // --- Gatilhos do BlockyFactions ---
        else if (command.equals("fac")) {
            if (args.length > 1) {
                String subCommand = args[1];

                if (subCommand.equals("entrar") || subCommand.equals("criar")) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            updateAllPlayerClaims(player.getName());
                        }
                    }, 1L);
                }
                else if (subCommand.equals("sair")) {
                    Faction faction = plugin.getBlockyFactions().getFactionManager().getPlayerFaction(player.getName());
                    if (faction == null) return;

                    // ***** LÓGICA DE DISSOLUÇÃO CORRIGIDA *****
                    // A dissolução ocorre se o LÍDER sai e NÃO HÁ OFICIAIS para transferir a liderança.
                    boolean isLeader = faction.getLeader().equalsIgnoreCase(player.getName());
                    boolean noOfficialsToPromote = faction.getOfficials().isEmpty();

                    final List<String> membersToUpdate = new ArrayList<String>();

                    if (isLeader && noOfficialsToPromote) {
                        // A facção SERÁ DISSOLVIDA. Salva todos os membros (incluindo Jogador2) para atualização.
                        membersToUpdate.add(faction.getLeader());
                        membersToUpdate.addAll(faction.getOfficials());
                        membersToUpdate.addAll(faction.getMembers());
                        if (faction.getTreasuryPlayer() != null && !faction.getTreasuryPlayer().isEmpty()) {
                            membersToUpdate.add(faction.getTreasuryPlayer());
                        }
                    } else {
                        // Cenário normal: Apenas o jogador atual está saindo, a facção continuará existindo.
                        membersToUpdate.add(player.getName());
                    }

                    // Adia a atualização para depois que BlockyFactions processar o comando
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            for (String playerName : membersToUpdate) {
                                updateAllPlayerClaims(playerName);
                            }
                        }
                    }, 1L);
                }
            }
        }
    }

    private void updateAllPlayerClaims(String playerName) {
        List<Claim> playerClaims = plugin.getBlockyClaim().getClaimManager().getClaimsByOwner(playerName);
        for (Claim claim : playerClaims) {
            plugin.getDynmapManager().createOrUpdateClaimMarker(claim);
        }
    }
}