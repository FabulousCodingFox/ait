package loqor.ait.core.commands.argument;

import com.mojang.brigadier.context.CommandContext;
import loqor.ait.tardis.data.travel.TravelHandlerBase;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.StringIdentifiable;

public class GroundSearchArgumentType extends EnumArgumentType<TravelHandlerBase.GroundSearch> {

    public static final StringIdentifiable.Codec<TravelHandlerBase.GroundSearch> CODEC = StringIdentifiable.createCodec(TravelHandlerBase.GroundSearch::values);

    protected GroundSearchArgumentType() {
        super(CODEC, TravelHandlerBase.GroundSearch::values);
    }

    public static GroundSearchArgumentType groundSearch() {
        return new GroundSearchArgumentType();
    }

    public static TravelHandlerBase.GroundSearch getGroundSearch(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, TravelHandlerBase.GroundSearch.class);
    }
}