package nl.pim16aap2.bigdoors.compatiblity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

interface ProtectionCompat
{
    public boolean canBreakBlock(Player player, Location loc);

    public boolean canBreakBlocksBetweenLocs(Player player, Location loc1, Location loc2);

    public boolean success();

    public JavaPlugin getPlugin();

    public String getName();
}
