package appenginehelpers;

import com.google.appengine.api.memcache.ErrorHandler;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.Stats;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


public class EhCacheWrapper implements MemcacheService {

    private final Cache cache;

    public EhCacheWrapper(Cache cache) {
        if (cache == null) throw new IllegalArgumentException("cannot set null cache");
        this.cache = cache;
    }

    @Override
    public String getNamespace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNamespace(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object key) {
        Element element = cache.get(key);
        return element == null ? null : element.getValue();
    }

    @Override
    public boolean contains(Object key) {
        return cache.isKeyInCache(key);
    }

    @Override
    public <T> Map<T, Object> getAll(Collection<T> ts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean put(Object o, Object o1, Expiration expiration, MemcacheService.SetPolicy setPolicy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(Object key, Object value, Expiration expiration) {
        int secondsToLive = (int) (expiration.getSecondsValue() - (System.currentTimeMillis() / 1000));
        Element e = new Element(key, value, false, secondsToLive, secondsToLive);
        cache.put(e);
    }

    @Override
    public void put(Object key, Object value) {
        Element e = new Element(key, value);
        cache.put(e);
    }

    @Override
    public <T> Set<T> putAll(Map<T, ?> tMap, Expiration expiration, MemcacheService.SetPolicy setPolicy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<?, ?> map, Expiration expiration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<?, ?> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete(Object key) {
        return cache.remove(key);
    }

    @Override
    public boolean delete(Object o, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Set<T> deleteAll(Collection<T> ts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Set<T> deleteAll(Collection<T> ts, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long increment(Object o, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long increment(Object o, long l, Long aLong) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<T, Long> incrementAll(Collection<T> ts, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<T, Long> incrementAll(Collection<T> ts, long l, Long aLong) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<T, Long> incrementAll(Map<T, Long> tLongMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Map<T, Long> incrementAll(Map<T, Long> tLongMap, Long aLong) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearAll() {
        cache.removeAll();
    }

    @Override
    public Stats getStatistics() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ErrorHandler getErrorHandler() {
        throw new UnsupportedOperationException();
    }
}
