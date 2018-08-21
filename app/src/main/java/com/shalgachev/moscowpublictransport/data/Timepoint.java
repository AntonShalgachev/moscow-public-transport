package com.shalgachev.moscowpublictransport.data;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import com.shalgachev.moscowpublictransport.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by anton on 4/8/2018.
 */
public class Timepoint {
    private static final Map<Color, Colors> COLORS_MAP;

    static
    {
        COLORS_MAP = new HashMap<>();
        COLORS_MAP.put(Color.RED, new Colors(R.color.timepoint_color_red_enabled, R.color.timepoint_color_red_disabled));
        COLORS_MAP.put(Color.GREEN, new Colors(R.color.timepoint_color_green_enabled, R.color.timepoint_color_green_disabled));
        COLORS_MAP.put(Color.BLUE, new Colors(R.color.timepoint_color_blue_enabled, R.color.timepoint_color_blue_disabled));
    }

    public enum Color
    {
        NONE,

        RED,
        GREEN,
        BLUE,
    }

    private static class Colors
    {
        Colors(@ColorRes int enabled, @ColorRes int disabled)
        {
            enabledColor = enabled;
            disabledColor = disabled;
        }

        @ColorRes int enabledColor;
        @ColorRes int disabledColor;
    };

    public Timepoint(int h, int m) {
        this.hour = h;
        this.minute = m;
    }
    public Timepoint(int h, int m, Color color, String note) {
        this.hour = h;
        this.minute = m;
        this.color = color;
        this.note = note;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%d:%d", hour, minute);
    }

    public long getId() {
        return hour * 60 + minute;
    }

    public int hour;
    public int minute;

    public Color color = null;
    public String note = null;

    // used in schedule activity to notify remaining time
    public long millisFromNow;
    public boolean isCountdownShown;
    public boolean isEnabled;

    public boolean isEnabled() {
        return isEnabled;
    }

    public int minutesFromNow() {
        long millis = millisFromNow;
        long coef = 1000 * 60;

        long minutes = millis / coef;
        if ((millis ^ coef) < 0 && (minutes * coef != millis)) {
            minutes--;
        }

        return (int) minutes;
    }

    public @ColorInt int getColor(Context context, boolean enabled)
    {
        if (color == null)
            throw new NullPointerException();
        if (!COLORS_MAP.containsKey(color))
            throw new IllegalArgumentException("No colors for " + color.name());

        Colors colors = COLORS_MAP.get(color);

        @ColorRes int color = enabled ? colors.enabledColor : colors.disabledColor;
        return context.getResources().getColor(color);
    }

    public @ColorInt int getColor(Context context)
    {
        return getColor(context, isEnabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Timepoint timepoint = (Timepoint) o;

        if (hour != timepoint.hour) return false;
        if (minute != timepoint.minute) return false;
        if (color != timepoint.color) return false;
        return note != null ? note.equals(timepoint.note) : timepoint.note == null;
    }

    @Override
    public int hashCode() {
        int result = hour;
        result = 31 * result + minute;
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        return result;
    }
}
