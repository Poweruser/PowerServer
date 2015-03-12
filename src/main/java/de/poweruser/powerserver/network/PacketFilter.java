package de.poweruser.powerserver.network;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.settings.Settings;

public class PacketFilter {

    private LinkedFilterInfoHashMap<InetAddress> filterMap;
    private Settings settings;

    public PacketFilter(Settings settings) {
        this.settings = settings;
        this.filterMap = new LinkedFilterInfoHashMap<InetAddress>(16, 0.75f, true);
    }

    public boolean newIncoming(InetAddress address, long time) {
        FilterInfo filterInfo = this.getOrCreateEntry(address, time);
        if(!filterInfo.incomingAndCheckViolations(time)) {
            this.filterMap.remove(address);
            return false;
        }
        return true;
    }

    private FilterInfo getOrCreateEntry(InetAddress address, long time) {
        FilterInfo filterInfo;
        if(this.filterMap.containsKey(address)) {
            filterInfo = this.filterMap.get(address);
        } else {
            filterInfo = new FilterInfo();
            this.filterMap.put(address, filterInfo);
        }
        return filterInfo;
    }

    private enum FilterCheckResult {
        VIOLATION,
        INTERVAL_OK,
        INTERVAL_LONG;
    }

    private class FilterInfo {

        private long lastIncoming;
        private int violations;

        public FilterInfo() {
            this.lastIncoming = -1L;
            this.violations = 0;
        }

        public boolean incomingAndCheckViolations(long time) {
            FilterCheckResult result = this.checkInterval(time, settings.getAllowedMinimumSendInterval(), settings.getAllowedMinimumSendInterval() * 100);
            this.lastIncoming = time;
            switch(result) {
                case VIOLATION:
                    if(++this.violations > settings.getMaximumSendViolations()) { return false; }
                    break;
                case INTERVAL_OK:
                    --this.violations;
                    break;
                case INTERVAL_LONG:
                    this.violations -= 10;
                default:
                    break;
            }
            if(this.violations < 0) {
                this.violations = 0;
            }
            return true;
        }

        private FilterCheckResult checkInterval(long time, long allowedMinimumInterval, long timeoutDuration) {
            if(this.lastIncoming > (time - allowedMinimumInterval)) {
                return FilterCheckResult.VIOLATION;
            } else if(this.lastIncoming < time - timeoutDuration) { return FilterCheckResult.INTERVAL_LONG; }
            return FilterCheckResult.INTERVAL_OK;
        }

        public boolean checkLastIncoming(long duration, TimeUnit unit) {
            return this.lastIncoming < (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(duration, unit));
        }
    }

    private class LinkedFilterInfoHashMap<K> extends LinkedHashMap<K, FilterInfo> {

        private static final long serialVersionUID = 1357559109479352534L;

        public LinkedFilterInfoHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
            super(initialCapacity, loadFactor, accessOrder);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, FilterInfo> eldest) {
            return eldest.getValue().checkLastIncoming(10L, TimeUnit.MINUTES);
        }
    }
}
