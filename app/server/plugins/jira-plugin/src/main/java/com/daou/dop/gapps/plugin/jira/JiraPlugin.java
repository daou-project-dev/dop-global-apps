package com.daou.dop.gapps.plugin.jira;

import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jira 플러그인
 */
public class JiraPlugin extends Plugin {

    private static final Logger log = LoggerFactory.getLogger(JiraPlugin.class);

    @Override
    public void start() {
        log.info("Jira plugin started");
    }

    @Override
    public void stop() {
        log.info("Jira plugin stopped");
    }
}
