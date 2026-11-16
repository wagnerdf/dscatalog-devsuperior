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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.services.UserService;

import jakarta.validation.Valid;

/**
 * Recurso responsável por disponibilizar os endpoints de gerenciamento de usuários.
 * Permite consultar, cadastrar, atualizar e excluir usuários, além de recuperar
 * os dados do usuário autenticado.
 */
@RestController
@RequestMapping(value = "/users")
public class UserResource {

	/**
	 * Serviço responsável pelas operações relacionadas aos usuários.
	 */
	@Autowired
	private UserService service;
	
	/**
	 * Busca todos os usuários de forma paginada.
	 * A operação exige permissão de ADMIN.
	 *
	 * @param pageable parâmetros de paginação e ordenação dos resultados
	 * @return página contendo os usuários encontrados, com status HTTP 200 (OK)
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping
	public ResponseEntity<Page<UserDTO>> findAll(Pageable pageable) {
		Page<UserDTO> list = service.findAllPaged(pageable);
		return ResponseEntity.ok().body(list);
	}

	/**
	 * Busca um usuário pelo seu identificador.
	 * A operação exige permissão de ADMIN.
	 *
	 * @param id identificador do usuário
	 * @return dados do usuário encontrado, com status HTTP 200 (OK)
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@GetMapping(value = "/{id}")
	public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
		UserDTO dto = service.findById(id);
		return ResponseEntity.ok().body(dto);
	}
	
	/**
	 * Recupera os dados do usuário atualmente autenticado.
	 * A operação exige permissão de OPERATOR ou ADMIN.
	 *
	 * @return dados do usuário autenticado, com status HTTP 200 (OK)
	 */
	@PreAuthorize("hasAnyRole('ROLE_OPERATOR', 'ROLE_ADMIN')")
	@GetMapping(value = "/me")
	public ResponseEntity<UserDTO> findMe() {
		UserDTO dto = service.findMe();
		return ResponseEntity.ok().body(dto);
	}
	
	/**
	 * Cadastra um novo usuário.
	 * Os dados recebidos são validados antes da execução da operação.
	 *
	 * @param dto dados necessários para inserir o usuário
	 * @return usuário criado e URI de acesso ao recurso, com status HTTP 201 (Created)
	 */
	@PostMapping
	public ResponseEntity<UserDTO> insert(@Valid @RequestBody UserInsertDTO dto) {
		UserDTO newDto = service.insert(dto);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newDto.getId()).toUri();
		return ResponseEntity.created(uri).body(newDto);
	}

	/**
	 * Atualiza os dados de um usuário existente.
	 * A operação exige permissão de ADMIN.
	 *
	 * @param id identificador do usuário a ser atualizado
	 * @param dto novos dados do usuário
	 * @return usuário atualizado, com status HTTP 200 (OK)
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PutMapping(value = "/{id}")
	public ResponseEntity<UserDTO> update(@PathVariable Long id,@Valid @RequestBody UserUpdateDTO dto) {
		UserDTO newDto = service.update(id, dto);
		return ResponseEntity.ok().body(newDto);
	}

	/**
	 * Exclui um usuário pelo seu identificador.
	 * A operação exige permissão de ADMIN.
	 *
	 * @param id identificador do usuário a ser excluído
	 * @return resposta sem conteúdo, com status HTTP 204 (No Content)
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
} 
