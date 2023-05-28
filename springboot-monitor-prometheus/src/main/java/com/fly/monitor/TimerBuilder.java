package com.fly.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.function.Consumer;

public class TimerBuilder {

    private final MeterRegistry meterRegistry;

    private Timer.Builder builder;

    private Consumer<Timer.Builder> consumer;

    public TimerBuilder(MeterRegistry meterRegistry, String name, Consumer<Timer.Builder> consumer) {
        this.builder = Timer.builder(name);
        this.meterRegistry = meterRegistry;
        this.consumer = consumer;
    }

    public Timer build() {
        this.consumer.accept(builder);
        return builder.register(meterRegistry);
    }
}
