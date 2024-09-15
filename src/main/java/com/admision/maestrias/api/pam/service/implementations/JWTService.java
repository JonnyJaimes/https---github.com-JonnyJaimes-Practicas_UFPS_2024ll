package com.admision.maestrias.api.pam.service.implementations;

import com.admision.maestrias.api.pam.entity.UserEntity;
import com.admision.maestrias.api.pam.repository.UserRepository;
import com.admision.maestrias.api.pam.security.SecurityConstants;
import com.admision.maestrias.api.pam.service.interfaces.JWTServiceInterface;
import com.admision.maestrias.api.pam.shared.SimpleGrantedAuthorityMixin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;

@Service
public class JWTService implements JWTServiceInterface {

	@Autowired
	private UserRepository userR;

	// Genera una clave segura a partir del token secret en Base64
	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(Base64.getDecoder().decode(SecurityConstants.getTokenSecret()));
	}

	@Override
	public String create(Authentication auth) throws IOException {
		String username = ((User) auth.getPrincipal()).getUsername();
		int idUser = userR.findByEmail(username).getId();

		Collection<? extends GrantedAuthority> roles = auth.getAuthorities();

		Claims claims = Jwts.claims();
		claims.put("authorities", new ObjectMapper().writeValueAsString(roles));
		claims.put("idUser", idUser);

		return Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.signWith(getSecretKey(), SignatureAlgorithm.HS512)  // Usa la clave segura
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_DATE))
				.compact();
	}

	@Override
	public boolean validate(String token) {
		try {
			getClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public Claims getClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSecretKey())  // Usa la clave segura para validar
				.build()
				.parseClaimsJws(resolve(token))
				.getBody();
	}

	@Override
	public String getUsername(String token) {
		return getClaims(token).getSubject();
	}

	@Override
	public Integer getId(String token) {
		return (Integer) getClaims(token).get("idUser");
	}

	@Override
	public Collection<? extends GrantedAuthority> getRoles(String token) throws IOException {
		Object roles = getClaims(token).get("authorities");

		return Arrays.asList(new ObjectMapper()
				.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class)
				.readValue(roles.toString().getBytes(), SimpleGrantedAuthority[].class));
	}

	@Override
	public String resolve(String token) {
		if (token != null && token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			return token.replace(SecurityConstants.TOKEN_PREFIX, "");
		}
		return null;
	}

	public String getRoleFromToken(String token) {
		Claims claims = getClaims(token);
		List<Map<String, String>> authorities = (List<Map<String, String>>) claims.get("authorities");
		return authorities.get(0).get("authority").split("_")[1];  // Extract ROLE from "ROLE_ADMIN" or similar
	}

	public String generarToken(UserEntity usuario) {
		Claims claims = Jwts.claims();
		claims.put("idUser", usuario.getId());
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(usuario.getEmail())
				.signWith(getSecretKey(), SignatureAlgorithm.HS512)  // Usa la clave segura
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_DATE))
				.compact();
	}
}
