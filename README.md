shiro-infinispan
================

Apache Shiro Infinispan cache support


About
=====

Shiro's default configuration native SessionManagers use in-memory-only Session storage. This is unsuitable for most production applications.

Shiro provides EHCache support for session management.

This project is drop-in replacement that uses [Infinispan](http://infinispan.org/) data grid cache.


How to use it?
==============

Configuring Infinispan cache for all of Shiro's caching needs in `shiro.ini`

```properties
[main]
...
sessionDAO = org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO

sessionManager = org.apache.shiro.web.session.mgt.DefaultWebSessionManager
sessionManager.sessionDAO = $sessionDAO

securityManager.sessionManager = $sessionManager

cacheManager = com.github.zbiljic.shiro.cache.infinispan.InfinispanCacheManager
securityManager.cacheManager = $cacheManager
...
```


### Infinispan configuration ###

Default `infinispan.xml` configuration uses Infinispan's [Distribution mode](http://infinispan.org/docs/6.0.x/user_guide/user_guide.html#_distribution_mode) with 2 cluster wide copies of cache entries. Transport configuration used is `jgroups-tcp.xml`.

Infinispan JMX statistics are enabled by default.

You can use provided `infinispan.xml` as a starting point for custom Infinispan configuration. Check [Infinispan User Guide](http://infinispan.org/docs/6.0.x/user_guide/user_guide.html) for configuration options.

After that, configure cache manager in `shiro.ini` so that it points to the location of your custom XML configuration file.

Example:

```properties
[main]
...
cacheManager = com.github.zbiljic.shiro.cache.infinispan.InfinispanCacheManager
cacheManager.cacheManagerConfigFile = "config/custom_infinispan.xml"
securityManager.cacheManager = $cacheManager
...
```


If you found any bugs
=====================

Please open an issue


License
-------

This software is licensed under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
