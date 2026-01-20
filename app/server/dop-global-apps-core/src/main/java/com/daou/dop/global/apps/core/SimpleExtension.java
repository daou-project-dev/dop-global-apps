package com.daou.dop.global.apps.core;

import org.pf4j.ExtensionPoint;

public interface SimpleExtension extends ExtensionPoint {
    String execute(String input);
}
