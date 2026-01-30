package com.daou.dop.gapps.plugin.ms365.calendar.dto;

/**
 * Microsoft Graph /me 응답 DTO
 */
public record UserProfile(
        String id,
        String displayName,
        String mail,
        String userPrincipalName,
        String jobTitle,
        String officeLocation
) {}
