package org.goldrenard.jb.utils;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.LenientChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervalUtils {

    private static final Logger log = LoggerFactory.getLogger(IntervalUtils.class);

    public static int getHoursBetween(final String date1, final String date2, String format) {
        try {
            final DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));
            return Hours.hoursBetween(
                    fmt.parseDateTime(date1),
                    fmt.parseDateTime(date2)
            ).getHours();
        } catch (Exception e) {
            log.error("getHoursBetween(date1=[{}], date2=[{}], format=[{}]) caused error", date1, date2, format, e);
            return 0;
        }
    }

    public static int getYearsBetween(final String date1, final String date2, String format) {
        try {
            final DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));
            return Years.yearsBetween(
                    fmt.parseDateTime(date1),
                    fmt.parseDateTime(date2)
            ).getYears();
        } catch (Exception e) {
            log.error("getYearsBetween(date1=[{}], date2=[{}], format=[{}]) caused error", date1, date2, format, e);
            return 0;
        }
    }

    public static int getMonthsBetween(final String date1, final String date2, String format) {
        try {
            final DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));
            return Months.monthsBetween(
                    fmt.parseDateTime(date1),
                    fmt.parseDateTime(date2)
            ).getMonths();
        } catch (Exception e) {
            log.error("getMonthsBetween(date1=[{}], date2=[{}], format=[{}]) caused error", date1, date2, format, e);
            return 0;
        }
    }

    public static int getDaysBetween(final String date1, final String date2, String format) {
        try {
            final DateTimeFormatter fmt =
                    DateTimeFormat
                            .forPattern(format)
                            .withChronology(
                                    LenientChronology.getInstance(
                                            GregorianChronology.getInstance()));
            return Days.daysBetween(
                    fmt.parseDateTime(date1),
                    fmt.parseDateTime(date2)
            ).getDays();
        } catch (Exception e) {
            log.error("getDaysBetween(date1=[{}], date2=[{}], format=[{}]) caused error", date1, date2, format, e);
            return 0;
        }
    }
}
