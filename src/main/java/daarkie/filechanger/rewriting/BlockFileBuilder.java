package main.java.daarkie.filechanger.rewriting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.daarkie.filechanger.registry.JSONableBlock;
import main.java.daarkie.filechanger.registry.JSONableItem;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockFileBuilder implements FileBuilder {
    private File iFolder;
    private final Properties blockProp = new Properties();

    @Override
    public void buildFile(File iFolder) {
        this.iFolder = iFolder;
        if (!iFolder.getName().contains(".json")){
            return;
        }
        setupBlockProperties();
        Map<String, Map<String, ?>> sorted = listBlock();
        if (sorted == null) {
            return;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (PrintWriter writer = new PrintWriter(iFolder)) {
            writer.print(gson.toJson(new JSONableBlock(sorted.get("vein_data"), sorted.get("xp_values"))));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupBlockProperties() {
        try (InputStream blockInfo = ItemFileBuilder.class.getClassLoader().getResourceAsStream("block.properties")
        ) {
            blockProp.load(blockInfo);
        } catch (IOException ex) {
            System.out.println("Issues while loading category specific properties");
            ex.printStackTrace();
        }
    }

    private Map<String, Map<String, ?>> listBlock() {
        Map<String, Map<String, ?>> blockList = new HashMap<>();
        int non = Integer.parseInt(blockProp.getProperty("breakable_blocks_noi"));
        for (int i = 1; i <= non; i++) {
            String blockString = blockProp.getProperty("breakable_blocks_items" + i).replace(",","|");
            Pattern blockPattern = Pattern.compile(blockString);
            Matcher blockMatcher = blockPattern.matcher(iFolder.getName());
            String blockBlacklist = blockProp.getProperty("breakable_blocks_blacklist" + i);
            boolean notOnBlackList = blockBlacklist != null;
            if (notOnBlackList) {
                String blockBlacklistPatterString = blockBlacklist.replace(",","|");
                Pattern blockBlacklistPattern = Pattern.compile(blockBlacklistPatterString);
                Matcher blockBlacklistMatcher = blockBlacklistPattern.matcher(iFolder.getName());
                notOnBlackList = !blockBlacklistMatcher.find();
            }
            if (blockMatcher.find() && notOnBlackList){
                Map<String, Integer> veinData = new HashMap<>();
                veinData.put("consumeAmount", Integer.parseInt(blockProp.getProperty("breakable_blocks_vein" + i)));
                blockList.put("vein_data", veinData);
                Map<String, Integer> xpSubData = new HashMap<>();
                Map<String, Map<String, ?>> xpData = new HashMap<>();
                for (String subData:blockProp.getProperty("breakable_blocks_xp" + i).split(",")) {
                    String[] subDataList = subData.split("-");
                    xpSubData.put(subDataList[0], Integer.parseInt(subDataList[1]));
                }
                xpData.put("BLOCK_BREAK", xpSubData);
                blockList.put("xp_values", xpData);
                return blockList;
            }
        }
        return null;
   }
}
