package main.java.daarkie.filechanger.registry;

import java.io.Serializable;
import java.util.Map;

public class JSONableBlock implements Serializable {
    Map<String, ?> vein_data;
    Map<String, ?> xp_values;
    boolean override = true;

    public JSONableBlock(Map<String, ?> vein_data, Map<String, ?> xp_values) {
        this.vein_data = vein_data;
        this.xp_values = xp_values;
    }
}
