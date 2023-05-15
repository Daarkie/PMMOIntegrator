package main.java.daarkie.filechanger.registry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JSONableItem implements Serializable {
    private Map<String, ?> requirements;
    private Map<String, ?> negative_effect;
    private Map<String, ?> xp_values;
    private Map<String, ?> bonuses;
    private Map<String, ?> salvage;
    boolean override = true;

    public JSONableItem(Map<String, ?> requirements, Map<String, ?> negative_effect, Map<String, ?> xp_values, Map<String, ?> bonuses, Map<String, ?> salvage) {
        this.requirements = requirements;
        this.negative_effect = negative_effect;
        this.xp_values = xp_values;
        this.bonuses = bonuses;
        this.salvage = salvage;
    }

    public JSONableItem(Map<String, ?> salvage) {
        this.salvage = salvage;
    }

    public JSONableItem(Map<String, ?> requirements, Map<String, ?> negative_effect, Map<String, ?> xp_values, Map<String, ?> bonuses) {
        this.requirements = requirements;
        this.negative_effect = negative_effect;
        this.xp_values = xp_values;
        this.bonuses = bonuses;
    }
}
