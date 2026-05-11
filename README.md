# CAN Bus Frame Simulation & Error Detection 🚗🔌

A Java-based simulation of standard **CAN 2.0A** (Controller Area Network) frames. This project focuses on the bit-level construction of CAN frames, calculating the CRC-15 checksum, simulating data transmission by injecting burst errors, and validating received frames through error detection algorithms.

## 🚀 Features

* **Standard CAN 2.0A Frame Construction:** Builds complete frames including SOF, 11-bit Arbitration ID, Control Field (RTR, IDE, r0, DLC), Data Field, CRC-15, and EOF.
* **CRC-15 Calculation:** Implements the standard CAN polynomial `0x4599` to generate accurate 15-bit checksums for data integrity.
* **Random Frame Generation:** Automatically generates CAN frames with random IDs and data payloads based on a specified Data Length Code (DLC).
* **Burst Error Injection:** Simulates real-world transmission noise by injecting a specific number of bit flips within a defined burst window.
* **Error Detection:** 
  * **CRC Error Detection:** Recalculates the CRC of the received frame and compares it with the embedded CRC.
  * **Form Error Detection:** Verifies the integrity of fixed-format fields (CRC Delimiter, ACK Delimiter, and EOF).
* **Bit-level Manipulation:** Heavily utilizes `java.util.BitSet` for efficient and accurate bitwise operations.

## 📁 Project Structure

The project is divided into two main packages: `model` and `logic`.

### `model`
* `CanFrame.java` - Represents the CAN frame. It handles the assembly of various fields (ID, DLC, Payload) into a continuous sequence of bits (`BitSet`) and appends the calculated CRC and structural bits.

### `logic`
* `Crc15Calculator.java` - Handles the bitwise arithmetic required to compute the 15-bit Cyclic Redundancy Check using the CAN standard polynomial.
* `FrameGenerator.java` - A utility to generate random `CanFrame` objects for testing purposes.
* `ErrorGenerator.java` - Takes a valid CAN frame and artificially introduces a specified number of bit errors within a randomized burst sequence.
* `ErrorDetector.java` - Analyzes incoming frames to detect CRC mismatches and structural (form) errors. If an error is detected, it prevents the ACK slot from being pulled dominant.

## 🛠️ Technologies Used

* **Java:** Core language used for the simulation.
* **`java.util.BitSet`:** Used extensively for managing sequences of bits, allowing precise manipulation of the CAN frame at the bit level.

## ⚙️ How It Works (Simulation Flow)

1. **Generation:** `FrameGenerator` creates a `CanFrame` with a valid payload and ID.
2. **Assembly:** The `CanFrame` calculates its own CRC using the `Crc15Calculator` and assembles the full bit stream.
3. **Transmission (Error Injection):** The `ErrorGenerator` receives the bit stream and randomly flips bits to simulate a noisy bus environment.
4. **Reception (Error Detection):** The `ErrorDetector` reads the corrupted bit stream, recalculates the CRC, checks static fields, and flags the frame as invalid if errors are found.
