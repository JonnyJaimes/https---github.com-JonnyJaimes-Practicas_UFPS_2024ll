package com.admision.maestrias.api.pam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;


import java.io.Serializable;

/**
 * Clase de entidad que representa la tabla "authorities" en la base de datos.
 * @author Gibson Arbey, Juan Pablo Correa Tarazona
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
@Table(name = "authorities", uniqueConstraints= {@UniqueConstraint(columnNames= {"id", "authority"})})
public class RolEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Identificador único de la entidad.
     * Se genera automáticamente mediante el uso de la estrategia de generación de identificación IDENTITY.
     */
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

    /**
     * Autorizacion del rol
     */
    @NotEmpty
    @Column(nullable = false, length = 20)
	private String authority;

}
