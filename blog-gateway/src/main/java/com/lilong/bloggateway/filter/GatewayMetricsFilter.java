package com.lilong.bloggateway.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author : lilong
 * @date : 2025-12-13 15:17
 * @description :
 */
@Slf4j
public class GatewayMetricsFilter implements GlobalFilter, Ordered {
    private final MeterRegistry meterRegistry;
    public GatewayMetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            log.warn("Failed to get routeId for the request");
            return chain.filter(exchange);
        }
        String routeId = route.getId();
        // 请求Uri信息
        Set<URI> uris = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        URI uriObj = new ArrayList<>(uris).get(0);
        String uri = uriObj.getPath();
        // 创建或获取计数器，用于统计请求数量
        Counter requestCounter = meterRegistry.counter("gateway_requests_total", "route", routeId, "uri", uri);
        requestCounter.increment();
        // 创建或获取计时器，用于统计请求耗时
        Timer.Sample timerSample = Timer.start(meterRegistry);
        // 创建或获取计数器，用于统计请求错误数量
        Counter errorCounter = meterRegistry.counter("gateway_requests_error_total", "route", routeId, "uri", uri);
        // 存储请求开始时的时间，用于计算请求带宽
        long startTime = System.currentTimeMillis();
        long requestSize = 0;
        if (exchange.getRequest().getHeaders()!= null) {
            requestSize = exchange.getRequest().getHeaders().getContentLength();
        } else {
            requestSize = 0;
        }
        long finalRequestSize = requestSize;
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    try {
                        // 计算请求耗时
                        timerSample.stop(
                                Timer.builder("gateway_request_duration")
                                        .tags(Tags.of("route", routeId, "uri", uri))
                                        .register(meterRegistry));
                        // 获取 HTTP 状态码
                        HttpStatus statusCode = (HttpStatus) exchange.getResponse().getStatusCode();
                        if (statusCode!= null) {
                            // 创建或获取计数器，用于统计不同状态码的请求数量
                            Counter statusCodeCounter = meterRegistry.counter("gateway_requests_status",
                                    Tags.of("route", routeId, "status", statusCode.value() + "", "uri", uri));
                            statusCodeCounter.increment();
                            // 检查是否为错误状态码，如果是，增加错误计数器
                            if (statusCode.isError()) {
                                errorCounter.increment();
                            }
                        }
                        // 计算请求带宽
                        long endTime = System.currentTimeMillis();
                        long responseSize = 0;
                        if (exchange.getResponse().getHeaders()!= null) {
                            responseSize = exchange.getResponse().getHeaders().getContentLength();
                        }
                        long totalSize = finalRequestSize + responseSize;
                        double bandwidth = (double) totalSize / (endTime - startTime);
                        // 创建或获取计数器，用于统计请求带宽
                        Counter bandwidthCounter = meterRegistry.counter("gateway_request_bandwidth", "route", routeId, "uri", uri);
                        bandwidthCounter.increment(bandwidth);
                    } catch (Exception e) {
                        log.error("Failed to record metrics for request", e);
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
