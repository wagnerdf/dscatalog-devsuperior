package com.devsuperior.dscatalog.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configura a segurança do servidor de recursos, incluindo a validação
 * de tokens JWT, a conversão das permissões do usuário e as regras de CORS.
 *
 * * Também configura o acesso ao console H2 durante a execução com
 * o perfil de teste e habilita a segurança em nível de método.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {

	@Value("${cors.origins}")
	private String corsOrigins;

	/**
	 * Configura uma cadeia de filtros específica para o console H2
	 * quando a aplicação utiliza o perfil test.
	 *
	 * * O console H2 utiliza frames para exibir sua interface. Por isso,
	 * esta configuração desabilita a proteção contra frames e o CSRF
	 * para as requisições correspondentes ao console.
	 *
	 * @param http objeto utilizado para configurar a segurança HTTP
	 * @return cadeia de filtros para o console H2
	 * @throws Exception caso ocorra um erro durante a configuração
	 */
    @Bean
    @Profile("test")
    @Order(1)
    SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) throws Exception {

		http.securityMatcher(PathRequest.toH2Console()).csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
		return http.build();
	}

    /**
	 * Configura a cadeia de filtros principal do servidor de recursos.
	 *
	 * * Desabilita o CSRF, permite as requisições HTTP sem exigir autenticação
	 * nesta cadeia, habilita a validação de tokens JWT e configura o CORS.
	 *
	 * * A permissão geral definida por anyRequest().permitAll() não impede
	 * a validação de um token Bearer enviado na requisição. Ela significa
	 * que esta regra de autorização, isoladamente, não exige autenticação.
	 *
	 * @param http objeto utilizado para configurar a segurança HTTP
	 * @return cadeia de filtros do servidor de recursos
	 * @throws Exception caso ocorra um erro durante a configuração
	 */
    @Bean
    @Order(3)
    SecurityFilterChain rsSecurityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(Customizer.withDefaults()));
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		return http.build();
	}

    /**
	 * Configura como as permissões contidas no JWT são convertidas
	 * em autoridades reconhecidas pelo Spring Security.
	 *
	 * * Indica que as permissões devem ser lidas da claim authorities
	 * e que nenhum prefixo deve ser acrescentado aos valores encontrados.
	 * Isso permite, por exemplo, utilizar valores como ROLE_ADMIN
	 * diretamente nas verificações de autorização.
	 *
	 * @return conversor de autenticação JWT com as autoridades configuradas
	 */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
		grantedAuthoritiesConverter.setAuthorityPrefix("");

		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

    /**
	 * Cria a configuração de CORS utilizada pela aplicação.
	 *
	 * * Obtém as origens permitidas da propriedade cors.origins,
	 * separa os valores por vírgula e define os métodos HTTP e os cabeçalhos
	 * que podem ser utilizados nas requisições de origem cruzada.
	 *
	 * * Também permite o envio de credenciais nas requisições.
	 *
	 * @return fonte de configuração CORS aplicada às rotas da aplicação
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {

		String[] origins = corsOrigins.split(",");

		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOriginPatterns(Arrays.asList(origins));
		corsConfig.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "PATCH"));
		corsConfig.setAllowCredentials(true);
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);
		return source;
	}

	/**
	 * Registra um filtro CORS na cadeia de filtros de servlets.
	 *
	 * * Utiliza a configuração definida em corsConfigurationSource()
	 * e atribui ao filtro a maior prioridade de execução, para que
	 * as requisições CORS sejam tratadas antecipadamente.
	 *
	 * @return registro do filtro CORS com prioridade máxima
	 */
	@Bean
	FilterRegistrationBean<CorsFilter> filterRegistrationBeanCorsFilter() {
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(
				new CorsFilter(corsConfigurationSource()));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}
}
