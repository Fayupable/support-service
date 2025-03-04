package com.fayupable.test.repository;

import com.fayupable.test.entity.SupportTicket;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ISupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

//    @EntityGraph(value = "SupportTicket.withDetails", type = EntityGraph.EntityGraphType.LOAD)
//    List<SupportTicket> findAll();

    @EntityGraph(value = "SupportTicket.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    Optional<SupportTicket> findById(UUID id);

    @Query("SELECT st FROM SupportTicket st " +
            "LEFT JOIN FETCH st.ticketDetails " +
            "LEFT JOIN FETCH st.resolutions " +
            "WHERE st.supportTicketId = :id")
    Optional<SupportTicket> findByIdWithDetails(UUID id);

    @EntityGraph(attributePaths = {"ticketDetails", "resolutions"})
    @Query("SELECT DISTINCT st FROM SupportTicket st " +
            "LEFT JOIN FETCH st.ticketDetails td " +
            "LEFT JOIN FETCH st.resolutions tr")
    List<SupportTicket> findAllWithDetails();
}
