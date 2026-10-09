
package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.tests.Factory;

@DataJpaTest
public class ProductRepositoryTest {
	
	@Autowired
	private ProductRepository repository;
	
	private long exixtingId;
	private long countTotalProducts;
	
	@BeforeEach
	void setUp() throws Exception{
		exixtingId = 1L;
		countTotalProducts = 25L;
	}
	
	@Test
	public void saveShouldPersistWithAutoincrementWhenIdIsNull() {
		
		Product product = Factory.createProduct();
		product.setId(null);
		
		product = repository.save(product);
		
		Assertions.assertNotNull(product.getId());
		Assertions.assertEquals(countTotalProducts + 1, product.getId());
		
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenIdExists () {
		
		repository.deleteById(exixtingId);
		
		Optional<Product> result = repository.findById(exixtingId);
		Assertions.assertFalse(result.isPresent());
		
	}
}