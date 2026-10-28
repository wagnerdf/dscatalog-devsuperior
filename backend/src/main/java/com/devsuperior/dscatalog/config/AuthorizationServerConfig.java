package com.devsuperior.dscatalog.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;

import com.devsuperior.dscatalog.config.customgrant.CustomPasswordAuthenticationConverter;
import com.devsuperior.dscatalog.config.customgrant.CustomPasswordAuthenticationProvider;
import com.devsuperior.dscatalog.config.customgrant.CustomUserAuthorities;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Configura o servidor de autorização OAuth2 e os componentes responsáveis
 * pela criação, configuração e validação dos tokens JWT.
 */
@Configuration
public class AuthorizationServerConfig {

	@Value("${security.client-id}")
	private String clientId;

	@Value("${security.client-secret}")
	private String clientSecret;

	@Value("${security.jwt.duration}")
	private Integer jwtDurationSeconds;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserDetailsService userDetailsService;

	/**
	 * Configura a cadeia de filtros responsável pelos endpoints do servidor
	 * de autorização OAuth2.
	 *
	 * * O método atende às rotas /oauth2/** e /.well-known/**, registra
	 * o conversor e o provedor do fluxo de autenticação personalizado
	 * por senha e habilita a validação de tokens JWT.
	 *
	 * @param http objeto utilizado para configurar a segurança HTTP
	 * @return cadeia de filtros configurada para o servidor OAuth2
	 * @throws Exception caso ocorra um erro durante a configuração
	 */
    @Bean
    @Order(2)
    SecurityFilterChain asSecurityFilterChain(HttpSecurity http) throws Exception {

		http.securityMatcher("/oauth2/**", "/.well-known/**").with(new OAuth2AuthorizationServerConfigurer(), Customizer.withDefaults());

		// @formatter:off
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
			.tokenEndpoint(tokenEndpoint -> tokenEndpoint
				.accessTokenRequestConverter(new CustomPasswordAuthenticationConverter())
				.authenticationProvider(new CustomPasswordAuthenticationProvider(authorizationService(), tokenGenerator(), userDetailsService, passwordEncoder)));

		http.oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(Customizer.withDefaults()));
		// @formatter:on

		return http.build();
	}

	/**
	 * Cria o serviço responsável por armazenar as autorizações OAuth2.
	 *
	 * * Utiliza armazenamento em memória, adequado para o contexto do curso,
	 * mas que não mantém os dados após o encerramento da aplicação.
	 *
	 * @return serviço de autorizações OAuth2 em memória
	 */
    @Bean
    OAuth2AuthorizationService authorizationService() {
		return new InMemoryOAuth2AuthorizationService();
	}

    /**
	 * Cria o serviço responsável por armazenar os consentimentos concedidos
	 * pelos usuários às aplicações clientes.
	 *
	 * * Os consentimentos são mantidos em memória.
	 *
	 * @return serviço de consentimentos OAuth2 em memória
	 */
    @Bean
    OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService() {
		return new InMemoryOAuth2AuthorizationConsentService();
	}

    /**
	 * Registra a aplicação cliente autorizada a solicitar tokens.
	 *
	 * * Define o identificador e o segredo do cliente, os escopos permitidos,
	 * o fluxo de concessão por senha utilizado neste projeto e as configurações
	 * de tokens e do cliente.
	 *
	 * * O segredo é codificado com o PasswordEncoder antes de ser armazenado.
	 *
	 * @return repositório em memória contendo o cliente OAuth2 registrado
	 */
    @Bean
    RegisteredClientRepository registeredClientRepository() {
		// @formatter:off
		RegisteredClient registeredClient = RegisteredClient
			.withId(UUID.randomUUID().toString())
			.clientId(clientId)
			.clientSecret(passwordEncoder.encode(clientSecret))
			.scope("read")
			.scope("write")
			.authorizationGrantType(new AuthorizationGrantType("password"))
			.tokenSettings(tokenSettings())
			.clientSettings(clientSettings())
			.build();
		// @formatter:on

		return new InMemoryRegisteredClientRepository(registeredClient);
	}

    /**
	 * Define as configurações dos tokens de acesso.
	 *
	 * * Utiliza o formato SELF_CONTAINED, que permite representar as
	 * informações do token no próprio JWT, e configura sua validade
	 * conforme a propriedade security.jwt.duration.
	 *
	 * @return configurações dos tokens de acesso
	 */
    @Bean
    TokenSettings tokenSettings() {
		// @formatter:off
		return TokenSettings.builder()
			.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
			.accessTokenTimeToLive(Duration.ofSeconds(jwtDurationSeconds))
			.build();
		// @formatter:on
	}

    /**
	 * Cria as configurações adicionais do cliente OAuth2.
	 *
	 * * Como o construtor é utilizado sem opções adicionais, são aplicadas
	 * as configurações padrão disponibilizadas pelo Spring Authorization Server.
	 *
	 * @return configurações do cliente OAuth2
	 */
    @Bean
    ClientSettings clientSettings() {
		return ClientSettings.builder().build();
	}

    /**
	 * Cria as configurações gerais do servidor de autorização.
	 *
	 * * Como nenhuma propriedade é informada explicitamente, o servidor
	 * utiliza os valores padrão previstos pela biblioteca.
	 *
	 * @return configurações do servidor de autorização
	 */
    @Bean
    AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}

    /**
	 * Configura os geradores responsáveis pela criação dos tokens OAuth2.
	 *
	 * * O JwtGenerator cria tokens JWT utilizando um codificador baseado
	 * em chaves RSA. O OAuth2AccessTokenGenerator atende à geração de tokens
	 * de acesso no formato compatível com sua implementação.
	 *
	 * * O DelegatingOAuth2TokenGenerator coordena os geradores registrados.
	 *
	 * @return gerador de tokens OAuth2
	 */
    @Bean
    OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
		NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource());
		JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
		jwtGenerator.setJwtCustomizer(tokenCustomizer());
		OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
		return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator);
	}

    /**
	 * Personaliza as informações incluídas no JWT.
	 *
	 * * Obtém as autoridades e o nome de usuário do objeto CustomUserAuthorities
	 * associado ao principal da autenticação do cliente.
	 *
	 * * Quando o token é do tipo access_token, adiciona as claims
	 * authorities e username ao conteúdo do JWT. Essas informações podem
	 * ser utilizadas pelo servidor de recursos para identificar o usuário
	 * e suas permissões.
	 *
	 * @return personalizador das informações do JWT
	 */
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return context -> {
			OAuth2ClientAuthenticationToken principal = context.getPrincipal();
			CustomUserAuthorities user = (CustomUserAuthorities) principal.getDetails();
			List<String> authorities = user.getAuthorities().stream().map(x -> x.getAuthority()).toList();
			if (context.getTokenType().getValue().equals("access_token")) {
				// @formatter:off
				context.getClaims()
					.claim("authorities", authorities)
					.claim("username", user.getUsername());
				// @formatter:on
			}
		};
	}

    /**
	 * Cria o decodificador utilizado para validar e interpretar tokens JWT.
	 *
	 * * O decodificador utiliza a fonte de chaves configurada para verificar
	 * a assinatura e processar os tokens recebidos.
	 *
	 * @param jwkSource fonte das chaves utilizadas na validação dos tokens
	 * @return decodificador JWT
	 */
    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

    /**
	 * Disponibiliza a chave RSA no formato JWK para os componentes OAuth2.
	 *
	 * * A chave é organizada em um conjunto JWK, permitindo que os componentes
	 * selecionem as chaves necessárias para operações criptográficas.
	 *
	 * * Nesta implementação, o conjunto é criado em memória a partir de
	 * uma nova chave RSA.
	 *
	 * @return fonte de chaves JWK
	 */
    @Bean
    JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = generateRsa();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

    /**
	 * Gera um par de chaves RSA e monta uma chave JWK com suas partes pública
	 * e privada.
	 *
	 * * A chave pública pode ser utilizada na verificação da assinatura;
	 * a chave privada é utilizada na assinatura dos tokens.
	 *
	 * @return chave RSA no formato JWK, incluindo a chave privada
	 */
	private static RSAKey generateRsa() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
	}

	/**
	 * Gera um par de chaves criptográficas RSA de 2048 bits.
	 *
	 * * Se não for possível criar o par de chaves, lança uma
	 * IllegalStateException para sinalizar a falha de inicialização.
	 *
	 * @return par contendo as chaves pública e privada
	 */
	private static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}
}
