/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nemanja Zbiljić
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.zbiljic.shiro.cache.infinispan;

import org.apache.shiro.ShiroException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.io.ResourceUtils;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.api.BasicCacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Shiro {@code CacheManager} implementation utilizing the Infinispan data grid for all cache
 * functionality.
 *
 * This class can {@link #setCacheManager(org.infinispan.manager.EmbeddedCacheManager) accept} a
 * manually configured {@link org.infinispan.manager.DefaultCacheManager DefaultCacheManager}
 * instance, or an {@code infinispan.xml} path location can be specified instead and one will be
 * constructed. If neither are specified, Shiro's failsafe <code><a href="./infinispan.xml">infinispan.xml</a>}
 * file will be used by default.
 *
 * This implementation requires Infinispan 6.0.0 and above.
 *
 * Please see the <a href="http://infinispan.org/" target="_top">Infinispan - Open Source Data Grids
 * website</a> for their documentation.
 *
 * @author Nemanja Zbiljic
 * @see <a href="http://www.jboss.org/infinispan/" target="_top">The Infinispan - Open Source Data
 * Grids website</a>
 */
public class InfinispanCacheManager implements CacheManager, Initializable, Destroyable {

    /**
     * This class's private log instance.
     */
    private static final Logger log = LoggerFactory.getLogger(InfinispanCacheManager.class);

    /**
     * The Infinispan cache container used to obtain a {@link org.infinispan.commons.api.BasicCache}.
     */
    protected BasicCacheContainer cacheContainer;

    /**
     * The Infinispan cache manager used by this implementation to create caches if no {@code
     * cacheContainer} is specified.
     */
    protected EmbeddedCacheManager manager;

    /**
     * Indicates if the EmbeddedCacheManager instance was implicitly/automatically created by this
     * instance, indicating that it should be automatically cleaned up as well on shutdown.
     */
    private boolean cacheManagerImplicitlyCreated = false;

    /**
     * Classpath file location of the Infinispan EmbeddedCacheManager config file.
     */
    private String cacheManagerConfigFile = "classpath:com/github/zbiljic/shiro/cache/infinispan/infinispan.xml";

    /**
     * Default no argument constructor
     */
    public InfinispanCacheManager() {
    }

    /**
     * Returns the Infinispan {@link org.infinispan.commons.api.BasicCacheContainer} instance.
     *
     * @return the Infinispan {@link org.infinispan.commons.api.BasicCacheContainer} instance.
     */
    public BasicCacheContainer getCacheContainer() {
        return cacheContainer;
    }

    /**
     * Sets the Infinispan {@link org.infinispan.commons.api.BasicCacheContainer} instance.
     *
     * @param cacheContainer the Infinispan {@link org.infinispan.commons.api.BasicCacheContainer}
     *                       instance.
     */
    public void setCacheContainer(BasicCacheContainer cacheContainer) {
        this.cacheContainer = cacheContainer;
    }

    /**
     * Returns the wrapped Infinispan {@link org.infinispan.manager.EmbeddedCacheManager} instance.
     *
     * @return the wrapped Infinispan {@link org.infinispan.manager.EmbeddedCacheManager} instance.
     */
    public EmbeddedCacheManager getCacheManager() {
        return this.manager;
    }

    /**
     * Sets the wrapped Infinispan {@link org.infinispan.manager.EmbeddedCacheManager} instance.
     *
     * @param manager the wrapped Infinispan {@link org.infinispan.manager.EmbeddedCacheManager}
     *                instance.
     */
    public void setCacheManager(EmbeddedCacheManager manager) {
        this.manager = manager;
    }

    /**
     * Returns the resource location of the config file used to initialize a new Infinispan
     * EmbeddedCacheManager instance.  The string can be any resource path supported by the {@link
     * org.apache.shiro.io.ResourceUtils#getInputStreamForPath(String)} call.
     *
     * This property is ignored if the EmbeddedCacheManager instance is injected directly - that is,
     * it is only used to lazily create a EmbeddedCacheManager if one is not already provided.
     *
     * @return the resource location of the config file used to initialize the wrapped Infinispan
     * EmbeddedCacheManager instance.
     */
    public String getCacheManagerConfigFile() {
        return this.cacheManagerConfigFile;
    }

    /**
     * Sets the resource location of the config file used to initialize the wrapped Infinispan
     * EmbeddedCacheManager instance.  The string can be any resource path supported by the {@link
     * org.apache.shiro.io.ResourceUtils#getInputStreamForPath(String)} call.
     *
     * This property is ignored if the EmbeddedCacheManager instance is injected directly - that is,
     * it is only used to lazily create a EmbeddedCacheManager if one is not already provided.
     *
     * @param classpathLocation resource location of the config file used to create the wrapped
     *                          Infinispan EmbeddedCacheManager instance.
     */
    public void setCacheManagerConfigFile(String classpathLocation) {
        this.cacheManagerConfigFile = classpathLocation;
    }

    /**
     * Acquires the InputStream for the Infinispan configuration file using {@link
     * ResourceUtils#getInputStreamForPath(String) ResourceUtils.getInputStreamForPath} with the
     * path returned from {@link #getCacheManagerConfigFile() getCacheManagerConfigFile()}.
     *
     * @return the InputStream for the Infinispan configuration file.
     */
    protected InputStream getCacheManagerConfigFileInputStream() {
        String configFile = getCacheManagerConfigFile();
        try {
            return ResourceUtils.getInputStreamForPath(configFile);
        } catch (IOException e) {
            throw new ConfigurationException("Unable to obtain input stream for cacheManagerConfigFile [" +
                    configFile + "]", e);
        }
    }

    /**
     * Loads an existing InfinispanCache from the cache manager, or starts a new cache if one is not
     * found.
     *
     * @param name the name of the cache to load/create.
     */
    @Override
    public final <K, V> Cache<K, V> getCache(String name) throws CacheException {

        if (log.isTraceEnabled()) {
            log.trace("Acquiring Infinispan instance named [" + name + "]");
        }

        try {
            this.ensureCacheContainer();
            BasicCache<K, V> cache;

            if (this.cacheManagerImplicitlyCreated) {
                if (!this.manager.getCacheNames().contains(name)) {
                    if (log.isInfoEnabled()) {
                        log.info("Cache with name '{}' does not yet exist.  Creating now.", name);
                    }

                    cache = this.manager.getCache(name, true);

                    if (log.isInfoEnabled()) {
                        log.info("Added InfinispanCache named [" + name + "]");
                    }
                } else {
                    this.manager.startCaches(name);
                    cache = this.manager.getCache(name, false);

                    if (log.isInfoEnabled()) {
                        log.info("Using existing InfinispanCache named [" + cache.getName() + "]");
                    }
                }
            } else {
                cache = this.cacheContainer.getCache(name);

                if (log.isInfoEnabled()) {
                    log.info("Using InfinispanCache named [" + cache.getName() + "]");
                }
            }

            return new InfinispanCache<K, V>(cache);
        } catch (CacheException e) {
            throw new CacheException(e);
        }
    }

    /**
     * Initializes this instance.
     *
     * If a {@link #setCacheContainer(BasicCacheContainer)} or a {@link
     * #setCacheManager(EmbeddedCacheManager)} has been explicitly set (e.g. via Dependency
     * Injection or programatically) prior to calling this method, this method does nothing.
     *
     * However, if no {@code CacheManager} has been set, the default Infinispan will be initialized,
     * where Infinispan will look for an {@code infinispan.xml} file at the root of the classpath.
     * If one is not found, Infinispan will use its own failsafe configuration file.
     *
     * Because Shiro cannot use the failsafe defaults (fail-safe expunges cached objects after 2
     * minutes, something not desirable for Shiro sessions), this class manages an internal default
     * configuration for this case.
     *
     * @throws org.apache.shiro.cache.CacheException if there are any CacheExceptions thrown by
     *                                               Infinispan.
     * @see org.infinispan.manager.DefaultCacheManager#createCache(String)
     */
    @Override
    public final void init() throws ShiroException {
        ensureCacheContainer();
    }

    private synchronized void ensureCacheContainer() {
        try {
            if (this.cacheContainer == null) {
                this.cacheContainer = ensureCacheManager();
            }
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    private synchronized EmbeddedCacheManager ensureCacheManager() {
        try {
            if (this.manager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("cacheManager property not set.  Constructing DefaultCacheManager instance... ");
                }

                try {
                    Class.forName("org.infinispan.manager.DefaultCacheManager");
                    this.manager = new DefaultCacheManager(getCacheManagerConfigFileInputStream());
                } catch (ClassNotFoundException e) {
                    throw new CacheException(e);
                }

                if (log.isTraceEnabled()) {
                    log.trace("instantiated Infinispan DefaultCacheManager instance.");
                }

                cacheManagerImplicitlyCreated = true;

                if (log.isDebugEnabled()) {
                    log.debug("implicit DefaultCacheManager created successfully.");
                }
            }
            return this.manager;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /**
     * Shuts-down the wrapped Infinispan EmbeddedCacheManager <b>only if implicitly created</b>.
     *
     * If another component injected a non-null EmbeddedCacheManager into this instance before
     * calling {@link #init() init}, this instance expects that same component to also destroy the
     * EmbeddedCacheManager instance, and it will not attempt to do so.
     */
    @Override
    public void destroy() {
        if (cacheManagerImplicitlyCreated) {
            try {
                EmbeddedCacheManager cacheMgr = getCacheManager();
                cacheMgr.stop();
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Unable to cleanly shutdown implicitly created EmbeddedCacheManager instance.  " +
                            "Ignoring (shutting down)...");
                }
            }
            cacheManagerImplicitlyCreated = false;
        }
    }
}
