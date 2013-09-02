package com.fererlab.map;

import com.fererlab.cache.Cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * acm 3/21/13
 */
public class CacheMap {

    public void readCacheMap(URL cacheMapFile) {
        InputStream in = null;
        try {
            Properties properties = new Properties();
            in = getClass().getResourceAsStream(cacheMapFile.getFile());
            properties.load(in);
            Cache.create(properties);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
