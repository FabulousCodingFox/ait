package loqor.ait.tardis.data.travel;

import net.minecraft.server.MinecraftServer;

public abstract class AnimatedTravelHandler extends ProgressiveTravelHandler {

    private int animationTicks;

    public AnimatedTravelHandler(Id id) {
        super(id);
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);

        State state = this.getState();

        if (state.animated()) {
            this.tickAnimationProgress(state);
        }
    }

    private void tickAnimationProgress(State state) {
        if (this.animationTicks++ < state.effect().length())
            return;

        this.animationTicks = 0;

        if (this instanceof TravelHandler handler)
            state.finish(handler);
    }

    public int getAnimTicks() {
        return animationTicks;
    }

    public int getMaxAnimTicks() {
        return this.getState().effect().length();
    }
}