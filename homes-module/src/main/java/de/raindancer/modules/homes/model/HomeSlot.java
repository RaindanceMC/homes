package de.raindancer.modules.homes.model;

import java.util.ArrayList;
import java.util.List;

/**
 * One place on the home list: a home, or a slot still free for one — so a player sees how many they have left.
 *
 * @param home null for a free slot
 */
public record HomeSlot<H>(H home) {

    /** Free slots drawn at most; beyond that the list would be pages of cobwebs. */
    public static final int MOST_FREE_SHOWN = 27;
    /** A limit this high reads as "unlimited" ({@code ∞}): one free slot says it well enough. */
    private static final int UNLIMITED = 100_000;

    public boolean isFree() {
        return home == null;
    }

    public static <H> List<HomeSlot<H>> list(List<H> homes, int limit) {
        List<HomeSlot<H>> slots = new ArrayList<>(homes.size() + 1);
        homes.forEach(home -> slots.add(new HomeSlot<>(home)));
        int free = limit >= UNLIMITED ? 1 : Math.min(MOST_FREE_SHOWN, Math.max(0, limit - homes.size()));
        for (int i = 0; i < free; i++) {
            slots.add(new HomeSlot<>(null));
        }
        return slots;
    }
}
