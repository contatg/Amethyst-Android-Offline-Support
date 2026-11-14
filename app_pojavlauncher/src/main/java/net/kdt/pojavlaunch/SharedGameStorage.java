package net.kdt.pojavlaunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import androidx.preference.PreferenceManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kdt.pojavlaunch.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class SharedGameStorage {
    private static final String PREPARED = "sharedGameStoragePrepared";

    private SharedGameStorage() {}

    public static boolean isPrepared(Context context) {
        return preferences(context).getBoolean(PREPARED, false);
    }

    static synchronized void prepare(Context context) throws IOException {
        File sharedRoot = new File(Tools.DIR_GAME_HOME);
        FileUtils.ensureDirectory(sharedRoot);
        FileUtils.ensureDirectory(new File(sharedRoot, "mods"));
        if (isPrepared(context)) return;
        migrate(context.getExternalFilesDir(null), sharedRoot);
        migrate(oldSharedRoot(), sharedRoot);
        if (!preferences(context).edit().putBoolean(PREPARED, true).commit()) {
            throw new IOException("Could not save storage migration state");
        }
    }

    private static File oldSharedRoot() {
        return new File(Environment.getExternalStorageDirectory(), "games/Amethyst");
    }

    private static SharedPreferences preferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String remapLegacyControlPath(Context context, String path) {
        for (File oldRoot : new File[]{context.getExternalFilesDir(null), oldSharedRoot()}) {
            if (oldRoot == null) continue;
            String prefix = oldRoot.getAbsolutePath() + "/controlmap/";
            if (path.startsWith(prefix)) {
                return new File(Tools.CTRLMAP_PATH, path.substring(prefix.length())).getPath();
            }
        }
        return path;
    }

    // Copy files into place individually so an interrupted migration can resume.
    // Leave the source intact and preserve existing files in the destination.
    private static void migrate(File oldRoot, File sharedRoot) throws IOException {
        if (oldRoot == null || !oldRoot.exists()) return;
        File oldMinecraft = new File(oldRoot, ".minecraft");
        File installRoot = new File(sharedRoot, ".minecraft");
        for (File child : children(oldMinecraft)) {
            String name = child.getName();
            switch (name) {
                case "launcher_profiles.json":
                    copy(child, new File(installRoot, name), true);
                    break;
                case "assets":
                case "versions":
                case "libraries":
                    copy(child, new File(installRoot, name), false);
                    break;
                default:
                    copy(child, new File(sharedRoot, name), false);
            }
        }
        for (String folder : new String[]{"controlmap", "custom_instances"}) {
            File source = new File(oldRoot, folder);
            if (source.exists()) copy(source, new File(sharedRoot, folder), false);
        }
    }

    private static File[] children(File directory) throws IOException {
        if (!directory.exists()) return new File[0];
        File[] listing = directory.listFiles();
        if (listing == null) throw new IOException("Could not read " + directory);
        return listing;
    }

    private static void copy(File source, File target, boolean stripProfileCredentials) throws IOException {
        if (org.apache.commons.io.FileUtils.isSymlink(source)) return;
        if (source.isDirectory()) {
            FileUtils.ensureDirectory(target);
            for (File child : children(source)) copy(child, new File(target, child.getName()), false);
            return;
        }
        if (!source.isFile() || target.exists()) return;

        FileUtils.ensureParentDirectory(target);
        File temporary = File.createTempFile(".amethyst-copy-", ".tmp", target.getParentFile());
        try {
            if (stripProfileCredentials) {
                copyProfiles(source, temporary);
            } else {
                org.apache.commons.io.FileUtils.copyFile(source, temporary);
            }
            if (!target.exists() && !temporary.renameTo(target)) {
                throw new IOException("Could not write " + target);
            }
        } finally {
            temporary.delete();
        }
    }

    private static void copyProfiles(File source, File target) throws IOException {
        try (InputStreamReader input = new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8);
             OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            JsonObject profiles = new JsonParser().parse(input).getAsJsonObject();
            profiles.remove("authenticationDatabase");
            profiles.remove("clientToken");
            profiles.remove("selectedUser");
            Tools.GLOBAL_GSON.toJson(profiles, output);
        } catch (RuntimeException e) {
            throw new IOException("Could not read " + source, e);
        }
    }
}
