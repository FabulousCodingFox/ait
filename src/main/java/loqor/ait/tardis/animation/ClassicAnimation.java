package loqor.ait.tardis.animation;

import loqor.ait.AITMod;
import loqor.ait.core.blockentities.ExteriorBlockEntity;
import loqor.ait.core.sounds.MatSound;
import loqor.ait.tardis.data.TravelHandler;

public class ClassicAnimation extends ExteriorAnimation {

	public ClassicAnimation(ExteriorBlockEntity exterior) {
		super(exterior);
	}

	@Override
	public void tick() {
		if (exterior.tardis().isEmpty())
			return;

		TravelHandler.State state = exterior.tardis().get().travel2().getState();

		if (this.timeLeft < 0)
			this.setupAnimation(exterior.tardis().get().travel2().getState()); // fixme is a jank fix for the timeLeft going negative on client

		if (state == TravelHandler.State.DEMAT) {
			timeLeft--;
			this.setAlpha(getFadingAlpha());
		} else if (state == TravelHandler.State.MAT) {
			timeLeft++;

			if (timeLeft > 680) {
				this.setAlpha(((float) timeLeft - 680) / (860 - 620));
			} else {
				this.setAlpha(0f);
			}
		} else if (state == TravelHandler.State.LANDED/* && alpha != 1f*/) {
			this.setAlpha(1f);
		}
	}

	public float getFadingAlpha() {
		return (float) (timeLeft) / (maxTime);
	}

	@Override
	public void setupAnimation(TravelHandler.State state) {
		if (exterior.tardis().isEmpty() || exterior.tardis().get().getExterior().getCategory() == null) {
			AITMod.LOGGER.error("Tardis for exterior " + exterior + " was null! Panic!!!!");
			alpha = 0f; // just make me vanish.
			return;
		}

		MatSound sound = exterior.tardis().get().getExterior().getVariant().getSound(state);
		this.tellClientsToSetup(state);

		timeLeft = sound.timeLeft();
		maxTime = sound.maxTime();
		startTime = sound.startTime();

		if (state == TravelHandler.State.DEMAT) {
			alpha = 1f;
		} else if (state == TravelHandler.State.MAT) {
			alpha = 0f;
		} else if (state == TravelHandler.State.LANDED) {
			alpha = 1f;
		}
	}
}
