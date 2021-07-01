package nl.pim16aap2.bigdoors.api.util.functional;


import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * Represents a {@link BiFunction} that throws an exception.
 *
 * @param <T> The type of the first input argument of the function
 * @param <U> The type of the second input argument of the function
 * @param <R> The type of the result of the function
 * @param <E> The type of the exception thrown by the function.
 * @author Pim
 */
@FunctionalInterface
public interface CheckedBiFunction<T, U, R, E extends Exception>
{
    /**
     * Applies this function to the given arguments.
     *
     * @param t The first function argument.
     * @param u The second function argument.
     * @return The function result.
     *
     * @throws E
     */
    @NotNull R apply(T t, U u)
        throws E;
}
