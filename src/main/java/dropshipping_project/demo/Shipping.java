package dropshipping_project.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping")
@Getter
@Setter
@NoArgsConstructor
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is the "owning" side of the relationship
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore // Prevents infinite loops
    private Order order;

    @Column(nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String shippingCompany;

    private LocalDateTime shippedDate;

    public Shipping(Order order, String trackingNumber, String shippingCompany) {
        this.order = order;
        this.trackingNumber = trackingNumber;
        this.shippingCompany = shippingCompany;
        this.shippedDate = LocalDateTime.now();
    }
}