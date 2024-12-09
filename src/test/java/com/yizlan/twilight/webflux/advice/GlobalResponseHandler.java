/*
 * Copyright (C) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yizlan.twilight.webflux.advice;

import com.yizlan.gelato.canonical.protocol.TerResult;
import com.yizlan.twilight.webflux.annotation.AbstractGlobalResponseBodyResultHandler;
import com.yizlan.twilight.webflux.autoconfigure.texture.HarmonyProperties;
import com.yizlan.twilight.webflux.protocol.Result;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalResponseHandler extends AbstractGlobalResponseBodyResultHandler<String, String, Object> {

    public GlobalResponseHandler(List<HttpMessageWriter<?>> writers, RequestedContentTypeResolver resolver,
                                 TerResult<String, String, Object> terResult, HarmonyProperties harmonyProperties) {
        super(writers, resolver, terResult, harmonyProperties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        final TerResult<String, String, Object> emptyResult = terResult.success();
        Object returnValue = result.getReturnValue();
        Object body;
        if (returnValue instanceof Mono) {
            body = ((Mono<Object>) result.getReturnValue())
                    .map(obj -> {
                        if (obj instanceof Result<?>) {
                            return obj;
                        }
                        return terResult.success().data(obj);
                    })
                    .defaultIfEmpty(emptyResult);
        } else if (returnValue instanceof Flux) {
            body = ((Flux<Object>) result.getReturnValue())
                    .collectList()
                    .map(this::wrap)
                    .defaultIfEmpty(emptyResult);
        } else {
            body = this.wrap(returnValue);
        }
        return super.writeBody(body, exchange);
    }

    private Object wrap(Object body) {
        if (body instanceof Result<?>) {
            return body;
        }
        return Result.success("操作成功", body);
    }

}
