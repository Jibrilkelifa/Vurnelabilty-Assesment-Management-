package com.example.nmapFetch.controller;

import com.example.nmapFetch.model.NmapScanResult;
import com.example.nmapFetch.repo.NmapScanResultRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/nmap")
public class NmapController {

    private final NmapScanResultRepository repository;

    public NmapController(NmapScanResultRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/scan")
    public ResponseEntity<List<NmapScanResult>> runNmapScan(@RequestParam String target, @RequestParam String profile) {
        List<NmapScanResult> result = new ArrayList<>();

        try {
            target = target.trim();
            String nmapCommand = getNmapCommand(profile, target);

            // Run Nmap command
            ProcessBuilder processBuilder = new ProcessBuilder(nmapCommand.split(" "));
            Process process = processBuilder.start();

            // Capture output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;

            // Log the entire output for debugging
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            System.out.println("Nmap Output:\n" + output.toString());

            // Parse the output (only lines containing "open")
            String[] outputLines = output.toString().split("\n");
            for (String outputLine : outputLines) {
                System.out.println(outputLine); // Log each line for debugging

                if (outputLine.contains("open")) {
                    String[] parts = outputLine.trim().split("\\s+");
                    String port = parts[0]; // The first part is the port
                    String status = parts[1]; // The second part is the status
                    String service = parts.length > 2 ? parts[2] : "unknown"; // Handle unknown services

                    // Create NmapScanResult object
                    NmapScanResult scanResult = new NmapScanResult(target, port, service, status);

                    // Save to database
                    repository.save(scanResult);

                    // Add to result list
                    result.add(scanResult);
                }
            }

            // Log any errors from the error stream
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line); // Log any errors for debugging
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private String getNmapCommand(String profile, String target) {
        switch (profile) {
            case "intense_scan":
                return "nmap -T4 -A -v " + target;
            case "intense_scan_udp":
                return "nmap -sS -sU -T4 -A -v " + target;
            case "intense_all_tcp":
                return "nmap -p 1-65535 -T4 -A -v " + target;
            case "intense_no_ping":
                return "nmap -T4 -A -v -Pn " + target;
            case "ping_scan":
                return "nmap -sn " + target;
            case "quick_scan":
                return "nmap -T4 -F " + target;
            case "quick_scan_plus":
                return "nmap -sV -T4 -O -F --version-light " + target;
            case "quick_traceroute":
                return "nmap -sn --traceroute " + target;
            case "regular_scan":
                return "nmap " + target;
            case "slow_comprehensive":
                return "nmap -sS -sU -T4 -A -v -p 1-65535 " + target;
            default:
                throw new IllegalArgumentException("Unknown profile: " + profile);
        }
    }
}
