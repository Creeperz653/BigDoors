package nl.pim16aap2.bigdoors.spigot.util.implementations.glowingblocks;

import lombok.Getter;
import lombok.extern.flogger.Flogger;
import nl.pim16aap2.bigdoors.api.IGlowingBlockSpawner;
import nl.pim16aap2.bigdoors.api.IPExecutor;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.api.PColor;
import nl.pim16aap2.bigdoors.api.restartable.IRestartable;
import nl.pim16aap2.bigdoors.api.restartable.Restartable;
import nl.pim16aap2.bigdoors.api.restartable.RestartableHolder;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.spigot.util.SpigotUtil;
import nl.pim16aap2.bigdoors.spigot.util.api.IGlowingBlockFactory;
import nl.pim16aap2.bigdoors.util.IGlowingBlock;
import nl.pim16aap2.bigdoors.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Singleton
@Flogger
public class GlowingBlockSpawner extends Restartable implements IGlowingBlockSpawner
{
    @Getter
    private final Map<PColor, Team> teams = new EnumMap<>(PColor.class);
    private final Set<IRestartable> restartables = new ConcurrentHashMap<IRestartable, Boolean>().keySet();

    private final IGlowingBlockFactory glowingBlockFactory;

    private volatile @Nullable Scoreboard scoreboard;

    /**
     * Keeps track of whether this class (specifically, {@link #scoreboard}) is initialized.
     */
    private volatile boolean isInitialized = false;
    private final IPExecutor executor;
    private final RestartableHolder restartableHolder = new RestartableHolder();

    @Inject
    public GlowingBlockSpawner(RestartableHolder holder, IGlowingBlockFactory glowingBlockFactory, IPExecutor executor)
    {
        super(holder);
        this.glowingBlockFactory = glowingBlockFactory;
        this.executor = executor;
    }

    @Override
    public Optional<IGlowingBlock> spawnGlowingBlock(IPPlayer player, IPWorld world, int time, TimeUnit timeUnit,
                                                     double x, double y, double z, PColor pColor)
    {
        ensureInitialized();
        if (scoreboard == null)
        {
            log.at(Level.WARNING).log("Failed to spawn glowing block: Scoreboard is null!");
            return Optional.empty();
        }

        if (teams.get(pColor) == null)
        {
            // FINER because it will already have been logged on startup.
            log.at(Level.FINER).log("GlowingBlock Color %s was not registered properly!", pColor.name());
            return Optional.empty();
        }

        final long ticks = TimeUnit.MILLISECONDS.convert(time, timeUnit) / 50;
        if (ticks == 0)
        {
            log.at(Level.SEVERE).withCause(new IllegalArgumentException("Invalid duration of " + time + " " +
                                                                            timeUnit.name() + " provided! ")).log();
            return Optional.empty();
        }

        final @Nullable Player spigotPlayer = SpigotAdapter.getBukkitPlayer(player);
        if (spigotPlayer == null)
        {
            log.at(Level.SEVERE).withCause(new NullPointerException())
               .log("Player %s does not appear to be online! They will not receive any GlowingBlock packets!", player);
            return Optional.empty();
        }

        final @Nullable World spigotWorld = SpigotAdapter.getBukkitWorld(world);
        if (spigotWorld == null)
        {
            log.at(Level.SEVERE).withCause(new NullPointerException())
               .log("World %s does not appear to be online! No Glowing Blocks can be spawned here!", world);
            return Optional.empty();
        }

        final Optional<IGlowingBlock> blockOpt =
            glowingBlockFactory.createGlowingBlock(spigotPlayer, spigotWorld, restartableHolder, this, executor);
        blockOpt.ifPresent(block -> block.spawn(pColor, x, y, z, ticks));
        return blockOpt;
    }

    /**
     * Registers a new team with a specific color.
     *
     * @param color
     *     The color to register the team for.
     */
    private void registerTeam(PColor color, Scoreboard scoreboard)
    {
        final ChatColor chatColor = SpigotUtil.toBukkitColor(color);
        final String name = "BigDoors" + color.ordinal();
        // Try to get an existing team, in case something had gone wrong unregistering them last time.
        @Nullable Team team = scoreboard.getTeam(name);
        if (team == null)
            team = scoreboard.registerNewTeam(name);
        team.setColor(chatColor);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        teams.put(color, team);
    }

    /**
     * Initializes all teams.
     */
    private void registerTeams(Scoreboard scoreboard)
    {
        for (final PColor col : PColor.values())
            registerTeam(col, scoreboard);
    }

    private void init()
    {
        final ScoreboardManager scoreboardManager = Util.requireNonNull(Bukkit.getServer().getScoreboardManager(),
                                                                        "scoreboardManager");
        scoreboard = Util.requireNonNull(scoreboardManager.getMainScoreboard(), "scoreboard");
        //noinspection ConstantConditions
        registerTeams(scoreboard);
    }

    /**
     * Ensures this class is initialized.
     * <p>
     * This method is required because this class is initialized lazily, as the required scoreboard isn't available
     * until the first world has been loaded (as per Spigot documentation), while this class may or may not be
     * instantiated before then.
     */
    private void ensureInitialized()
    {
        // Use double-checked locking to avoid synchronization when not needed (99+% of all cases).
        if (!isInitialized)
        {
            synchronized (this)
            {
                if (!isInitialized)
                {
                    init();
                    isInitialized = true;
                }
            }
        }
    }

    @Override
    public void restart()
    {
        teams.forEach((color, team) -> team.unregister());
        teams.clear();
        registerTeams(Util.requireNonNull(scoreboard, "Scoreboard"));
        restartables.forEach(IRestartable::restart);
    }

    @Override
    public void shutdown()
    {
        teams.forEach((color, team) -> team.unregister());
        teams.clear();
        restartables.forEach(IRestartable::shutdown);
    }
}