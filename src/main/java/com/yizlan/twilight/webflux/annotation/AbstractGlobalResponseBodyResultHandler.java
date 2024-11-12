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

package com.yizlan.twilight.webflux.annotation;

import com.yizlan.gelato.canonical.protocol.TerResult;
import com.yizlan.twilight.webflux.autoconfigure.texture.HarmonyProperties;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

/**
 * Abstract global response body handler class for unifying response data handling.
 * This class implements the ResponseBodyAdvice interface to customize the processing of controller return values.
 * The main functionalities include determining whether to process response data based on configuration and
 * providing a base response result object and configuration properties.
 *
 * @param <T> the type of the code field, should implement {@link Comparable} and {@link Serializable}
 * @param <U> the type of the message field, should implement {@link Comparable} and {@link Serializable}
 * @param <S> the type of the data filed
 * @author Zen Gershon
 * @since 1.0
 */
public abstract class AbstractGlobalResponseBodyResultHandler<T extends Comparable<T> & Serializable,
        U extends Comparable<U> & Serializable, S> extends ResponseBodyResultHandler {

    private final HarmonyProperties harmonyProperties;

    protected final TerResult<T, U, S> terResult;

    protected AbstractGlobalResponseBodyResultHandler(List<HttpMessageWriter<?>> writers,
                                                      RequestedContentTypeResolver resolver,
                                                      HarmonyProperties harmonyProperties,
                                                      TerResult<T, U, S> terResult) {
        super(writers, resolver);
        Assert.notNull(terResult, "TerResult must not be null");
        Assert.notNull(harmonyProperties, "HarmonyProperties must not be null");
        this.harmonyProperties = harmonyProperties;
        this.terResult = terResult;
    }

    public boolean supports(HandlerResult result) {
        MethodParameter parameter = result.getReturnTypeSource();
        Class<?> containingClass = parameter.getContainingClass();

        Object object = result.getHandler();

        String[] packages = harmonyProperties.getPackages();
        boolean emptyPackage = ObjectUtils.isEmpty(packages);

        // 全局或指定包拦截
        boolean intercept = emptyPackage || Stream.of(packages).anyMatch(p -> object.toString().startsWith(p));

        return harmonyProperties.isEnabled() && intercept &&
                (AnnotatedElementUtils.hasAnnotation(containingClass, ResponseBody.class) ||
                        parameter.hasMethodAnnotation(ResponseBody.class));
    }
}
