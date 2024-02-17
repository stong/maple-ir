class MemeIn {
    static int ONE = 1;

    public static void main(String[] args) {
        // int

        try {
            testIntOverflow();
            System.out.println("[failure] overflow not caught");
        } catch (UndefinedBehaviorException e) {
            System.out.println("[ok] " + e);
        }

        testIntNoOverflow();
        System.out.println("[ok]");

        // long

        try {
            testLongOverflow();
            System.out.println("[failure] overflow not caught");
        } catch (UndefinedBehaviorException e) {
            System.out.println("[ok] " + e);
        }

        testLongNoOverflow();
        System.out.println("[ok]");

        // short

        try {
            testShortOverflow();
            System.out.println("[failure] overflow not caught");
        } catch (UndefinedBehaviorException e) {
            System.out.println("[ok] " + e);
        }

        testShortNoOverflow();
        System.out.println("[ok]");
    }

    static int testIntOverflow() {
        int x = 2147483647;
        int y = ONE; // avoid constant expression to avoid getting optimized out
        return x + y; // should oveflow
    }

    static int testIntNoOverflow() {
        int x = 1234;
        int y = ONE; // avoid constant expression to avoid getting optimized out
        return x + y; // should be ok
    }

    static long testLongOverflow() {
        long x = Long.MAX_VALUE;
        long y = ONE; // avoid constant expression to avoid getting optimized out
        return x + y; // should oveflow
    }

    static long testLongNoOverflow() {
        long x = 1234;
        long y = ONE; // avoid constant expression to avoid getting optimized out
        return x + y; // should be ok
    }

    static int testShortOverflow() {
        short x = Short.MAX_VALUE;
        short y = (short)ONE; // avoid constant expression to avoid getting optimized out
        return x + y; // should oveflow
    }

    static int testShortNoOverflow() {
        short x = 1234;
        short y = (short)ONE; // avoid constant expression to avoid getting optimized out
        return x + y; // should be ok
    }
}
