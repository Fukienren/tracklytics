package com.orhanobut.tracklytics;

import com.orhanobut.tracklytics.debugger.EventQueue;
import com.orhanobut.tracklytics.trackers.TrackingAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Tracker {

  final Map<String, Object> superAttributes = new HashMap<>();

  private final TrackingAdapter[] adapters;

  private boolean enabled = true;
  private TracklyticsLogger logger;

  private Tracker(TrackingAdapter[] adapters) {
    this.adapters = adapters;
  }

  public static Tracker init(TrackingAdapter... adapters) {
    Tracker tracker = new Tracker(adapters);
    TrackerAspect.init(tracker);
    return tracker;
  }

  public void event(String title, Map<String, Object> values, Map<String, Object> superAttributes) {
    event(title, values, superAttributes, Collections.<Integer>emptySet());
  }

  public void event(String title, Map<String, Object> attributes, Map<String, Object> superAttributes,
                    Set<Integer> filter) {
    if (!enabled) {
      return;
    }
    for (TrackingAdapter tool : adapters) {
      if (filter.isEmpty() || filter.contains(tool.id())) {
        tool.trackEvent(title, attributes, superAttributes);
        EventQueue.add(tool.id(), tool.toString(), title, attributes);
      }
    }
  }

  public Tracker enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void start() {
    for (TrackingAdapter tool : adapters) {
      tool.start();
    }
  }

  public void stop() {
    for (TrackingAdapter tool : adapters) {
      tool.stop();
    }
  }

  void log(long start, long stopMethod, long stopTracking, String event, Map<String, Object> attrs,
           Map<String, Object> superAttrs) {
    if (logger != null) {
      long method = TimeUnit.NANOSECONDS.toMillis(stopMethod - start);
      long total = TimeUnit.NANOSECONDS.toMillis(stopTracking - start);
      StringBuilder builder = new StringBuilder()
          .append("[")
          .append(method)  // Method execution time
          .append("+")
          .append(total - method)  // Tracking execution time
          .append("=")
          .append(total)  // Total execution time
          .append("ms] ")
          .append(event)
          .append("-> ")
          .append(attrs.toString())
          .append(", super attrs: ")
          .append(superAttrs.toString());
      logger.log(builder.toString());
    }
  }

  public void setLogger(TracklyticsLogger logger) {
    this.logger = logger;
  }
}