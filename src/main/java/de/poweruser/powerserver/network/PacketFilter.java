package de.poweruser.powerserver.network;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.settings.Settings;

public class PacketFilter {

    private HashMap<InetAddress, FilterInfo> filterMap;
    private LinkedList<InetAddress> cleanUpQueue;
    private Settings settings;

    public PacketFilter(Settings settings) {
        this.settings = settings;
        this.filterMap = new HashMap<InetAddress, FilterInfo>();
        this.cleanUpQueue = new LinkedList<InetAddress>();
    }

    public boolean newIncoming(InetAddress address, long time) {
        FilterInfo filterInfo = this.getOrCreateEntry(address, time);
        this.cleanUpQueue.remove(address);
        this.cleanUpQueue.addLast(address);
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

    public void cleanup() {
        Iterator<InetAddress> iter = this.cleanUpQueue.iterator();
        while(iter.hasNext()) {
            FilterInfo filterInfo = this.filterMap.get(iter.next());
            if(filterInfo != null) {
                if(filterInfo.checkLastIncoming(10L, TimeUnit.MINUTES)) {
                    iter.remove();
                } else {
                    break;
                }
            }
        }
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
}
