package com.devsuperior.dscatalog.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

//Carrega o contexto completo da aplicação para executar testes de integração.
@SpringBootTest
@Transactional
public class ProductServiceIt {
	
	// Injeta o serviço real que será testado.
	@Autowired
	private ProductService service;
	
	// Injeta o repositório real para consultar os dados persistidos no banco.
	@Autowired
	private ProductRepository repository;

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
	public void deleteShouldDeleteResourceWhenIdExists() {
		
		// Executa a exclusão do produto com ID existente.
		service.delete(existingId);
		
		// Verifica se a quantidade de produtos diminuiu em uma unidade.
		Assertions.assertEquals(countTotalProducts - 1, repository.count());
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		// Verifica se o método delete() lança ResourceNotFoundException
	    // ao tentar excluir um produto que não existe.
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}
	
	@Test
	public void findAllPagedShouldReturnPageWhenPage0Size10(){
		
		// Cria uma requisição para a primeira página, com 10 produtos por página.
		PageRequest pageRequest = PageRequest.of(0, 10);
		
		// Executa a busca paginada de produtos no serviço.
		Page<ProductDTO> result = service.findAllPaged(pageRequest);
		
		// Verifica se a página retornada contém produtos.
		Assertions.assertFalse(result.isEmpty());
		// Verifica se a página retornada é a primeira (índice 0).
		Assertions.assertEquals(0, result.getNumber());
		// Verifica se o tamanho da página é de 10 produtos.
		Assertions.assertEquals(10, result.getSize());
		// Verifica se a quantidade total de produtos corresponde ao esperado.
		Assertions.assertEquals(countTotalProducts, result.getTotalElements());
	}
	
	@Test
	public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExist(){
		
		// Solicita a página de índice 50, com 10 produtos por página.
		PageRequest pageRequest = PageRequest.of(50, 10);
		
		// Executa a busca paginada de produtos no serviço.
		Page<ProductDTO> result = service.findAllPaged(pageRequest);
		
		// Verifica se a página retornada está vazia.
		Assertions.assertTrue(result.isEmpty());
	}
	
	@Test
	public void findAllPagedShouldReturnSortedPageWhenSortByName(){
		
		// Cria uma requisição para a primeira página, ordenada pelo nome.
		PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));
		
		// Executa a busca paginada e ordenada de produtos no serviço.
		Page<ProductDTO> result = service.findAllPaged(pageRequest);
		
		// Verifica se a página retornada contém produtos.
		Assertions.assertFalse(result.isEmpty());
		// Verifica se o primeiro produto está ordenado pelo nome esperado.
		Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
		// Verifica se o segundo produto está ordenado pelo nome esperado.
		Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
		// Verifica se o terceiro produto está ordenado pelo nome esperado.
		Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());
	}
}
