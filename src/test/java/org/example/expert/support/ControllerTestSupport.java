package org.example.expert.support;

import org.example.expert.config.CustomAccessDeniedHandler;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.SecurityConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import(SecurityConfig.class)
public abstract class ControllerTestSupport {

    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    CustomAccessDeniedHandler customAccessDeniedHandler;

}
