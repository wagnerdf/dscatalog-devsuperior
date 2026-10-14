package com.devsuperior.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.devsuperior.dscatalog.tests.Factory;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private ProductService service;
	
	private ProductDTO productDTO;
	private PageImpl<ProductDTO> page;
	
	@BeforeEach
	void setup() throws Exception {
		
		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDTO));
		
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception{
		
		// Configura o serviço simulado para retornar a página de produtos.
	    when(service.findAllPaged(any())).thenReturn(page);
		
	    // Executa a requisição GET, informando que aceita uma resposta JSON.
	    ResultActions result =  
	    		mockMvc.perform(get("/products")
	    				.accept(MediaType.APPLICATION_JSON));
	       
	    // Verifica se a resposta HTTP possui o status 200 (OK).
	    result.andExpect(status().isOk());
	}

}
