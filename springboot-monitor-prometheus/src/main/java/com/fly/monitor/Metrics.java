package com.fly.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Consumer;

/**
 * 需要注意，在上述逻辑实现中对“Timer”及“Counter”等指标类型的构建这里并没有直接使用“micrometer-registry-prometheus”依赖包中的构建对象，
 * 而是通过自定义的Metrics.newTimer()这样的方式实现，其主要用意是希望以更简洁、灵活的方式去实现指标的上报，其代码定义如下：
 */
public class Metrics implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


    public static ApplicationContext getContext() {
        return context;
    }


    public static Counter newCounter(String name, Consumer<Counter.Builder> consumer) {
        MeterRegistry meterRegistry = context.getBean(MeterRegistry.class);
        return new CounterBuilder(meterRegistry, name, consumer).build();
    }

    public static Timer newTimer(String name, Consumer<Timer.Builder> consumer) {
        return new TimerBuilder(context.getBean(MeterRegistry.class), name, consumer).build();
    }


}
