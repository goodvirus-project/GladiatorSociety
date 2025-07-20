package src.data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import src.data.utils.GladiatorSociety_Constants;

/**
 * System für Blueprint-Belohnungen basierend auf Credit-Wert
 * Alle 3 Runden wird ein Blueprint vergeben, dessen Wert dem aktuellen Credit-Reward entspricht
 */
public class GladiatorSociety_BlueprintRewardSystem {
    
    public static final Logger LOG = Global.getLogger(GladiatorSociety_BlueprintRewardSystem.class);
    
    private static final String BLUEPRINT_REWARDS_PATH = "data/config/gsounty/BlueprintRewards.csv";
    private static List<BlueprintReward> BLUEPRINT_REWARDS = new ArrayList<>();
    
    // Lade Blueprint-Konfiguration beim ersten Zugriff
    static {
        loadBlueprintRewards();
    }
    
    /**
     * Prüft ob der Spieler eine Blueprint-Belohnung erhalten soll
     * @param currentRound Aktuelle Runde
     * @return true wenn Blueprint vergeben werden soll
     */
    public static boolean shouldGiveBlueprintReward(int currentRound) {
        return currentRound > 0 && currentRound % 3 == 0;
    }
    
    /**
     * Lädt Blueprint-Belohnungen aus der CSV-Datei
     */
    private static void loadBlueprintRewards() {
        try {
            JSONArray csv = Global.getSettings().getMergedSpreadsheetDataForMod("item_id", BLUEPRINT_REWARDS_PATH, GladiatorSociety_Constants.MOD_ID);
            LOG.info("GS Blueprint Rewards: Loading blueprint rewards from CSV");
            
            for (int i = 0; i < csv.length(); i++) {
                JSONObject row = csv.getJSONObject(i);
                
                String itemId = row.optString("item_id", "");
                if (itemId.isEmpty() || itemId.startsWith("#")) continue; // Skip comments
                
                String blueprintType = getBlueprintTypeFromCategory(row.optString("blueprint_type", ""));
                String name = row.optString("name", itemId);
                int minValue = row.optInt("min_credit_value", 0);
                int maxValue = row.optInt("max_credit_value", 999999);
                float weight = (float) row.optDouble("weight", 1.0);
                
                // Prüfe ob Item existiert
                if (isValidItem(itemId, blueprintType)) {
                    BLUEPRINT_REWARDS.add(new BlueprintReward(itemId, blueprintType, name, minValue, maxValue, weight));
                    LOG.info("GS Blueprint Rewards: Added " + name + " (" + minValue + "-" + maxValue + " credits)");
                }
            }
            
            LOG.info("GS Blueprint Rewards: Loaded " + BLUEPRINT_REWARDS.size() + " blueprint rewards");
            
        } catch (IOException | JSONException ex) {
            LOG.error("GS Blueprint Rewards: Failed to load blueprint rewards", ex);
        }
    }
    
    /**
     * Gibt einen Blueprint basierend auf dem Credit-Wert der Runde
     * @param creditValue Der Credit-Wert der aktuellen Runde
     * @param playerCargo Das Cargo des Spielers
     */
    public static void giveBlueprintReward(int creditValue, CargoAPI playerCargo) {
        LOG.info("GS Blueprint Reward: Giving blueprint for credit value: " + creditValue);
        
        WeightedRandomPicker<BlueprintReward> picker = new WeightedRandomPicker<>();
        
        // Sammle verfügbare Blueprints basierend auf Credit-Wert
        for (BlueprintReward reward : BLUEPRINT_REWARDS) {
            if (creditValue >= reward.minValue && creditValue <= reward.maxValue) {
                picker.add(reward, reward.weight);
            }
        }
        
        if (picker.isEmpty()) {
            LOG.warn("GS Blueprint Reward: No blueprints available for credit value: " + creditValue);
            return;
        }
        
        BlueprintReward reward = picker.pick();
        if (reward != null) {
            addBlueprintToPlayer(reward, playerCargo);
            LOG.info("GS Blueprint Reward: Gave " + reward.name + " blueprint");
        }
    }
    
    /**
     * Konvertiert Blueprint-Kategorie zu Starsector Item-ID
     */
    private static String getBlueprintTypeFromCategory(String category) {
        switch (category.toLowerCase()) {
            case "ship": return Items.SHIP_BP;
            case "weapon": return Items.WEAPON_BP;
            case "fighter": return Items.FIGHTER_BP;
            default: return Items.SHIP_BP;
        }
    }
    
    /**
     * Prüft ob ein Item existiert
     */
    private static boolean isValidItem(String itemId, String blueprintType) {
        try {
            switch (blueprintType) {
                case Items.SHIP_BP:
                    return Global.getSettings().doesVariantExist(itemId);
                case Items.WEAPON_BP:
                    return Global.getSettings().getWeaponSpec(itemId) != null;
                case Items.FIGHTER_BP:
                    return Global.getSettings().getFighterWingSpec(itemId) != null;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    

    
    /**
     * Fügt den Blueprint zum Spieler-Cargo hinzu
     */
    private static void addBlueprintToPlayer(BlueprintReward reward, CargoAPI playerCargo) {
        SpecialItemData blueprintData = new SpecialItemData(reward.blueprintType, reward.itemId);
        playerCargo.addSpecial(blueprintData, 1);
        
        // Nachricht an den Spieler
        Global.getSector().getCampaignUI().addMessage(
            "Blueprint Reward: Received " + reward.name + "!",
            Global.getSettings().getColor("textFriendColor")
        );
    }
    
    /**
     * Datenklasse für Blueprint-Belohnungen
     */
    private static class BlueprintReward {
        public final String itemId;
        public final String blueprintType;
        public final String name;
        public final int minValue;
        public final int maxValue;
        public final float weight;
        
        public BlueprintReward(String itemId, String blueprintType, String name, int minValue, int maxValue, float weight) {
            this.itemId = itemId;
            this.blueprintType = blueprintType;
            this.name = name;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.weight = weight;
        }
    }
}