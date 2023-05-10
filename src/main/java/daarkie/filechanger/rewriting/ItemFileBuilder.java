package main.java.daarkie.filechanger.rewriting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.daarkie.filechanger.items.*;
import main.java.daarkie.filechanger.registry.SalvagePart;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemFileBuilder implements FileBuilder {
    private final Map<String, Map<String, List<String>>> materialList;
    private static final List<String> availableMaterials = new ArrayList<>();
    private File iFolder;
    private final Properties salvageProp = new Properties();
    private final Properties sortProp = new Properties();

    public ItemFileBuilder(Map<String, Map<String, List<String>>> materialList) {
        this.materialList = materialList;
        setupProperties();
    }

    // checks for correct files,
    // for whether item can be salvaged
    // for whether item should have other specs
    // adds result together and puts it in the file in json format
    @Override
    public void buildFile(File iFolder) {
        this.iFolder = iFolder;
        if (!iFolder.getName().contains(".json")) {
            return;
        }
        Map<String, SalvagePart> salvagePart = getSalvageInfo();
        Map<String, Map<String, ?>> sorted = sortItem();
        if (sorted == null && salvagePart == null) {
            return;
        }
        Map<String, Map<String, ?>> toWrite = sorted == null ? new HashMap<>() : new HashMap<>(sorted);
        if (salvagePart != null) toWrite.put("salvage", salvagePart);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintWriter writer = new PrintWriter(iFolder)) {
            writer.print(gson.toJson(toWrite));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //  Sorts items into categories by file name and lists their characteristics when applicable
    private Map<String, Map<String, ?>> sortItem() {
        String itemCategory = null;
        for (String category : sortProp.getProperty("category_list").split(",")) {
            String categoryPatterString = sortProp.getProperty(category + "_binding_item").replace(",", "|");
            Pattern categoryPattern = Pattern.compile(categoryPatterString);
            Matcher categoryMatcher = categoryPattern.matcher(iFolder.getName());
            String categoryBlacklist = sortProp.getProperty(category + "_binding_blacklist");
            boolean notOnBlackList = categoryBlacklist.isEmpty();
            if (!notOnBlackList) {
                String categoryBlacklistPatterString = categoryBlacklist.replace(",", "|");
                Pattern categoryBlacklistPattern = Pattern.compile(categoryBlacklistPatterString);
                Matcher categoryBlacklistMatcher = categoryBlacklistPattern.matcher(iFolder.getName());
                notOnBlackList = !categoryBlacklistMatcher.find();
            }
            if (categoryMatcher.find() && notOnBlackList) {
                itemCategory = category;
                break;
            }
        }
        if (itemCategory == null) {
            return null;
        }
        IItem categorizedItem;

        // ...Didn't actually need to categorize...But properties would be pain...
        switch (itemCategory) {
            case "combat":
                categorizedItem = new WeaponItem(iFolder.getName());
                break;
            case "fishing":
                categorizedItem = new FishingItem(iFolder.getName());
                break;
            case "armor":
                categorizedItem = new ArmorItem(iFolder.getName());
                break;
            default:
                categorizedItem = new ToolItem(iFolder.getName(), itemCategory);
                break;
        }
        Map<String, Map<String, ?>> listedItem = categorizedItem.filledListing();
        return listedItem.isEmpty() ? null : listedItem;
    }

    //  Connects to the class properties
    private void setupProperties() {
        try (InputStream salvageInfo = ItemFileBuilder.class.getClassLoader().getResourceAsStream("items/salvage.properties");
             InputStream sortInfo = ItemFileBuilder.class.getClassLoader().getResourceAsStream("items/sort.properties")
        ) {
            salvageProp.load(salvageInfo);
            sortProp.load(sortInfo);
        } catch (IOException ex) {
            System.out.println("Issues while loading salvage.properties and sort.properties");
            ex.printStackTrace();
        }
    }

    //  Lists through the salvage properties to correctly set salvage items
    private Map<String, SalvagePart> getSalvageInfo() {
        if (availableMaterials.isEmpty()) {
            getMaterials();
        }

        //  Check if to-be-salvaged
        String patternString = String.join("|", availableMaterials);
        Pattern mPattern = Pattern.compile(patternString);
        Matcher mMatcher = mPattern.matcher(iFolder.getName());
        String foundPart;
        if (mMatcher.find()) {
            foundPart = mMatcher.group();
        } else {
            return null;
        }
        String keySalvageMaterial = getStringKeyFromSalvage(foundPart, true);
        String keyMaterialBase = getStringKeyFromSalvage(foundPart, false);
        if (keySalvageMaterial == null || keyMaterialBase == null) {
            return null;
        }

        int materialCount = Integer.parseInt(salvageProp.getProperty(keySalvageMaterial + "_salvage_noi"));
        Map<String, SalvagePart> salvagePartMap = new HashMap<>();
        for (int k = 1; k <= materialCount; k++) {
            int sPartItems = Integer.parseInt(salvageProp.getProperty(keySalvageMaterial + "_salvage_items_max_" + k));
            float sPartBaseChance = Float.parseFloat(salvageProp.getProperty(keySalvageMaterial + "_salvage_chance_base_" + k));
            float sPartMaxChance = Float.parseFloat(salvageProp.getProperty(keySalvageMaterial + "_salvage_chance_max_" + k));
            String sLvlChance = salvageProp.getProperty(keySalvageMaterial + "_salvage_chance_lvl_" + k);
            String sBaseRequirement = salvageProp.getProperty(keySalvageMaterial + "_salvage_requirement_" + k);
            String sXPGain = salvageProp.getProperty(keySalvageMaterial + "_salvage_xp_gain_" + k);
            Map<String, ? extends Number> sPartLvlChance = combineParties(sLvlChance, true);
            Map<String, ? extends Number> sPartBaseReq = combineParties(sBaseRequirement, false);
            Map<String, ? extends Number> sPartXPGain = combineParties(sXPGain, false);
            SalvagePart sPart = new SalvagePart(sPartItems, sPartBaseChance, sPartMaxChance, sPartLvlChance, sPartBaseReq, sPartXPGain);
            try {
                String salvageMaterial = salvageProp.getProperty(keySalvageMaterial + "_salvage_item_" + k).contains("+") ?
                        salvageProp.getProperty(keySalvageMaterial + "_salvage_item_" + k).replace("+", ":") :
                        salvageProp.getProperty(keyMaterialBase).replace("+", ":");
                salvagePartMap.put(salvageMaterial, sPart);
            } catch (NullPointerException npl) {
                System.out.println("Issues fetching salvage material for: " + keyMaterialBase);
            }
        }
        return salvagePartMap;
    }

    //  Collects all the available salvage materials
    private void getMaterials() {
        for (Map<String, List<String>> materialItems : materialList.values()) {
            for (List<String> materials : materialItems.values()) {
                availableMaterials.addAll(materials);
            }
        }
    }

    //  Returns either a key to salvage operations
    //  Or key to base materials
    private String getStringKeyFromSalvage(String value, Boolean needsRoot) {
        for (Map.Entry<String, Map<String, List<String>>> entry : materialList.entrySet()) {
            for (Map.Entry<String, List<String>> iteratedList : entry.getValue().entrySet()) {
                if (iteratedList.getValue().contains(value)) {
                    if (needsRoot) {
                        return entry.getKey();
                    }
                    return iteratedList.getKey();
                }
            }
        }
        return null;
    }

    //  Returns salvage map from string either as a float or as an integer
    private Map<String, ? extends Number> combineParties(String sParties, Boolean isFloat) {
        Map<String, Float> sFloatParts = new HashMap<>();
        Map<String, Integer> sIntParts = new HashMap<>();
        for (String sParty : sParties.split(",")) {
            String[] parties = sParty.split("-");
            if (isFloat) {
                sFloatParts.put(parties[0].replace("+", ":"), Float.parseFloat(parties[1]));
            } else {
                sIntParts.put(parties[0].replace("+", ":"), Integer.parseInt(parties[1]));
            }
        }
        if (isFloat) {
            return sFloatParts;
        }
        return sIntParts;
    }
}
