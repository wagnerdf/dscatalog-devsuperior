package com.devsuperior.dscatalog.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

//Carrega o contexto completo da aplicação para executar testes de integração.
@SpringBootTest
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
}
