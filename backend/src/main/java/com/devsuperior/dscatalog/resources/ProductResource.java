package com.devsuperior.dscatalog.resources;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;

import jakarta.validation.Valid;

/**
 * Recurso responsável por disponibilizar os endpoints de gerenciamento de produtos.
 * Permite consultar, inserir, atualizar e excluir produtos.
 */
@RestController
@RequestMapping(value = "/products")
public class ProductResource {

	/**
	 * Serviço responsável pelas operações relacionadas aos produtos.
	 */
	@Autowired
	private ProductService service;
	
	/**
	 * Busca todos os produtos de forma paginada, permitindo filtrar por nome e categoria.
	 *
	 * @param name nome ou parte do nome do produto utilizado como filtro
	 * @param categoryId identificador da categoria utilizada como filtro
	 * @param pageable parâmetros de paginação e ordenação dos resultados
	 * @return página contendo os produtos encontrados, com status HTTP 200 (OK)
	 */
	@GetMapping
	public ResponseEntity<Page<ProductDTO>> findAll(
			@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "0") String categoryId,
			Pageable pageable) {
		Page<ProductDTO> list = service.findAllPaged(name, categoryId, pageable);
		return ResponseEntity.ok().body(list);
	}

	/**
	 * Busca um produto pelo seu identificador.
	 *
	 * @param id identificador do produto
	 * @return dados do produto encontrado, com status HTTP 200 (OK)
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<ProductDTO> findById(@PathVariable Long id) {
		ProductDTO dto = service.findById(id);
		return ResponseEntity.ok().body(dto);
	}
	
	/**
	 * Insere um novo produto no catálogo.
	 * A operação exige permissão de ADMIN ou OPERATOR.
	 *
	 * @param dto dados do produto a ser inserido
	 * @return produto criado e URI de acesso ao recurso, com status HTTP 201 (Created)
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
	@PostMapping
	public ResponseEntity<ProductDTO> insert(@Valid @RequestBody ProductDTO dto) {
		dto = service.insert(dto);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(dto.getId()).toUri();
		return ResponseEntity.created(uri).body(dto);
	}

	/**
	 * Atualiza os dados de um produto existente.
	 * A operação exige permissão de ADMIN ou OPERATOR.
	 *
	 * @param id identificador do produto a ser atualizado
	 * @param dto novos dados do produto
	 * @return produto atualizado, com status HTTP 200 (OK)
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
	@PutMapping(value = "/{id}")
	public ResponseEntity<ProductDTO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
		dto = service.update(id, dto);
		return ResponseEntity.ok().body(dto);
	}

	/**
	 * Exclui um produto pelo seu identificador.
	 * A operação exige permissão de ADMIN ou OPERATOR.
	 *
	 * @param id identificador do produto a ser excluído
	 * @return resposta sem conteúdo, com status HTTP 204 (No Content)
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_OPERATOR')")
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
} 
