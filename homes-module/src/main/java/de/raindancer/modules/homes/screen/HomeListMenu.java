package de.raindancer.modules.homes.screen;

import de.raindancer.core.social.economy.Fees;
import de.raindancer.core.social.economy.Money;
import de.raindancer.core.ui.menu.Icons;
import de.raindancer.core.ui.menu.Menu;
import de.raindancer.core.ui.menu.PaginatedMenu;
import de.raindancer.modules.homes.HomeServices;
import de.raindancer.modules.homes.model.Home;
import de.raindancer.modules.homes.model.HomeSlot;
import de.raindancer.modules.homes.service.HomeTravelService;
import de.raindancer.modules.homes.util.HomeIcons;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Every home this player has: what bare {@code /home} opens.
 *
 * <h2>Why bare {@code /home} is the list and not a guess</h2>
 * Because guessing is worse than asking. {@code /home} could reasonably mean "the one called home", and
 * on a server where somebody called theirs something else it would then be a command that always fails
 * for them. The list is the one answer that is right for everybody, and the home called {@code home} is
 * one click away on it.
 */
public final class HomeListMenu extends PaginatedMenu<HomeSlot<Home>> implements IHomeScreen {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private static final java.time.Duration TO_ANSWER = java.time.Duration.ofSeconds(60);

    private final HomeServices services;

    public HomeListMenu(HomeServices services, Player viewer, Menu parent) {
        super(viewer, services.brand(), parent);
        this.services = services;
    }

    @Override
    protected Component title() {
        // Never the brand: it is already prefixed, so "Homes" here would render as "Homes » Homes".
        return MINI.deserialize("<dark_gray>Your homes");
    }

    @Override
    public String breadcrumb() {
        return "Your homes";
    }

    @Override
    protected List<HomeSlot<Home>> entries() {
        return HomeSlot.list(services.homes().of(viewer.getUniqueId()), services.keeping().limitFor(viewer));
    }

    /**
     * Only seen with a limit of zero: anybody allowed a home sees their free slots as cobwebs instead.
     * The button does what the sentence says — buys a slot where this server sells them.
     */
    @Override
    protected ItemStack emptyIcon() {
        boolean sold = services.slots().whyNot(viewer.getUniqueId()).isEmpty();
        return Icons.of(Material.BARRIER, "<gray>No home slots",
                "<gray>You may not have a home right now.",
                sold ? "<yellow>Click<gray> to buy a slot." : "<gray>Staff can give you one.");
    }

    @Override
    protected void emptyAction(InventoryClickEvent event) {
        if (services.slots().whyNot(viewer.getUniqueId()).isEmpty()) {
            new SlotOfferScreen(services, viewer, this, false, null).open();
            return;
        }
        viewer.closeInventory();
        services.messages().send(viewer, "homes.switched-off");
    }

    @Override
    protected ItemStack icon(HomeSlot<Home> slot) {
        if (slot.isFree()) {
            return Icons.of(Material.COBWEB, "<gray>Free home slot",
                    "<gray>Room for one more home.",
                    "",
                    "<yellow>Click<gray> to set a home where you stand.");
        }
        Home home = slot.home();
        List<String> lore = new ArrayList<>();
        if (home.isReachable()) {
            lore.add("<dark_gray>" + home.world() + " " + home.coordinates());
            if (!home.isHere()) {
                lore.add("<aqua>On the server " + home.server() + ".");
            }
        } else {
            // Said in words as well as by the barrier the icon becomes: a home in a world that is
            // unloaded for maintenance is not a home to delete.
            lore.add("<red>Its world is not loaded right now.");
        }
        lore.add("");
        if (services.config().warmup() > 0) {
            lore.add("<gray>Click to go. Stand still for "
                    + services.config().warmup() + "s.");
        } else {
            lore.add("<gray>Click to go.");
        }
        Money price = Fees.quote(HomeTravelService.SOURCE, Fees.amount(services.config().teleportPrice()));
        if (price.isPositive()) {
            lore.add("<gray>Costs <white>" + Fees.format(price) + "<gray>, paid back if you do not get there.");
        }
        lore.add("<gray>Right click to rename it, change its");
        lore.add("<gray>block, or delete it.");

        return Icons.of(HomeIcons.materialFor(home), "<white>" + home.name(), lore);
    }

    /**
     * Left click goes, right click edits.
     *
     * <p>Right rather than shift-right, which is what the old plugin used for deleting outright — one
     * modifier away from going there, with no confirmation. Editing is the safe thing to put behind the
     * easier gesture, and the delete now lives behind a page of its own.
     */
    @Override
    protected void onClick(HomeSlot<Home> slot, InventoryClickEvent event) {
        if (slot.isFree()) {
            askForANewHome();
            return;
        }
        Home home = slot.home();
        if (event.isRightClick()) {
            new HomeEditMenu(services, viewer, this, home.name()).open();
            return;
        }
        // Closed first: going home can take a few seconds of standing still, and a window open over it
        // is a window the player has to shut before they can be seen to have moved.
        viewer.closeInventory();
        services.travelling().go(viewer, home);
    }

    /** A free slot clicked: a name in chat, then the home is set where they stand by then — as /sethome does. */
    private void askForANewHome() {
        viewer.closeInventory();
        boolean asking = services.core().prompts().ask(viewer.getUniqueId(), "homes", TO_ANSWER,
                answer -> {
                    if (answer != null && answer.equalsIgnoreCase("cancel")) {
                        services.messages().send(viewer, "homes.no-new-home");
                        open();
                        return;
                    }
                    services.keeping().set(viewer, answer).ifPresent(home -> open());
                },
                this::open);
        if (!asking) {
            services.messages().send(viewer, "homes.busy");
            open();
            return;
        }
        services.messages().send(viewer, "homes.ask-new-name");
    }

    /** The counter, which is the one thing on the page that is not a home. */
    @Override
    protected void decorate() {
        super.decorate();
        int have = services.homes().count(viewer.getUniqueId());
        toolbar(4, Icons.of(Material.RED_BED, "<white>Your homes",
                        "<gray>" + have + " of " + services.keeping().describeLimitFor(viewer)
                                + " used.",
                        "<dark_gray>A permission can raise that; nothing lowers it."),
                click -> {
                });
        UUID who = viewer.getUniqueId();
        java.util.Optional<String> whyNot = services.slots().whyNot(who);
        toolbar(5, whyNot.isEmpty(), Icons.of(Material.EMERALD, "<white>Buy a home slot",
                        services.slots().isOn() ? "<gray>One more home for <white>"
                                + services.slots().describeNextPrice(who) + "<gray>." : "<gray>One more home.",
                        "<gray>You have bought <white>" + services.slots().bought(who) + "<gray> so far.",
                        "<yellow>Click<gray> to buy one"),
                whyNot.orElse(""),
                click -> new SlotOfferScreen(services, viewer, this, false, null).open());
        if (services.mayOpenSettings(viewer)) {
            toolbar(7, Icons.of(Material.COMPARATOR, "<white>Server settings for homes",
                            "<gray>Limits, waits, buying home slots,", "<gray>the price of going home.",
                            "<yellow>Click<gray> to open"),
                    click -> new de.raindancer.core.data.settings.SettingsMenu(viewer, services.brand(),
                            services.core().chatFor(services.brand()), services.core().settingsNavigation(), "homes",
                            this).open());
        }
    }

    @Override
    protected List<String> helpLines() {
        // Core draws these as a book. Generated from the settings that are loaded, so the page cannot
        // come to describe a wait this server does not have.
        return services.messages().lines("homes.manual.using",
                        "warmup", services.config().warmup(),
                        "cooldown", services.config().cooldown(),
                        "limit", services.keeping().describeLimitFor(viewer))
                .stream().map(MINI::serialize).toList();
    }

    @Override
    public String describe() {
        return "every home this player has";
    }
}
