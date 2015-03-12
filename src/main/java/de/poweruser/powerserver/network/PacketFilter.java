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

    private class FilterInfo {

        private long lastIncoming;
        private int violations;

        public FilterInfo() {
            this.lastIncoming = -1L;
            this.violations = 0;
        }

        public boolean incomingAndCheckViolations(long time) {
            boolean intervalOk = this.checkInterval(time, settings.getAllowedMinimumSendInterval());
            this.lastIncoming = time;
            if(!intervalOk) {
                this.violations++;
                if(this.violations > settings.getMaximumSendViolations()) { return false; }
            } else if(this.violations > 0) {
                this.violations--;
            }
            return true;
        }

        private boolean checkInterval(long time, long allowedMinimumInterval) {
            return this.lastIncoming <= (time - allowedMinimumInterval);
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
