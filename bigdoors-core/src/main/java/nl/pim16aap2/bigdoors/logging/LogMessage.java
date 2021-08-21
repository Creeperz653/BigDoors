package nl.pim16aap2.bigdoors.logging;

import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Represents base class of the logMessage types.
 *
 * @author Pim
 */
public abstract class LogMessage
{
    protected final String message;

    protected final Level logLevel;

    /**
     * The id of the thread from which this message was created.
     */
    private final long threadID;

    protected LogMessage(String message, Level logLevel)
    {
        this.message = message;
        this.logLevel = logLevel;
        threadID = Thread.currentThread().getId();
    }

    @Override
    public String toString()
    {
        return getFormattedLevel() + getFormattedThreadID() + message;
    }

    /**
     * Formats a message. If it's null or empty, it'll return an empty String.
     *
     * @param str
     *     The message to format.
     * @return The formatted message.
     */
    protected static String checkMessage(@Nullable String str)
    {
        if ("\n".equals(str))
            return "";
        return str + "\n";
    }

    private String getFormattedThreadID()
    {
        return String.format("Thread [%d] ", threadID);
    }

    private String getFormattedLevel()
    {
        return String.format("(%s) ", logLevel);
    }

    /**
     * Represents a logMessage that logs a {@link Throwable}.
     *
     * @author Pim
     */
    public static class LogMessageThrowable extends LogMessage
    {
        LogMessageThrowable(Throwable throwable, String message, Level logLevel)
        {
            super(checkMessage(message) + checkMessage(throwableStackTraceToString(throwable)), logLevel);
        }

        /**
         * Converts the stack trace of a {@link Throwable} to a string.
         *
         * @param throwable
         *     The {@link Throwable} whose stack trace to retrieve as String.
         * @return A string of the stack trace.
         */
        private static String throwableStackTraceToString(Throwable throwable)
        {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }

    /**
     * Represents a {@link LogMessage} that logs a stack trace.
     *
     * @author Pim
     */
    public static class LogMessageStackTrace extends LogMessage
    {
        /**
         * Logs a new stack trace.
         *
         * @param stackTrace
         *     The stack trace to log.
         * @param message
         *     The message to log as the header.
         * @param logLevel
         *     The level at which to log the resulting message.
         * @param skip
         *     The number of elements in the stack trace to skip.
         */
        LogMessageStackTrace(StackTraceElement[] stackTrace, String message, Level logLevel, int skip)
        {
            super(checkMessage(message) + checkMessage(stackTraceToString(stackTrace, skip)), logLevel);
        }

        private static String stackTraceToString(StackTraceElement[] stackTrace, int skip)
        {
            final StringBuilder sb = new StringBuilder();
            for (int idx = skip; idx < stackTrace.length; ++idx)
                sb.append("\tat ").append(stackTrace[idx]).append("\n");
            return sb.toString();
        }
    }

    /**
     * Represents a logMessage that logs a string.
     *
     * @author Pim
     */
    public static class LogMessageString extends LogMessage
    {
        LogMessageString(String message, Level logLevel)
        {
            super(checkMessage(message), logLevel);
        }
    }

    public static class LogMessageStringSupplier extends LogMessage
    {
        LogMessageStringSupplier(String message, Supplier<String> stringSupplier, Level logLevel)
        {
            super(checkMessage(message) + checkMessage(stringSupplier.get()), logLevel);
        }
    }
}
