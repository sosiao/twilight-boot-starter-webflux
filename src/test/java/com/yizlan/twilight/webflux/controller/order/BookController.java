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

package com.yizlan.twilight.webflux.controller.order;

import com.yizlan.twilight.webflux.protocol.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RestController
public class BookController {

    @GetMapping("/order/book")
    public Mono<String> createOrder() {
        return Mono.just("book");
    }

    @GetMapping("/order/book/detail")
    public Flux<String> list(){
        List<String> books = Arrays.asList("book1","book2","book3");

        return Flux.fromIterable(books);
    }

    @GetMapping("/resultFlux")
    public Flux<Result<String>> resultFlux(){
        return Flux.just(Result.success("成功","resultFlux"));
    }

}
