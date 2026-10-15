package com.devsuperior.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

import tools.jackson.databind.ObjectMapper;



@WebMvcTest(ProductResource.class)
public class ProductResourceTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private ProductService service;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private Long existingId;
	private Long nonExistingId;
	private ProductDTO productDTO;
	private PageImpl<ProductDTO> page;
	
	@BeforeEach
	void setup() throws Exception {
		
		existingId = 1L;
		nonExistingId = 2L;
		
		// Cria um ProductDTO com dados fictícios para utilizar nos testes.
		productDTO = Factory.createProductDTO();
		
		// Cria uma página contendo o ProductDTO para simular a listagem paginada.
		page = new PageImpl<>(List.of(productDTO));
		
		
		// Configura o serviço simulado para retornar a página de produtos
	    // sempre que findAllPaged() for chamado com qualquer argumento.
	    when(service.findAllPaged(any())).thenReturn(page);
	    
	    // Configura o serviço simulado para retornar o ProductDTO
	    // quando findById() receber o identificador de um produto existente.
		when(service.findById(existingId)).thenReturn(productDTO);
		
		// Configura o serviço simulado para lançar ResourceNotFoundException
	    // quando findById() receber o identificador de um produto inexistente.
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		// Configura o serviço simulado para retornar o DTO quando o produto existir.
		when(service.update(eq(existingId), any())).thenReturn(productDTO);
		
		// Configura o serviço simulado para lançar uma exceção quando o produto não existir.
		when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);
		
		
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception{
		
		
		
	    // Executa a requisição GET, informando que aceita uma resposta JSON.
	    ResultActions result =  
	    		mockMvc.perform(get("/products")
	    				.accept(MediaType.APPLICATION_JSON));
	       
	    // Verifica se a resposta HTTP possui o status 200 (OK).
	    result.andExpect(status().isOk());
	}
	
	@Test
	public void findByIdShouldReturnProductWhenIdExists() throws Exception {
		
		// Executa uma requisição HTTP GET para buscar o produto pelo ID existente.
	    // O método accept() informa que o teste aceita uma resposta em formato JSON.
		ResultActions result =  
	    		mockMvc.perform(get("/products/{id}", existingId)
	    				.accept(MediaType.APPLICATION_JSON));
		
		// Verifica se a resposta HTTP possui o status 200 (OK).
		result.andExpect(status().isOk());
		
		// Verifica se o JSON retornado contém as propriedades.
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		
		// Executa uma requisição HTTP GET para buscar um produto inexistente.
	    // O método accept() informa que o teste aceita uma resposta em formato JSON.
		ResultActions result =  
	    		mockMvc.perform(get("/products/{id}", nonExistingId)
	    				.accept(MediaType.APPLICATION_JSON));
		
		// Verifica se a resposta HTTP possui o status 404 (Not Found),
	    // indicando que o produto solicitado não foi encontrado.
		result.andExpect(status().isNotFound());
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists()  throws Exception {
		
		// Converte o DTO do produto para uma string no formato JSON.
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		// Converte o DTO do produto para uma string no formato JSON.
	    String jsonBody = objectMapper.writeValueAsString(productDTO);

	    // Executa a requisição PUT para atualizar o produto com o ID existente,
	    // enviando os dados do produto no corpo da requisição.
		ResultActions result =  
				mockMvc.perform(put("/products/{id}", existingId)
		  				.content(jsonBody)
		  				.contentType(MediaType.APPLICATION_JSON)
		   				.accept(MediaType.APPLICATION_JSON));
		
		// Verifica se a resposta HTTP possui o status 200 (OK).
		result.andExpect(status().isOk());
		
		// Verifica se a resposta contém o campo ID.
		result.andExpect(jsonPath("$.id").exists());
		
		// Verifica se a resposta contém o campo nome.
		result.andExpect(jsonPath("$.name").exists());
		
		// Verifica se a resposta contém o campo descrição.
		result.andExpect(jsonPath("$.description").exists());
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist()  throws Exception {
		
		// Converte o DTO do produto para uma string no formato JSON.
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		// Executa a requisição PUT utilizando um ID que não existe,
	    // enviando os dados do produto no corpo da requisição.
		ResultActions result =  
				mockMvc.perform(put("/products/{id}", nonExistingId)
		  				.content(jsonBody)
		  				// Informa que o corpo da requisição está no formato JSON.
		  				.contentType(MediaType.APPLICATION_JSON)
		  				// Solicita que a resposta seja retornada no formato JSON.
		   				.accept(MediaType.APPLICATION_JSON));
		
		// Verifica se a resposta HTTP possui o status 404 (Not Found).
		result.andExpect(status().isNotFound());
	}

}
