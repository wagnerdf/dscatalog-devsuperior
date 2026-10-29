package com.devsuperior.dscatalog.tests;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Utilitário de testes responsável por obter tokens de acesso OAuth2.
 *
 * • Centraliza a autenticação utilizada pelos testes de integração.
 * • Utiliza o MockMvc para simular uma requisição HTTP ao endpoint de tokens.
 * • Retorna o access token que será utilizado nas requisições autenticadas.
 */
@Component
public class TokenUtil {

	@Value("${security.client-id}")
	private String clientId;

	@Value("${security.client-secret}")
	private String clientSecret;
	
	/**
	 * Solicita um token de acesso OAuth2 utilizando as credenciais do cliente
	 * e as credenciais do usuário informado.
	 *
	 * • Prepara os parâmetros do fluxo de concessão por senha (password).
	 * • Autentica o cliente utilizando HTTP Basic.
	 * • Envia uma requisição POST para /oauth2/token.
	 * • Verifica se a resposta possui status HTTP 200 e conteúdo JSON.
	 * • Extrai e retorna o valor da propriedade access_token.
	 *
	 * @param mockMvc componente utilizado para simular a requisição HTTP
	 * @param username nome de usuário utilizado na autenticação
	 * @param password senha do usuário utilizado na autenticação
	 * @return token de acesso OAuth2 extraído da resposta
	 * @throws Exception caso a requisição, as verificações ou a leitura
	 *                   da resposta apresentem algum erro
	 */
	public String obtainAccessToken(MockMvc mockMvc, String username, String password) throws Exception {

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "password");
		params.add("client_id", clientId);
		params.add("username", username);
		params.add("password", password);

		ResultActions result = mockMvc
				.perform(post("/oauth2/token")
						.params(params)
						.with(httpBasic(clientId, clientSecret))
						.accept("application/json;charset=UTF-8"))
						.andExpect(status().isOk())
						.andExpect(content().contentType("application/json;charset=UTF-8"));

		String resultString = result.andReturn().getResponse().getContentAsString();

		JacksonJsonParser jsonParser = new JacksonJsonParser();
		return jsonParser.parseMap(resultString).get("access_token").toString();
	}
}
