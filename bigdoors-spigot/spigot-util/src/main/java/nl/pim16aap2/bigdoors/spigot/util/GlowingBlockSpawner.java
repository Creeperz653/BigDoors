package nl.pim16aap2.bigdoors.spigot.util;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import nl.pim16aap2.bigdoors.api.IGlowingBlock;
import nl.pim16aap2.bigdoors.api.IGlowingBlockSpawner;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.api.PColor;
import nl.pim16aap2.bigdoors.api.restartable.IRestartable;
import nl.pim16aap2.bigdoors.api.restartable.IRestartableHolder;
import nl.pim16aap2.bigdoors.api.restartable.Restartable;
import nl.pim16aap2.bigdoors.spigot.util.api.BigDoorsSpigotAbstract;
import nl.pim16aap2.bigdoors.spigot.util.api.IGlowingBlockFactory;
import nl.pim16aap2.bigdoors.spigot.util.api.ISpigotPlatform;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class GlowingBlockSpawner extends Restartable implements IGlowingBlockSpawner, IRestartableHolder
{
    private final @NonNull Map<IRestartable, Boolean> restartables = new ConcurrentHashMap<>();
    @Getter
    private final @NonNull Map<PColor, Team> teams = new EnumMap<>(PColor.class);
    private final @NonNull Scoreboard scoreboard;
    private final @NonNull IGlowingBlockFactory glowingBlockFactory;

    public GlowingBlockSpawner(final @NonNull IRestartableHolder holder)
        throws Exception
    {
        super(holder);
        final @Nullable ScoreboardManager scoreBoardManager = Bukkit.getServer().getScoreboardManager();
        if (scoreBoardManager == null)
            throw new Exception("Could not find a ScoreBoardManager! No glowing blocks can be spawned!");

        scoreboard = scoreBoardManager.getMainScoreboard();

        final @NonNull IBigDoorsPlatform platform = BigDoors.get().getPlatform();
        if (!(platform instanceof BigDoorsSpigotAbstract))
            throw new Exception("Spigot's GlowingBlockSpawner can only be used with the Spigot Platform!");

        final @Nullable ISpigotPlatform spigotPlatform = ((BigDoorsSpigotAbstract) platform).getPlatformManagerSpigot()
                                                                                            .getSpigotPlatform();
        if (spigotPlatform == null)
            throw new Exception("No valid Spigot platform was found!");

        glowingBlockFactory = spigotPlatform.getGlowingBlockFactory();

        registerTeams();
    }

    /**
     * Initializes all teams.
     */
    private void registerTeams()
    {
        for (final @NonNull PColor col : PColor.values())
            registerTeam(col, scoreboard);
    }

    /**
     * Registers a new team with a specific color.
     *
     * @param color The color to register the team for.
     */
    private void registerTeam(final @NonNull PColor color, final @NonNull Scoreboard scoreboard)
    {
        final @NonNull ChatColor chatColor = SpigotUtil.toBukkitColor(color);
        final String name = "BigDoors" + color.ordinal();
        // Try to get an existing team, in case something had gone wrong unregistering them last time.
        Team team = scoreboard.getTeam(name);
        if (team == null)
            team = scoreboard.registerNewTeam(name);
        team.setColor(chatColor);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        teams.put(color, team);
    }

    @Override
    public void restart()
    {
        teams.forEach((K, V) -> V.unregister());
        teams.clear();
        registerTeams();
        restartables.forEach((K, V) -> K.restart());
    }

    @Override
    public void shutdown()
    {
        teams.forEach((K, V) -> V.unregister());
        teams.clear();
        restartables.forEach((K, V) -> K.shutdown());
    }

    @Override
    public @NonNull Optional<IGlowingBlock> spawnGlowingBlock(@NonNull IPPlayer player, @NonNull IPWorld world,
                                                              final int time, final @NonNull TimeUnit timeUnit,
                                                              final double x, final double y, final double z,
                                                              final @NonNull PColor pColor)
    {
        if (teams.get(pColor) == null)
        {
            // FINER because it will already have been logged on startup.
            BigDoors.get().getPLogger()
                    .logMessage(Level.FINER, "GlowingBlock Color " + pColor.name() + " was not registered properly!");
            return Optional.empty();
        }

        final long ticks = TimeUnit.MILLISECONDS.convert(time, timeUnit) / 50;
        if (ticks == 0)
        {
            BigDoors.get().getPLogger().logThrowable(
                new IllegalArgumentException("Invalid duration of " + time + " " + timeUnit.name() + " provided! "));
            return Optional.empty();
        }

        final @Nullable Player spigotPlayer = SpigotAdapter.getBukkitPlayer(player);
        if (spigotPlayer == null)
        {
            BigDoors.get().getPLogger().logThrowable(new NullPointerException(), "Player " + player.toString() +
                " does not appear to be online! They will not receive any GlowingBlock packets!");
            return Optional.empty();
        }

        final @Nullable World spigotWorld = SpigotAdapter.getBukkitWorld(world);
        if (spigotWorld == null)
        {
            BigDoors.get().getPLogger().logThrowable(new NullPointerException(), "World " + world.toString() +
                " does not appear to be online! No Glowing Blocks can be spawned here!");
            return Optional.empty();
        }

        @NonNull Optional<IGlowingBlock> blockOpt =
            glowingBlockFactory.createGlowingBlock(spigotPlayer, spigotWorld, this);
        blockOpt.ifPresent(block -> block.spawn(pColor, x, y, z, ticks));
        return blockOpt;
    }

    @Override
    public void registerRestartable(final @NonNull IRestartable restartable)
    {
        restartables.put(restartable, true);
    }

    @Override
    public boolean isRestartableRegistered(final @NonNull IRestartable restartable)
    {
        return restartables.containsKey(restartable);
    }

    @Override
    public void deregisterRestartable(@NonNull IRestartable restartable)
    {
        restartables.remove(restartable);
    }
}
