package de.raindancer.modules.homes.rules;

import de.raindancer.modules.homes.model.Home;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** What the homes' screens count, rank and say. */
public final class ScreenLineRule implements IHomeRule {

    public record Keeper(UUID owner, int homes) {
    }

    public record Crowd(String world, int homes) {
    }

    /** One line per home, up to {@code shown}, then how many more there are. */
    public List<String> yours(List<Home> homes, int shown) {
        List<String> lines = new ArrayList<>();
        for (Home home : homes.stream().limit(shown).toList()) {
            lines.add("<white>" + MiniMessage.miniMessage().escapeTags(home.name())
                    + " <dark_gray>· <gray>" + MiniMessage.miniMessage().escapeTags(home.world()));
        }
        if (homes.size() > shown) {
            lines.add("<gray>+" + (homes.size() - shown) + " more");
        }
        return lines;
    }

    public List<Keeper> keepers(Collection<Home> homes, int limit) {
        Map<UUID, Integer> counted = new HashMap<>();
        homes.stream().filter(home -> home.owner() != null).forEach(home -> counted.merge(home.owner(), 1, Integer::sum));
        return counted.entrySet().stream()
                .map(entry -> new Keeper(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(Keeper::homes).reversed()
                        .thenComparing(keeper -> String.valueOf(keeper.owner())))
                .limit(limit).toList();
    }

    public List<Crowd> worlds(Collection<Home> homes, int limit) {
        Map<String, Integer> counted = new HashMap<>();
        homes.forEach(home -> counted.merge(home.world(), 1, Integer::sum));
        return counted.entrySet().stream()
                .map(entry -> new Crowd(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(Crowd::homes).reversed().thenComparing(Crowd::world))
                .limit(limit).toList();
    }

    public String place(int place) {
        return switch (place) {
            case 1 -> "<gold>";
            case 2 -> "<gray>";
            case 3 -> "<#cd7f32>";
            default -> "<white>";
        };
    }

    @Override
    public String describe() {
        return "what the homes' screens count, rank and say";
    }
}
