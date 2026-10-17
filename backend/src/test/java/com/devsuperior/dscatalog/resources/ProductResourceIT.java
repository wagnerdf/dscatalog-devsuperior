package com.devsuperior.dscatalog.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
	
	// ID de um produto existente no banco de dados de teste.
	private long existingId;
	// ID que não corresponde a nenhum produto existente.
	private long nonExistingId;
	// Quantidade esperada de produtos antes da execução do teste.
	private Long countTotalProducts;
	
	@BeforeEach
	void setup() throws Exception{
		
		// Define o ID de um produto existente.
		existingId = 1L;
		// Define um ID inexistente para testar o tratamento de erro.
		nonExistingId = 1000L;
		// Define a quantidade inicial esperada de produtos no banco.
		countTotalProducts = 25L;
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

}