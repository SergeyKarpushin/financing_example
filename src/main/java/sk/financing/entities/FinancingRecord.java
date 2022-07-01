package sk.financing.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancingRecord implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne
    private Invoice invoice;

    @OneToOne
    private Purchaser purchaser;

    @OneToOne
    private Creditor creditor;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date processingDate;
}
