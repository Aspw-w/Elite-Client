package com.instrumentalist.elite.utils.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListValue extends SettingValue<String> {
    public final String[] values;

    private int currentIndex;

    public ListValue(String name, String[] values, String value, DisplayableCondition displayable) {
        super(name, value, displayable);
        this.values = values;
        set(value);
    }

    public ListValue(String name, String[] values, String value) {
        this(name, values, value, () -> true);
    }

    public boolean contains(String string) {
        for (String s : values) {
            if (s.equalsIgnoreCase(string)) return true;
        }
        return false;
    }

    @Override
    protected void changeValue(String value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(value)) {
                super.changeValue(values[i]);
                this.currentIndex = i;
                break;
            }
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setByIndex(int index) {
        if (index >= 0 && index < values.length) {
            this.currentIndex = index;
            changeValue(values[index]);
        }
    }

    public void nextValue() {
        int index = (currentIndex + 1) % values.length;
        setByIndex(index);
    }

    public void previousValue() {
        int index = (currentIndex - 1 + values.length) % values.length;
        setByIndex(index);
    }
}