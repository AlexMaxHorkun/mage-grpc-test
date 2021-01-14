package com.magento.grpctest.client.data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class Measurement {
    private final Long totalMs;

    private final Integer totalRequests;

    private final Long p90Ms;

    private final Long slowestMs;

    private final Long fastestMs;

    public static final Measurement from(Duration total, Collection<Duration> perRequest) {
        var responses = new ArrayList<>(perRequest);
        responses.sort(new Comparator<Duration>() {
            @Override
            public int compare(Duration o1, Duration o2) {
                return Long.compare(o1.toMillis(), o2.toMillis());
            }
        });
        var size = responses.size();

        return new Measurement(total.toMillis(), size, responses.get((int) (size* 0.95) - 1).toMillis(),
                responses.get(size - 1).toMillis(), responses.get(0).toMillis());
    }

    protected Measurement(Long totalMs, Integer totalRequests, Long p90Ms, Long slowestMs, Long fastestMs) {
        this.totalMs = totalMs;
        this.totalRequests = totalRequests;
        this.p90Ms = p90Ms;
        this.slowestMs = slowestMs;
        this.fastestMs = fastestMs;
    }

    public Long getTotalMs() {
        return totalMs;
    }

    public Integer getTotalRequests() {
        return totalRequests;
    }

    public Long getP90Ms() {
        return p90Ms;
    }

    public Long getSlowestMs() {
        return slowestMs;
    }

    public Long getFastestMs() {
        return fastestMs;
    }
}
