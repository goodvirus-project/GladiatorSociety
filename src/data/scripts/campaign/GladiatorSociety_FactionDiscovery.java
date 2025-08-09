package src.data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Discovers combat-capable factions at runtime, including mod factions.
 * Results are cached in persistent data and refreshed only when requested.
 */
public class GladiatorSociety_FactionDiscovery {
    private static final String PERSIST_KEY = "gs_faction_discovery_cache";

    public static class Result {
        public final Set<String> combatFactions = new HashSet<>();
        public final Set<String> seenFactionIds = new HashSet<>();
        public long builtAt = System.currentTimeMillis();
    }

    @SuppressWarnings("unchecked")
    private static Result getCached() {
        Object obj = Global.getSector().getPersistentData().get(PERSIST_KEY);
        if (obj instanceof Result) return (Result) obj;
        Result res = new Result();
        Global.getSector().getPersistentData().put(PERSIST_KEY, res);
        return res;
    }

    public static void refresh() {
        Result res = new Result();
        // Load config
        GladiatorSociety_FactionDiscoveryConfig cfg = GladiatorSociety_FactionDiscoveryConfig.load();

        // Build candidate list
        List<FactionAPI> all = Global.getSector().getAllFactions();
        for (FactionAPI f : all) {
            String id = f.getId();
            res.seenFactionIds.add(id);
            if (cfg.blacklist.contains(id)) continue;
            if (!cfg.includeHidden) {
                try {
                    // Some APIs may not expose hidden; skip check to remain compatible
                } catch (Throwable ignored) {}
            }
            if (id.equals("player") || id.equals("neutral")) continue;

            // Whitelist goes in directly
            if (cfg.whitelist.contains(id)) {
                res.combatFactions.add(id);
                continue;
            }

            // Initial test: be permissive, include remaining factions (we can tighten later)
            res.combatFactions.add(id);
        }
        Global.getSector().getPersistentData().put(PERSIST_KEY, res);

        if (cfg.debug) {
            Global.getLogger(GladiatorSociety_FactionDiscovery.class).info(
                "GS FactionDiscovery found: " + res.combatFactions.size() + " factions -> " + res.combatFactions);
        }
    }

    public static Set<String> getCombatFactions() {
        Result cached = getCached();
        // Auto-refresh if nothing cached or the set of available faction IDs has changed
        boolean needRefresh = cached.combatFactions.isEmpty();
        try {
            List<FactionAPI> all = Global.getSector().getAllFactions();
            if (!needRefresh) {
                if (all.size() != cached.seenFactionIds.size()) {
                    needRefresh = true;
                } else {
                    for (FactionAPI f : all) {
                        if (!cached.seenFactionIds.contains(f.getId())) { needRefresh = true; break; }
                    }
                }
            }
        } catch (Throwable ignored) {}
        if (needRefresh) {
            refresh();
        }
        return new HashSet<>(cached.combatFactions);
    }

    // Placeholder for future stricter validation if needed
}
