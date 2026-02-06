package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Ova metoda će ti vratiti sve reporte sortirane tako da najnoviji budu prvi
    List<Report> findAllByOrderByCreatedAtDesc();

    // Opciono: Ako želiš da vidiš sve prijave za jedan specifičan oglas
    List<Report> findByAdId(Long adId);


}