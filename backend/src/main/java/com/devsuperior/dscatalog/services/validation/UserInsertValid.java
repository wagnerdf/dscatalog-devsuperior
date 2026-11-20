package com.devsuperior.dscatalog.services.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotação de validação personalizada utilizada para validar
 * os dados de inserção de usuários.
 * Associa a validação à classe UserInsertValidator,
 * permitindo aplicar regras específicas aos objetos anotados.
 */
@Constraint(validatedBy = UserInsertValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)

public @interface UserInsertValid {
	/**
	 * Mensagem apresentada quando a validação não é satisfeita.
	 *
	 * @return mensagem padrão do erro de validação
	 */
	String message() default "Validation error";

	/**
	 * Define os grupos de validação aos quais esta restrição pertence.
	 *
	 * @return grupos de validação
	 */
	Class<?>[] groups() default {};

	/**
	 * Permite associar informações adicionais à restrição de validação.
	 *
	 * @return dados adicionais da validação
	 */
	Class<? extends Payload>[] payload() default {};
}
