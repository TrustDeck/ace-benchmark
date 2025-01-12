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
