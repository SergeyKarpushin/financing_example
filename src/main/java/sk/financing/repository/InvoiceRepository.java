package sk.financing.repository;

import sk.financing.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByIsFinancedTrue();

    List<Invoice> findByIsFinancedFalse();
}
