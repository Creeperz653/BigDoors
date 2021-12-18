package nl.pim16aap2.bigdoors.api.debugging;

import lombok.AllArgsConstructor;
import lombok.extern.flogger.Flogger;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatformProvider;
import nl.pim16aap2.bigdoors.managers.DoorTypeManager;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.util.SafeStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

@Flogger
@AllArgsConstructor
public abstract class DebugReporter
{
    private final List<IDebuggable> debuggables = new ArrayList<>();

    protected final IBigDoorsPlatformProvider platformProvider;
    private final @Nullable DoorTypeManager doorTypeManager;

    public final void registerDebuggable(IDebuggable debuggable)
    {
        debuggables.add(Objects.requireNonNull(debuggable, "Cannot register null debuggable!"));
    }

    /**
     * Gets the data-dump containing useful information for debugging issues.
     */
    public final String getDebugReport()
    {
        final SafeStringBuilder sb = new SafeStringBuilder("BigDoors debug dump:\n");

        System.getProperties()
              .forEach((key, val) -> sb.append(String.format("%-30s", key)).append(": ").append(val).append('\n'));

        sb.append("\n")
          .append("BigDoors version: ")
          .append(() -> platformProvider.getPlatform().map(IBigDoorsPlatform::getVersion).orElse("NULL"))
          .append('\n')
          .append("Registered Platform: ")
          .append(() -> platformProvider.getPlatform().map(platform -> platform.getClass().getName()).orElse("NULL"))
          .append('\n')

          .append("Registered door types: ")
          .append(() -> Util.toString(doorTypeManager == null ? "" : doorTypeManager.getRegisteredDoorTypes()))
          .append('\n')
          .append("Disabled door types: ")
          .append(() -> Util.toString(doorTypeManager == null ? "" : doorTypeManager.getDisabledDoorTypes()))
          .append('\n')

          .append(this::getAdditionalDebugReport0)
          .append('\n');

        for (final IDebuggable debuggable : debuggables)
            appendDebuggable(sb, debuggable);

        return sb.toString();
    }

    private static void appendDebuggable(SafeStringBuilder sb, IDebuggable debuggable)
    {
        final String debuggableName = debuggable.getClass().getName();
        @Nullable String msg;

        try
        {
            msg = debuggable.getDebugInformation();
            if (msg == null || msg.isBlank())
                msg = "Nothing to log!";
        }
        catch (Exception e)
        {
            log.at(Level.SEVERE).withCause(e).log("Failed to get debug information for class: %s", debuggableName);
            msg = "ERROR";
        }

        sb.append(debuggableName).append(":\n");
        msg.lines().forEach(line -> sb.append("  ").append(line).append('\n'));
    }

    private String getAdditionalDebugReport0()
    {
        try
        {
            return getAdditionalDebugReport();
        }
        catch (Exception e)
        {
            log.at(Level.SEVERE).withCause(e).log("Failed to get additional debug data!");
            return "ERROR";
        }
    }

    /**
     * Gets the additional debug report generated by the subclass.
     *
     * @return The formatted additional debug report. This is appended to the general debug report.
     */
    protected abstract String getAdditionalDebugReport();

    @Override
    public final String toString()
    {
        return getDebugReport();
    }
}