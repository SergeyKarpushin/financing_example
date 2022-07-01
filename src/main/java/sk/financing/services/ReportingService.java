package sk.financing.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import sk.financing.entities.FinancingRecord;
import sk.financing.entities.Invoice;
import sk.financing.repository.FinancingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static sk.financing.Constants.BPS_DAILY_RATE_COEFFICIENT;

@Slf4j
@Service
public class ReportingService {

    @Autowired
    private FinancingRecordRepository financingRecordRepository;

    @Transactional
    public void printFinancingReport() {
        log.info("Latest financing results:");

        generateFinancingReport().stream()
                .map(FinancingReportDto::toString)
                .forEach(log::info);
    }

    private List<FinancingReportDto> generateFinancingReport() {

        // TODO
        // This is the reporting part that needs to be implemented. This method should return a list of DTO records.
        // The list should contain a record for each unique Purchaser/Creditor pair that participated in the latest
        // financing: if the Purchaser financed any invoices of this Creditor in this financing round, there should
        // be one record for this Purchaser/Creditor. If the Purchaser didn't finance any invoices of this Creditor,
        // there should be no record for this Purchaser/Creditor.

        List<FinancingReportDto> financingReportDtoList = new ArrayList<>();

        // TODO: implement search for the last financing round only
        Date latestProcessingDate = financingRecordRepository.findFirstByOrderByProcessingDateDesc().getProcessingDate();
        List<FinancingRecord> financingRecords = financingRecordRepository.findAllByProcessingDate(latestProcessingDate);
        for (FinancingRecord fr : financingRecords) {
            Invoice invoice = fr.getInvoice();
            double rate = fr.getPurchaser().getPurchaserFinancingSettings().stream()
                    .filter(pfs -> pfs.getCreditor().getId() == fr.getCreditor().getId())
                    .findFirst()
                    .get()
                    .getAnnualRateInBps() * BPS_DAILY_RATE_COEFFICIENT;

            double interest = invoice.getValueInCents() * rate * 0.0001;

            FinancingReportDto financingReportDto = new FinancingReportDto(
                    fr.getCreditor().getName(),
                    fr.getPurchaser().getName(),
                    (long) (invoice.getValueInCents() - interest),
                    (long) interest
            );

            financingReportDtoList.add(financingReportDto);
        }

        return financingReportDtoList;
    }

    @Data
    public static class FinancingReportDto {

        private final String purchaserName;
        private final String creditorName;
        // the total amount that the Purchaser has to pay to the Creditor for this financing
        private final long totalCreditorPaymentInCents;
        // the total amount of Purchaser interest for the latest financing of this Creditor
        private final long totalPurchaserInterestInCents;
    }

}
