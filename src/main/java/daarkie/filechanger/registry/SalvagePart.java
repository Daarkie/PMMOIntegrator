package main.java.daarkie.filechanger.registry;

import java.io.Serializable;
import java.util.Map;

public class SalvagePart implements Serializable {
    int salvageMax;
    float baseChance;
    float maxChance;
    Map<String, ? extends Number> chancePerLevel;
    Map<String, ? extends Number> levelReq;
    Map<String, ? extends Number> xpPerItem;

    public SalvagePart(int salvageMax, float baseChance, float maxChance, Map<String, ? extends Number> chancePerLevel, Map<String, ? extends Number> levelReq, Map<String, ? extends Number> xpPerItem) {
        this.salvageMax = salvageMax;
        this.baseChance = baseChance;
        this.maxChance = maxChance;
        this.chancePerLevel = chancePerLevel;
        this.levelReq = levelReq;
        this.xpPerItem = xpPerItem;
    }

}
