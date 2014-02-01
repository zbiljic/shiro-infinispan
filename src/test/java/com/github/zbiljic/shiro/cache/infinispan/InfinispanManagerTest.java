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
import org.apache.shiro.util.LifecycleUtils;

import org.infinispan.manager.EmbeddedCacheManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Nemanja Zbiljic
 */
public class InfinispanManagerTest {

    private InfinispanCacheManager cacheManager;

    @Before
    public void setUp() {
        cacheManager = new InfinispanCacheManager();
    }

    @After
    public void tearDown() {
        LifecycleUtils.destroy(cacheManager);
    }

    @Test
    public void testCacheManagerCreationDuringInit() {
        EmbeddedCacheManager infinispanCacheManager = cacheManager.getCacheManager();
        assertNull(infinispanCacheManager);
        cacheManager.init();
        //now assert that an internal CacheManager has been created:
        infinispanCacheManager = cacheManager.getCacheManager();
        assertNotNull(infinispanCacheManager);
    }

    @Test
    public void testLazyCacheManagerCreationWithoutCallingInit() {
        EmbeddedCacheManager infinispanCacheManager = cacheManager.getCacheManager();
        assertNull(infinispanCacheManager);

        //don't call init here - the Infinispan EmbeddedCacheManager should be lazily created
        //because of the default Shiro infinispan.xml file in the classpath.  Just acquire a cache:
        Cache<String, String> cache = cacheManager.getCache("test");

        //now assert that an internal EmbeddedCacheManager has been created:
        infinispanCacheManager = cacheManager.getCacheManager();
        assertNotNull(infinispanCacheManager);

        assertNotNull(cache);
        cache.put("hello", "world");
        String value = cache.get("hello");
        assertNotNull(value);
        assertEquals(value, "world");
    }

}
