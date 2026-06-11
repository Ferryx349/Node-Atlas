package com.project1.shriganeshaynamah.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("!'${spring.ai.openai.api-key:}'.isBlank()")
public class AiConfig {

    @Bean
    public ChatModel chatModel(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String model) {

        OpenAiApi openAiApi = OpenAiApi.builder().apiKey(apiKey).build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(0.4)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }
}
