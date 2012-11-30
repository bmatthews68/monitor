package com.btmatthews.utils.monitor;

/**
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public abstract class AbstractServer implements Server {
    /**
     * Invoked by the monitor to configure a server property.
     *
     * @param logger Used to log error messages.
     * @see com.btmatthews.utils.monitor.Server#configure(String, Object, com.btmatthews.utils.monitor.Logger)
     */
    public void configure(final String name, final Object value, final Logger logger) {
    }

    /**
     * Invoked by the monitor to launch the server.
     *
     * @param logger Used to log status and error messages.
     * @see com.btmatthews.utils.monitor.Server#start(com.btmatthews.utils.monitor.Logger)
     */
    public void start(final Logger logger) {
    }

    /**
     * Invoked by the monitor to pause the server.
     *
     * @param logger Used to log error messages.
     * @see com.btmatthews.utils.monitor.Server#pause(com.btmatthews.utils.monitor.Logger)
     */
    public void pause(final Logger logger) {
    }

    /**
     * Invoked by the monitor to resume the server.
     *
     * @param logger Used to log error messages.
     * @see com.btmatthews.utils.monitor.Server#resume(com.btmatthews.utils.monitor.Logger)
     */
    public void resume(final Logger logger) {
    }

    /**
     * Invoked by the monitor to halt the server.
     *
     * @param logger Used to log status and error messages.
     * @see com.btmatthews.utils.monitor.Server#start(com.btmatthews.utils.monitor.Logger)
     */
    public void stop(final Logger logger) {
    }
}
