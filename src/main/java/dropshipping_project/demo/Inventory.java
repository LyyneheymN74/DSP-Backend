package dropshipping_project.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is a 1-to-1 link. One Product has one Inventory record.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // Prevents infinite loops
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public Inventory(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}