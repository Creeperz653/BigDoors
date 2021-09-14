package nl.pim16aap2.bigdoors.spigot;

import dagger.Component;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import nl.pim16aap2.bigdoors.api.IBlockAnalyzer;
import nl.pim16aap2.bigdoors.api.IChunkManager;
import nl.pim16aap2.bigdoors.api.IGlowingBlockSpawner;
import nl.pim16aap2.bigdoors.api.IMessageable;
import nl.pim16aap2.bigdoors.api.IMessagingInterface;
import nl.pim16aap2.bigdoors.api.IPExecutor;
import nl.pim16aap2.bigdoors.api.IPowerBlockRedstoneManager;
import nl.pim16aap2.bigdoors.api.ISoundEngine;
import nl.pim16aap2.bigdoors.api.factories.IBigDoorsEventFactory;
import nl.pim16aap2.bigdoors.api.factories.IFallingBlockFactory;
import nl.pim16aap2.bigdoors.api.factories.IPBlockDataFactory;
import nl.pim16aap2.bigdoors.api.factories.IPLocationFactory;
import nl.pim16aap2.bigdoors.api.factories.IPPlayerFactory;
import nl.pim16aap2.bigdoors.api.factories.IPWorldFactory;
import nl.pim16aap2.bigdoors.api.restartable.RestartableHolderModule;
import nl.pim16aap2.bigdoors.commands.CommandFactory;
import nl.pim16aap2.bigdoors.commands.IPServer;
import nl.pim16aap2.bigdoors.extensions.DoorTypeLoader;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.localization.LocalizationManager;
import nl.pim16aap2.bigdoors.localization.LocalizationModule;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.logging.PLoggerModule;
import nl.pim16aap2.bigdoors.managers.DatabaseManager;
import nl.pim16aap2.bigdoors.managers.DelayedCommandInputManager;
import nl.pim16aap2.bigdoors.managers.DoorRegistry;
import nl.pim16aap2.bigdoors.managers.DoorSpecificationManager;
import nl.pim16aap2.bigdoors.managers.DoorTypeManager;
import nl.pim16aap2.bigdoors.managers.LimitsManager;
import nl.pim16aap2.bigdoors.managers.PowerBlockManager;
import nl.pim16aap2.bigdoors.managers.ToolUserManager;
import nl.pim16aap2.bigdoors.moveblocks.AutoCloseScheduler;
import nl.pim16aap2.bigdoors.moveblocks.DoorActivityManager;
import nl.pim16aap2.bigdoors.spigot.compatiblity.ProtectionCompatManagerModule;
import nl.pim16aap2.bigdoors.spigot.compatiblity.ProtectionCompatManagerSpigot;
import nl.pim16aap2.bigdoors.spigot.config.ConfigLoaderSpigot;
import nl.pim16aap2.bigdoors.spigot.config.ConfigLoaderSpigotModule;
import nl.pim16aap2.bigdoors.spigot.factories.bigdoorseventfactory.BigDoorsEventFactorySpigotModule;
import nl.pim16aap2.bigdoors.spigot.factories.plocationfactory.PLocationFactorySpigotModule;
import nl.pim16aap2.bigdoors.spigot.factories.pplayerfactory.PPlayerFactorySpigotModule;
import nl.pim16aap2.bigdoors.spigot.factories.pworldfactory.PWorldFactorySpigotModule;
import nl.pim16aap2.bigdoors.spigot.implementations.BigDoorsToolUtilSpigot;
import nl.pim16aap2.bigdoors.spigot.implementations.BigDoorsToolUtilSpigotModule;
import nl.pim16aap2.bigdoors.spigot.listeners.LoginResourcePackListener;
import nl.pim16aap2.bigdoors.spigot.listeners.RedstoneListener;
import nl.pim16aap2.bigdoors.spigot.listeners.WorldListener;
import nl.pim16aap2.bigdoors.spigot.managers.HeadManager;
import nl.pim16aap2.bigdoors.spigot.managers.PlatformManagerSpigotModule;
import nl.pim16aap2.bigdoors.spigot.managers.PowerBlockRedstoneManagerSpigotModule;
import nl.pim16aap2.bigdoors.spigot.managers.UpdateManager;
import nl.pim16aap2.bigdoors.spigot.managers.VaultManager;
import nl.pim16aap2.bigdoors.spigot.managers.VaultManagerModule;
import nl.pim16aap2.bigdoors.spigot.util.DebugReporterSpigotModule;
import nl.pim16aap2.bigdoors.spigot.util.api.IPlatformManagerSpigot;
import nl.pim16aap2.bigdoors.spigot.util.api.ISpigotPlatform;
import nl.pim16aap2.bigdoors.spigot.util.implementations.chunkmanager.ChunkManagerSpigotModule;
import nl.pim16aap2.bigdoors.spigot.util.implementations.glowingblocks.GlowingBlockSpawnerModule;
import nl.pim16aap2.bigdoors.spigot.util.implementations.messageable.MessagingInterfaceSpigotModule;
import nl.pim16aap2.bigdoors.spigot.util.implementations.pexecutor.PExecutorModule;
import nl.pim16aap2.bigdoors.spigot.util.implementations.pserver.PServerModule;
import nl.pim16aap2.bigdoors.spigot.util.implementations.soundengine.SoundEngineSpigotModule;
import nl.pim16aap2.bigdoors.storage.sqlite.SQLiteStorageModule;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(modules = {
    BigDoorsSpigotModule.class, RestartableHolderModule.class, PLoggerModule.class,
    ProtectionCompatManagerModule.class,
    ConfigLoaderSpigotModule.class, LocalizationModule.class, PExecutorModule.class, GlowingBlockSpawnerModule.class,
    PServerModule.class, PWorldFactorySpigotModule.class, PLocationFactorySpigotModule.class,
    BigDoorsEventFactorySpigotModule.class, PPlayerFactorySpigotModule.class, ChunkManagerSpigotModule.class,
    MessagingInterfaceSpigotModule.class, SoundEngineSpigotModule.class, PowerBlockRedstoneManagerSpigotModule.class,
    BigDoorsSpigotPlatformModule.class, SQLiteStorageModule.class, DebugReporterSpigotModule.class,
    VaultManagerModule.class, PlatformManagerSpigotModule.class, BigDoorsToolUtilSpigotModule.class
})
interface BigDoorsSpigotComponent
{
    IBigDoorsPlatform getBigDoorsPlatform();

    ISpigotPlatform getSpigotPlatform();

    IPlatformManagerSpigot getPlatformManagerSpigot();

    IPLogger getLogger();

    ProtectionCompatManagerSpigot getProtectionCompatManager();

    ConfigLoaderSpigot getConfig();

    RedstoneListener getRedstoneListener();

    LoginResourcePackListener getLoginResourcePackListener();

    IPExecutor getPExecutor();

    PowerBlockManager getPowerBlockManager();

    WorldListener getWorldListener();

    VaultManager getVaultManager();

    IGlowingBlockSpawner getIGlowingBlockSpawner();

    LimitsManager getLimitsManager();

    HeadManager getHeadManager();

    UpdateManager getUpdateManager();

    IPServer getIPServer();

    IPLocationFactory getIPLocationFactory();

    IPWorldFactory getIPWorldFactory();

    IPPlayerFactory getIPPlayerFactory();

    ISoundEngine getISoundEngine();

    IMessagingInterface getIMessagingInterface();

    IChunkManager getIChunkManager();

    @Named("MessageableServer")
    IMessageable getMessageable();

    IBigDoorsEventFactory getIBigDoorsEventFactory();

    IPowerBlockRedstoneManager getIPowerBlockRedstoneManager();

    BigDoorsToolUtilSpigot getBigDoorsToolUtilSpigot();

    DatabaseManager getDatabaseManager();

    DoorRegistry getDoorRegistry();

    AutoCloseScheduler getAutoCloseScheduler();

    DoorActivityManager getDoorActivityManager();

    DoorSpecificationManager getDoorSpecificationManager();

    DoorTypeManager getDoorTypeManager();

    ToolUserManager getToolUserManager();

    DelayedCommandInputManager getDelayedCommandInputManager();

    ILocalizer getILocalizer();

    LocalizationManager getLocalizationManager();

    IPBlockDataFactory getBlockDataFactory();

    IFallingBlockFactory getFallingBlockFactory();

    IBlockAnalyzer getBlockAnalyzer();

    DoorTypeLoader getDoorTypeLoader();

    CommandFactory getCommandFactory();
}