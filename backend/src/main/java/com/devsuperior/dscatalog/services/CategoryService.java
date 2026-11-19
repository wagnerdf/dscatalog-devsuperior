package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

/**
 * Serviço responsável pelas operações de gerenciamento de categorias.
 * Implementa as funcionalidades de consulta, inserção, atualização e exclusão
 * de categorias, realizando a comunicação com o repositório.
 */
@Service
public class CategoryService {

	@Autowired
	private CategoryRepository repository;
	
	/**
	 * Busca todas as categorias cadastradas no banco de dados.
	 *
	 * @return lista contendo todas as categorias convertidas para CategoryDTO
	 */
	@Transactional(readOnly = true)
	public List<CategoryDTO> findAll() {
		List<Category> list = repository.findAll();
		return list.stream().map(x -> new CategoryDTO(x)).toList();
	}

	/**
	 * Busca uma categoria pelo seu identificador.
	 *
	 * @param id identificador da categoria
	 * @return dados da categoria encontrada
	 * @throws ResourceNotFoundException caso não exista categoria com o identificador informado
	 */
	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> obj = repository.findById(id);
		Category entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new CategoryDTO(entity);
	}

	/**
	 * Insere uma nova categoria no banco de dados.
	 *
	 * @param dto dados da categoria a ser cadastrada
	 * @return dados da categoria criada
	 */
	@Transactional
	public CategoryDTO insert(CategoryDTO dto) {
		Category entity = new Category();
		entity.setName(dto.getName());
		entity = repository.save(entity);
		return new CategoryDTO(entity);
	}

	/**
	 * Atualiza os dados de uma categoria existente.
	 *
	 * @param id identificador da categoria a ser atualizada
	 * @param dto novos dados da categoria
	 * @return dados da categoria atualizada
	 * @throws ResourceNotFoundException caso não exista categoria com o identificador informado
	 */
	@Transactional
	public CategoryDTO update(Long id, CategoryDTO dto) {
		try {
			Category entity = repository.getReferenceById(id);
			entity.setName(dto.getName());
			entity = repository.save(entity);
			return new CategoryDTO(entity);
		}
		catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}		
	}

	/**
	 * Exclui uma categoria pelo seu identificador.
	 * Verifica previamente sua existência e trata possíveis violações
	 * de integridade referencial no banco de dados.
	 *
	 * @param id identificador da categoria a ser excluída
	 * @throws ResourceNotFoundException caso a categoria não seja encontrada
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
}
