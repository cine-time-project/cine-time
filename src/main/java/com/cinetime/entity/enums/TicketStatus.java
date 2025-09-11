package com.cinetime.entity.enums;

/**
 * Status values for a cinema ticket.
 * These will be stored as STRING in the database.
 */
public enum TicketStatus {
    RESERVED,   // Default: reserved but not paid yet
    PAID,       // Payment completed
    CANCELLED,  // Ticket cancelled
    USED;       // Ticket already used (entered the cinema)

    /** Helper: case-insensitive parsing (optional) */
    public static TicketStatus fromString(String value) {
        return TicketStatus.valueOf(value.toUpperCase());
    }
}