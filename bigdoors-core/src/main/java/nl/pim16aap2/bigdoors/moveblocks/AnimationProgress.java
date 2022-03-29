package nl.pim16aap2.bigdoors.moveblocks;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import nl.pim16aap2.bigdoors.api.animatedblock.IAnimatedBlock;
import nl.pim16aap2.bigdoors.util.Cuboid;

import java.util.Collections;
import java.util.List;

public class AnimationProgress<T extends IAnimatedBlock> implements IAnimationProgress<T>
{
    private final int duration;

    @Setter(AccessLevel.PACKAGE)
    private volatile Cuboid region;
    @Getter
    private final List<T> animatedBlocks;
    @Setter(AccessLevel.PACKAGE)
    private volatile AnimationState state = AnimationState.PENDING;
    @Setter(AccessLevel.PACKAGE)
    private volatile int stepsExecuted = 0;

    AnimationProgress(int duration, Cuboid region, List<T> animatedBlocks)
    {
        this.duration = duration;
        this.region = region;
        this.animatedBlocks = Collections.unmodifiableList(animatedBlocks);
    }

    @Override
    public Cuboid getRegion()
    {
        return region;
    }

    @Override
    public int getDuration()
    {
        return duration;
    }

    @Override
    public int getStepsExecuted()
    {
        return stepsExecuted;
    }

    @Override
    public AnimationState getState()
    {
        return state;
    }
}
