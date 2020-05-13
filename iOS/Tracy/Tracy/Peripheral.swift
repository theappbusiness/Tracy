//
//  Peripheral.swift
//  Tracy
//
//  Created by Kane Cheshire on 12/05/2020.
//  Copyright © 2020 Kin and Carta. All rights reserved.
//

import CoreBluetooth
import UIKit

final class Peripheral: NSObject {

  private var peripheralManager: CBPeripheralManager!

  override init() {
    super.init()
    peripheralManager = CBPeripheralManager(delegate: self, queue: nil, options: [CBPeripheralManagerOptionRestoreIdentifierKey: "PeripheralRestoreID"])
  }

  private func startAdvertising() {
    guard !peripheralManager.isAdvertising else { return print("Already advertising, cannot start advertising") }
    let service = CBMutableService(type: serviceUUID, primary: true)
    let value = Data(UIDevice.current.name.utf8)
    let characteristic = CBMutableCharacteristic(type: characteristicUUID, properties: .read, value: value, permissions: .readable)
    service.characteristics = [characteristic]
    peripheralManager.add(service)
  }

}

extension Peripheral: CBPeripheralManagerDelegate {

  func peripheralManager(_ peripheral: CBPeripheralManager, willRestoreState dict: [String : Any]) {
    print("Peripheral will restore state", dict)
  }

  func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
    print("Peripheral state updated", peripheral.state.rawValue)
    switch peripheral.state {
    case .poweredOn: startAdvertising()
    default: peripheral.removeAllServices()
    }
  }

  func peripheralManager(_ peripheral: CBPeripheralManager, didAdd service: CBService, error: Error?) {
    if let error = error {
      return print("Failed to add service", error, error.localizedDescription)
    }
    let adData = [CBAdvertisementDataServiceUUIDsKey: [serviceUUID]]
    peripheral.startAdvertising(adData)
  }

  func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
    if let error = error {
      return print("Failed to add service", error, error.localizedDescription)
    }
    print("Started advertising")
  }

}
