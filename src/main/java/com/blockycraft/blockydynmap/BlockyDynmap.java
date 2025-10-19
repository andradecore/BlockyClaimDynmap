package com.blockycraft.blockydynmap;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.managers.ClaimManager;
import com.blockycraft.blockyfactions.BlockyFactions;
import com.blockycraft.blockydynmap.listener.CommandListener;
import com.blockycraft.blockydynmap.manager.DynmapManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockyDynmap extends JavaPlugin {

    private DynmapManager dynmapManager;
    private BlockyClaim blockyClaim;
    private BlockyFactions blockyFactions;
    private boolean initialized = false;

    private int checkTaskId = -1;

    @Override
    public void onEnable() {
        // O dynmap é nojento de pesado e carrega muito depois pqp
        this.checkTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (setupDependencies()) {
                    getServer().getScheduler().cancelTask(checkTaskId);
                    initialize();
                }
            }
        }, 0L, 20L);
    }

    /**
     * Contém a lógica de inicialização principal do plugin.
     * Só será chamado quando setupDependencies() retornar true.
     */
    private void initialize() {
        if (initialized) return;

        this.dynmapManager = new DynmapManager(this);
        this.syncAllClaims();
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);

        System.out.println("[BlockyDynmap] Dependencias encontradas. Plugin ativado e integrado com sucesso!");
        initialized = true;
    }

    @Override
    public void onDisable() {
        if (dynmapManager != null) {
            dynmapManager.cleanup();
        }
        System.out.println("[BlockyDynmap] Plugin desativado.");
    }

    private boolean setupDependencies() {
        PluginManager pm = getServer().getPluginManager();

        Plugin dynmapPlugin = pm.getPlugin("dynmap");
        if (dynmapPlugin == null || !dynmapPlugin.isEnabled()) {
            System.out.println("[BlockyDynmap] Aguardando o plugin Dynmap ser ativado...");
            return false;
        }

        Plugin claimPlugin = pm.getPlugin("BlockyClaim");
        if (claimPlugin == null || !(claimPlugin instanceof BlockyClaim)) {
            System.out.println("[BlockyDynmap] ERRO CRITICO: BlockyClaim nao encontrado.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        this.blockyClaim = (BlockyClaim) claimPlugin;

        Plugin factionsPlugin = pm.getPlugin("BlockyFactions");
        if (factionsPlugin == null || !(factionsPlugin instanceof BlockyFactions)) {
            System.out.println("[BlockyDynmap] ERRO CRITICO: BlockyFactions nao encontrado.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        this.blockyFactions = (BlockyFactions) factionsPlugin;

        return true;
    }

    /**
     * Varre todos os claims existentes no BlockyClaim e os desenha no Dynmap.
     * Essencial para manter essa bosta atualizada após reinicializações.
     */
    public void syncAllClaims() {
        ClaimManager claimManager = blockyClaim.getClaimManager();
        if (claimManager != null) {
            System.out.println("[BlockyDynmap] Iniciando sincronizacao completa de todos os claims existentes...");
            
            claimManager.getAllClaims().forEach(claim -> dynmapManager.createOrUpdateClaimMarker(claim));
            
            System.out.println("[BlockyDynmap] Sincronizacao completa finalizada.");
        }
    }

    public DynmapManager getDynmapManager() {
        return dynmapManager;
    }

    public BlockyClaim getBlockyClaim() {
        return blockyClaim;
    }

    public BlockyFactions getBlockyFactions() {
        return blockyFactions;
    }
}