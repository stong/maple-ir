class UndefinedBehaviorException extends RuntimeException {
    public UndefinedBehaviorException(String msg) {
        super(msg);
    }
}

public class UbsanHelper {
    // Int
    public static int checked_add(int x, int y) {
        if (y > 0 && x > Integer.MAX_VALUE - y) {
            throw new UndefinedBehaviorException("addition of " + x + " and " + y + " would overflow int32");
        }
        if (y < 0 && x < Integer.MIN_VALUE - y) {
            throw new UndefinedBehaviorException("addition of " + x + " and " + y + " would underflow int32");
        }
        return x + y;
    }

    public static int checked_sub(int x, int y) {
        if (y < 0 && x > Integer.MAX_VALUE + y) {
            throw new UndefinedBehaviorException("subtraction of " + x + " and " + y + " would overflow int32");
        }
        if (y > 0 && x < Integer.MIN_VALUE + y) {
            throw new UndefinedBehaviorException("subtraction of " + x + " and " + y + " would underflow int32");
        }
        return x - y;
    }

    public static int checked_mul(int x, int y) {
        if (x == -1 && y == Integer.MIN_VALUE) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int32");
        }
        if (y == -1 && x == Integer.MIN_VALUE) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int32");
        }
        if (y != 0 && x > Integer.MAX_VALUE / y) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int32");
        }
        if (y != 0 && x < Integer.MIN_VALUE / y) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would underflow int32");
        }
        return x * y;
    }

    // Short
    public static short checked_add(short x, short y) {
        if (y > 0 && x > Short.MAX_VALUE - y) {
            throw new UndefinedBehaviorException("addition of " + x + " and " + y + " would overflow int16");
        }
        if (y < 0 && x < Short.MIN_VALUE - y) {
            throw new UndefinedBehaviorException("addition of " + x + " and " + y + " would underflow int16");
        }
        return (short)(x + y);
    }

    public static short checked_sub(short x, short y) {
        if (y < 0 && x > Short.MAX_VALUE + y) {
            throw new UndefinedBehaviorException("subtraction of " + x + " and " + y + " would overflow int16");
        }
        if (y > 0 && x < Short.MIN_VALUE + y) {
            throw new UndefinedBehaviorException("subtraction of " + x + " and " + y + " would underflow int16");
        }
        return (short)(x - y);
    }

    public static short checked_mul(short x, short y) {
        if (x == -1 && y == Short.MIN_VALUE) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int16");
        }
        if (y == -1 && x == Short.MIN_VALUE) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int16");
        }
        if (y != 0 && x > Short.MAX_VALUE / y) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int16");
        }
        if (y != 0 && x < Short.MIN_VALUE / y) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would underflow int16");
        }
        return (short)(x * y);
    }

    // Long
        public static long checked_add(long x, long y) {
        if (y > 0 && x > Long.MAX_VALUE - y) {
            throw new UndefinedBehaviorException("addition of " + x + " and " + y + " would overflow int64");
        }
        if (y < 0 && x < Long.MIN_VALUE - y) {
            throw new UndefinedBehaviorException("addition of " + x + " and " + y + " would underflow int64");
        }
        return x + y;
    }

    public static long checked_sub(long x, long y) {
        if (y < 0 && x > Long.MAX_VALUE + y) {
            throw new UndefinedBehaviorException("subtraction of " + x + " and " + y + " would overflow int64");
        }
        if (y > 0 && x < Long.MIN_VALUE + y) {
            throw new UndefinedBehaviorException("subtraction of " + x + " and " + y + " would underflow int64");
        }
        return x - y;
    }

    public static long checked_mul(long x, long y) {
        if (x == -1 && y == Long.MIN_VALUE) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int64");
        }
        if (y == -1 && x == Long.MIN_VALUE) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int64");
        }
        if (y != 0 && x > Long.MAX_VALUE / y) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would overflow int64");
        }
        if (y != 0 && x < Short.MIN_VALUE / y) {
            throw new UndefinedBehaviorException("multiplication of " + x + " and " + y + " would underflow int64");
        }
        return x * y;
    }
}