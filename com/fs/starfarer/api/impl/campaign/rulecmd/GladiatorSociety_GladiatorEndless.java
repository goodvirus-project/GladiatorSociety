package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import org.lwjgl.input.Keyboard;
import src.data.scripts.campaign.GladiatorSociety_EndlessContent;
import src.data.scripts.campaign.GladiatorSociety_TinyFleetFactoryV2;
import src.data.scripts.campaign.dataclass.*;
import src.data.utils.GladiatorSociety_Constants;

public class GladiatorSociety_GladiatorEndless extends BaseCommandPlugin {

    public static final String GLADIATORSOCIETY_ENDLESSCONTENTKEY = "$GladiatorSociety_EndlessContentKey";

    public GladiatorSociety_EndlessContent endcontent;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String arg = params.get(0).getString(memoryMap);

        if (endcontent == null) {
            if (Global.getSector().getPersistentData().containsKey(GLADIATORSOCIETY_ENDLESSCONTENTKEY)) {
                endcontent = (GladiatorSociety_EndlessContent) Global.getSector().getPersistentData().get(GLADIATORSOCIETY_ENDLESSCONTENTKEY);
            } else {
                endcontent = new GladiatorSociety_EndlessContent();
                Global.getSector().getPersistentData().put(GLADIATORSOCIETY_ENDLESSCONTENTKEY, endcontent);
            }
        }
        if (endcontent == null) {
            return false;
        }

        switch (arg) {
            case "Display":
                display(dialog, memoryMap.get(MemKeys.LOCAL));
                return true;
            case "Accept":
                accept(dialog, memoryMap.get(MemKeys.LOCAL));
                return true;
            // Reward case removed - rewards are now given automatically
            case "Increment":
                endcontent.incEndlessRound();
                return true;
            // Choice case removed - rewards are now given automatically

        }

        return false;
    }

    public void display(InteractionDialogAPI dialog, MemoryAPI memory) {

        OptionPanelAPI opts = dialog.getOptionPanel();
        opts.clearOptions();

        dialog.getTextPanel().addParagraph("Reward: " + endcontent.getEndlessReward() + " credits");
        dialog.getTextPanel().addParagraph("Round: " + endcontent.getEndlessRound());
        dialog.getTextPanel().highlightInLastPara(Color.BLUE, endcontent.getEndlessRound() + "");
        float point = Global.getSector().getPlayerFleet().getFleetPoints();
        /* float difficulty = point*3 - endcontent.getEndlessPower();
        int dif = 0;

        if (difficulty < -1 * (point / 10)) {
            dif--;
            if (difficulty < -1 * (point / 5)) {
                dif--;
                if (difficulty < -1 * point) {
                    dif--;
                }
            }
        } else {
            if (difficulty > (point / 5)) {
                dif++;
                if (difficulty > (point)) {
                    dif++;
                }
            }
        }
        String difmes = "Normal";
        Color dangercolor = Color.ORANGE;
        switch (dif) {
            case -3:
                difmes = "Impossible";
                dangercolor = Color.RED;
                break;
            case -2:
                difmes = "Very hard";
                dangercolor = Color.RED;
                break;
            case -1:
                difmes = "Hard";
                break;
            case 0:
                break;
            case 1:
                difmes = "Easy";
                dangercolor = Color.GREEN;
                break;
            case 2:
                difmes = "Very easy";
                dangercolor = Color.GREEN;

                break;
        }*/
        dialog.getTextPanel().addParagraph("Estimated Comparaison force : You(" + point + ") Ennemi(" + endcontent.getEndlessPower() + ")", Color.RED);

        //   dialog.getTextPanel().addParagraph("Estimated Difficulty: " + difmes);
        //    dialog.getTextPanel().highlightInLastPara(dangercolor, difmes);
        //dialog.getTextPanel().addParagraph(difmes, dangercolor);
        dialog.getTextPanel().addParagraph("Reward: " + endcontent.getEndlessReward() + " credits");
        FactionAPI faction = Global.getSector().getFaction(endcontent.getEndlessFaction());
        if (faction != null) {
            dialog.getTextPanel().addParagraph("Faction: " + faction.getDisplayNameLongWithArticle());
        }
        dialog.getTextPanel().addParagraph("WARNING: The fleet will appear near you.", Color.RED);
        opts.addOption("Accept", "AcceptEndless", "Accept");
        opts.setShortcut("AcceptEndless", Keyboard.KEY_G,
                false, false, false, false);

        // Reward button removed - rewards are now given automatically
        if (Global.getSettings().isDevMode()) {
            opts.addOption(">> (dev) Increment", "DevIncEndless", "Clic on the button will have the same effect than beat a round.");
            opts.setShortcut("AcceptEndless", Keyboard.KEY_D,
                    false, false, false, false);
        }

        String exitOpt = "gladiatorComRelay";
        opts.addOption(Misc.ucFirst("back"), exitOpt);
        opts.setShortcut(exitOpt, Keyboard.KEY_ESCAPE,
                false, false, false, false);
    }

    // Reward method removed - rewards are now given automatically

    public void accept(InteractionDialogAPI dialog, MemoryAPI memory) {

        OptionPanelAPI opts = dialog.getOptionPanel();
        opts.clearOptions();
        // GladiatorSociety_GladiatorManager.getInstance().createEvent(endcontent);
        dialog.getTextPanel().addParagraph("The mission have be accepted");

        final SectorEntityToken entity = dialog.getInteractionTarget();
        final CampaignFleetAPI endlessfleet = spawnFleet(endcontent);

        dialog.setInteractionTarget(endlessfleet);

        final FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();
        config.leaveAlwaysAvailable = false;
        config.showCommLinkOption = false;
        config.showEngageText = true;

        config.showFleetAttitude = false;
        config.showTransponderStatus = false;
        config.showWarningDialogWhenNotHostile = false;
        config.alwaysAttackVsAttack = true;
        config.impactsAllyReputation = false;
        config.impactsEnemyReputation = false;
        config.pullInAllies = false;
        config.pullInEnemies = false;
        config.pullInStations = false;
        config.lootCredits = false;
        config.firstTimeEngageOptionText = "Engage the gladiator fleet";
        config.afterFirstTimeEngageOptionText = "Re-engage the gladiator fleet";
        config.noSalvageLeaveOptionText = "Continue";
        config.dismissOnLeave = true;
        config.printXPToDialog = true;

        long seed = memory.getLong(MemFlags.SALVAGE_SEED);
        config.salvageRandom = Misc.getRandom(seed, 75);

        final FleetInteractionDialogPluginImpl plugin = new FleetInteractionDialogPluginImpl(config);
        final InteractionDialogPlugin originalPlugin = dialog.getPlugin();
        config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {

            @Override
            public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                bcc.aiRetreatAllowed = false;
                bcc.enemyDeployAll = true;
            }

            @Override
            public void notifyLeave(InteractionDialogAPI dialog) {
                // nothing in there we care about keeping; clearing to reduce savefile size
                endlessfleet.getMemoryWithoutUpdate().clear();
                // there's a "standing down" assignment given after a battle is finished that we don't care about
                endlessfleet.clearAssignments();
                endlessfleet.deflate();

                dialog.setPlugin(originalPlugin);
                dialog.setInteractionTarget(entity);

                //Global.getSector().getCampaignUI().clearMessages();
                if (plugin.getContext() instanceof FleetEncounterContext) {
                    FleetEncounterContext context = (FleetEncounterContext) plugin.getContext();
                    if(context.didPlayerWinEncounterOutright())
                    {
                    //if (context.didPlayerWinEncounter()) {
                        int payment = (int) (endcontent.getEndlessReward() * context.getBattle().getPlayerInvolvementFraction());
                        Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);
                        endcontent.incEndlessRound();
                        
                        
                        //  dialog.dismiss();
                        // FireBest.fire(null, dialog, memory, "GladiatorEBDismissDialog");
                    } else {
                        dialog.dismiss();
                    }
                } else {
                    dialog.dismiss();
                }
            }
        };
        dialog.setPlugin(plugin);
        plugin.init(dialog);
    }

    private FactionAPI pickFaction(GladiatorSociety_EndlessContent content) {
        FactionAPI faction = Global.getSector().getFaction(content.getEndlessFaction());
        return faction;
    }

    private PersonAPI initPerson(FactionAPI faction) {
        float level = ((OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp")).getMaxLevel(null);
        int personLevel = (int) (5 + level * 1.5f);
        return OfficerManagerEvent.createOfficer(Global.getSector().getFaction(faction.getId()),
                personLevel, true);
    }

    private CampaignFleetAPI spawnFleet(GladiatorSociety_EndlessContent content) {
        FactionAPI faction = pickFaction(content);
        PersonAPI person = initPerson(faction);

        float random = (int) (Math.random() * 10) / 10f;

        FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                faction.getId(), // quality will always be reduced by non-market-faction penalty, which is what we want 
                null,
                FleetTypes.PERSON_BOUNTY_FLEET,
                content.getEndlessPower() + random, // combatPts
                0, // freighterPts 
                0, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                1f // qualityMod
        );
        params.ignoreMarketFleetSizeMult = true;

        CampaignFleetAPI fleet = GladiatorSociety_TinyFleetFactoryV2.createFleet(params);
        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        fleet.setCommander(person);
        fleet.getFlagship().setCaptain(person);
        FleetFactoryV3.addCommanderSkills(person, fleet, null);

        Misc.makeImportant(fleet, GladiatorSociety_Constants.GSFACTION_ID, 120);
     //   fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
     //   fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
        fleet.setNoFactionInName(true);
        fleet.setFaction(GladiatorSociety_Constants.GSFACTION_ID, true);
        fleet.setName("Gladiator fleet");
      //  fleet.removeAbility(Abilities.INTERDICTION_PULSE);
        fleet.getAI().addAssignment(FleetAssignment.INTERCEPT, Global.getSector().getPlayerFleet(), 1000000f, null);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        fleet.getMemoryWithoutUpdate().set("$dialog", "The gladiator glares at you briefly before shutting down the comm link.");
    //fleet.getInflater().setRemoveAfterInflating(false);
					
        return fleet;
    }

}
