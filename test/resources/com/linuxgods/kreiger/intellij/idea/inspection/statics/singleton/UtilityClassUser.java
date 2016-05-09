package com.linuxgods.kreiger.intellij.idea.inspection.statics.singleton;

public class UtilityClassUser {

    public boolean isFoo() {
        return UtilityClass.foo;
    }

    public void setFoo(boolean value) {
        UtilityClass.foo = value;
    }

}
