class MemeIn {
    static int ONE = 1;

    public static void main(String[] args) {
        try {
            testOverflow();
            assert false;
        } catch (UndefinedBehaviorException e) {
            System.out.println("[ok] " + e);
        }

        testNoOverflow();
        System.out.println("[ok]");
    }

    static int testOverflow() {
        int x = 2147483647;
        int y = ONE; // avoid constant expression to avoid getting optimized out
        return x + y; // should oveflow
    }

    static int testNoOverflow() {
        int x = 1234;
        int y = ONE; // avoid constant expression to avoid getting optimized out
        return x + y; // should be ok
    }
}
