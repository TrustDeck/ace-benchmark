# ACE Benchmark

## Overview

This project implements a benchmark designed to stress test pseudonymization services and measure their throughput
across different workload scenarios. It allows users to evaluate the performance, scalability, and stability of a
service deployment under high loads. Through a connector abstraction, the benchmark can be applied to different
pseudonymization services. We provide an example implementation for the pseudonymization service [ACE](https://github.com/TrustDeck/ace).

## Features

- **Multiple connections**: Simulate a large number of concurrent connections.
- **Endpoint testing**: Configure different endpoints to be evaluated.
- **Customizable requests**: Can be used to benchmark different services by implementing new connectors.
- **Metrics Collection**: Gather and report metrics like transactions per second and used storage space.

## Prerequisites

- Java 17 or higher
- Maven (for building the project)

## Configuration

- Example configuration files can be found in the resources directory.

## How to cite

If you use this software in your research, please cite the accompanying articles:

Benchmark:
> Müller A, Wirth FN, Prasser F. **An Open Source Benchmark for Pseudonymization Services in Translational Research.** *Stud Health Technol Inform.* 2025 Aug 7;329:219-223. doi: 10.3233/SHTI250833. PMID: 40775851.

ACE:
> Müller A, Wündisch E, Wirth FN, Meier Zu Ummeln S, Weber J, Prasser F. **The Advanced Confidentiality Engine as a Scalable Tool for the Pseudonymization of Biomedical Data in Translational Settings: Development and Usability Study.** *J Med Internet Res.* 2025 Nov 5;27:e71822. doi:10.2196/71822. PMID: 41191920.