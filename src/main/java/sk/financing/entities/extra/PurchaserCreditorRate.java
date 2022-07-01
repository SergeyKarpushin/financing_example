package sk.financing.entities.extra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.financing.entities.Purchaser;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaserCreditorRate {

    private Purchaser purchaser;

    private double rateInBps;
}
