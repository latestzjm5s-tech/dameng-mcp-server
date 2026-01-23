package com.uniin.ioc.dameng.config;

import com.uniin.ioc.dameng.mcp.DamengMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider damengToolCallbackProvider(DamengMcpTools damengMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(damengMcpTools)
                .build();
    }
}
