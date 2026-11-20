package com.devsuperior.dscatalog.services.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.resources.exceptions.FieldMessage;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador personalizado para a inserção de usuários.
 * <p>
 * Verifica se o e-mail informado no cadastro já está registrado no banco
 * de dados. Caso exista, adiciona uma mensagem de validação ao campo
 * correspondente.
 */
public class UserInsertValidator implements ConstraintValidator<UserInsertValid, UserInsertDTO> {
	
	@Autowired
	private UserRepository repository;
	
	/**
	 * Inicializa o validador personalizado.
	 *
	 * @param ann anotação de validação associada ao validador
	 */
	@Override
	public void initialize(UserInsertValid ann) {
	}

	/**
	 * Verifica se os dados informados para inserção do usuário são válidos.
	 * 
	 * Consulta o banco de dados para verificar se o e-mail já está cadastrado.
	 * Caso esteja, adiciona uma mensagem de erro vinculada ao campo "email".
	 *
	 * @param dto objeto contendo os dados do usuário a serem validados
	 * @param context contexto utilizado para registrar as violações de validação
	 * @return {@code true} se não houver erros de validação; {@code false} caso contrário
	 */
	@Override
	public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {
		
		List<FieldMessage> list = new ArrayList<>();
		
		User user = repository.findByEmail(dto.getEmail());
		if(user != null) {
			list.add(new FieldMessage("email", "Email já existe"));
		}
		
		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();
	}
}
