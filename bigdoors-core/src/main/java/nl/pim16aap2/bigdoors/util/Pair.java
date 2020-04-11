package nl.pim16aap2.bigdoors.util;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a name-value pair.
 *
 * @autor Pim
 */
public final class Pair<K, V>
{
    @NotNull
    private K key;
    @NotNull
    private V value;

    public Pair(final @NotNull K key, final @NotNull V value)
    {
        this.key = key;
        this.value = value;
    }

    /**
     * Retrieves the key of this pair.
     *
     * @return The key of this pair.
     */
    @NotNull
    public K key()
    {
        return key;
    }

    /**
     * Retrieves the value of this pair.
     *
     * @return The value of this pair.
     */
    @NotNull
    public V value()
    {
        return value;
    }

    /**
     * Sets the key.
     *
     * @param key The key.
     */
    public void setKey(final @NotNull K key)
    {
        this.key = key;
    }

    /**
     * Sets the value.
     *
     * @param value The value.
     */
    public void setValue(final @NotNull V value)
    {
        this.value = value;
    }
}