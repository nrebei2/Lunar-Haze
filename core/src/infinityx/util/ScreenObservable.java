package infinityx.util;

/**
 * Simple Observer pattern for use with many-screen to one-game communication
 */
public abstract class ScreenObservable {
    /** Should always be a Game class */
    protected ScreenObserver observer;

    /**
     * Sets the ScreenObserver for this object.
     *
     * The ScreenObserver will respond to any requests through exitScreen().
     */
    public void setObserver(ScreenObserver observer) {
        this.observer = observer;
    }
}
