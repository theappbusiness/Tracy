//
//  Central.swift
//  Tracy
//
//  Created by Kane Cheshire on 12/05/2020.
//  Copyright © 2020 Kin and Carta. All rights reserved.
//

import CoreBluetooth

final class Central: NSObject {

  private var centralManager: CBCentralManager!
  private var discoveries = Set<CBPeripheral>()

  override init() {
    super.init()
    centralManager = CBCentralManager(delegate: self, queue: nil, options: [CBCentralManagerOptionRestoreIdentifierKey: "CentralRestoreID"])
  }

  private func startScanning() {
    guard !centralManager.isScanning else { return print("Already scanning, cannot start scanning") }
    print("Beginning scan for peripherals")
    centralManager.scanForPeripherals(withServices: [serviceUUID])
  }

}

extension Central: CBCentralManagerDelegate {

  func centralManager(_ central: CBCentralManager, willRestoreState dict: [String : Any]) {
    print("Central will restore state", dict)
  }

  func centralManagerDidUpdateState(_ central: CBCentralManager) {
    print("Central state updated", central.state.rawValue)
    switch central.state {
    case .poweredOn: startScanning()
    default: break
    }
  }

  func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
    guard discoveries.insert(peripheral).inserted else { return }
    print("Discovered peripheral", peripheral.identifier, RSSI, peripheral.name ?? "")
    // This is where you'd connect to the discovered device (peripheral) and transfer whatever data you needed to identify it as a trace
  }

}