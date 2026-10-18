
package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.tests.Factory;

@DataJpaTest
public class ProductRepositoryTest {
	
	@Autowired
	private ProductRepository repository;
	
	private long exixtingId;
	private long nonExistingId;
	private long countTotalProducts;
	
	@BeforeEach
	void setUp() throws Exception{
		exixtingId = 1L;
		nonExistingId = 1000L;
		countTotalProducts = 25L;
	}
	
	@Test
	public void findByIdShouldReturnNonEmptyOptionalWhenIdExists() {

	    // Busca um produto pelo ID existente no banco de dados de teste.
	    Optional<Product> result = repository.findById(exixtingId);

	    // Verifica se o Optional retornado contém um produto.
	    Assertions.assertTrue(result.isPresent());
	}

	@Test
	public void findByIdShouldReturnEmptyOptionalWhenIdDoesNotExist() {

	    // Busca um produto utilizando um ID que não existe no banco de dados.
	    Optional<Product> result = repository.findById(nonExistingId);

	    // Verifica se o Optional retornado está vazio.
	    Assertions.assertFalse(result.isPresent());
	    Assertions.assertTrue(result.isEmpty());
	}
	
	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {
		
		// Cria um produto sem ID para testar a geração automática.
		Product product = Factory.createProduct();
		product.setId(null);
		
		// Salva o produto no banco de dados de teste.
		product = repository.save(product);
		
		// Verifica se o ID foi gerado e corresponde ao valor esperado.
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());
		
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenIdExists () {
		
		// Exclui o produto utilizando um ID existente.
		repository.deleteById(exixtingId);
		
		// Busca o produto excluído para verificar se não existe mais.
		Optional<Product> result = repository.findById(exixtingId);
		
		// Verifica se o produto não foi encontrado.
		Assertions.assertFalse(result.isPresent());
		
	}
}