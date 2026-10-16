// package com.crm.integration.repository;

// import com.crm.integration.model.AgMensApp;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// import java.time.LocalDateTime;
// import java.util.List;

// @Repository
// public interface AgMensAppRepository
//         extends JpaRepository<AgMensApp, Long> {

//     List<AgMensApp>
//     findByDtEnvioIsNullAndDtInclusaoIsNotNullAndDtInclusaoGreaterThanEqualAndChaveUnicaIsNotNullOrderByDtInclusaoAsc(
//             LocalDateTime dataLimite,
//             Pageable pageable
//     );
// }

package com.crm.integration.repository;

import com.crm.integration.model.AgMensApp;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgMensAppRepository
        extends JpaRepository<AgMensApp, Long> {

    List<AgMensApp>
    findByProcAndDtEnvioIsNullAndDtInclusaoIsNotNullAndDtInclusaoGreaterThanEqualAndChaveUnicaIsNotNullOrderByDtInclusaoAsc(
            String proc,
            LocalDateTime dataInicial,
            Pageable pageable
    );

    boolean existsByIdRegistro(String idRegistro);
}
