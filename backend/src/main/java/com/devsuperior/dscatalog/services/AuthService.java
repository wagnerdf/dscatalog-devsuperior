package com.devsuperior.dscatalog.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.EmailDTO;
import com.devsuperior.dscatalog.dto.NewPasswordDTO;
import com.devsuperior.dscatalog.entities.PasswordRecover;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.PasswordRecoverRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

/**
 * Serviço responsável pelas operações de autenticação e recuperação de senha.
 * Gerencia a geração de tokens de recuperação, a redefinição de senhas
 * e a identificação do usuário autenticado.
 */
@Service
public class AuthService {
	
	/**
	 * Tempo de validade do token de recuperação de senha, em minutos.
	 * Valor definido nas configurações da aplicação.
	 */
	@Value("${email.password-recover.token.minutes}")
	private Long tokenMinutes;
	
	/**
	 * URI utilizada para direcionar o usuário ao processo de recuperação de senha.
	 * Valor definido nas configurações da aplicação.
	 */
	@Value("${email.password-recover.uri}")
	private String recoverUri;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordRecoverRepository passwordRecoverRepository;
	
	@Autowired
	private EmailService emailService;

	/**
	 * Gera e envia um token para recuperação de senha do usuário.
	 * O token possui um prazo de validade definido nas configurações da aplicação
	 * e é armazenado antes do envio do e-mail.
	 *
	 * @param body objeto contendo o endereço de e-mail do usuário
	 * @throws ResourceNotFoundException caso não exista usuário associado ao e-mail informado
	 */
	@Transactional
	public void createRecoverToken(EmailDTO body) {
		
		User user = userRepository.findByEmail(body.getEmail());
		if (user == null) {
			throw new ResourceNotFoundException("Email não encontrado");
		}
		
		String token = UUID.randomUUID().toString();
		
		PasswordRecover entity = new PasswordRecover();
		entity.setEmail(body.getEmail());
		entity.setToken(token);
		entity.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));
		entity= passwordRecoverRepository.save(entity);
		
		String text = "Acesse o link para definir uma nova senha\n\n"
				+ recoverUri + token + ". Validade de " + tokenMinutes + " minutos";
		
		emailService.sendEmail(body.getEmail(), "Recuperação de senha", text);
		
	}

	/**
	 * Define uma nova senha para o usuário utilizando um token de recuperação válido.
	 * A senha recebida é codificada antes de ser armazenada.
	 *
	 * @param body objeto contendo o token de recuperação e a nova senha
	 * @throws ResourceNotFoundException caso o token informado seja inválido ou esteja expirado
	 */
	@Transactional
	public void saveNewPassword(NewPasswordDTO body) {
		List<PasswordRecover> result = passwordRecoverRepository.searchValidTokens(body.getToken(), Instant.now());
		if (result.size() == 0) {
			throw new ResourceNotFoundException("Token inválido");
		}
		
		User user = userRepository.findByEmail(result.get(0).getEmail());
		user.setPassword(passwordEncoder.encode(body.getPassword()));
		user = userRepository.save(user);
	}
	
	/**
	 * Recupera os dados do usuário atualmente autenticado a partir do contexto
	 * de segurança da aplicação e das informações presentes no token JWT.
	 *
	 * @return entidade correspondente ao usuário autenticado
	 * @throws UsernameNotFoundException caso não seja possível identificar o usuário autenticado
	 */
	protected User authenticated() {
		  try {
		    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		    Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
		    String username = jwtPrincipal.getClaim("username");
		    return userRepository.findByEmail(username);
		  }
		  catch (Exception e) {
		    throw new UsernameNotFoundException("Invalid user");
		  }
		}
}
