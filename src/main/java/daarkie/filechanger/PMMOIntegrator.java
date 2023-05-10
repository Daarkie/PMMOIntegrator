package main.java.daarkie.filechanger;

import main.java.daarkie.filechanger.rewriting.BlockFileBuilder;
import main.java.daarkie.filechanger.rewriting.FileBuilder;
import main.java.daarkie.filechanger.rewriting.ItemFileBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

public class PMMOIntegrator {

    private static final Properties properties = new Properties();
    private static final Map<String, Map<String, List<String>>> salvageMaterialList = new HashMap<>();

    public static void main(String[] args) throws IOException {
        setupProperties();
        setupSalvageMaterialList();

        File dir = Paths.get(properties.getProperty("mod_data_folder")).toFile();

        try {
            for (File modDir:dir.listFiles()) {
                iterateFiles(modDir.listFiles());
            }
        } catch (NullPointerException npl) {
            System.out.println("Couldn't find correct folder structure at: " + properties.getProperty("mod_data_folder"));
        }

    }

    // Adds salvage and system property files to class properties
    private static void setupProperties() {
        try (InputStream salvageInfo = PMMOIntegrator.class.getClassLoader().getResourceAsStream("items/salvage.properties");
             InputStream systemInfo = PMMOIntegrator.class.getClassLoader().getResourceAsStream("system.properties")) {
            properties.load(salvageInfo);
            properties.load(systemInfo);
        } catch (IOException ex) {
            System.out.println("Issues while loading salvage.properties and sort.properties");
            ex.printStackTrace();
        }
    }

    // Sets all base materials - adds parts of file names that can be matched to the full file names
    // This allows to for example set all STONE tools to the same specific salvage values
    // stone_ => stone_shovel, stone_pickaxe, stone_hoe, stone_axe, stone_sword...
    // There are two ways this works: 1) example above
    //                                2) explicitly naming each component with the same salvage items under the same material
    private static void setupSalvageMaterialList() {
        int materialCount = Integer.parseInt(properties.getProperty("material_bindings_noi"));
        for (int i = 1; i < materialCount; i++) {
            List<String> baseMaterialsTemp = new ArrayList<>();
            String[] bindingConnections = properties.getProperty("material_binding" + i).split("-");
            String referenceMaterial;
            if (bindingConnections[1].isEmpty()) {
                int sMaterialCount = Integer.parseInt(properties.getProperty(bindingConnections[2] + "_noi"));
                for (int j = 1; j <= sMaterialCount; j++) {
                    baseMaterialsTemp.add(properties.getProperty(bindingConnections[2] + j));
                }
                referenceMaterial = bindingConnections[2];
            } else {
                baseMaterialsTemp.add(bindingConnections[1]);
                referenceMaterial = bindingConnections[1];
            }
            Map<String, List<String>> tempMap = new HashMap<>();
            if (salvageMaterialList.containsKey(bindingConnections[0])) {
                tempMap = salvageMaterialList.get(bindingConnections[0]);
                tempMap.put(referenceMaterial, baseMaterialsTemp);
                salvageMaterialList.replace(bindingConnections[0], tempMap);
            } else {
                tempMap.put(referenceMaterial, baseMaterialsTemp);
                salvageMaterialList.put(bindingConnections[0], tempMap);
            }
        }
    }

    // checks the folder name and request the corresponding FileBuilder which then build the file to be rewritten
    static void iterateFiles(File[] source) throws NullPointerException {
        if (!Arrays.stream(source).findFirst().isPresent()) throw new NullPointerException();
        File modFile = Arrays.stream(source).findFirst().get();
        for (File folder : modFile.listFiles()) {
            FileBuilder fileBuilder;
            switch (folder.getName()) {
                case "items":
                    fileBuilder = new ItemFileBuilder(salvageMaterialList);
                    break;
                case "blocks":
                    fileBuilder = new BlockFileBuilder();
                    break;
                default:
                    continue;
            }
            for (File existence:folder.listFiles()) {
                fileBuilder.buildFile(existence);
            }
        }
    }
}