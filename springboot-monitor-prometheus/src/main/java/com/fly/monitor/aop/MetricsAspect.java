package com.fly.monitor.aop;

import com.fly.monitor.Metrics;
import com.fly.monitor.annotation.Tp;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.Function;

/**
 * 自定义监控指标注解AOP代理逻辑实现
 */
@Aspect
@Component
public class MetricsAspect {
    /**
     * Prometheus指标管理
     */
    private MeterRegistry registry;

    private Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint;

    public MetricsAspect(MeterRegistry registry) {
        //
//        this.registry = registry;
        this.init(registry,pjp -> Tags.of(new String[]{"class",pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                "method",pjp.getStaticPart().getSignature().getName()}));
    }

    public void init(MeterRegistry registry, Function<ProceedingJoinPoint, Iterable<Tag>> tagsBasedOnJoinPoint) {
        this.registry = registry;
        this.tagsBasedOnJoinPoint = tagsBasedOnJoinPoint;
    }

    /**
     * 针对@Tp指标配置注解的逻辑实现
     */
    @Around("@annotation(com.fly.monitor.annotation.Tp)")
    public Object timedMethod(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
        Tp tp = method.getAnnotation(Tp.class);
        Timer.Sample sample = Timer.start(this.registry);
        String exceptionClass = "none";
        try {
            return pjp.proceed();
        } catch (Exception ex) {
            exceptionClass = ex.getClass().getSimpleName();
            throw ex;
        } finally {
            try {
                String finalExceptionClass = exceptionClass;
                //创建定义计数器，并设置指标的Tags信息（名称可以自定义）
                Timer timer = Metrics.newTimer("tp.method.timed",
                        builder -> builder.tags(new String[]{"exception", finalExceptionClass})
                                .tags(this.tagsBasedOnJoinPoint.apply(pjp)).tag("description", tp.description())
                                .publishPercentileHistogram().register(this.registry));
                sample.stop(timer);
            } catch (Exception exception) {
            }
        }
    }
}
