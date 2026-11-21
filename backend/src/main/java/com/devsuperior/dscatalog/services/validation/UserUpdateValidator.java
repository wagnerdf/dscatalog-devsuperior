package com.devsuperior.dscatalog.services.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.resources.exceptions.FieldMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador personalizado para a atualização de usuários.
 * 
 * Verifica se o e-mail informado já pertence a outro usuário cadastrado.
 * Utiliza o identificador do usuário presente na URL para permitir que
 * o próprio usuário mantenha seu e-mail durante a atualização.
 */
public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private UserRepository repository;
	
	/**
	 * Inicializa o validador personalizado.
	 *
	 * @param ann anotação de validação associada ao validador
	 */
	@Override
	public void initialize(UserUpdateValid ann) {
	}

	/**
	 * Verifica se os dados informados para atualização do usuário são válidos.
	 * 
	 * Obtém o identificador do usuário a partir das variáveis da URL e consulta
	 * o repositório para verificar se o e-mail informado já está cadastrado
	 * para outro usuário.
	 *
	 * @param dto objeto contendo os dados do usuário a serem validados
	 * @param context contexto utilizado para registrar as violações de validação
	 * @return {@code true} se não houver erros de validação; {@code false} caso contrário
	 */
	@Override
	public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {
		
		@SuppressWarnings("unchecked")
		var uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		long userId = Long.parseLong(uriVars.get("id"));
		
		List<FieldMessage> list = new ArrayList<>();
		
		User user = repository.findByEmail(dto.getEmail());
		if(user != null && userId != user.getId()) {
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
