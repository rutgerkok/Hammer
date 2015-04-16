package nl.rutgerkok.hammer.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents the progress of some operation.
 *
 */
public abstract class Progress {

    /**
     * A progress indicator based on units of work.
     *
     * @see Progress#ofUnits(int)
     */
    public static class UnitsProgress extends Progress {

        private int currentUnits;
        private int totalUnits;

        private UnitsProgress(int totalUnits) {
            this.currentUnits = 0;
            this.totalUnits = totalUnits;
        }

        @Override
        public double getFraction() {
            return ((double) currentUnits) / totalUnits;
        }

        /**
         * Updates this instance to indicate that one more unit of work is done.
         * If the amount of current units is about to get above the amount of
         * total units, the amount of total units is increased.
         */
        public void increment() {
            currentUnits++;
            totalUnits = Math.max(currentUnits, totalUnits);
            notifyListeners();
        }

    }

    private static final Progress COMPLETE = new Progress() {
        @Override
        public double getFraction() {
            return 1.0;
        }
    };

    /**
     * Gets a progress object that marks the operation as complete.
     *
     * @return The progress object.
     */
    public static final Progress complete() {
        return COMPLETE;
    }

    /**
     * Gets a progress indicator for processes split up into units.
     *
     * @param totalUnits
     *            The total amount of units.
     * @return The progress indicator.
     * @throws IllegalArgumentException
     *             If totalUnits is negative or zero.
     * @see UnitsProgress
     * @see UnitsProgress#increment()
     */
    public static final UnitsProgress ofUnits(int totalUnits) {
        if (totalUnits <= 0) {
            throw new IllegalArgumentException("totalUnits must be positive, was " + totalUnits);
        }
        return new UnitsProgress(totalUnits);
    }

    private List<Runnable> listeners = new CopyOnWriteArrayList<>();

    /**
     * Adds a new listener to this progress indicator.
     *
     * @param listener
     *            The listener.
     */
    public final void addListener(Runnable listener) {
        this.listeners.add(listener);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Progress)) {
            return false;
        }
        Progress other = (Progress) obj;
        if (Double.doubleToLongBits(getFraction()) != Double.doubleToLongBits(other.getFraction())) {
            return false;
        }
        return true;
    }

    /**
     * Gets the progress fraction, from 0 to 1, inclusive.
     *
     * @return The fraction.
     */
    public abstract double getFraction();

    /**
     * Gets the progress percentage as an int.
     *
     * @return The progress percentage.
     */
    public int getIntPercentage() {
        double fraction = getFraction();
        if (fraction >= 1) {
            return 100;
        }
        return (int) (fraction * 100.0);
    }

    /**
     * Gets the progress percentage.
     *
     * @return The percentage.
     */
    public double getPercentage() {
        return getFraction() * 100.0;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(getFraction());
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Subclasses must call this any time the progress is updated.
     */
    protected final void notifyListeners() {
        for (Runnable runnable : listeners) {
            runnable.run();
        }
    }

}
