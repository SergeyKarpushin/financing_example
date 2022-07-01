package sk.financing.repository;

import sk.financing.entities.FinancingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface FinancingRecordRepository extends JpaRepository<FinancingRecord, Long> {

    FinancingRecord findFirstByOrderByProcessingDateDesc();

    List<FinancingRecord> findAllByProcessingDate(Date processingDate);
}
