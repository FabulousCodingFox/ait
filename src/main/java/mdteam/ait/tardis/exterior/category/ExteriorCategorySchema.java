package mdteam.ait.tardis.exterior.category;

import com.google.gson.*;
import mdteam.ait.client.registry.ClientExteriorVariantRegistry;
import mdteam.ait.client.registry.exterior.ClientExteriorVariantSchema;
import mdteam.ait.registry.CategoryRegistry;
import mdteam.ait.registry.ExteriorVariantRegistry;
import mdteam.ait.registry.datapack.Identifiable;
import mdteam.ait.tardis.exterior.variant.ExteriorVariantSchema;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.lang.reflect.Type;

/**
 * @author duzo
 */
public abstract class ExteriorCategorySchema implements Identifiable {
    private final Identifier id;
    private final String name;

    protected ExteriorCategorySchema(Identifier id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() == null) return false;

        ExteriorCategorySchema that = (ExteriorCategorySchema) o;

        return id.equals(that.id);
    }

    public Identifier id() {
        return this.id;
    }
    public String name() { return this.name; }

    /**
     * The default exterior for this category
     */
    public ExteriorVariantSchema getDefaultVariant() {
        return ExteriorVariantRegistry.withParentToList(this).get(0);
    }

    @Deprecated // Replace with the exteriors own hasPortals method, they need to override it
    public boolean hasPortals() {return false;}

    @Override
    public String toString() {
        return this.name();
    }

    public static Object serializer() {
        return new Serializer();
    }

    private static class Serializer implements JsonSerializer<ExteriorCategorySchema>, JsonDeserializer<ExteriorCategorySchema> {

        @Override
        public ExteriorCategorySchema deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Identifier id;

            try {
                id = new Identifier(json.getAsJsonPrimitive().getAsString());
            } catch (InvalidIdentifierException e) {
                id = CapsuleCategory.REFERENCE;
            }

            return CategoryRegistry.getInstance().get(id);
        }

        @Override
        public JsonElement serialize(ExteriorCategorySchema src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.id().toString());
        }
    }
}
