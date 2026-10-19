package de.raindancer.modules.homes.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HomeSlotTest {

    private static final List<String> TWO = List.of("base", "farm");

    @Test
    @DisplayName("the homes come first, then one free slot for every home still allowed")
    void freeAfterHomes() {
        List<HomeSlot<String>> slots = HomeSlot.list(TWO, 5);
        assertThat(slots).hasSize(5);
        assertThat(slots.subList(0, 2)).extracting(HomeSlot::home).containsExactly("base", "farm");
        assertThat(slots.subList(2, 5)).allMatch(HomeSlot::isFree);
    }

    @Test
    @DisplayName("no free slot when the limit is reached, or passed after the limit was lowered")
    void full() {
        assertThat(HomeSlot.list(TWO, 2)).noneMatch(HomeSlot::isFree);
        assertThat(HomeSlot.list(TWO, 1)).hasSize(2).noneMatch(HomeSlot::isFree);
        assertThat(HomeSlot.list(List.<String>of(), 0)).isEmpty();
    }

    @Test
    @DisplayName("an unlimited player sees one free slot, not millions")
    void unlimited() {
        assertThat(HomeSlot.list(TWO, Integer.MAX_VALUE)).hasSize(3);
        assertThat(HomeSlot.list(TWO, 2 + HomeSlot.MOST_FREE_SHOWN + 10))
                .filteredOn(HomeSlot::isFree).hasSize(HomeSlot.MOST_FREE_SHOWN);
    }
}
