package com.instrumentalist.elite.utils.value;

public class KeyBindValue extends SettingValue<Integer> {
    private ChangeListener changeListener;

    public KeyBindValue(String name, int value, DisplayableCondition displayable) {
        super(name, value, displayable);
    }

    public KeyBindValue(String name, int value) {
        super(name, value, () -> true);
    }

    @Override
    protected void onChanged(Integer oldValue, Integer newValue) {
        super.onChanged(oldValue, newValue);
        if (changeListener != null)
            changeListener.onChange(newValue);
    }

    public interface ChangeListener {
        void onChange(int newValue);
    }
}