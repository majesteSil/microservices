package com.mindgarden.customer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.client.RestTemplate;


@Configuration
public class CustomerConfig
{

    @Bean
    @LoadBalanced
    public NewTopic customerTopic(){
        return TopicBuilder.name("mindgarden.customer.topic").build();
    }
 }
