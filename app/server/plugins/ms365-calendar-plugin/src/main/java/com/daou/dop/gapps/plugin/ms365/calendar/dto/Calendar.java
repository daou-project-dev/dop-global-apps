package com.daou.dop.gapps.plugin.ms365.calendar.dto;

/**
 * Microsoft Graph Calendar DTO
 */
public record Calendar(
        String id,
        String name,
        String color,
        Boolean isDefaultCalendar,
        Boolean canEdit,
        Boolean canShare,
        Boolean canViewPrivateItems,
        Owner owner
) {
    public record Owner(
            String name,
            String address
    ) {}
}
