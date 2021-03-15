package nl.pim16aap2.bigdoors.storage;

import nl.pim16aap2.bigdoors.util.Functional.CheckedTriConsumer;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.Util;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.regex.Pattern;

/**
 * Represents a wrapper around {@link PreparedStatement}. It can be used to set variables before obtaining a
 * connection.
 *
 * @author Pim
 */
public class PPreparedStatement
{
    private final @NotNull Action[] actions;
    private @NotNull String statement;
    public static final @NotNull Pattern QUESTION_MARK = Pattern.compile("\\?");
    private int currentIDX = 1;

    /**
     * Keeps track of the number of variables that should be skipped.
     * <p>
     * This number is almost always going to 0, but in case {@link #setRawString(int, String)} has been invoked, it will
     * be higher.
     */
    private int skipCount = 0;

    /**
     * Constructs a new {@link PPreparedStatement}.
     * <p>
     * It counts the number of variables ('?' characters) in the statement.
     *
     * @param statement The SQL statement.
     */
    public PPreparedStatement(final @NotNull String statement)
    {
        actions = new Action[Util.countPatternOccurrences(PPreparedStatement.QUESTION_MARK, statement)];
        this.statement = statement;
    }

    /**
     * Constructs a new {@link PPreparedStatement}.
     *
     * @param variableCount The number of variables ('?' characters) in the statement.
     * @param statement     The SQL statement.
     */
    public PPreparedStatement(final int variableCount, final @NotNull String statement)
    {
        actions = new Action[variableCount];
        this.statement = statement;
    }

    @Override
    public @NotNull String toString()
    {
        String result = statement;
        for (int idx = 0; idx < (actions.length - skipCount); ++idx)
        {
            final Action action = actions[idx];
            if (action == null || action.obj == null)
                result = result.replaceFirst("[?]", "NULL");
            else if (action.obj instanceof Number)
                result = result.replaceFirst("[?]", action.obj.toString());
            else
                result = result.replaceFirst("[?]", "\"" + action.obj.toString() + "\"");
        }
        return result;
    }

    /**
     * Constructs a {@link PreparedStatement} from this {@link PPreparedStatement}, applying all variables as defined up
     * to this point.
     *
     * @param conn A connection to a database.
     * @return The constructed {@link PreparedStatement}.
     *
     * @throws SQLException
     */
    public @NotNull PreparedStatement construct(final @NotNull Connection conn)
        throws SQLException
    {
        final PreparedStatement ps = conn.prepareStatement(statement);
        for (int idx = 0; idx < (actions.length - skipCount); ++idx)
        {
            final Action action = actions[idx];
            action.getFun().accept(ps, action.getIdx(), action.getObj());
        }
        return ps;
    }

    /**
     * Constructs a {@link PreparedStatement} from this {@link PPreparedStatement}, applying all variables as defined up
     * to this point.
     *
     * @param conn              A connection to a database.
     * @param autoGeneratedKeys A flag indicating whether auto-generated keys should be returned; one of
     *                          Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
     * @return The constructed {@link PreparedStatement}.
     *
     * @throws SQLException
     */
    public @NotNull PreparedStatement construct(final @NotNull Connection conn, final int autoGeneratedKeys)
        throws SQLException
    {
        final PreparedStatement ps = conn.prepareStatement(statement, autoGeneratedKeys);
        for (int idx = 0; idx < actions.length - skipCount; ++idx)
        {
            final Action action = actions[idx];
            action.getFun().accept(ps, action.getIdx(), action.getObj());
        }
        return ps;
    }

    /**
     * This method does not do anything. Setting table named should be done via {@link
     * PPreparedStatement#setRawString(int, String)} instead. This method only exists to avoid null-values in a simple
     * manner.
     *
     * @param preparedStatement Ignored.
     * @param idx               Ignored.
     * @param obj               Ignored.
     */
    private static void setRawString(final @NotNull PreparedStatement preparedStatement, final int idx,
                                     final @NotNull Object obj)
    {
    }

    /**
     * See {@link PreparedStatement#setBoolean(int, boolean)}.
     */
    private static void setBoolean(final @NotNull PreparedStatement preparedStatement, final int idx,
                                   final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setBoolean(idx, (Boolean) obj);
    }

    /**
     * See {@link PreparedStatement#setByte(int, byte)}.
     */
    private static void setByte(final @NotNull PreparedStatement preparedStatement, final int idx,
                                final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setByte(idx, (Byte) obj);
    }

    /**
     * See {@link PreparedStatement#setShort(int, short)}.
     */
    private static void setShort(final @NotNull PreparedStatement preparedStatement, final int idx,
                                 final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setShort(idx, (Short) obj);
    }

    /**
     * See {@link PreparedStatement#setInt(int, int)}.
     */
    private static void setInt(final @NotNull PreparedStatement preparedStatement, final int idx,
                               final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setInt(idx, (Integer) obj);
    }

    /**
     * See {@link PreparedStatement#setLong(int, long)}.
     */
    private static void setLong(final @NotNull PreparedStatement preparedStatement, final int idx,
                                final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setLong(idx, (Long) obj);
    }

    /**
     * See {@link PreparedStatement#setFloat(int, float)}.
     */
    private static void setFloat(final @NotNull PreparedStatement preparedStatement, final int idx,
                                 final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setFloat(idx, (Float) obj);
    }

    /**
     * See {@link PreparedStatement#setDouble(int, double)}.
     */
    private static void setDouble(final @NotNull PreparedStatement preparedStatement, final int idx,
                                  final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setDouble(idx, (Double) obj);
    }

    /**
     * See {@link PreparedStatement#setBigDecimal(int, BigDecimal)}.
     */
    private static void setBigDecimal(final @NotNull PreparedStatement preparedStatement, final int idx,
                                      final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setBigDecimal(idx, (BigDecimal) obj);
    }

    /**
     * See {@link PreparedStatement#setString(int, String)}.
     */
    private static void setString(final @NotNull PreparedStatement preparedStatement, final int idx,
                                  final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setString(idx, (String) obj);
    }

    /**
     * See {@link PreparedStatement#setBytes(int, byte[])}.
     */
    private static void setBytes(final @NotNull PreparedStatement preparedStatement, final int idx,
                                 final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setBytes(idx, (byte[]) obj);
    }

    /**
     * See {@link PreparedStatement#setDate(int, Date)}.
     */
    private static void setDate(final @NotNull PreparedStatement preparedStatement, final int idx,
                                final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setDate(idx, (Date) obj);
    }

    /**
     * See {@link PreparedStatement#setTime(int, Time)}.
     */
    private static void setTime(final @NotNull PreparedStatement preparedStatement, final int idx,
                                final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setTime(idx, (Time) obj);
    }

    /**
     * See {@link PreparedStatement#setTimestamp(int, Timestamp)}.
     */
    private static void setTimestamp(final @NotNull PreparedStatement preparedStatement, final int idx,
                                     final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setTimestamp(idx, (Timestamp) obj);
    }

    /**
     * See {@link PreparedStatement#setObject(int, Object)}.
     */
    private static void setObject(final @NotNull PreparedStatement preparedStatement, final int idx,
                                  final @NotNull Object obj)
        throws SQLException
    {
        preparedStatement.setObject(idx, obj);
    }

    /**
     * Gets the real index based on the received index value.
     *
     * @param idx The received index value.
     * @return The actual index value.
     */
    private int getRealIndex(final int idx)
    {
        return idx - 1 - skipCount;
    }

    /**
     * Replaces a '?' at the given index with a raw String.. Unlike all other types, this is executed before
     * constructing the actual {@link PreparedStatement} and modifies {@link #statement}.
     * <p>
     * Note that you'll have to make sure that the String is sanitized and valid. For example, when using it to specify
     * the name of a table, the name must comply with {@link IStorage#isValidTableName(String)}. This means that only
     * alphanumerics characters and the underscore character are allowed.
     * <p>
     * Most importantly: When using this method, it <b><u>MUST BE USED in the correct order</u></b>. Variables that
     * should be set before should be added before and those that should be set after should be added after.
     *
     * @param idx The index of the variable character (= "?") in the statement that should be replaced by the table
     *            name.
     * @param obj The name of the table.
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setRawString(final int idx, final @NotNull String obj)
    {
        if (!IStorage.isValidTableName(obj))
        {
            PLogger.get()
                   .logThrowable(new IllegalArgumentException(
                       "Trying to set table name using an invalid string: " + obj));
            return this;
        }

        String tmp = statement;
        int questionMarkPos = tmp.indexOf('?');
        if (questionMarkPos == -1)
        {
            PLogger.get()
                   .logThrowable(new IllegalArgumentException(
                       "Trying to set table name in a statement without any variables!"));
            return this;
        }

        // TODO: Precompute the indices of the question marks.
        int questionMarkIDX = 1;

        while (questionMarkPos > -1 && questionMarkIDX < idx)
        {
            ++questionMarkIDX;
            questionMarkPos = tmp.indexOf('?', questionMarkPos + 1);
        }

        statement = statement.substring(0, questionMarkPos) + obj + statement.substring(questionMarkPos + 1);

        ++skipCount;

        return this;
    }

    /**
     * See {@link PreparedStatement#setBoolean(int, boolean)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextBoolean(final @NotNull Boolean obj)
    {
        return setBoolean(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setBoolean(int, boolean)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setBoolean(final int idx, final @NotNull Boolean obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setBoolean, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setByte(int, byte)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextByte(final @NotNull Byte obj)
    {
        return setByte(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setByte(int, byte)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setByte(final int idx, final @NotNull Byte obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setByte, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setShort(int, short)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextShort(final @NotNull Short obj)
    {
        return setShort(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setShort(int, short)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setShort(final int idx, final @NotNull Short obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setShort, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setInt(int, int)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextInt(final @NotNull Integer obj)
    {
        return setInt(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setInt(int, int)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setInt(final int idx, final @NotNull Integer obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setInt, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setLong(int, long)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextLong(final @NotNull Long obj)
    {
        return setLong(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setLong(int, long)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setLong(final int idx, final @NotNull Long obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setLong, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setFloat(int, float)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextFloat(final @NotNull Float obj)
    {
        return setFloat(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setFloat(int, float)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setFloat(final int idx, final @NotNull Float obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setFloat, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setDouble(int, double)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextDouble(final @NotNull Double obj)
    {
        return setDouble(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setDouble(int, double)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setDouble(final int idx, final @NotNull Double obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setDouble, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setBigDecimal(int, BigDecimal)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextBigDecimal(final @NotNull BigDecimal obj)
    {
        return setBigDecimal(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setBigDecimal(int, BigDecimal)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setBigDecimal(final int idx, final @NotNull BigDecimal obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setBigDecimal, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setString(int, String)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextString(final @NotNull String obj)
    {
        return setString(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setString(int, String)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setString(final int idx, final @NotNull String obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setString, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setBytes(int, byte[])}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextBytes(final byte[] obj)
    {
        return setBytes(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setBytes(int, byte[])}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setBytes(final int idx, final byte[] obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setBytes, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setDate(int, Date)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextDate(final @NotNull Date obj)
    {
        return setDate(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setDate(int, Date)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setDate(final int idx, final @NotNull Date obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setDate, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setTime(int, Time)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextTime(final @NotNull Time obj)
    {
        return setTime(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setTime(int, Time)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setTime(final int idx, final @NotNull Time obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setTime, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setTimestamp(int, Timestamp)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextTimestamp(final @NotNull Timestamp obj)
    {
        return setTimestamp(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setTimestamp(int, Timestamp)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setTimestamp(final int idx, final @NotNull Timestamp obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setTimestamp, idx - skipCount, obj);
        return this;
    }

    /**
     * See {@link PreparedStatement#setObject(int, Object)}.
     * <p>
     * The index used is simply the one following from the previous set action.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setNextObject(final @NotNull Object obj)
    {
        return setObject(currentIDX, obj);
    }

    /**
     * See {@link PreparedStatement#setObject(int, Object)}.
     *
     * @return This {@link PPreparedStatement}.
     */
    public @NotNull PPreparedStatement setObject(final int idx, final @NotNull Object obj)
    {
        currentIDX = idx + 1;
        actions[getRealIndex(idx)] = new Action(PPreparedStatement::setObject, idx - skipCount, obj);
        return this;
    }

    private static final class Action
    {
        @NotNull
        private final CheckedTriConsumer<PreparedStatement, Integer, Object, SQLException> fun;
        private final int idx;
        @NotNull
        private final Object obj;

        private Action(final @NotNull CheckedTriConsumer<PreparedStatement, Integer, Object, SQLException> fun,
                       final int idx, final @NotNull Object obj)
        {
            this.fun = fun;
            this.idx = idx;
            this.obj = obj;
        }

        private @NotNull Object getObj()
        {
            return obj;
        }

        private int getIdx()
        {
            return idx;
        }

        @NotNull
        private CheckedTriConsumer<PreparedStatement, Integer, Object, SQLException> getFun()
        {
            return fun;
        }
    }
}
