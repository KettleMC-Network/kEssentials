package net.kettlemc.kessentials.teleport;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.kettlemc.kessentials.Essentials;
import net.kettlemc.kessentials.config.Configuration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public class HomeHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    private static final Path HOMES_DIR = Configuration.CONFIG_PATH.resolve("data").resolve("homes");

    private final HashMap<UUID, ArrayList<Warp>> homes = new HashMap<>();

    private final BukkitRunnable autoSaveTask = new BukkitRunnable() {
        @Override
        public void run() {
            saveHomes(false);
        }
    };

    public void init() {
        this.homes.clear();

        if (!HOMES_DIR.toFile().exists()) {
            HOMES_DIR.toFile().mkdirs();
        }

        this.autoSaveTask.runTaskTimerAsynchronously(Essentials.instance(), Configuration.AUTO_SAVE_INTERVAL_SECONDS.getValue() * 20, Configuration.AUTO_SAVE_INTERVAL_SECONDS.getValue() * 20);
    }

    public void loadHomes(UUID uuid) {
        if (!HOMES_DIR.resolve(uuid + ".json").toFile().exists()) return;
        try {
            Warp[] homes = OBJECT_MAPPER.readValue(HOMES_DIR.resolve(uuid + ".json").toFile(), Warp[].class);
            this.homes.put(uuid, new ArrayList<>());
            this.homes.get(uuid).addAll(Arrays.asList(homes));
        } catch (IOException e) {
            Essentials.instance().getLogger().log(Level.SEVERE, "Couldn't load homes for uuid '" + uuid + "'.", e);
        }
    }

    private void saveHomes(boolean unload) {

        if (!HOMES_DIR.toFile().exists()) {
            HOMES_DIR.toFile().mkdirs();
        }
        this.homes.keySet().forEach(uuid -> saveHomes(uuid, unload));
    }

    public void saveHomes(UUID owner, boolean unload) {
        if (!this.homes.containsKey(owner)) return;
        File file = HOMES_DIR.resolve(owner + ".json").toFile();
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            OBJECT_MAPPER.writeValue(file, homes.get(owner).toArray(new Warp[0]));
            if (unload)
                this.homes.remove(owner);
        } catch (IOException e) {
            Essentials.instance().getLogger().log(Level.SEVERE, "Couldn't save homes for uuid '" + owner + "'.", e);
        }
    }

    public List<Warp> getHomes(UUID uuid) {
        return this.homes.getOrDefault(uuid, new ArrayList<>());
    }

    public void addHome(Warp home) {
        if (!this.homes.containsKey(home.owner()))
            this.homes.put(home.owner(), new ArrayList<>());

        this.homes.get(home.owner()).add(home);

    }

    public void removeHome(Warp home) {
        if (!this.homes.containsKey(home.owner()))
            return;
        this.homes.get(home.owner()).remove(home);
        if (this.homes.get(home.owner()).isEmpty())
            this.homes.remove(home.owner());
    }

    public void unload() {
        this.saveHomes(true);
        this.autoSaveTask.cancel();
        homes.clear();
    }

    public Warp getHome(UUID owner, String name) {
        return getHomes(owner).stream().filter(home -> home.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
