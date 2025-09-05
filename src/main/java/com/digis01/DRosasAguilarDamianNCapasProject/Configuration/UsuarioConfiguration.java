
package com.digis01.DRosasAguilarDamianNCapasProject.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration

public class UsuarioConfiguration {

@Bean
public RestTemplate restTemplade(){

    return new RestTemplate();
}

    
}
