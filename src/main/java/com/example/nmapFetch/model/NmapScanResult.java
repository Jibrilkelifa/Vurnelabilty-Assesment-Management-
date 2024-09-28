package com.example.nmapFetch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="nmap_scan_results")
public class NmapScanResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String target;
    private String port;
    private String service;
    private String status;


    public NmapScanResult(String target, String port, String service, String status) {
        this.target = target;
        this.port = port;
        this.service = service;
        this.status = status;
    }
}
