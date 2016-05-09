package com.linuxgods.kreiger.intellij.idea.inspection.statics.singleton;

public class UtilityClass {
    public static boolean isFoo() {
        return UtilityClass.foo;
    }

    public static void setFoo(boolean foo) {
        UtilityClass.foo = foo;
    }

    static boolean foo = false;
    public final static String SOME_CONSTANT = "some constant";

    {

    }

    static {
        foo = true;
    }

    public static Runnable getRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                setFoo(true);
            }
        };
    }

    public class InnerClass {
        boolean bar = foo;

        public boolean isFoo() {
            bar = UtilityClass.isFoo();
            return bar;
        }

        public void setFoo(boolean foo) {
            bar = foo;
            UtilityClass.setFoo(bar);
        }

        public class InnerInnerClass {
            boolean bar = foo;

            public boolean isFoo() {
                bar = UtilityClass.isFoo();
                return bar;
            }

            public void setFoo(boolean foo) {
                bar = foo;
                UtilityClass.setFoo(bar);
            }

        }
    }

    public static class StaticInnerClass {
        boolean bar = foo;

        public boolean isFoo() {
            bar = UtilityClass.isFoo();
            return bar;
        }

        public void setFoo(boolean foo) {
            bar = foo;
            UtilityClass.setFoo(bar);
        }

        public class InnerStaticInnerClass {
            boolean bar = foo;

            public boolean isFoo() {
                bar = UtilityClass.isFoo();
                return bar;
            }

            public void setFoo(boolean foo) {
                bar = foo;
                UtilityClass.setFoo(bar);
            }
        }
    }

    public enum EnumClass {
        FOO(foo);

        EnumClass(boolean foo) {
            setFoo(foo);
        }
    }
}
