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

    // Variável para guardar o ID da nossa tarefa de verificação
    private int checkTaskId = -1;

    @Override
    public void onEnable() {
        // Usa o agendador compatível com a Beta 1.7.3 para esperar as dependências.
        // Ele agenda uma tarefa que se repete e nos dá um ID para poder cancelá-la depois.
        this.checkTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                // Tenta encontrar as dependências a cada execução da tarefa
                if (setupDependencies()) {
                    // Se encontrou, cancela esta tarefa para que ela pare de rodar
                    getServer().getScheduler().cancelTask(checkTaskId);
                    // E então, inicializa o plugin
                    initialize();
                }
            }
        }, 0L, 20L); // Tenta imediatamente (0L) e repete a cada 1 segundo (20L ticks)
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

    /**
     * Verifica e obtém instâncias dos plugins dos quais o BlockyDynmap depende.
     * @return true se TODAS as dependências estiverem ativas, false caso contrário.
     */
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
     * Essencial para manter o mapa atualizado após reinicializações.
     */
    public void syncAllClaims() {
        ClaimManager claimManager = blockyClaim.getClaimManager();
        if (claimManager != null) {
            System.out.println("[BlockyDynmap] Iniciando sincronizacao completa de todos os claims existentes...");
            
            // CORREÇÃO FINAL: Usa o método getAllClaims() para obter a lista completa de terrenos.
            claimManager.getAllClaims().forEach(claim -> dynmapManager.createOrUpdateClaimMarker(claim));
            
            System.out.println("[BlockyDynmap] Sincronizacao completa finalizada.");
        }
    }

    // Getters para que outras classes possam acessar as APIs necessárias
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