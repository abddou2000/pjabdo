package com.example.login.Repositories;

import com.example.login.Models.Administrateur;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrateurRepository extends JpaRepository<Administrateur, String> {
    Optional<Administrateur> findByEmail(String email);
<<<<<<< HEAD
    List<Administrateur> findAllByEmail(String email);
=======
List<Administrateur> findAllByEmail(String email);
>>>>>>> 7b40c81313b0dd70c91cf4dad2c1d24de839dea4
}