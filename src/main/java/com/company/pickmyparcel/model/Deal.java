package com.company.pickmyparcel.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;


@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Deal extends AuditModel {
    @Id
    @GeneratedValue(strategy =  GenerationType.SEQUENCE)
    private Integer dealId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SENDER_ID", referencedColumnName = "senderId")
    private Sender sender;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CARRIER_ID", referencedColumnName = "carrierId")
    private Carrier carrier;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "RECEIVER_ID", referencedColumnName = "receiverId")
    private Receiver receiver;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PARCEL_ID", referencedColumnName = "parcelId")
    private Parcel parcel;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PICKUP_LOCATION_ID", referencedColumnName = "pickUpLocationId")
    private PickUpLocation pickUpLocation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "DROP_LOCATION_ID", referencedColumnName = "dropLocationId")
    private DropLocation dropLocation;

    private Double dealTotal;

    private Double dealTotalAsCarrier;

    private Double dealDistanceInKilometers;

    private DealPaymentStatus dealPaymentStatus;


    private Date date;

    @Override
    public String toString() {
        return "Deal{" +
                "dealId=" + dealId +
                ", sender=" + sender +
                ", carrier=" + carrier +
                ", receiver=" + receiver +
                ", parcel=" + parcel +
                ", pickUpLocation=" + pickUpLocation +
                ", dropLocation=" + dropLocation +
                ", dealTotal=" + dealTotal +
                ", dealTotalAsCarrier=" + dealTotalAsCarrier +
                ", dealDistanceInKilometers=" + dealDistanceInKilometers +
                ", dealPaymentStatus=" + dealPaymentStatus +
                ", date=" + date +
                '}';
    }
}
