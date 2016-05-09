package com.linuxgods.kreiger.intellij.idea.inspection.statics.singleton;

public class UtilityClassUser2 {

    private static boolean bar;

    static {
        UtilityClassUser2.bar = UtilityClass.foo;
    }

    public boolean isFoo() {
        return UtilityClass.foo;
    }

    public void setFoo(boolean value) {
        UtilityClass.foo = value;
    }

}
