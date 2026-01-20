package com.daou.dop.global.apps.plugin.slack;

import com.daou.dop.global.apps.core.SimpleExtension;
import org.pf4j.Extension;

@Extension
public class SlackExtension implements SimpleExtension {
    @Override
    public String execute(String input) {
        return "Slack: " + input;
    }
}
