package com.blockycraft.blockydynmap.manager;

import com.blockycraft.blockyclaim.data.Claim;
import com.blockycraft.blockydynmap.BlockyDynmap;
import com.blockycraft.blockyfactions.data.Faction;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

/**
 * Gerencia toda integração com o Dynmap para visualização de claims
 * e suas cores vinculadas a facções. Corrigido para formato HTML.
 */
public class DynmapManager {
    private static final String MARKER_SET_ID = "blockyclaim.markers";
    private static final String MARKER_SET_LABEL = "Territórios (BlockyClaim)";
    private static final String DEFAULT_COLOR = "#808080";
    private static final double FILL_OPACITY = 0.3;
    private static final double LINE_OPACITY = 0.8;
    private static final int LINE_WEIGHT = 3;

    private final BlockyDynmap plugin;
    private MarkerAPI markerApi;
    private MarkerSet markerSet;

    public DynmapManager(BlockyDynmap plugin) {
        this.plugin = plugin;
        setupDynmap();
    }

    private void setupDynmap() {
        Plugin dynmapPlugin = plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapPlugin instanceof DynmapAPI) {
            DynmapAPI api = (DynmapAPI) dynmapPlugin;
            this.markerApi = api.getMarkerAPI();
            this.markerSet = markerApi.getMarkerSet(MARKER_SET_ID);
            if (this.markerSet == null) {
                this.markerSet = markerApi.createMarkerSet(MARKER_SET_ID, MARKER_SET_LABEL, null, false);
            } else {
                this.markerSet.setMarkerSetLabel(MARKER_SET_LABEL);
            }
        }
    }

    /**
     * Cria ou atualiza o marcador de um Claim no Dynmap, usando cor e tags da facção do dono.
     * Usa HTML para destaque (negrito), ao invés de markdown/asterisco.
     */
    public void createOrUpdateClaimMarker(Claim claim) {
        if (markerSet == null || claim == null) return;

        String markerId = generateMarkerId(claim);
        String worldName = claim.getWorldName();

        AreaMarker existingMarker = markerSet.findAreaMarker(markerId);
        if (existingMarker != null) {
            existingMarker.deleteMarker();
        }

        // Define as coordenadas do terreno
        double[] xCorners = { claim.getMinX(), claim.getMaxX() + 1 };
        double[] zCorners = { claim.getMinZ(), claim.getMaxZ() + 1 };

        // Cria o novo marcador
        AreaMarker marker = markerSet.createAreaMarker(markerId, "", false, worldName, xCorners, zCorners, false);
        if (marker == null) {
            System.out.println("[BlockyDynmap] Erro: Nao foi possivel criar o marcador para o claim: " + claim.getClaimName());
            return;
        }

        String ownerName = claim.getOwnerName();
        Faction ownerFaction = plugin.getBlockyFactions().getFactionManager().getPlayerFaction(ownerName);

        // ADIÇÃO: log de debug extra para identificar problemas de sincronização
        if (ownerFaction == null) {
            System.out.println("[BlockyDynmap] AVISO: claim '" + claim.getClaimName() + "' criado por '" + ownerName + "' sem facção detectada no momento da atualização do dynmap.");
        }

        String color;
        String label;
        if (ownerFaction != null) {
            color = ownerFaction.getColorHex();
            label = "§f" + claim.getClaimName() + " §7(" + ownerFaction.getTag() + ")";
        } else {
            color = DEFAULT_COLOR;
            label = "§f" + claim.getClaimName();
        }

        // *** CORRIGIDO PARA HTML ***
        StringBuilder description = new StringBuilder();
        description.append("<b>Dono:</b> ").append(ownerName);
        if (ownerFaction != null) {
            description.append("<br><b>Faccao:</b> ").append(ownerFaction.getName());
        }

        marker.setLabel(label, true);
        marker.setDescription(description.toString());
        int colorInt = Integer.parseInt(color.substring(1), 16);
        marker.setLineStyle(LINE_WEIGHT, LINE_OPACITY, colorInt);
        marker.setFillStyle(FILL_OPACITY, colorInt);
        // Se precisar, adicione outros ajustes ou refresh explícito
    }

    public void deleteClaimMarker(Claim claim) {
        if (markerSet == null || claim == null) return;
        String markerId = generateMarkerId(claim);
        AreaMarker marker = markerSet.findAreaMarker(markerId);
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    private String generateMarkerId(Claim claim) {
        return "claim_" + claim.getOwnerName().toLowerCase(Locale.ROOT) + "_" + claim.getClaimName();
    }

    public void cleanup() {
        // Limpeza customizada se necessário
    }
}
