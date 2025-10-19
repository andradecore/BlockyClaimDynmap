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

        String color;
        String label;

        if (ownerFaction != null) {
            color = ownerFaction.getColorHex();
            label = "§f" + claim.getClaimName() + " §7(" + ownerFaction.getTag() + ")";
        } else {
            color = DEFAULT_COLOR;
            label = "§f" + claim.getClaimName();
        }

        String description = "<div><strong>Dono:</strong> " + ownerName + "</div>";
        if (ownerFaction != null) {
            description += "<div><strong>Faccao:</strong> " + ownerFaction.getName() + "</div>";
        }

        marker.setLabel(label, true);
        marker.setDescription(description);

        int colorInt = Integer.parseInt(color.substring(1), 16);
        marker.setLineStyle(LINE_WEIGHT, LINE_OPACITY, colorInt);
        marker.setFillStyle(FILL_OPACITY, colorInt);
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
    }
}