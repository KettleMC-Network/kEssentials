package net.kettlemc.kessentials.teleport;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.Configuration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class WarpHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private static final File WARPS_FILE = Configuration.CONFIG_PATH.resolve("data").resolve("warps.json").toFile();

    private final Set<Warp> warps = new HashSet<>();

    private final BukkitRunnable autoSaveTask = new BukkitRunnable() {
        @Override
        public void run() {
            saveWarps();
        }
    };

    public void loadWarps() {
        if (!WARPS_FILE.exists()) {
            this.saveWarps();
        }
        try {
            this.warps.clear();
            Warp[] warps = OBJECT_MAPPER.readValue(WARPS_FILE, Warp[].class);
            this.warps.addAll(Arrays.asList(warps));
        } catch (IOException e) {
            Essentials.instance().getLogger().log(Level.SEVERE, "Couldn't load warps from file!", e);
        }

        this.autoSaveTask.runTaskTimerAsynchronously(Essentials.instance(), Configuration.AUTO_SAVE_INTERVAL_SECONDS.getValue() * 20, Configuration.AUTO_SAVE_INTERVAL_SECONDS.getValue() * 20);
    }

    public void saveWarps() {
        try {
            if (!WARPS_FILE.exists()) {
                WARPS_FILE.getParentFile().mkdirs();
                WARPS_FILE.createNewFile();
            }
            OBJECT_MAPPER.writeValue(WARPS_FILE, this.warps.toArray(new Warp[0]));
        } catch (IOException e) {
            Essentials.instance().getLogger().log(Level.SEVERE, "Couldn't save warps to file!", e);
        }
    }

    public Set<Warp> getWarps() {
        return Collections.unmodifiableSet(this.warps);
    }

    public @Nullable Warp getWarp(String name) {
        return this.warps.stream().filter(warp -> warp.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void addWarp(Warp warp) {
        this.warps.add(warp);
    }

    public void removeWarp(Warp warp) {
        this.warps.remove(warp);
    }

    public void unload() {
        this.saveWarps();
        this.autoSaveTask.cancel();
        warps.clear();
    }

}
