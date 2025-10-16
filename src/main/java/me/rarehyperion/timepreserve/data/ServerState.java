package me.rarehyperion.timepreserve.data;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the persistent state of the server.
 * This class is responsible for storing and restoring {@code GameRule} values, accross server restarts.
 */
public class ServerState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SAVE_DATA_NAME = "states.dat";

    public boolean paused = false;
    public Map<String, Object> saved = new HashMap<>();

    public static void save(final Plugin plugin, final File folder, final ServerState state) {
        final File saveFile = new File(folder, SAVE_DATA_NAME);

        try(final ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(saveFile)))) {
            stream.writeObject(state);
            stream.flush();
        } catch (final IOException exception) {
            plugin.getLogger().severe("Failed to save state.");
        }
    }

    public static ServerState load(final Plugin plugin, final File folder) {
        final File saveFile = new File(folder, SAVE_DATA_NAME);
        if(!saveFile.exists()) return new ServerState();

        try(final ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(saveFile)))) {
            final Object object = stream.readObject();

            if(object instanceof ServerState state) {
                return state;
            } else {
                plugin.getLogger().severe("Save state is invalid.");
            }
        } catch (final IOException | ClassNotFoundException ignored) {
            plugin.getLogger().warning("Failed to load state.");
        }

        return new ServerState();
    }

}
