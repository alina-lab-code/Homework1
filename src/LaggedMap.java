

import java.util.*;
import java.util.concurrent.*;

public class LaggedMap<K, V> {
    private final int draftSeconds;
    private final Map<K, V> published = new ConcurrentHashMap<>();
    private final Map<K, Draft<V>> drafts = new ConcurrentHashMap<>();
    private final Map<K, LinkedList<V>> history = new ConcurrentHashMap<>();
    private final Map<K, V> backupMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LaggedMap(int draftSeconds) {
        this.draftSeconds = draftSeconds;

        scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    private static class Draft<V> {
        V value;
        long publishTime;
        boolean isRemove;

        Draft(V value, long seconds, boolean isRemove) {
            this.value = value;
            this.publishTime = System.currentTimeMillis() + (seconds * 1000);
            this.isRemove = isRemove;
        }
    }

    public synchronized void put(K key, V value) {
        drafts.put(key, new Draft<>(value, draftSeconds, false));
    }

    public synchronized V get(K key) {
        return published.get(key);
    }

    public synchronized void abort() {
        drafts.clear();
    }

    public synchronized void remove(K key, boolean full) {

        drafts.put(key, new Draft<>(null, draftSeconds, true));

    }

    public synchronized void rollback() {
        published.clear();
        published.putAll(backupMap);
        drafts.clear();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        // 1. Публикация
        for (Iterator<Map.Entry<K, Draft<V>>> it = drafts.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<K, Draft<V>> entry = it.next();
            if (now >= entry.getValue().publishTime) {
                K key = entry.getKey();
                Draft<V> draft = entry.getValue();

                if (draft.isRemove) {
                    published.remove(key);
                } else {

                    history.computeIfAbsent(key, k -> new LinkedList<>()).addFirst(published.get(key));
                    published.put(key, draft.value);
                }
                it.remove();
            }
        }

        for (LinkedList<V> list : history.values()) {
            while (list.size() > 3) list.removeLast();
        }

        if (now % 60000 < 1000) {
            backupMap.clear();
            backupMap.putAll(published);
        }
    }

}