package wagner.stephanie.lizzie;

import org.json.*;

import java.io.*;
import java.util.Iterator;

public class Config {

    public boolean showMoveNumber = false;
    public boolean showWinrate = true;
    public boolean showVariationGraph = true;
    public boolean showRawBoard = false;
    
    // For plug-ins
    public boolean showBranch = true;

    public boolean showBestMoves = true;
    public boolean showNextMoves = true;
    
    public JSONObject config;
    public JSONObject persisted;

    private String persistFilename = "persist.properties";

    private JSONObject loadAndMergeConfig(JSONObject defaultCfg, String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.canRead()) {
            System.err.printf("Creating config file %s\n", fileName);
            try {
                writeConfig(defaultCfg, file);
            } catch (JSONException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        FileInputStream fp = new FileInputStream(file);

        JSONObject mergedcfg = null;
        boolean modified = false;
        try {
            mergedcfg = new JSONObject(new JSONTokener(fp));
            modified = merge_defaults(mergedcfg, defaultCfg);
        } catch (JSONException e) {
            mergedcfg = null;
            e.printStackTrace();
        }

        fp.close();

        if (modified) {
            writeConfig(mergedcfg, file);
        }
        return mergedcfg;


    }

    public Config() throws IOException {
        JSONObject defaultConfig = createDefaultConfig();
        JSONObject persistConfig = createPersistConfig();

        // Main properties
        this.config = loadAndMergeConfig(defaultConfig, "lizzie.properties");
        // Persisted properties
        this.persisted = loadAndMergeConfig(persistConfig, persistFilename);

        JSONObject uiConfig = config.getJSONObject("ui");
        showMoveNumber = uiConfig.getBoolean("show-move-number");
        showBranch = uiConfig.getBoolean("show-leelaz-variation");
        showWinrate = uiConfig.getBoolean("show-winrate");
        showVariationGraph = uiConfig.getBoolean("show-variation-graph");
        showBestMoves = uiConfig.getBoolean("show-best-moves");
        showNextMoves = uiConfig.getBoolean("show-next-moves");
    }

    // Modifies config by adding in values from default_config that are missing.
    // Returns whether it added anything.
    public boolean merge_defaults(JSONObject config, JSONObject defaults_config) {
        boolean modified = false;
        Iterator<String> keys = defaults_config.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object new_val = defaults_config.get(key);
            if (new_val instanceof JSONObject) {
                if (!config.has(key)) {
                    config.put(key, new JSONObject());
                    modified = true;
                }
                Object old_val = config.get(key);
                modified |= merge_defaults((JSONObject) old_val, (JSONObject) new_val);
            } else {
                if (!config.has(key)) {
                    config.put(key, new_val);
                    modified = true;
                }
            }
        }
        return modified;
    }

    public void toggleShowMoveNumber() {
        this.showMoveNumber = !this.showMoveNumber;
    }
    public void toggleShowBranch() {
        this.showBranch = !this.showBranch;
    }
    public void toggleShowWinrate() {
        this.showWinrate = !this.showWinrate;
    }
    public void toggleShowVariationGraph() {
        this.showVariationGraph = !this.showVariationGraph;
    }
    public void toggleShowBestMoves() {
        this.showBestMoves = !this.showBestMoves;
    }
    public void toggleShowNextMoves() {
        this.showNextMoves = !this.showNextMoves;
    }

    private JSONObject createDefaultConfig() {
        JSONObject config = new JSONObject();

        // About engine parameter
        JSONObject leelaz = new JSONObject();
        leelaz.put("weights", "network");
        leelaz.put("threads", 2);
        leelaz.put("gpu", new JSONArray("[]"));
        leelaz.put("max-analyze-time-minutes", 2);
        leelaz.put("max-game-thinking-time-seconds", 2);
        leelaz.put("print-comms", false);

        config.put("leelaz", leelaz);

        // About User Interface display
        JSONObject ui = new JSONObject();

        ui.put("board-color", new JSONArray("[178, 140, 0]"));
        ui.put("theme", "wagner.stephanie.lizzie.theme.DefaultTheme");
        ui.put("shadows-enabled", true);
        ui.put("fancy-stones", true);
        ui.put("fancy-board", true);
        ui.put("shadow-size", 100);
        ui.put("show-move-number", false);
        ui.put("show-leelaz-variation", true);
        ui.put("show-winrate", true);
        ui.put("show-variation-graph", true);
        ui.put("show-best-moves", true);
        ui.put("show-next-moves", true);
        ui.put("win-rate-always-black", false);
        ui.put("confirm-exit", false);
        ui.put("handicap-instead-of-winrate",true);

        config.put("ui", ui);
        return config;
    }

    private JSONObject createPersistConfig() {
        JSONObject config = new JSONObject();

        // About engine parameter
        JSONObject filesys = new JSONObject();
        filesys.put("last-folder", "");

        config.put("filesystem", filesys);

        // About User Interface display
        //JSONObject ui = new JSONObject();

        //ui.put("window-height", 657);
        //ui.put("window-width", 687);

        //config.put("ui", ui);
        return config;
    }

    private void writeConfig(JSONObject config, File file) throws IOException, JSONException {
        file.createNewFile();

        FileOutputStream fp = new FileOutputStream(file);
        OutputStreamWriter writer = new OutputStreamWriter(fp);

        writer.write(config.toString(2));

        writer.close();
        fp.close();
    }

    public void persist() throws IOException {
        writeConfig(this.persisted, new File(persistFilename));
    }

}
