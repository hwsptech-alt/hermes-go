package com.hermesgo.api.repositories;

import com.hermesgo.api.models.Carga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargaRepository extends JpaRepository<Carga, Long> {
}