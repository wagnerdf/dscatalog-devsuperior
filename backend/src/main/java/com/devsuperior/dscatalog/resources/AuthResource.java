package com.devsuperior.dscatalog.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devsuperior.dscatalog.dto.EmailDTO;
import com.devsuperior.dscatalog.dto.NewPasswordDTO;
import com.devsuperior.dscatalog.services.AuthService;

import jakarta.validation.Valid;

/**
 * Resource responsável pelos endpoints de autenticação
 * e recuperação de senha dos usuários.
 */
@RestController
@RequestMapping(value = "/auth")
public class AuthResource {

	/**
     * Serviço responsável pelas operações de autenticação
     * e recuperação de senha.
     */
	@Autowired
	private AuthService authService;
	
	 /**
     * Solicita a criação de um token para recuperação de senha.
     *
     * @param body DTO contendo o e-mail do usuário
     * @return resposta HTTP 204 (No Content) em caso de sucesso
     */
	@PostMapping(value = "/recover-token")
	public ResponseEntity<Void> createRecoverToken(@Valid @RequestBody EmailDTO body) {
		authService.createRecoverToken(body);
		return ResponseEntity.noContent().build();
	}
	
	/**
     * Salva uma nova senha após validar o token de recuperação.
     *
     * @param body DTO contendo o token e a nova senha
     * @return resposta HTTP 204 (No Content) em caso de sucesso
     */
	@PutMapping(value = "/new-password")
	public ResponseEntity<Void> saveNewPassword(@Valid @RequestBody NewPasswordDTO body) {
		authService.saveNewPassword(body);
		return ResponseEntity.noContent().build();
	}
} 
