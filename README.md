# 🚀 ReGuard: Lightweight UAV Fault Detection Framework (Open-Source Rewrite)

**Author:** Wei Zhili （魏至立）  
**Affiliation:** School of Cyber Engineering, Xidian University  
**Advisor:** Prof. Li Teng (李腾)  
**Contact:** [2907762730@qq.com](mailto:2907762730@qq.com)

---

## 🧩 Project Overview

**ReGuard** is an open-source reimplementation and enhancement of a UAV fault detection framework originally designed for real-time onboard operation.  
This project aims to **rewrite and optimize the original system** using a lightweight, modular, and maintainable architecture suitable for embedded and edge devices.

> 🏆 Submitted to the Huawei Open Source Talent Award Program — *Open-Source Software Rewrite Track*.

---

## ✨ Key Features

- **Hybrid Model Architecture:**  
  Combines AutoRegressive with eXogenous input (ARX) and Long Short-Term Memory (LSTM) models for adaptive multi-fault detection.

- **Dynamic Weight Allocation (DDF):**  
  Utilizes Analytic Hierarchy Process (AHP)-based residual weighting for real-time adaptability across different fault scenarios.

- **Statistical Fault Verification:**  
  Integrates Z-score and Sequential Probability Ratio Test (SPRT) for robust detection under noisy flight conditions.

- **Lightweight Design:**  
  Optimized for Raspberry Pi and edge UAVs with sub-5ms detection latency.

- **Cross-Platform Implementation:**  
  Fully compatible with ROS-based UAVs, supporting PX4 log ingestion and flight replay.

---

## 🧠 System Architecture

```mermaid
graph TD
    A[Flight Data Stream] --> B[Preprocessing & Windowing]
    B --> C1[ARX Model]
    B --> C2[LSTM Model]
    C1 --> D[Residual Calculation]
    C2 --> D
    D --> E[DDF Weight Fusion]
    E --> F[Z-score & SPRT Detection]
    F --> G[Fault Report & Visualization]
