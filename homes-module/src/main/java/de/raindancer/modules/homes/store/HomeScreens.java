package de.raindancer.modules.homes.store;

import de.raindancer.core.ui.hologram.ScreenKind;
import de.raindancer.core.ui.hologram.Screens;
import de.raindancer.modules.homes.rules.ScreenLineRule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/** The homes' screens for Core's {@code /screen}: everybody's own homes, who has most, and where they are. */
public final class HomeScreens {

    public static final List<String> KINDS = List.of("homes-you", "homes-keepers", "homes-worlds");
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final int YOURS_SHOWN = 8;
    private static final int TOP = 5;

    private final HomeCatalogue homes;
    private final Function<UUID, String> nameOf;
    private final ScreenLineRule rule = new ScreenLineRule();

    public HomeScreens(HomeCatalogue homes, Function<UUID, String> nameOf) {
        this.homes = homes;
        this.nameOf = nameOf;
    }

    public void register(Screens screens, Plugin owner) {
        screens.register(owner, ScreenKind.personal("homes-you", "Your homes",
                "Everybody's own homes and the worlds they are in.", Material.RED_BED, this::you));
        screens.register(owner, ScreenKind.everyone("homes-keepers", "Most homes",
                "Who has set the most homes.", Material.WHITE_BED, this::keepers));
        screens.register(owner, ScreenKind.everyone("homes-worlds", "Homes by world",
                "Which worlds the homes are in.", Material.COMPASS, this::worlds));
    }

    private Component you(ScreenKind.Viewer viewer) {
        var mine = homes.of(viewer.id());
        StringBuilder text = new StringBuilder("<gold><bold>Your homes</bold></gold>");
        if (mine.isEmpty()) {
            return MINI.deserialize(text + "\n<gray>None yet — <white>/sethome");
        }
        text.append("\n<gray>Homes <white>").append(mine.size());
        rule.yours(mine, YOURS_SHOWN).forEach(line -> text.append('\n').append(line));
        return MINI.deserialize(text.toString());
    }

    private Component keepers() {
        StringBuilder text = new StringBuilder("<gold><bold>Most homes</bold></gold>");
        List<ScreenLineRule.Keeper> top = rule.keepers(homes.all(), TOP);
        if (top.isEmpty()) {
            return MINI.deserialize(text + "\n<gray>No homes yet");
        }
        int place = 1;
        for (ScreenLineRule.Keeper keeper : top) {
            String name = nameOf.apply(keeper.owner());
            text.append('\n').append(rule.place(place)).append(place).append(". ")
                    .append(MINI.escapeTags(name == null || name.isBlank() ? "somebody" : name))
                    .append(" <dark_gray>· <white>").append(keeper.homes());
            place++;
        }
        return MINI.deserialize(text.toString());
    }

    private Component worlds() {
        StringBuilder text = new StringBuilder("<gold><bold>Homes by world</bold></gold>");
        List<ScreenLineRule.Crowd> top = rule.worlds(homes.all(), TOP);
        if (top.isEmpty()) {
            return MINI.deserialize(text + "\n<gray>No homes yet");
        }
        int place = 1;
        for (ScreenLineRule.Crowd crowd : top) {
            text.append('\n').append(rule.place(place)).append(place).append(". ")
                    .append(MINI.escapeTags(crowd.world()))
                    .append(" <dark_gray>· <white>").append(crowd.homes());
            place++;
        }
        return MINI.deserialize(text.toString());
    }
}
