package loqor.ait.data.schema.desktop;

import java.lang.reflect.Type;
import java.util.Optional;

import com.google.gson.*;

import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;

import loqor.ait.core.util.WorldUtil;
import loqor.ait.data.Loyalty;
import loqor.ait.data.schema.BasicSchema;
import loqor.ait.data.schema.desktop.textures.DesktopPreviewTexture;
import loqor.ait.registry.impl.DesktopRegistry;
import loqor.ait.registry.unlockable.Unlockable;

public abstract class TardisDesktopSchema extends BasicSchema implements Unlockable {

    private final Identifier id;

    private final DesktopPreviewTexture preview;
    private final Loyalty loyalty;

    protected TardisDesktopSchema(Identifier id, DesktopPreviewTexture texture, Optional<Loyalty> loyalty) {
        super("desktop");
        this.id = id;

        this.preview = texture;
        this.loyalty = loyalty.orElse(null);
    }

    protected TardisDesktopSchema(Identifier id, DesktopPreviewTexture texture, Loyalty loyalty) {
        this(id, texture, Optional.of(loyalty));
    }

    protected TardisDesktopSchema(Identifier id, DesktopPreviewTexture texture) {
        this(id, texture, Optional.empty());
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public Optional<Loyalty> requirement() {
        return Optional.ofNullable(loyalty);
    }

    @Override
    public UnlockType unlockType() {
        return UnlockType.DESKTOP;
    }

    public DesktopPreviewTexture previewTexture() {
        return this.preview;
    }

    public Optional<StructureTemplate> findTemplate() {
        return WorldUtil.getTardisDimension().getStructureTemplateManager()
                .getTemplate(this.getStructureLocation());
    }

    private Identifier getStructureLocation() {
        Identifier id = this.id();

        return new Identifier(id.getNamespace(), "interiors/" + id.getPath());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o instanceof TardisDesktopSchema that)
            return id.equals(that.id);

        return false;
    }

    public static Object serializer() {
        return new Serializer();
    }

    private static class Serializer
            implements
                JsonSerializer<TardisDesktopSchema>,
                JsonDeserializer<TardisDesktopSchema> {

        @Override
        public TardisDesktopSchema deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return DesktopRegistry.getInstance().get(new Identifier(json.getAsJsonPrimitive().getAsString()));
        }

        @Override
        public JsonElement serialize(TardisDesktopSchema src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.id().toString());
        }
    }
}