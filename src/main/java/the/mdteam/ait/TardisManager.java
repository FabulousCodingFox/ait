package the.mdteam.ait;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mdteam.ait.api.tardis.ILinkable;
import mdteam.ait.core.helper.TardisUtil;
import mdteam.ait.data.Corners;
import mdteam.ait.data.SerialDimension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class TardisManager {

    protected final Map<UUID, Tardis> lookup = new HashMap<>();
    protected final Gson gson;

    public TardisManager() {
        this.gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                return field.getAnnotation(Exclude.class) != null;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).registerTypeAdapter(TardisDesktopSchema.class, TardisDesktopSchema.serializer())
                .registerTypeAdapter(SerialDimension.class, SerialDimension.serializer())
                .registerTypeAdapter(Corners.class, Corners.serializer())
                .create();
    }

    public void getTardis(UUID uuid, Consumer<Tardis> consumer) {
        if (this.lookup.containsKey(uuid)) {
            consumer.accept(this.lookup.get(uuid));
            return;
        }

        this.loadTardis(uuid, consumer);
    }

    public void link(UUID uuid, ILinkable linkable) {
        this.getTardis(uuid, linkable::setTardis);
    }

    public abstract void loadTardis(UUID uuid, Consumer<Tardis> consumer);

    public Map<UUID, Tardis> getLookup() {
        return this.lookup;
    }

    public static TardisManager getInstance() {
        return TardisUtil.isServer() ? ServerTardisManager.getInstance() : ClientTardisManager.getInstance();
    }
}
