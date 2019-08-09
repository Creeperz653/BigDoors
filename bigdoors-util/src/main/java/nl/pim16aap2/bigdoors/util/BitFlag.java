package nl.pim16aap2.bigdoors.util;

public interface BitFlag
{
    /**
     * Sets the value of a flag to either on or off.
     *
     * @param flagValue    The value of the flag.
     * @param enabled      Whether or not this flag will be on.
     * @param currentValue The current value of the flag(s).
     * @return The new value of the flag(s).
     */
    static int changeFlag(final int flagValue, final boolean enabled, final int currentValue)
    {
        return enabled ? setFlag(flagValue, currentValue) : unsetFlag(flagValue, currentValue);
    }

    /**
     * Enables a flag if not previously enabled.
     *
     * @param flagValue    The value of the flag.
     * @param currentValue The current value of the flag(s).
     * @return The new value of the flag(s).
     */
    static int setFlag(final int flagValue, final int currentValue)
    {
        return currentValue | flagValue;
    }

    /**
     * Sets the value of a flag to either off.
     *
     * @param flagValue    The value of the flag.
     * @param currentValue The current value of the flag(s).
     * @return The new value of the flag(s).
     */
    static int unsetFlag(final int flagValue, final int currentValue)
    {
        return currentValue & ~flagValue;
    }

    /**
     * Checks if a given flag is enabled
     *
     * @param flagValue    The value of the flag.
     * @param currentValue The value of the current flag(s).
     * @return True if the flag is enabled.
     */
    static boolean hasFlag(final int flagValue, final int currentValue)
    {
        return (currentValue & flagValue) == flagValue;
    }
}