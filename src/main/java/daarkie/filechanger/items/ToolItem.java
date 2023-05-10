package main.java.daarkie.filechanger.items;

import main.java.daarkie.filechanger.rewriting.ItemFileBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToolItem implements IItem {
    private final String itemName;
    private final String itemCategory;
    private final Properties toolProp = new Properties();

    public ToolItem(String itemName, String itemCategory) {
        this.itemName=itemName;
        this.itemCategory=itemCategory;
    }

    @Override
    public Map<String, Map<String, ?>> filledListing() {
        Map<String, Map<String, ?>> finalListing = new HashMap<>();
        Map<String, Map<String, ? extends Number>> requirements;
        Map<String, Map<String, ? extends Number>> bonuses;
        Map<String, Map<String, ? extends Number>> xpValues;
        Map<String, Map<String, ? extends Number>> malus;
        setupProperties();
        requirements = getRequirements();
        bonuses = getBonuses();
        xpValues = getXPValues();
        malus = getNegative();
        if (!requirements.isEmpty()) finalListing.put("requirements", requirements);
        if (!xpValues.isEmpty()) finalListing.put("xp_values", xpValues);
        if (!bonuses.isEmpty()) finalListing.put("bonuses", bonuses);
        if (!malus.isEmpty()) finalListing.putAll(malus);
        return finalListing;
    }

    private void setupProperties() {
        try (InputStream toolInfo = ItemFileBuilder.class.getClassLoader().getResourceAsStream("items/tool.properties")
        ) {
            toolProp.load(toolInfo);
        } catch (IOException ex) {
            System.out.println("Issues while loading category specific properties");
            ex.printStackTrace();
        }
    }

    private Map<String, Map<String, ? extends Number>> getRequirements() {
        Map<String, Map<String, ? extends Number>> finalReq = new HashMap<>();
        Map<String, Integer> useReqMap = new HashMap<>();
        int useReqCount = Integer.parseInt(toolProp.getProperty("to_use_req_noi"));
        for (int i = 1; i <= useReqCount; i++) {
            String sUseReqRegexList = toolProp.getProperty("to_use_req_items" + i).replace(",","|");
            Pattern useReqPattern = Pattern.compile(sUseReqRegexList);
            Matcher useReqMatcher = useReqPattern.matcher(itemName);
            if (useReqMatcher.find()) {
                useReqMap.put(itemCategory, Integer.parseInt(toolProp.getProperty("to_use_req_lvl" + i)));
                break;
            }
        }
        if (!useReqMap.isEmpty()) finalReq.put("TOOL", useReqMap);
        Map<String, Integer> wearReqMap = new HashMap<>();
        int wearReqCount = Integer.parseInt(toolProp.getProperty("to_wear_req_noi"));
        for (int i = 1; i <= wearReqCount; i++) {
            String sUseReqRegexList = toolProp.getProperty("to_wear_req_items" + i).replace(",","|");
            Pattern wearReqPattern = Pattern.compile(sUseReqRegexList);
            Matcher wearReqMatcher = wearReqPattern.matcher(itemName);
            if (wearReqMatcher.find()) {
                wearReqMap.put(itemCategory, Integer.parseInt(toolProp.getProperty("to_wear_req_lvl" + i)));
                break;
            }
        }
        if (!wearReqMap.isEmpty()) finalReq.put("WEAR", wearReqMap);
        Map<String, Integer> weaponReqMap = new HashMap<>();
        int weaponReqCount = Integer.parseInt(toolProp.getProperty("to_fight_req_noi"));
        for (int i = 1; i <= weaponReqCount; i++) {
            String sWeaponReqRegexList = toolProp.getProperty("to_fight_req_items" + i).replace(",","|");
            Pattern useWeaponPattern = Pattern.compile(sWeaponReqRegexList);
            Matcher useWeaponMatcher = useWeaponPattern.matcher(itemName);
            if (useWeaponMatcher.find()) {
                weaponReqMap.put("combat", Integer.parseInt(toolProp.getProperty("to_fight_req_lvl" + i)));
                break;
            }
        }
        if (!weaponReqMap.isEmpty()) finalReq.put("WEAPON", weaponReqMap);
        return finalReq;
    }

    private Map<String, Map<String, ? extends Number>> getBonuses() {
        Map<String, Map<String, ? extends Number>> finalBonus = new HashMap<>();
        Map<String, Float> holdBonusMap = new HashMap<>();
        int holdBonusCount = Integer.parseInt(toolProp.getProperty("when_used_gain_noi"));
        for (int i = 1; i <= holdBonusCount; i++) {
            String sHoldBonusRegexList = toolProp.getProperty("when_used_gain_items" + i).replace(",","|");
            Pattern holdBonusPattern = Pattern.compile(sHoldBonusRegexList);
            Matcher holdBonusMatcher = holdBonusPattern.matcher(itemName);
            if (holdBonusMatcher.find()) {
                holdBonusMap.put(itemCategory, Float.parseFloat(toolProp.getProperty("when_used_gain_bonus" + i)));
                break;
            }
        }
        if (!holdBonusMap.isEmpty()) finalBonus.put("HELD", holdBonusMap);
        return finalBonus;
    }

    private Map<String, Map<String, ? extends Number>> getNegative() {
        Map<String, Map<String, ? extends Number>> finalMalus = new HashMap<>();
        Map<String, Integer> holdMalusMap = new HashMap<>();
        int holdMalusCount = Integer.parseInt(toolProp.getProperty("negative_effect_noi"));
        for (int i = 1; i <= holdMalusCount; i++) {
            String sHoldMalusRegexList = toolProp.getProperty("negative_effect_items" + i).replace(",","|");
            Pattern holdMalusPattern = Pattern.compile(sHoldMalusRegexList);
            Matcher holdMalusMatcher = holdMalusPattern.matcher(itemName);
            if (holdMalusMatcher.find()) {
                String[] holdMalusList = toolProp.getProperty("negative_effect_malus" + i).split(",");
                for (String holdMalus:holdMalusList) {
                    String[] holdMalusItem = holdMalus.split("-");
                    holdMalusMap.put(holdMalusItem[0].replace("+", ":"), Integer.parseInt(holdMalusItem[1]));
                }
                break;
            }
        }
        if (!holdMalusMap.isEmpty()) finalMalus.put("negative_effect", holdMalusMap);
        return finalMalus;
    }

    private Map<String, Map<String, ? extends Number>> getXPValues () {
        Map<String, Map<String, ? extends Number>> finalXP = new HashMap<>();
        Map<String, Integer> craftBonusMap = new HashMap<>();
        int craftBonusCount = Integer.parseInt(toolProp.getProperty("when_crafted_gain_noi"));
        for (int i = 1; i <= craftBonusCount; i++) {
            String sCraftBonusRegexList = toolProp.getProperty("when_crafted_gain_items" + i).replace(",", "|");
            Pattern craftBonusPattern = Pattern.compile(sCraftBonusRegexList);
            Matcher craftBonusMatcher = craftBonusPattern.matcher(itemName);
            if (craftBonusMatcher.find()) {
                String[] craftBonusList = toolProp.getProperty("when_crafted_gain_bonus" + i).split(",");
                for (String craftBonus : craftBonusList) {
                    String[] craftBonusItem = craftBonus.split("-");
                    craftBonusMap.put(craftBonusItem[0], Integer.parseInt(craftBonusItem[1]));
                }
                break;
            }
        }
        if (!craftBonusMap.isEmpty()) finalXP.put("CRAFT", craftBonusMap);
        Map<String, Integer> enchantBonusMap = new HashMap<>();
        int enchantBonusCount = Integer.parseInt(toolProp.getProperty("when_enchanted_gain_noi"));
        for (int i = 1; i <= enchantBonusCount; i++) {
            String sEnchantBonusRegexList = toolProp.getProperty("when_enchanted_gain_items" + i).replace(",", "|");
            Pattern enchantBonusPattern = Pattern.compile(sEnchantBonusRegexList);
            Matcher enchantBonusMatcher = enchantBonusPattern.matcher(itemName);
            if (enchantBonusMatcher.find()) {
                int enchantBonus = Integer.parseInt(toolProp.getProperty("when_enchanted_gain_bonus" + i));
                enchantBonusMap.put("magic", enchantBonus);
                break;
            }
        }
        if (!enchantBonusMap.isEmpty()) finalXP.put("ENCHANT", enchantBonusMap);
        Map<String, Integer> repairBonusMap = new HashMap<>();
        int repairBonusCount = Integer.parseInt(toolProp.getProperty("when_repaired_gain_noi"));
        for (int i = 1; i <= repairBonusCount; i++) {
            String sRepairBonusRegexList = toolProp.getProperty("when_repaired_gain_items" + i).replace(",", "|");
            Pattern repairBonusPattern = Pattern.compile(sRepairBonusRegexList);
            Matcher repairBonusMatcher = repairBonusPattern.matcher(itemName);
            if (repairBonusMatcher.find()) {
                String[] repairBonusList = toolProp.getProperty("when_repaired_gain_bonus" + i).split(",");
                for (String repairBonus : repairBonusList) {
                    String[] repairBonusItem = repairBonus.split("-");
                    repairBonusMap.put(repairBonusItem[0], Integer.parseInt(repairBonusItem[1]));
                }
                break;
            }
        }
        if (!repairBonusMap.isEmpty()) finalXP.put("ANVIL_REPAIR", repairBonusMap);
        return finalXP;
    }
}