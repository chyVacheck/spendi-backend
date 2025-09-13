package com.spendi.core.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.bson.BsonDateTime;
import java.util.Date;

public class InstantUtilsTest {

    /**
     * ? getInstantOrNull
     */

    @Test
    void getInstantOrNull_shouldReturnNullForNullInput() {
        assertNull(InstantUtils.getInstantOrNull(null));
    }

    @Test
    void getInstantOrNull_shouldReturnInstantForInstantInput() {
        Instant now = Instant.now();
        assertEquals(now, InstantUtils.getInstantOrNull(now));
    }

    @Test
    void getInstantOrNull_shouldReturnInstantForDateInput() {
        Date date = new Date();
        Instant expectedInstant = date.toInstant();
        assertEquals(expectedInstant, InstantUtils.getInstantOrNull(date));
    }

    @Test
    void getInstantOrNull_shouldReturnInstantForBsonDateTimeInput() {
        long epochMilli = System.currentTimeMillis();
        BsonDateTime bsonDateTime = new BsonDateTime(epochMilli);
        Instant expectedInstant = Instant.ofEpochMilli(epochMilli);
        assertEquals(expectedInstant, InstantUtils.getInstantOrNull(bsonDateTime));
    }

    @Test
    void getInstantOrNull_shouldReturnNullForOtherObjectInput() {
        Object obj = new Object();
        assertNull(InstantUtils.getInstantOrNull(obj));
    }

    @Test
    void getInstantOrNull_shouldReturnInstantForValidStringInput() {
        String instantString = "2023-01-01T12:00:00Z";
        Instant expectedInstant = Instant.parse(instantString);
        assertEquals(expectedInstant, InstantUtils.getInstantOrNull(instantString));
    }


    /**
     * ? get24HourTime
     */

    @Test
    void get24HourTime_withInstant_returnsFormattedTime() {
        Instant fixedInstant = Instant.parse("2023-10-27T10:30:45.123Z");
        String expected = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                .withZone(ZoneId.systemDefault())
                .format(fixedInstant);
        assertEquals(expected, InstantUtils.get24HourTime(fixedInstant));
    }

    @Test
    void get24HourTime_withAnotherInstant_returnsFormattedTime() {
        Instant anotherInstant = Instant.parse("2024-01-01T00:00:00.000Z");
        String expected = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                .withZone(ZoneId.systemDefault())
                .format(anotherInstant);
        assertEquals(expected, InstantUtils.get24HourTime(anotherInstant));
    }

    @Test
    void get24HourTime_noArgs_returnsCurrentFormattedTime() {
        // This test might be flaky due to time sensitivity, but we can check the format
        String time = InstantUtils.get24HourTime();
        assertNotNull(time);
        assertTrue(time.matches("\\d{2}:\\d{2}:\\d{2}.\\d{3}"));
    }

    /**
     * ? getCurrentStrictDateString
     */

    @Test
    void getCurrentStrictDateString_returnsFormattedDate() {
        String date = InstantUtils.getCurrentStrictDateString();
        assertNotNull(date);
        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void getCurrentHourString_returnsFormattedHour() {
        String hour = InstantUtils.getCurrentHourString();
        assertNotNull(hour);
        assertTrue(hour.matches("\\d{2}"));
    }
}