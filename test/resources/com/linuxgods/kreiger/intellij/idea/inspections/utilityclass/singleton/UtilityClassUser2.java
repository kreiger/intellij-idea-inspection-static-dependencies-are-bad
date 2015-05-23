package com.linuxgods.kreiger.intellij.idea.inspections.utilityclass.singleton;

import static com.linuxgods.kreiger.intellij.idea.inspections.utilityclass.singleton.UtilityClass.foo;

public class UtilityClassUser2 {

    private static boolean bar;

    static {
        UtilityClassUser2.bar = foo;
    }

    public boolean isFoo() {
        return foo;
    }

    public void setFoo(boolean value) {
        foo = value;
    }

}
