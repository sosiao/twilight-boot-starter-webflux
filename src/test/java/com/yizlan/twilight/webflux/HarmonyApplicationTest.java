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

package com.yizlan.twilight.webflux;

import com.yizlan.gelato.canonical.protocol.TerResult;
import com.yizlan.twilight.webflux.actuate.HealthController;
import com.yizlan.twilight.webflux.advice.GlobalExceptionHandler;
import com.yizlan.twilight.webflux.advice.GlobalResponseHandler;
import com.yizlan.twilight.webflux.autoconfigure.texture.HarmonyAutoConfiguration;
import com.yizlan.twilight.webflux.autoconfigure.texture.HarmonyProperties;
import com.yizlan.twilight.webflux.config.TwilightConfig;
import com.yizlan.twilight.webflux.controller.HomeController;
import com.yizlan.twilight.webflux.controller.order.BookController;
import com.yizlan.twilight.webflux.controller.personal.AccountController;
import com.yizlan.twilight.webflux.protocol.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;

import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {HarmonyAutoConfiguration.class, TwilightConfig.class})
@TestPropertySource("classpath:application.yaml")
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
public class HarmonyApplicationTest {

    @InjectMocks
    private AccountController accountController;

    @InjectMocks
    private BookController bookController;

    @InjectMocks
    private HomeController homeController;

    @InjectMocks
    private HealthController healthController;

    @Resource
    private ServerCodecConfigurer serverCodecConfigurer;

    @Resource
    private RequestedContentTypeResolver requestedContentTypeResolver;

    @Resource
    private TerResult<String, String, Object> terResult;

    @Resource
    private HarmonyProperties harmonyProperties;

    private WebTestClient webClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        webClient = WebTestClient
                .bindToController(homeController, accountController, bookController, healthController)
                .controllerAdvice(new GlobalResponseHandler(serverCodecConfigurer.getWriters(),
                        requestedContentTypeResolver,
                        terResult, harmonyProperties), new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGlobalResponseResult() {
        // echo `{"success":true,"code":"code","message":"操作成功","data":"book"}`
        webClient.get().uri("/order/book")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Result.class)
                .consumeWith(response -> {
                    Result<?> result = response.getResponseBody();

                    Assert.assertNotNull(result);
                    Assert.assertTrue(result.getSuccess());
                    Assert.assertEquals("code", result.getCode());
                    Assert.assertEquals("操作成功", result.getMessage());
                    Assert.assertEquals("book", result.getData());
                });
        // echo `{"success":true,"code":"code","message":"操作成功","data":["book1","book2","book3"]}`
        webClient.get().uri("/order/book/detail")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.code").isEqualTo("200")
                .jsonPath("$.message").isEqualTo("操作成功")
                .jsonPath("$.data").isEqualTo(
                        Stream.of("book1", "book2", "book3").collect(Collectors.toList())
                );

        webClient.get().uri("/personal/account")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Result.class)
                .consumeWith(response -> {
                    Result<?> result = response.getResponseBody();

                    Assert.assertNotNull(result);
                    Assert.assertFalse(result.getSuccess());
                    Assert.assertEquals("500", result.getCode());
                    Assert.assertEquals("无权查看.", result.getMessage());
                    Assert.assertNull(result.getData());
                });
    }

}
