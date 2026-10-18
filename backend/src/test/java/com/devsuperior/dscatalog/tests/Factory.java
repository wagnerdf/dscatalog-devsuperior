package com.devsuperior.dscatalog.tests;

import java.time.Instant;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;

/**
 * Classe auxiliar responsável por criar objetos utilizados nos testes.
 */
public class Factory {
	
	/**
     * Cria e retorna um produto com dados predefinidos para os testes.
     * Também adiciona uma categoria ao produto.
     *
     * @return produto preenchido com dados de teste
     */
	public static Product createProduct() {
		
		Product product = new Product (
				1L, 
				"Phone", 
				"Good Phone", 
				800.5, 
				"https://img.com/img.png", 
				Instant.parse("2026-09-30T03:50:07.12345Z"));
		
		product.getCategories().add(new Category(2L, "Eletronics"));
		return product;
	}
	
	/**
     * Cria e retorna um DTO de produto utilizando os dados
     * do produto criado pelo método createProduct().
     *
     * @return DTO de produto preenchido com dados de teste
     */
	public static ProductDTO createProductDTO() {
		Product product = createProduct();
		return new ProductDTO(product, product.getCategories());
	}

}
