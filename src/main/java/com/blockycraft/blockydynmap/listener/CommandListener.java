package com.blockycraft.blockydynmap.listener;

import com.blockycraft.blockyclaim.data.Claim;
import com.blockycraft.blockydynmap.BlockyDynmap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener implements Listener {

    private final BlockyDynmap plugin;

    public CommandListener(BlockyDynmap plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().toLowerCase().split(" ");
        String command = args[0].replace("/", "");

        // Roda a lógica um "tick" depois do comando, para garantir que a ação do plugin original já tenha sido processada
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                handleCommand(command, args, player);
            }
        }, 1L);
    }

    private void handleCommand(String command, String[] args, Player player) {
        // --- Gatilhos do BlockyClaim ---
        if (command.equals("claim")) {
            if (args.length > 1) {
                String subCommand = args[1];

                // Requisito 1: Um novo claim foi criado
                if (subCommand.equals("confirm")) {
                    Claim claim = plugin.getBlockyClaim().getClaimManager().getClaimAt(player.getLocation());
                    if (claim != null && claim.getOwnerName().equalsIgnoreCase(player.getName())) {
                        plugin.getDynmapManager().createOrUpdateClaimMarker(claim);
                    }
                }
                // Requisito 4 e 5: Um claim foi comprado ou ocupado
                else if (subCommand.equals("buy") || subCommand.equals("occupy")) {
                     Claim claim = plugin.getBlockyClaim().getClaimManager().getClaimAt(player.getLocation());
                    if (claim != null) {
                        plugin.getDynmapManager().createOrUpdateClaimMarker(claim);
                    }
                }
            }
        }
        // --- Gatilhos do BlockyFactions ---
        else if (command.equals("fac")) {
            if (args.length > 1) {
                String subCommand = args[1];

                // Requisito 7: Jogador entrou em uma facção
                if (subCommand.equals("entrar")) {
                    updateAllPlayerClaims(player);
                }
                // Requisito 6: Jogador saiu da facção
                else if (subCommand.equals("sair")) {
                    updateAllPlayerClaims(player);
                }
            }
        }
    }
    
    /**
     * Atualiza todos os marcadores de um jogador específico.
     * Útil quando ele muda de status de facção.
     */
    private void updateAllPlayerClaims(Player player) {
        List<Claim> playerClaims = plugin.getBlockyClaim().getClaimManager().getClaimsByOwner(player.getName());
        for (Claim claim : playerClaims) {
            plugin.getDynmapManager().createOrUpdateClaimMarker(claim);
        }
    }
}