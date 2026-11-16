package com.devsuperior.dscatalog.resources;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.services.CategoryService;

/**
 * Resource responsável pelos endpoints de gerenciamento de categorias.
 * Disponibiliza operações de consulta, inserção, atualização e exclusão.
 */
@RestController
@RequestMapping(value = "/categories")
public class CategoryResource {

	/**
     * Serviço responsável pelas operações relacionadas às categorias.
     */
	@Autowired
	private CategoryService service;
	
	/**
     * Retorna todas as categorias cadastradas.
     *
     * @return resposta HTTP 200 contendo a lista de categorias
     */
	@GetMapping
	public ResponseEntity<List<CategoryDTO>> findAll() {
		List<CategoryDTO> list = service.findAll();		
		return ResponseEntity.ok().body(list);
	}

	/**
     * Busca uma categoria pelo seu identificador.
     *
     * @param id identificador da categoria
     * @return resposta HTTP 200 contendo a categoria encontrada
     */
	@GetMapping(value = "/{id}")
	public ResponseEntity<CategoryDTO> findById(@PathVariable Long id) {
		CategoryDTO dto = service.findById(id);
		return ResponseEntity.ok().body(dto);
	}
	
	 /**
     * Insere uma nova categoria.
     * Operação permitida aos usuários com perfil ADMIN ou OPERATOR.
     *
     * @param dto objeto contendo os dados da categoria
     * @return resposta HTTP 201 contendo a categoria criada e sua URI
     */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
	@PostMapping
	public ResponseEntity<CategoryDTO> insert(@RequestBody CategoryDTO dto) {
		dto = service.insert(dto);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(dto.getId()).toUri();
		return ResponseEntity.created(uri).body(dto);
	}

	/**
     * Atualiza os dados de uma categoria existente.
     * Operação permitida aos usuários com perfil ADMIN ou OPERATOR.
     *
     * @param id  identificador da categoria que será atualizada
     * @param dto objeto contendo os novos dados da categoria
     * @return resposta HTTP 200 contendo a categoria atualizada
     */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
	@PutMapping(value = "/{id}")
	public ResponseEntity<CategoryDTO> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
		dto = service.update(id, dto);
		return ResponseEntity.ok().body(dto);
	}

	/**
     * Exclui uma categoria pelo seu identificador.
     * Operação permitida aos usuários com perfil ADMIN ou OPERATOR.
     *
     * @param id identificador da categoria que será excluída
     * @return resposta HTTP 204 (No Content) em caso de sucesso
     */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
} 
