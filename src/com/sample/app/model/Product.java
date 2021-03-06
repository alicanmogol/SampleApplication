package com.sample.app.model;

import com.fererlab.dto.Model;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.persistence.*;

/**
 * acm | 1/16/13
 */

@Entity
@Table(name = "fer_et_product")
@XStreamAlias("product")
public class Product implements Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "serial_number")
    String serialNumber;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", serialNumber='" + serialNumber + '\'' +
                '}';
    }
}
