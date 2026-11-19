package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.RoleDTO;
import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.entities.Role;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.projections.UserDetailsProjection;
import com.devsuperior.dscatalog.repositories.RoleRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

/**
 * Serviço responsável pelo gerenciamento de usuários e pela autenticação
 * da aplicação, implementando a interface UserDetailsService do Spring Security.
 * Disponibiliza operações de consulta, cadastro, atualização e exclusão
 * de usuários, além de carregar os dados necessários para autenticação.
 */
@Service
public class UserService implements UserDetailsService {
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository repository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private AuthService authService;
	
	/**
	 * Busca todos os usuários de forma paginada.
	 *
	 * @param pageable parâmetros de paginação e ordenação dos resultados
	 * @return página contendo os usuários convertidos para UserDTO
	 */
	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(Pageable pageable) {
		Page<User> list = repository.findAll(pageable);
		return list.map(x -> new UserDTO(x));
	}
	
	/**
	 * Recupera os dados do usuário atualmente autenticado.
	 *
	 * @return dados do usuário autenticado convertidos para UserDTO
	 */
	@Transactional(readOnly = true)
	public UserDTO findMe() {
		User entity = authService.authenticated();
		return new UserDTO(entity);
	}

	/**
	 * Busca um usuário pelo seu identificador.
	 *
	 * @param id identificador do usuário
	 * @return dados do usuário encontrado
	 * @throws ResourceNotFoundException caso não exista usuário com o identificador informado
	 */
	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {		
		Optional<User> obj = repository.findById(id);
		User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new UserDTO(entity);
	}

	/**
	 * Cadastra um novo usuário, atribuindo inicialmente a permissão ROLE_OPERATOR.
	 * A senha recebida é codificada antes de ser armazenada no banco de dados.
	 *
	 * @param dto dados necessários para cadastrar o usuário
	 * @return dados do usuário criado
	 */
	@Transactional
	public UserDTO insert(UserInsertDTO dto) {
		User entity = new User();
		copyDtoToEntity(dto, entity);
		
		entity.getRoles().clear();
		Role role = roleRepository.findByAuthority("ROLE_OPERATOR");
		entity.getRoles().add(role);
		
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity = repository.save(entity);
		return new UserDTO(entity);
	}

	/**
	 * Atualiza os dados de um usuário existente.
	 *
	 * @param id identificador do usuário a ser atualizado
	 * @param dto novos dados do usuário
	 * @return dados do usuário atualizado
	 * @throws ResourceNotFoundException caso não exista usuário com o identificador informado
	 */
	@Transactional
	public UserDTO update(Long id, UserUpdateDTO dto) {
		try {
			User entity = repository.getReferenceById(id);
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new UserDTO(entity);
		}
		catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}		
	}

	/**
	 * Exclui um usuário pelo seu identificador.
	 * Verifica previamente sua existência e trata possíveis violações
	 * de integridade referencial no banco de dados.
	 *
	 * @param id identificador do usuário a ser excluído
	 * @throws ResourceNotFoundException caso o usuário não seja encontrado
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
	 * Copia os dados do DTO para a entidade User e atualiza
	 * as associações com as permissões informadas.
	 *
	 * @param dto objeto contendo os dados do usuário e suas permissões
	 * @param entity entidade de usuário que receberá os dados
	 */
	private void copyDtoToEntity(UserDTO dto, User entity) {

		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());
		
		entity.getRoles().clear();
		for (RoleDTO roleDto : dto.getRoles()) {
			Role role = roleRepository.getReferenceById(roleDto.getId());
			entity.getRoles().add(role);		
		}
	}

	/**
	 * Carrega os dados de um usuário pelo endereço de e-mail,
	 * incluindo suas permissões, para utilização no processo de autenticação.
	 *
	 * @param username endereço de e-mail utilizado como identificador do usuário
	 * @return dados do usuário e suas permissões para autenticação
	 * @throws UsernameNotFoundException caso não exista usuário associado ao e-mail informado
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);
		if (result.size() == 0) {
			throw new UsernameNotFoundException("Email not found");
		}
		
		User user = new User();
		user.setEmail(result.get(0).getUsername());
		user.setPassword(result.get(0).getPassword());
		for (UserDetailsProjection projection : result) {
			user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
		}
		
		return user;
	}	
}
