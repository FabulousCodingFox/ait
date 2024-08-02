package loqor.ait.tardis.data.travel;

import loqor.ait.AITMod;
import loqor.ait.core.AITSounds;
import loqor.ait.core.data.DirectedGlobalPos;
import loqor.ait.core.data.base.Exclude;
import loqor.ait.core.sounds.MatSound;
import loqor.ait.core.util.DeltaTimeManager;
import loqor.ait.core.util.TimeUtil;
import loqor.ait.tardis.base.KeyedTardisComponent;
import loqor.ait.tardis.base.TardisTickable;
import loqor.ait.tardis.data.TardisCrashData;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.data.properties.v2.Property;
import loqor.ait.tardis.data.properties.v2.Value;
import loqor.ait.tardis.data.properties.v2.bool.BoolProperty;
import loqor.ait.tardis.data.properties.v2.bool.BoolValue;
import loqor.ait.tardis.data.properties.v2.integer.IntProperty;
import loqor.ait.tardis.data.properties.v2.integer.IntValue;
import loqor.ait.tardis.util.TardisUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("removal")
public abstract class TravelHandlerBase extends KeyedTardisComponent implements TardisTickable {

    private static final Property<State> STATE = Property.forEnum("state", State.class, State.LANDED);

    private static final Property<DirectedGlobalPos.Cached> POSITION = new Property<>(Property.Type.CDIRECTED_GLOBAL_POS, "position", (DirectedGlobalPos.Cached) Property.warnCompat("previous_position", null));
    private static final Property<DirectedGlobalPos.Cached> DESTINATION = new Property<>(Property.Type.CDIRECTED_GLOBAL_POS, "destination", (DirectedGlobalPos.Cached) Property.warnCompat("previous_position", null));
    private static final Property<DirectedGlobalPos.Cached> PREVIOUS_POSITION = new Property<>(Property.Type.CDIRECTED_GLOBAL_POS, "previous_position", (DirectedGlobalPos.Cached) Property.warnCompat("previous_position", null));

    private static final BoolProperty CRASHING = new BoolProperty("crashing", Property.warnCompat("crashing", false));
    private static final BoolProperty ANTIGRAVS = new BoolProperty("ANTIGRAVS", Property.warnCompat("antigravs", false));

    private static final IntProperty SPEED = new IntProperty("speed", Property.warnCompat("speed", 0));
    private static final IntProperty MAX_SPEED = new IntProperty("max_speed", Property.warnCompat("max_speed", 7));

    protected final Value<State> state = STATE.create(this);
    protected final Value<DirectedGlobalPos.Cached> position = POSITION.create(this);
    protected final Value<DirectedGlobalPos.Cached> destination = DESTINATION.create(this);
    protected final Value<DirectedGlobalPos.Cached> previousPosition = PREVIOUS_POSITION.create(this);

    protected final BoolValue crashing = CRASHING.create(this);
    protected final BoolValue antigravs = ANTIGRAVS.create(this);

    protected final IntValue speed = SPEED.create(this);
    protected final IntValue maxSpeed = MAX_SPEED.create(this);

    @Exclude(strategy = Exclude.Strategy.NETWORK)
    protected int hammerUses = 0;

    public TravelHandlerBase(Id id) {
        super(id);
    }

    @Override
    public void onLoaded() {
        state.of(this, STATE);

        position.of(this, POSITION);
        destination.of(this, DESTINATION);
        previousPosition.of(this, PREVIOUS_POSITION);

        speed.of(this, SPEED);
        maxSpeed.of(this, MAX_SPEED);

        crashing.of(this, CRASHING);
        antigravs.of(this, ANTIGRAVS);

        if (this.isClient())
            return;

        MinecraftServer current = TravelHandlerBase.server();

        this.position.ifPresent(cached -> cached.init(current), false);
        this.destination.ifPresent(cached -> cached.init(current), false);
        this.previousPosition.ifPresent(cached -> cached.init(current), false);
    }

    @Override
    public void tick(MinecraftServer server) {
        TardisCrashData crash = tardis.crash();

        if (crash.getState() != TardisCrashData.State.NORMAL)
            crash.addRepairTicks(2 * this.speed());

        if (this.hammerUses > 0 && !DeltaTimeManager.isStillWaitingOnDelay(AITMod.MOD_ID + "-tardisHammerAnnoyanceDecay")) {
            this.hammerUses--;

            DeltaTimeManager.createDelay(AITMod.MOD_ID + "-tardisHammerAnnoyanceDecay", (long) TimeUtil.secondsToMilliseconds(10));
        }
    }

    public void speed(int value) {
        this.speed.set(this.clampSpeed(value));
    }

    public int speed() {
        return this.speed.get();
    }

    protected int clampSpeed(int value) {
        return MathHelper.clamp(value, 0, this.maxSpeed.get());
    }

    public IntValue maxSpeed() {
        return maxSpeed;
    }

    public State getState() {
        return state.get();
    }

    public DirectedGlobalPos.Cached position() {
        return this.position.get();
    }

    public BoolValue antigravs() {
        return antigravs;
    }

    public boolean isCrashing() {
        return crashing.get();
    }

    public void setCrashing(boolean crashing) {
        this.crashing.set(crashing);
    }

    public int getHammerUses() {
        return hammerUses;
    }

    public int instability() {
        return this.getHammerUses() + 1;
    }

    public void useHammer() {
        this.hammerUses++;
    }

    public void resetHammerUses() {
        this.hammerUses = 0;
    }

    public void forcePosition(DirectedGlobalPos.Cached cached) {
        cached.init(TravelHandlerBase.server());
        this.previousPosition.set(this.position);
        this.position.set(cached);
    }

    public void forcePosition(Function<DirectedGlobalPos.Cached, DirectedGlobalPos.Cached> position) {
        this.forcePosition(position.apply(this.position()));
    }

    public DirectedGlobalPos.Cached destination() {
        return destination.get();
    }

    public void destination(DirectedGlobalPos.Cached cached) {
        if (this.destination().equals(cached))
            return;

        cached.init(TravelHandlerBase.server());

        WorldBorder border = cached.getWorld().getWorldBorder();
        BlockPos pos = cached.getPos();

        cached = border.contains(pos) ? cached : cached.pos(
                border.clamp(pos.getX(), pos.getY(), pos.getZ())
        );

        // TODO: how about, instead of doing the checks at demat, do them at remat?
        //  it makes much more sense, because the target could be obstructed by the time the tardis is there
        cached = this.checkDestination(cached, AITMod.AIT_CONFIG.SEARCH_HEIGHT(), PropertiesHandler.getBool(
                this.tardis().properties(), PropertiesHandler.FIND_GROUND)
        );

        this.forceDestination(cached);
    }

    public void forceDestination(DirectedGlobalPos.Cached cached) {
        cached.init(TravelHandlerBase.server());
        this.destination.set(cached);
    }

    public void destination(Function<DirectedGlobalPos.Cached, DirectedGlobalPos.Cached> position) {
        this.destination(position.apply(this.destination()));
    }

    public DirectedGlobalPos.Cached previousPosition() {
        return previousPosition.get();
    }

    protected static MinecraftServer server() {
        return TardisUtil.getOverworld().getServer();
    }

    protected static boolean isReplaceable(BlockState... states) {
        for (BlockState state1 : states) {
            if (!state1.isReplaceable()) {
                return false;
            }
        }

        return true;
    }

    protected DirectedGlobalPos.Cached checkDestination(DirectedGlobalPos.Cached destination, int limit, boolean fullCheck) {
        ServerWorld world = destination.getWorld();
        BlockPos.Mutable temp = destination.getPos().mutableCopy();

        if (!antigravs.get()) {
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    for (int y = -limit; y <= limit; y++) {
                        temp.set(temp.getX() + x,
                                temp.getY() + y,
                                temp.getZ() + z);

                        BlockState current = world.getBlockState(temp);
                        BlockState top = world.getBlockState(temp.up());

                        if (isReplaceable(current, top)) {

                            if (isLargePoolOfWater(world, temp)) {
                                continue;
                            }

                            if (isLargePoolOfLava(world, temp)) {
                                continue;
                            }

                            return destination.pos(temp);
                        }
                    }
                }
            }
        }

        return destination;
    }

    private boolean isLargePoolOfWater(ServerWorld world, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockState blockState = world.getBlockState(pos.add(x, 0, z));
                if (blockState.getBlock() == Blocks.WATER && blockState.get(FluidBlock.LEVEL) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLargePoolOfLava(ServerWorld world, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockState blockState = world.getBlockState(pos.add(x, 0, z));
                if (blockState.getBlock() == Blocks.LAVA && blockState.get(FluidBlock.LEVEL) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasAirInArea(ServerWorld world, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockState blockState = world.getBlockState(pos.add(x, y, z));
                    if (blockState.isAir()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public enum State {
        LANDED,
        DEMAT(AITSounds.DEMAT_ANIM, TravelHandler::finishDemat),
        FLIGHT(AITSounds.FLIGHT_ANIM),
        MAT(AITSounds.MAT_ANIM, TravelHandler::finishRemat);

        private final MatSound sound;
        private final boolean animated;

        private final Consumer<TravelHandler> finish;

        State() {
            this(null);
        }

        State(MatSound sound) {
            this(sound, null, false);
        }

        State(MatSound sound, Consumer<TravelHandler> finish) {
            this(sound, finish, true);
        }

        State(MatSound sound, Consumer<TravelHandler> finish, boolean animated) {
            this.sound = sound;
            this.animated = animated;

            this.finish = finish;
        }

        public MatSound effect() {
            return this.sound;
        }

        public boolean animated() {
            return animated;
        }

        public void finish(TravelHandler handler) {
            this.finish.accept(handler);
        }
    }
}
