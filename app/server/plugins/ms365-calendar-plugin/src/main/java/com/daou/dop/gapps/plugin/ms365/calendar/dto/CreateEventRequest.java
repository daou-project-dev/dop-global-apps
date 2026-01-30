package com.daou.dop.gapps.plugin.ms365.calendar.dto;

import java.util.List;

/**
 * Microsoft Graph 이벤트 생성 요청 DTO
 */
public record CreateEventRequest(
        String subject,
        Body body,
        DateTimeTimeZone start,
        DateTimeTimeZone end,
        Location location,
        List<Attendee> attendees,
        Boolean isAllDay,
        Boolean isOnlineMeeting,
        String onlineMeetingProvider
) {
    public record Body(
            String contentType,
            String content
    ) {}

    public record DateTimeTimeZone(
            String dateTime,
            String timeZone
    ) {}

    public record Location(
            String displayName
    ) {}

    public record Attendee(
            EmailAddress emailAddress,
            String type
    ) {}

    public record EmailAddress(
            String address,
            String name
    ) {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String subject;
        private Body body;
        private DateTimeTimeZone start;
        private DateTimeTimeZone end;
        private Location location;
        private List<Attendee> attendees;
        private Boolean isAllDay;
        private Boolean isOnlineMeeting;
        private String onlineMeetingProvider;

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String contentType, String content) {
            this.body = new Body(contentType, content);
            return this;
        }

        public Builder start(String dateTime, String timeZone) {
            this.start = new DateTimeTimeZone(dateTime, timeZone);
            return this;
        }

        public Builder end(String dateTime, String timeZone) {
            this.end = new DateTimeTimeZone(dateTime, timeZone);
            return this;
        }

        public Builder location(String displayName) {
            this.location = new Location(displayName);
            return this;
        }

        public Builder attendees(List<Attendee> attendees) {
            this.attendees = attendees;
            return this;
        }

        public Builder isAllDay(Boolean isAllDay) {
            this.isAllDay = isAllDay;
            return this;
        }

        public Builder isOnlineMeeting(Boolean isOnlineMeeting) {
            this.isOnlineMeeting = isOnlineMeeting;
            return this;
        }

        public Builder onlineMeetingProvider(String onlineMeetingProvider) {
            this.onlineMeetingProvider = onlineMeetingProvider;
            return this;
        }

        public CreateEventRequest build() {
            return new CreateEventRequest(
                    subject, body, start, end, location,
                    attendees, isAllDay, isOnlineMeeting, onlineMeetingProvider
            );
        }
    }
}
