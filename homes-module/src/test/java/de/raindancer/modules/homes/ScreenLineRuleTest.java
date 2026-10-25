package de.raindancer.modules.homes;

import de.raindancer.core.world.poi.Poi;
import de.raindancer.modules.homes.model.Home;
import de.raindancer.modules.homes.rules.ScreenLineRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("What the homes' screens say")
class ScreenLineRuleTest {

    private final ScreenLineRule rule = new ScreenLineRule();
    private final UUID ann = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    private Home home(UUID owner, String name, String world) {
        return new Home(Poi.builder(name, world, 0, 64, 0).kind("home").owner(owner).build());
    }

    @Test
    @DisplayName("your homes read name and world, and the ones that do not fit are counted")
    void yours() {
        List<Home> homes = List.of(home(ann, "base", "world"), home(ann, "farm", "world_nether"),
                home(ann, "mine", "world"));

        assertThat(rule.yours(homes, 2)).containsExactly(
                "<white>base <dark_gray>· <gray>world",
                "<white>farm <dark_gray>· <gray>world_nether",
                "<gray>+1 more");
        assertThat(rule.yours(homes, 3)).hasSize(3).doesNotContain("<gray>+0 more");
        assertThat(rule.yours(List.of(), 3)).isEmpty();
    }

    @Test
    @DisplayName("tags in a home's name cannot style the screen")
    void escaped() {
        assertThat(rule.yours(List.of(home(ann, "<red>x", "world")), 3).get(0)).startsWith("<white>\\<red>x");
    }

    @Test
    @DisplayName("keepers are ranked by how many homes they have")
    void keepers() {
        List<ScreenLineRule.Keeper> ranked = rule.keepers(List.of(
                home(ann, "a", "w"), home(bob, "a", "w"), home(bob, "b", "w"), home(bob, "c", "w")), 5);

        assertThat(ranked).extracting(ScreenLineRule.Keeper::owner).containsExactly(bob, ann);
        assertThat(ranked.get(0).homes()).isEqualTo(3);
        assertThat(rule.keepers(List.of(home(ann, "a", "w"), home(bob, "a", "w")), 1)).hasSize(1);
        assertThat(rule.keepers(List.of(), 5)).isEmpty();
    }

    @Test
    @DisplayName("worlds are ranked by how many homes are in them")
    void worlds() {
        List<ScreenLineRule.Crowd> ranked = rule.worlds(List.of(
                home(ann, "a", "nether"), home(ann, "b", "world"), home(bob, "a", "world")), 5);

        assertThat(ranked).extracting(ScreenLineRule.Crowd::world).containsExactly("world", "nether");
        assertThat(ranked.get(0).homes()).isEqualTo(2);
    }

    @Test
    @DisplayName("places are coloured gold, grey, bronze, then white")
    void colour() {
        assertThat(rule.place(1)).isEqualTo("<gold>");
        assertThat(rule.place(2)).isEqualTo("<gray>");
        assertThat(rule.place(3)).isEqualTo("<#cd7f32>");
        assertThat(rule.place(9)).isEqualTo("<white>");
    }
}
