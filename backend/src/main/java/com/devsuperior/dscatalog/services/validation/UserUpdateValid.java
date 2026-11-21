package com.devsuperior.dscatalog.services.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotação de validação personalizada para atualização de usuários.
 * <p>
 * Associa a validação à classe {@link UserUpdateValidator}, permitindo
 * aplicar regras específicas aos dados informados durante a atualização
 * de um usuário.
 */
@Constraint(validatedBy = UserUpdateValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)

public @interface UserUpdateValid {
	
	/**
	 * Define a mensagem padrão apresentada quando ocorre uma falha
	 * na validação.
	 *
	 * @return mensagem de erro da validação
	 */
	String message() default "Validation error";

	/**
	 * Define os grupos de validação aos quais esta anotação pertence.
	 *
	 * @return grupos de validação
	 */
	Class<?>[] groups() default {};

	/**
	 * Permite associar metadados adicionais à anotação de validação.
	 *
	 * @return informações adicionais da validação
	 */
	Class<? extends Payload>[] payload() default {};
}
