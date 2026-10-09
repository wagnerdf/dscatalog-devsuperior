
package com.devsuperior.dscatalog.repositories;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.devsuperior.dscatalog.entities.Product;

@DataJpaTest
public class ProductRepositoryTest {
	
	@Autowired
	private ProductRepository repository;
	
	private long exixtingId;
	
	@BeforeEach
	void setUp() throws Exception{
		exixtingId = 1L;
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenIdExists () {
		
		repository.deleteById(exixtingId);
		
		Optional<Product> result = repository.findById(exixtingId);
		Assertions.assertFalse(result.isPresent());
		
	}
}