package com.devsuperior.dscatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.projections.ProductProjection;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.util.Utils;

import jakarta.persistence.EntityNotFoundException;

/**
 * Serviço responsável pelas operações de gerenciamento de produtos.
 * Implementa funcionalidades de consulta, inserção, atualização e exclusão,
 * além de realizar a associação entre produtos e categorias.
 */
@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	/**
	 * Busca produtos de forma paginada, permitindo filtrar pelo nome
	 * e pelos identificadores das categorias.
	 * Mantém a ordenação e os dados de paginação da consulta original.
	 *
	 * @param name nome ou parte do nome utilizado como filtro
	 * @param categoryId identificador ou lista de identificadores das categorias
	 * @param pageable parâmetros de paginação e ordenação
	 * @return página contendo os produtos encontrados convertidos para ProductDTO
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(String name, String categoryId, Pageable pageable) {

	    List<Long> categoryIds = Arrays.asList();
	    if (!"0".equals(categoryId)) {
	        categoryIds = Arrays.asList(categoryId.split(",")).stream().map(Long::parseLong).toList();
	    }

	    Page<ProductProjection> page = repository.searchProducts(categoryIds, name.trim(), pageable);
	    List<Long> productIds = page.map(x -> x.getId()).toList();

	    List<Product> entities = repository.searchProductsWithCategories(productIds);
	    
	    entities = (List<Product>) Utils.replace(page.getContent(), entities);
	    
	    List<ProductDTO> dtos = entities.stream().map(p -> new ProductDTO(p, p.getCategories())).toList();

	    return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
	}

	/**
	 * Busca um produto pelo seu identificador, incluindo suas categorias associadas.
	 *
	 * @param id identificador do produto
	 * @return dados do produto encontrado, incluindo suas categorias
	 * @throws ResourceNotFoundException caso não exista produto com o identificador informado
	 */
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new ProductDTO(entity, entity.getCategories());
	}

	/**
	 * Insere um novo produto no banco de dados.
	 * Copia os dados recebidos e associa as categorias informadas.
	 *
	 * @param dto dados do produto a ser cadastrado
	 * @return dados do produto criado
	 */
	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}

	/**
	 * Atualiza os dados de um produto existente, incluindo suas categorias.
	 *
	 * @param id identificador do produto a ser atualizado
	 * @param dto novos dados do produto
	 * @return dados do produto atualizado
	 * @throws ResourceNotFoundException caso não exista produto com o identificador informado
	 */
	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getReferenceById(id);
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
		}
		catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}		
	}


	/**
	 * Exclui um produto pelo seu identificador.
	 * Verifica previamente sua existência e trata possíveis violações
	 * de integridade referencial no banco de dados.
	 *
	 * @param id identificador do produto a ser excluído
	 * @throws ResourceNotFoundException caso o produto não seja encontrado
	 * @throws DatabaseException caso a exclusão viole alguma restrição de integridade do banco de dados
	 */
	@Transactional(propagation = Propagation.SUPPORTS)
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
		try {
			repository.deleteById(id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
	
	/**
	 * Copia os dados do DTO para a entidade Product e atualiza
	 * as associações com as categorias informadas.
	 *
	 * @param dto objeto contendo os dados do produto e suas categorias
	 * @param entity entidade de produto que receberá os dados
	 */
	private void copyDtoToEntity(ProductDTO dto, Product entity) {

		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());
		
		entity.getCategories().clear();
		for (CategoryDTO catDto : dto.getCategories()) {
			Category category = categoryRepository.getReferenceById(catDto.getId());
			entity.getCategories().add(category);			
		}
	}
}
