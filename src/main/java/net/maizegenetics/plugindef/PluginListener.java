/*
 * PluginListener.java
 */

package net.maizegenetics.plugindef;



/**
 *
 * @author terryc
 */
public interface PluginListener {
    
    /**
     * Returns Tassel data set after complete.
     *
     * @param event event
     */
    public void dataSetReturned (PluginEvent event);
    
    
    /**
     * Returns progress of execution.
     *
     * @param event event
     */
    public void progress (PluginEvent event);
    
    
    /**
     * Notifies that a non-interactive plugin failed. The default is to do nothing.
     *
     * @param plugin plugin that failed
     * @param cause exception that caused the failure
     */
    public default void pluginFailed (Plugin plugin, Exception cause) {
        // do nothing
    }
    
}
