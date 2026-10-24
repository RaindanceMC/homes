package de.raindancer.modules.homes;

import de.raindancer.core.ui.help.HelpLint;
import de.raindancer.modules.api.ModuleCommand;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class GuideTest {

    @Test
    void theGuideIsComplete() {
        assertThat(HelpLint.of(Path.of("src/main/resources/rains-help"))
                .module("homes")
                .settings(HomeSettings.class, HomeSettings.DEFAULTS)
                .commands(new HomeModule().commands().stream().map(ModuleCommand::name).toList())
                .problems()).isEmpty();
    }
}
