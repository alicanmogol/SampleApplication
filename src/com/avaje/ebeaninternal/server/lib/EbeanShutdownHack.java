package com.avaje.ebeaninternal.server.lib;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebeaninternal.server.lib.sql.DataSourceGlobalManager;
import com.avaje.ebeaninternal.server.lib.thread.ThreadPoolManager;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exists to work around this issue:
 * <p/>
 * https://github.com/rbygrave/avaje-ebeanorm-server/issues/4
 * <p/>
 * As luck would have it, Ebean tends to use package private fields, which allows us to do hacks like this without
 * using reflection.
 *
 * @author jroper
 */
public class EbeanShutdownHack {
    private static final Logger logger = Logger.getLogger(EbeanShutdownHack.class.getName());

    public static void shutdownAllActiveEbeanServers(EbeanServer ebeanServer) {
        ebeanServer.getServerCacheManager().clearAll();
        // First remove all the background thread runnables.  To do this, we need to synchronize on its monitor field,
        // which is private
        Object monitor = getPrivateField(BackgroundThread.class, "monitor",
                getPrivateField(BackgroundThread.class, "me", null));
        synchronized (monitor) {
            Iterator<BackgroundRunnable> runnables = BackgroundThread.runnables();
            while (runnables.hasNext()) {
                runnables.next();
                runnables.remove();
            }
        }
        // Now shut down the ebean servers
        try {
            synchronized (ShutdownManager.runnables) {
                for (Runnable runnable : ShutdownManager.runnables) {
                    try {
                        runnable.run();
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }

                }
                ShutdownManager.runnables.clear();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        ThreadPoolManager.shutdown();
        DataSourceGlobalManager.shutdown();
        ebeanServer = null;

    }

    private static Object getPrivateField(Class clazz, String name, Object object) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            throw new RuntimeException("Error getting private filed " + name + " on class " + clazz + " from object " + object, e);
        }
    }
}