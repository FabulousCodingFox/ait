package loqor.ait.data.schema.exterior;

import java.lang.reflect.Type;

import com.google.gson.*;
import dev.pavatus.lib.api.Identifiable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector3f;

import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import loqor.ait.AITMod;
import loqor.ait.client.models.exteriors.ExteriorModel;
import loqor.ait.data.datapack.exterior.BiomeOverrides;
import loqor.ait.data.schema.door.ClientDoorSchema;
import loqor.ait.registry.impl.door.ClientDoorRegistry;
import loqor.ait.registry.impl.exterior.ClientExteriorVariantRegistry;
import loqor.ait.registry.impl.exterior.ExteriorVariantRegistry;

@Environment(EnvType.CLIENT)
public abstract class ClientExteriorVariantSchema implements Identifiable {

    private final Identifier parent;
    private final Identifier id;

    private ClientDoorSchema door;

    protected ClientExteriorVariantSchema(Identifier parent, Identifier id) {
        this.parent = parent;
        this.id = id;
    }

    protected ClientExteriorVariantSchema(Identifier parent) {
        this.id = parent;
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        return o instanceof ClientExteriorVariantSchema other && this.id.equals(other.id);
    }

    public ExteriorVariantSchema parent() {
        return ExteriorVariantRegistry.getInstance().get(this.parent);
    }

    public Identifier id() {
        return id;
    }

    public abstract Identifier texture();

    public abstract Identifier emission();

    public abstract ExteriorModel model();

    public abstract Vector3f sonicItemTranslations();

    public abstract BiomeOverrides overrides();

    public float[] sonicItemRotations() {
        return new float[]{0f, 45f};
    }

    /**
     * The default exterior for this category
     */
    public ExteriorVariantSchema getDefaultVariant() {
        return ExteriorVariantRegistry.getInstance().get(this.parent().id()).category().getDefaultVariant();
    }

    public ClientDoorSchema getDoor() {
        if (this.door == null)
            this.door = ClientDoorRegistry.withParent(this.parent().door());

        return this.door;
    }

    public static Object serializer() {
        return new Serializer();
    }

    private static class Serializer
            implements
                JsonSerializer<ClientExteriorVariantSchema>,
                JsonDeserializer<ClientExteriorVariantSchema> {

        @Override
        public ClientExteriorVariantSchema deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            Identifier id;

            try {
                id = new Identifier(json.getAsJsonPrimitive().getAsString());
            } catch (InvalidIdentifierException e) {
                id = AITMod.id("capsule_default");
            }

            return ClientExteriorVariantRegistry.getInstance().get(id);
        }

        @Override
        public JsonElement serialize(ClientExteriorVariantSchema src, Type typeOfSrc,
                JsonSerializationContext context) {
            return new JsonPrimitive(src.id().toString());
        }
    }
}
