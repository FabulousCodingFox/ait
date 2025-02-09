package loqor.ait.data.schema.console.variant.renaisance.client;

import org.joml.Vector3f;

import net.minecraft.util.Identifier;

import loqor.ait.AITMod;
import loqor.ait.client.models.consoles.ConsoleModel;
import loqor.ait.client.models.consoles.RenaissanceConsoleModel;
import loqor.ait.data.schema.console.ClientConsoleVariantSchema;
import loqor.ait.data.schema.console.variant.renaisance.RenaissanceVariant;

public class ClientRenaissanceVariant extends ClientConsoleVariantSchema {
    public static final Identifier TEXTURE = new Identifier(AITMod.MOD_ID,
            ("textures/blockentities/consoles/renaisance_default.png"));
    public static final Identifier EMISSION = new Identifier(AITMod.MOD_ID,
            ("textures/blockentities/consoles/renaisance_default_emission.png"));

    public ClientRenaissanceVariant() {
        super(RenaissanceVariant.REFERENCE, RenaissanceVariant.REFERENCE);
    }

    @Override
    public Identifier texture() {
        return TEXTURE;
    }

    @Override
    public Identifier emission() {
        return EMISSION;
    }

    @Override
    public ConsoleModel model() {
        return new RenaissanceConsoleModel(RenaissanceConsoleModel.getTexturedModelData().createModel());
    }

    @Override
    public Vector3f sonicItemTranslations() {
        return new Vector3f(-0.013f, 1.2f, -0.895f);
    }

    @Override
    public float[] sonicItemRotations() {
        return new float[]{-180f, -30f};
    }

    @Override
    public Vector3f handlesTranslations() {
        return new Vector3f(0.75f, 1.25f, 0.4f);
    }

    @Override
    public float[] handlesRotations() {
        return new float[]{-90f, 120f};
    }
}
