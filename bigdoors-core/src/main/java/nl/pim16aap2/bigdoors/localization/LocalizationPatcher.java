package nl.pim16aap2.bigdoors.localization;

import nl.pim16aap2.bigdoors.BigDoors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.pim16aap2.bigdoors.localization.LocalizationUtil.*;

/**
 * Represents a class that can be used to apply user-defined localization patches.
 *
 * @author Pim
 */
final class LocalizationPatcher
{
    private final String baseName;
    private final Path directory;

    LocalizationPatcher(@NotNull Path directory, @NotNull String baseName)
        throws IOException
    {
        this.directory = directory;
        this.baseName = baseName;
        ensureFileExists(directory.resolve(this.baseName + ".properties"));
    }

    /**
     * Updates the localization keys in the patch file(s).
     * <p>
     * Any root keys not already present in the patch file(s) will be appended to the file without a value.
     *
     * @param rootKeys The localization keys in the root locale file.
     * @return The list of patch files that were found.
     */
    @NotNull List<LocaleFile> updatePatchKeys(@NotNull Collection<String> rootKeys)
    {
        final List<LocaleFile> patchFiles = getLocaleFilesInDirectory(directory, baseName);
        patchFiles.forEach(patchFile -> updatePatchKeys(rootKeys, patchFile));
        return patchFiles;
    }

    /**
     * Updates the localization keys in the patch file.
     * <p>
     * Any root keys not already present in the patch file will be appended to the file without a value.
     *
     * @param rootKeys   The localization keys in the root locale file.
     * @param localeFile The locale file to append any missing localization keys to.
     */
    void updatePatchKeys(@NotNull Collection<String> rootKeys, @NotNull LocaleFile localeFile)
    {
        final Set<String> patchKeys = getKeySet(localeFile.path());
        final Set<String> appendableKeys = new LinkedHashSet<>(rootKeys);
        appendableKeys.removeAll(patchKeys);
        appendKeys(localeFile, appendableKeys);
    }

    /**
     * Appends localization keys to a locale file.
     *
     * @param localeFile     The locale file to append the keys to.
     * @param appendableKeys The localization keys to append to the locale file.
     */
    void appendKeys(@NotNull LocaleFile localeFile, @NotNull Set<String> appendableKeys)
    {
        if (appendableKeys.isEmpty())
            return;

        final StringBuilder sb = new StringBuilder();
        appendableKeys.forEach(key -> sb.append(key).append("=\n"));
        try
        {
            Files.write(localeFile.path(), sb.toString().getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e)
        {
            BigDoors.get().getPLogger().logThrowable(e, "Failed to append new keys to file: " + localeFile.path());
        }
    }

    /**
     * Gets the patches for a given locale that are specified in the patch file.
     * <p>
     * A patch is defined in this context as a key/value pair with a non-empty value.
     *
     * @param localeFile The locale file to read the patches from.
     * @return A map of the patches. The keys are the localization keys and the values the full line (i.e. "key=value").
     */
    @NotNull Map<String, String> getPatches(@NotNull LocaleFile localeFile)
    {
        final Map<String, String> ret = new LinkedHashMap<>();
        readFile(localeFile.path(), line ->
        {
            final @Nullable String key = getKeyFromLine(line);
            if (isValidPatch(key, line))
                ret.put(key, line);
        });
        return ret;
    }

    /**
     * Tests if a line is a valid patch.
     *
     * @param key  The key of the line.
     * @param line The line itself.
     * @return True if the patch is valid (i.e. not empty).
     */
    static boolean isValidPatch(@Nullable String key, @NotNull String line)
    {
        if (key == null)
            return false;
        return !(key + "=").equals(line);
    }
}
