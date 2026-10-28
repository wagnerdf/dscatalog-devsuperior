package com.devsuperior.dscatalog.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.tests.Factory;
import com.devsuperior.dscatalog.tests.TokenUtil;

import tools.jackson.databind.ObjectMapper;

//Carrega o contexto completo da aplicação para realizar o teste de integração.
@SpringBootTest
//Configura o MockMvc para testar os endpoints HTTP sem iniciar um servidor real.
@AutoConfigureMockMvc
//Executa cada teste em uma transação, revertendo as alterações ao final.
@Transactional
public class ProductResourceIT {
	
	// Injeta o MockMvc para executar requisições HTTP durante os testes.
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	// ID de um produto existente no banco de dados de teste.
	private long existingId;
	// ID que não corresponde a nenhum produto existente.
	private long nonExistingId;
	// Quantidade esperada de produtos antes da execução do teste.
	private Long countTotalProducts;
	
	private String username, password, bearerToken;
	
	@BeforeEach
	void setup() throws Exception{
		
		// Define o ID de um produto existente.
		existingId = 1L;
		// Define um ID inexistente para testar o tratamento de erro.
		nonExistingId = 1000L;
		// Define a quantidade inicial esperada de produtos no banco.
		countTotalProducts = 25L;
		
		username = "maria@gmail.com";
		password = "123456";
		
		bearerToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
	}
	
	@Test
	public void findAllSholdReturnSortedPageWhenSortByName() throws Exception {
		
		// Executa uma requisição GET para buscar a primeira página de produtos,
        // com 12 itens por página e ordenação crescente pelo nome.
		ResultActions result =
				mockMvc.perform(get("/products?page=0&size=12&sort=name,asc")
						// Solicita que a resposta seja retornada no formato JSON.
		           		.accept(MediaType.APPLICATION_JSON));
		 
		// Verifica se a resposta HTTP possui o status 200 (OK).
		result.andExpect(status().isOk());
		// Verifica se a resposta informa a quantidade total esperada de produtos.
		result.andExpect(jsonPath("$.totalElements").value(countTotalProducts));
		// Verifica se a resposta contém a lista de produtos da página.
		result.andExpect(jsonPath("$.content").exists());
		
		// Verifica se o primeiro produto está ordenado pelo nome esperado.
		result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
        // Verifica se o segundo produto está ordenado pelo nome esperado.
		result.andExpect(jsonPath("$.content[1].name").value("PC Gamer"));
		// Verifica se o terceiro produto está ordenado pelo nome esperado.
		result.andExpect(jsonPath("$.content[2].name").value("PC Gamer Alfa"));
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists()  throws Exception {
		
		ProductDTO productDTO = Factory.createProductDTO();
		
		// Converte o DTO do produto para uma string no formato JSON.
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		// Armazena os valores esperados para validar a resposta da requisição.
		String expectedName = productDTO.getName();
		String expectedDescription = productDTO.getDescription();

		// Executa uma requisição PUT para atualizar o produto com o ID existente,
	    // enviando os dados do produto no corpo da requisição em formato JSON.
		ResultActions result =  
				mockMvc.perform(put("/products/{id}", existingId)
						.header("Authorization", "Bearer " + bearerToken)
		  				.content(jsonBody)
		  				.contentType(MediaType.APPLICATION_JSON)
		   				.accept(MediaType.APPLICATION_JSON));
		
		// Verifica se a resposta HTTP possui o status 200 (OK).
		result.andExpect(status().isOk());
		
		// Verifica se o produto retornado possui o ID, name e description esperado.
		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(expectedName));
		result.andExpect(jsonPath("$.description").value(expectedDescription));
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist()  throws Exception {
		
		// Cria um DTO de produto para utilizar na requisição de atualização.
		ProductDTO productDTO = Factory.createProductDTO();
		
		// Converte o DTO do produto para uma string no formato JSON.
		String jsonBody = objectMapper.writeValueAsString(productDTO);

		// Executa uma requisição PUT utilizando um ID que não existe no banco,
	    // enviando os dados do produto no corpo da requisição em formato JSON.
		ResultActions result =  
				mockMvc.perform(put("/products/{id}", nonExistingId)
						.header("Authorization", "Bearer " + bearerToken)
		  				.content(jsonBody)
		  				.contentType(MediaType.APPLICATION_JSON)
		   				.accept(MediaType.APPLICATION_JSON));
		
		// Verifica se a resposta HTTP possui o status 404 (Not Found).
		result.andExpect(status().isNotFound());
	}

}