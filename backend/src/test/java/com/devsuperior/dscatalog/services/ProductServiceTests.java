package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {
	
	@InjectMocks
	private ProductService service;

	@Mock
	private ProductRepository repository;
	
	@Mock
	private CategoryRepository categoryRepository;
	
	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private PageImpl<Product> page;
	private Product product;
	
	@BeforeEach
	void setup() throws Exception{
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 4L;
		product = Factory.createProduct();
		page = new PageImpl<>(List.of(product));		 
	}
	

	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
	    // Configura o Mockito para retornar um Optional vazio ao pesquisar um produto inexistente.
	    Mockito.when(repository.findById(nonExistingId))
	           .thenReturn(Optional.empty());
	
	    // Verifica se o serviço lança ResourceNotFoundException ao não encontrar o produto.
	    Assertions.assertThrows(ResourceNotFoundException.class,
	            () -> service.findById(nonExistingId));
	
	    // Verifica se o repositório foi consultado com o ID do produto inexistente.
	    Mockito.verify(repository).findById(nonExistingId);
	}

	
	@Test
	public void findByIdShouldReturnProductDTOWhenIdExists() {
	    // Configura o Mockito para retornar o produto quando o ID existente for pesquisado.
	    Mockito.when(repository.findById(existingId))
	           .thenReturn(Optional.of(product));

	    // Executa a busca do produto pelo identificador.
	    ProductDTO result = service.findById(existingId);

	    // Verifica se o resultado não é nulo.
	    Assertions.assertNotNull(result);

	    // Verifica se o nome do produto retornado está correto.
	    Assertions.assertEquals("Phone", result.getName());

	    // Verifica se o preço do produto retornado está correto.
	    Assertions.assertEquals(800.5, result.getPrice());

	    // Verifica se o repositório foi consultado com o ID esperado.
	    Mockito.verify(repository).findById(existingId);
	}

	@Test
	public void insertShouldReturnProductDTO() {
	
	    // Cria um ProductDTO com dados fictícios utilizando a classe Factory.
	    ProductDTO dto = Factory.createProductDTO();
	
	    // Cria um produto fictício que será utilizado como retorno simulado do repository.save().
	    Product product = Factory.createProduct();
	
	    // Configura o Mockito para que repository.save() retorne o produto criado acima,
	    // independentemente do objeto Product recebido como argumento.
	    Mockito.when(repository.save(ArgumentMatchers.any(Product.class)))
	           .thenReturn(product);
	
	    // Executa o método insert() do ProductService, passando o DTO criado pela Factory.
	    // Internamente, o serviço copia os dados do DTO para uma entidade Product,
	    // salva essa entidade e retorna um novo ProductDTO.
	    ProductDTO result = service.insert(dto);
	
	    // Verifica se o resultado retornado pelo serviço não é nulo.
	    Assertions.assertNotNull(result);
	
	    // Verifica se o nome do produto retornado corresponde ao esperado.
	    Assertions.assertEquals("Phone", result.getName());
	
	    // Verifica se o preço do produto retornado corresponde ao esperado.
	    Assertions.assertEquals(800.5, result.getPrice());
	
	    // Verifica se o método save() do ProductRepository foi chamado
	    // pelo menos uma vez, recebendo algum objeto do tipo Product.
	    Mockito.verify(repository).save(ArgumentMatchers.any(Product.class));
	}


	
	@Test
	public void findAllPagedShouldReturnPage() {
		
		Mockito.when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
		
		Pageable pageable = PageRequest.of(0, 10);
		
		Page<ProductDTO> result = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(result);
		Mockito.verify(repository).findAll(pageable);
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
		Mockito.when(repository.existsById(dependentId)).thenReturn(true);
		
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		// Configura o Mockito para simular que o produto não existe no banco de dados.
	    // Quando existsById() receber nonExistingId, retornará false.
		Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);

		// Verifica se o método delete() lança ResourceNotFoundException
	    // ao tentar excluir um produto que não existe.
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});

	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {

		// Configura o Mockito para simular a existência do produto no banco de dados.
	    // Quando existsById() receber existingId, retornará true.
		Mockito.when(repository.existsById(existingId)).thenReturn(true);
		
		// Executa o método delete() e verifica se nenhuma exceção é lançada.
	    // Isso indica que a exclusão foi executada sem erros.
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		
		// Verifica se deleteById() foi chamado exatamente uma vez
	    // com o identificador do produto existente.
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
	}
}
