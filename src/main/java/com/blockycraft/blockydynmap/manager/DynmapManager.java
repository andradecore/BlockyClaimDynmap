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

public class DynmapManager {

    private static final String MARKER_SET_ID = "blockyclaim.markers";
    private static final String MARKER_SET_LABEL = "Territórios (BlockyClaim)";
    private static final String DEFAULT_COLOR = "#808080"; // Cinza
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

            // Cria ou obtém o "conjunto" de marcadores onde os claims serão armazenados
            this.markerSet = markerApi.getMarkerSet(MARKER_SET_ID);
            if (this.markerSet == null) {
                this.markerSet = markerApi.createMarkerSet(MARKER_SET_ID, MARKER_SET_LABEL, null, false);
            } else {
                this.markerSet.setMarkerSetLabel(MARKER_SET_LABEL);
            }
        }
    }

    /**
     * A função central que cria ou atualiza um marcador de claim no Dynmap.
     * @param claim O objeto Claim do BlockyClaim.
     */
    public void createOrUpdateClaimMarker(Claim claim) {
        if (markerSet == null || claim == null) return;

        String markerId = generateMarkerId(claim);
        String worldName = claim.getWorldName();

        // Encontra o marcador existente ou cria um novo
        AreaMarker marker = markerSet.findAreaMarker(markerId);
        if (marker == null) {
            double[] xCorners = { claim.getMinX(), claim.getMaxX() + 1 };
            double[] zCorners = { claim.getMinZ(), claim.getMaxZ() + 1 };
            marker = markerSet.createAreaMarker(markerId, "", false, worldName, xCorners, zCorners, false);
        }

        if (marker == null) {
            System.out.println("[BlockyDynmap] Erro: Nao foi possivel criar o marcador para o claim: " + claim.getClaimName());
            return;
        }

        // --- Lógica de Cor e Label ---
        String ownerName = claim.getOwnerName();
        Faction ownerFaction = plugin.getBlockyFactions().getFactionManager().getPlayerFaction(ownerName);

        String color;
        String label;

        if (ownerFaction != null) {
            // Requisito 3, 5, 7: O jogador tem uma facção, usa a cor da facção
            color = ownerFaction.getColorHex();
            // Requisito 8: Label com nome da facção e do jogador
            label = "§f" + claim.getClaimName() + " §7(" + ownerFaction.getTag() + ")";
        } else {
            // Requisito 2, 4, 6: O jogador não tem facção, usa cinza
            color = DEFAULT_COLOR;
            label = "§f" + claim.getClaimName();
        }

        // --- Descrição do Marcador (HTML para o pop-up no mapa) ---
        String description = "<div><strong>Dono:</strong> " + ownerName + "</div>";
        if (ownerFaction != null) {
            description += "<div><strong>Facção:</strong> " + ownerFaction.getName() + "</div>";
        }

        // --- Atualiza o marcador com as novas informações ---
        marker.setLabel(label, true); // O 'true' indica que a label pode conter HTML/códigos de cor
        marker.setDescription(description);

        int colorInt = Integer.parseInt(color.substring(1), 16);
        marker.setLineStyle(LINE_WEIGHT, LINE_OPACITY, colorInt);
        marker.setFillStyle(FILL_OPACITY, colorInt);
    }

    /**
     * Remove um marcador de claim do mapa.
     * @param claim O claim a ser removido.
     */
    public void deleteClaimMarker(Claim claim) {
        if (markerSet == null || claim == null) return;
        String markerId = generateMarkerId(claim);
        AreaMarker marker = markerSet.findAreaMarker(markerId);
        if (marker != null) {
            marker.deleteMarker();
        }
    }
    
    /**
     * Gera um ID único para o marcador baseado no claim.
     */
    private String generateMarkerId(Claim claim) {
        // Usar um formato consistente garante que sempre possamos encontrar o marcador novamente
        return "claim_" + claim.getOwnerName().toLowerCase(Locale.ROOT) + "_" + claim.getClaimName();
    }

    public void cleanup() {
        // No futuro, pode ser usado para limpar o markerSet se necessário
    }
}