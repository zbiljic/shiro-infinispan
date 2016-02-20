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

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.util.CollectionUtils;
import org.infinispan.commons.api.BasicCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Shiro {@link org.apache.shiro.cache.Cache} implementation that wraps an {@link
 * org.infinispan.commons.api.BasicCache} instance.
 *
 * @author Nemanja Zbiljic
 */
public class InfinispanCache<K, V> implements Cache<K, V> {

    /**
     * Private internal log instance.
     */
    private static final Logger log = LoggerFactory.getLogger(InfinispanCache.class);

    /**
     * The wrapped Infinispan instance.
     */
    private BasicCache cache;

    /**
     * Constructs a new InfinispanCache instance with the given cache.
     *
     * @param cache - delegate InfinispanCache instance this Shiro cache instance will wrap.
     */
    public InfinispanCache(BasicCache cache) {
        if (cache == null) {
            throw new IllegalArgumentException("Cache argument cannot be null.");
        }
        this.cache = cache;
    }

    /**
     * Gets a value of an element which matches the given key.
     *
     * @param key the key of the element to return.
     * @return The value placed into the cache with an earlier put, or null if not found or expired
     */
    @Override
    public V get(K key) throws CacheException {
        try {
            if (log.isTraceEnabled()) {
                log.trace("Getting object from cache [" + cache.getName() + "] for key [" + key + "]");
            }
            if (key == null) {
                return null;
            }
            Object value = cache.get(key);
            if (value == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Object for [" + key + "] is null.");
                }
                return null;
            } else {
                //noinspection unchecked
                return (V) value;
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    /**
     * Puts an object into the cache.
     *
     * @param key   the key.
     * @param value the value.
     */
    @Override
    public V put(K key, V value) throws CacheException {
        if (log.isTraceEnabled()) {
            log.trace("Putting object in cache [" + cache.getName() + "] for key [" + key + "]");
        }
        try {
            V previous = get(key);
            cache.put(key, value);
            return previous;
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    /**
     * Removes the value which matches the key.
     *
     * If no key matches, nothing is removed and no Exception is thrown.
     *
     * @param key the key of the element to remove
     */
    @Override
    public V remove(K key) throws CacheException {
        if (log.isTraceEnabled()) {
            log.trace("Removing object from cache [" + cache.getName() + "] for key [" + key + "]");
        }
        try {
            V previous = get(key);
            cache.remove(key);
            return previous;
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    /**
     * Removes all elements in the cache, but leaves the cache in a usable state.
     */
    @Override
    public void clear() throws CacheException {
        if (log.isTraceEnabled()) {
            log.trace("Clearing all objects from cache [" + cache.getName() + "]");
        }
        try {
            cache.clear();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    @Override
    public int size() {
        try {
            return cache.size();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    @Override
    public Set<K> keys() {
        try {
            @SuppressWarnings({"unchecked"})
            Set<K> keys = cache.keySet();
            if (!CollectionUtils.isEmpty(keys)) {
                return Collections.unmodifiableSet(keys);
            } else {
                return Collections.emptySet();
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    @Override
    public Collection<V> values() {
        try {
            @SuppressWarnings({"unchecked"})
            Set<K> keys = cache.keySet();
            if (!CollectionUtils.isEmpty(keys)) {
                List<V> values = new ArrayList<V>(keys.size());
                for (K key : keys) {
                    V value = get(key);
                    if (value != null) {
                        values.add(value);
                    }
                }
                return Collections.unmodifiableList(values);
            } else {
                return Collections.emptyList();
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }


    /**
     * Returns &quot;InfinispanCache [&quot; + cache.getName() + &quot;]&quot;
     *
     * @return &quot;InfinispanCache [&quot; + cache.getName() + &quot;]&quot;
     */
    public String toString() {
        return "InfinispanCache [" + cache.getName() + "]";
    }
}
