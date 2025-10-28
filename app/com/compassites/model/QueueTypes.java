package com.compassites.model;

public enum QueueTypes {

    WAITLIST("1"),
    FLIGHT_NOT_OPERATING("1"),
    FLIGHT_CLOSED("1"),
    WAITLIST_CONFIRMED("2"),
    SCHEDULE_CHANGE("7"),
    EXPIRE_TIME_LIMIT("12");

    private final String itemNumber;

    QueueTypes(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getItemNumber() {
        return itemNumber;
    }

}
