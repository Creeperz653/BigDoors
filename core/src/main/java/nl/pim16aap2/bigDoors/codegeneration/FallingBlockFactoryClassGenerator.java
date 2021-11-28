package nl.pim16aap2.bigDoors.codegeneration;

import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import nl.pim16aap2.bigDoors.NMS.FallingBlockFactory;
import nl.pim16aap2.bigDoors.NMS.NMSBlock;
import nl.pim16aap2.bigDoors.reflection.ReflectionBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

import static net.bytebuddy.implementation.MethodCall.construct;
import static net.bytebuddy.implementation.MethodCall.invoke;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static nl.pim16aap2.bigDoors.codegeneration.ReflectionRepository.*;
import static nl.pim16aap2.bigDoors.reflection.ReflectionBuilder.findEnumValues;
import static nl.pim16aap2.bigDoors.reflection.ReflectionBuilder.findMethod;

/**
 * Represents an implementation of a {@link ClassGenerator} to generate a subclass of {@link FallingBlockFactory}.
 *
 * @author Pim
 */
public class FallingBlockFactoryClassGenerator extends ClassGenerator
{
    private static final @NotNull Class<?>[] CONSTRUCTOR_PARAMETER_TYPES = new Class<?>[0];

    public static final String FIELD_AXES_VALUES = "generated$axesValues";
    public static final String FIELD_ROTATION_VALUES = "generated$blockRotationValues";
    public static final String FIELD_MOVE_TYPE_VALUES = "generated$enumMoveTypeValues";

    public static final String METHOD_POST_PROCESS = "generated$postProcessEntity";
    public static final String METHOD_CREATE_CRAFT_ENTITY = "generated$createEntity";
    public static final String METHOD_SPAWN_ENTITY = "generated$spawnEntity";
    public static final String METHOD_VERIFY_IMPL_0 = "generated$verify0";
    public static final String METHOD_VERIFY_IMPL_1 = "generated$verify1";

    public static final Method METHOD_FBLOCK_FACTORY =
        findMethod().inClass(FallingBlockFactory.class).withName("fallingBlockFactory").get();
    public static final Method METHOD_NMS_BLOCK_FACTORY =
        findMethod().inClass(FallingBlockFactory.class).withName("nmsBlockFactory").get();
    public static final Method METHOD_VERIFY =
        findMethod().inClass(FallingBlockFactory.class).withName("verify").get();


    private final @NotNull ClassGenerator nmsBlockClassGenerator;
    private final @NotNull ClassGenerator craftFallingBlockClassGenerator;
    private final @NotNull ClassGenerator entityFallingBlockClassGenerator;

    public FallingBlockFactoryClassGenerator(@NotNull String mappingsVersion,
                                             @NotNull ClassGenerator nmsBlockClassGenerator,
                                             @NotNull ClassGenerator craftFallingBlockClassGenerator,
                                             @NotNull ClassGenerator entityFallingBlockClassGenerator)
        throws Exception
    {
        super(mappingsVersion);
        this.nmsBlockClassGenerator = nmsBlockClassGenerator;
        this.craftFallingBlockClassGenerator = craftFallingBlockClassGenerator;
        this.entityFallingBlockClassGenerator = entityFallingBlockClassGenerator;

        generate();
    }

    @Override
    protected @NotNull Class<?>[] getConstructorArgumentTypes()
    {
        return CONSTRUCTOR_PARAMETER_TYPES;
    }

    @Override
    protected @NotNull String getBaseName()
    {
        return "FallingBlockFactory";
    }

    @Override
    protected void generateImpl()
    {
        DynamicType.Builder<?> builder = createBuilder(FallingBlockFactory.class);

        builder = addCTor(builder);
        builder = addFields(builder);
        builder = addFallingBlockFactoryMethod(builder);
        builder = addNMSBlockFactoryMethod(builder);
        builder = addVerifyMethod(builder);

        finishBuilder(builder);
    }

    private DynamicType.Builder<?> addFields(DynamicType.Builder<?> builder)
    {
        return builder
            .defineField(FIELD_AXES_VALUES, asArrayType(classEnumDirectionAxis), Visibility.PRIVATE)
            .defineField(FIELD_ROTATION_VALUES, asArrayType(classEnumBlockRotation), Visibility.PRIVATE)
            .defineField(FIELD_MOVE_TYPE_VALUES, asArrayType(classEnumMoveType), Visibility.PRIVATE);
    }

    private DynamicType.Builder<?> addCTor(DynamicType.Builder<?> builder)
    {
        final Object[] axesValues = findEnumValues().inClass(classEnumDirectionAxis).get();
        final Object[] blockRotationValues = findEnumValues().inClass(classEnumBlockRotation).get();
        final Object[] enumMoveTypeValues = findEnumValues().inClass(classEnumMoveType).get();

        return builder
            .defineConstructor(Visibility.PUBLIC)
            .intercept(SuperMethodCall.INSTANCE.andThen(
                FieldAccessor.ofField(FIELD_AXES_VALUES).setsValue(axesValues)).andThen(
                FieldAccessor.ofField(FIELD_ROTATION_VALUES).setsValue(blockRotationValues)).andThen(
                FieldAccessor.ofField(FIELD_MOVE_TYPE_VALUES).setsValue(enumMoveTypeValues)));
    }

    private DynamicType.Builder<?> addNMSBlockFactoryMethod(DynamicType.Builder<?> builder)
    {
        final MethodCall getNMSWorld = (MethodCall)
            invoke(methodGetNMSWorld).onArgument(0).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);

        final MethodCall createBlockPosition = construct(cTorBlockPosition).withArgument(1, 2, 3);

        final MethodCall getType = invoke(methodGetTypeFromBlockPosition)
            .onMethodCall(getNMSWorld).withMethodCall(createBlockPosition);

        final MethodCall getBlock = invoke(methodGetBlockFromBlockData).onMethodCall(getType);

        final MethodCall createBlockInfo = (MethodCall)
            invoke(methodBlockInfoFromBlockBase).withMethodCall(getBlock)
                                                .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);

        builder = builder
            .define(METHOD_NMS_BLOCK_FACTORY)
            .intercept(construct(nmsBlockClassGenerator.getGeneratedConstructor())
                           .withArgument(0, 1, 2, 3).withMethodCall(createBlockInfo)
                           .withField(FIELD_AXES_VALUES, FIELD_ROTATION_VALUES));

        return builder;
    }

    private DynamicType.Builder<?> addFallingBlockFactoryMethod(DynamicType.Builder<?> builder)
    {
        builder = builder
            .defineMethod(METHOD_POST_PROCESS, craftFallingBlockClassGenerator.getGeneratedClass(), Visibility.PRIVATE)
            .withParameters(craftFallingBlockClassGenerator.getGeneratedClass())
            .intercept(invoke(methodSetCraftEntityCustomName).onArgument(0).with("BigDoorsEntity").andThen(
                invoke(methodSetCraftEntityCustomNameVisible).onArgument(0).with(false)).andThen(
                FixedValue.argument(0)));

        builder = builder
            .defineMethod(METHOD_SPAWN_ENTITY, craftFallingBlockClassGenerator.getGeneratedClass(), Visibility.PRIVATE)
            .withParameters(craftFallingBlockClassGenerator.getGeneratedClass())
            .intercept(invoke(named(EntityFallingBlockClassGenerator.METHOD_SPAWN))
                           .onMethodCall(invoke(named(methodGetEntityHandle.getName())).onArgument(0))
                           .andThen(FixedValue.argument(0)));

        final Method methodGetMyBlockData =
            ReflectionBuilder.findMethod().inClass(nmsBlockClassGenerator.getGeneratedClass())
                             .withName(NMSBlockClassGenerator.METHOD_GET_MY_BLOCK_DATA).withoutParameters().get();

        final MethodCall createBlockData = (MethodCall)
            invoke(methodGetMyBlockData).onArgument(1).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);

        final MethodCall createEntity = construct(entityFallingBlockClassGenerator.getGeneratedConstructor())
            .withMethodCall(invoke(methodLocationGetWorld).onArgument(0))
            .withMethodCall(invoke(methodLocationGetX).onArgument(0))
            .withMethodCall(invoke(methodLocationGetY).onArgument(0))
            .withMethodCall(invoke(methodLocationGetZ).onArgument(0))
            .withMethodCall(createBlockData)
            .withField(FIELD_MOVE_TYPE_VALUES);

        final MethodCall createCraftFallingBlock = (MethodCall)
            construct(craftFallingBlockClassGenerator.getGeneratedConstructor())
                .withMethodCall(invoke(methodGetBukkitServer))
                .withMethodCall(createEntity)
                .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);

        builder = builder
            .defineMethod(METHOD_CREATE_CRAFT_ENTITY, craftFallingBlockClassGenerator.getGeneratedClass(),
                          Visibility.PRIVATE)
            .withParameters(Location.class, NMSBlock.class, byte.class, Material.class)
            .intercept(invoke(named(METHOD_POST_PROCESS)).withMethodCall(createCraftFallingBlock)
                                                         .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));

        builder = builder
            .define(METHOD_FBLOCK_FACTORY)
            .intercept(invoke(named(METHOD_SPAWN_ENTITY))
                           .withMethodCall(invoke(named(METHOD_CREATE_CRAFT_ENTITY)).withAllArguments())
                           .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));

        return builder;
    }

    private DynamicType.Builder<?> addVerifyMethod(DynamicType.Builder<?> builder)
    {
        // Second verify impl. Takes an NMSBlock and a CraftFallingBlock as input.
        // Then calls the three separate verify implementations (NMSBlock + CraftFallingBlock + EntityFallingBlock).
        builder = builder
            .defineMethod(METHOD_VERIFY_IMPL_1, void.class, Visibility.PRIVATE)
            .withParameters(nmsBlockClassGenerator.generatedClass, craftFallingBlockClassGenerator.generatedClass)
            .intercept(invoke(METHOD_VERIFY_CLASS).onArgument(0).andThen(
                invoke(METHOD_VERIFY_CLASS).onArgument(1).andThen(
                    invoke(METHOD_VERIFY_CLASS)
                        .onMethodCall(invoke(named(methodGetEntityHandle.getName())).onArgument(1)))));

        // First verify impl. Takes NMSBlock + Location.
        // Constructs a new CraftFallingBlock (which includes entity) without spawning it.
        // Then calls the second verify impl.
        builder = builder
            .defineMethod(METHOD_VERIFY_IMPL_0, void.class, Visibility.PRIVATE)
            .withParameters(nmsBlockClassGenerator.generatedClass, Location.class)
            .intercept(invoke(named(METHOD_VERIFY_IMPL_1))
                           .withArgument(0)
                           .withMethodCall(invoke(named(METHOD_CREATE_CRAFT_ENTITY))
                                               .withArgument(1,0)
                                               .with((byte) 0, Material.STONE)));

        builder = builder
            .define(METHOD_VERIFY)
            .intercept(invoke(named(METHOD_VERIFY_IMPL_0))
                           .withMethodCall(invoke(METHOD_NMS_BLOCK_FACTORY).withAllArguments())
                           .withMethodCall(construct(ctorLocation).withAllArguments())
                           .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));
        return builder;
    }
}
