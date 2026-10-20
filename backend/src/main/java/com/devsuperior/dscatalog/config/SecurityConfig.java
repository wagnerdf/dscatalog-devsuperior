package com.devsuperior.dscatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// Desativa a proteção CSRF nesta etapa do desenvolvimento.
	    http.csrf(csrf -> csrf.disable());

	    // Permite que o H2 Console seja exibido corretamente dentro de frames.
	    http.headers(headers -> headers
	            .frameOptions(frame -> frame.disable()));

	    // Permite todas as requisições, sem exigir autenticação nesta etapa.
	    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

	    // Constrói e retorna a cadeia de filtros de segurança configurada.
	    return http.build();
	}
}
