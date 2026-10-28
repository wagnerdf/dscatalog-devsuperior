package com.devsuperior.dscatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
* Classe responsável por definir configurações e componentes
* utilizados pela aplicação.
*/
@Configuration
public class AppConfig {

    /**
    * Cria um componente para codificar senhas utilizando o algoritmo BCrypt.
    *
    * @return codificador de senhas BCrypt gerenciado pelo Spring
    */
    @Bean
    PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
