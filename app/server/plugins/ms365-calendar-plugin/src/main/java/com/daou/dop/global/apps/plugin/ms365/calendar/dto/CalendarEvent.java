package com.daou.dop.global.apps.plugin.ms365.calendar.dto;

import java.util.List;

/**
 * Microsoft Graph Calendar Event DTO
 */
public record CalendarEvent(
        String id,
        String subject,
        String bodyPreview,
        DateTimeTimeZone start,
        DateTimeTimeZone end,
        Location location,
        Boolean isAllDay,
        Boolean isCancelled,
        Boolean isOrganizer,
        String showAs,
        String importance,
        String sensitivity,
        Organizer organizer,
        List<Attendee> attendees,
        String webLink
) {
    public record DateTimeTimeZone(
            String dateTime,
            String timeZone
    ) {}

    public record Location(
            String displayName,
            String locationType,
            String uniqueId,
            String uniqueIdType
    ) {}

    public record Organizer(
            EmailAddress emailAddress
    ) {}

    public record Attendee(
            EmailAddress emailAddress,
            String type,
            ResponseStatus status
    ) {}

    public record EmailAddress(
            String name,
            String address
    ) {}

    public record ResponseStatus(
            String response,
            String time
    ) {}
}
