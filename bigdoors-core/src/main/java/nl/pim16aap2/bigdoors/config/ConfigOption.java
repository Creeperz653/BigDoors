package nl.pim16aap2.bigdoors.config;

import nl.pim16aap2.bigdoors.BigDoors;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Represents an option in a config file.
 *
 * @author Pim
 */
public final class ConfigOption<V>
{
    private final BigDoors plugin;
    private final FileConfiguration config;
    private final String optionName;
    private V value;
    private final V defaultValue;
    private final String[] comment;

    public ConfigOption(BigDoors plugin, FileConfiguration config, String optionName, V defaultValue, String[] comment)
    {
        this.plugin = plugin;
        this.config = config;
        this.optionName = optionName;
        this.defaultValue = defaultValue;
        this.comment = comment;
        setValue();
    }

    @SuppressWarnings("unchecked")
    /**
     * Set the value of this config option.
     */
    private void setValue()
    {
        try
        {
            value = (V) config.get(optionName, defaultValue);
        }
        catch (Exception e)
        {
            plugin.getMyLogger()
                .logException(e,
                              "Failed to read config value of: \"" + optionName + "\"! Using default value instead!");
            value = defaultValue;
        }
    }

    /**
     * Get the value of this config option.
     *
     * @return The value of the config option.
     */
    public V getValue()
    {
        return value;
    }

    /**
     * Get the comment of this config option.
     *
     * @return The comment of the config option.
     */
    public String[] getComment()
    {
        return comment;
    }

    /**
     * Convert the comment, name and value(s) of this config option into a string
     * that can be used for writing the config.
     *
     * @return The config option formatted for printing in the config file
     */
    @Override
    public String toString()
    {
        String string = "";

        // Print the comments, if there are any.
        if (comment != null)
            for (String comLine : comment)
                // Prefix every line by a comment-sign (#).
                string += "# " + comLine + "\n";

        string += optionName + ": ";
        if (value.getClass().isAssignableFrom(String.class))
            string += "\'" + value.toString() + "\'";
        else if (value instanceof List<?>)
        {
            StringBuilder builder = new StringBuilder();
            builder.append("\n");
            int listSize = ((List<?>) value).size();
            for (int index = 0; index < listSize; ++index)
                // Don't print newline at the end
                builder.append("  - " + ((List<?>) value).get(index) + (index == listSize - 1 ? "" : "\n"));
            string += builder.toString();
        }
        else
            string += value.toString();
        return string;
    }
}