//
//  Central.swift
//  Tracy
//
//  Created by Kane Cheshire on 12/05/2020.
//  Copyright © 2020 Kin and Carta. All rights reserved.
//

import CoreBluetooth

/// A Central is responsible for discovering to nearby Peripherals,
/// as well as connecting to them to request data.
///
/// A Central automatically starts scanning for nearby Peripherals
/// as soon as Bluetooth is reported as on, and will automatically
/// handle connecting to discoveries.
///
/// A Central employs background restoration, which means that if
/// the app itself is killed in the background by the system,
/// the system will automatically relaunch the app if a discovery or
/// other Central Bluetooth event occurs so that the Central is re-created
/// and can handle the event.
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
    default: discoveries.removeAll()
    }
  }

  func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
    print("Discovered peripheral", peripheral.identifier, RSSI, peripheral.name ?? "")
    guard discoveries.insert(peripheral).inserted else { return print("Peripheral has already been discovered, ignoring", peripheral.identifier) }
    centralManager.connect(peripheral)
  }

  func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
    print("Failed to connect to peripheral", peripheral.identifier, error?.localizedDescription ?? "")
  }

  func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
    print("Connected to peripheral", peripheral.identifier)
    peripheral.delegate = self
    peripheral.discoverServices([serviceUUID])
  }

  func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
    print("Disconnected from peripheral", peripheral.identifier, error?.localizedDescription ?? "")
  }

}

extension Central: CBPeripheralDelegate {

  func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
    if let error = error {
      return print("Failed to discover services", peripheral.identifier, error.localizedDescription)
    }
    guard let service = peripheral.services?.first(where: { $0.uuid == serviceUUID }) else {
      return print("Expected service does not exist in array of services", peripheral.identifier)
    }
    peripheral.discoverCharacteristics([characteristicUUID], for: service)
  }

  func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
    if let error = error {
      return print("Failed to discover characteristics", peripheral.identifier, service.uuid, error.localizedDescription)
    }
    guard let characteristic = service.characteristics?.first(where: { $0.uuid == characteristicUUID }) else {
      return print("Expected characteristic does not exist in array of characteristics", peripheral.identifier, service.uuid)
    }
    peripheral.readValue(for: characteristic)
  }

  func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
    if let error = error {
      return print("Failed to read value for characteristic", peripheral.identifier, characteristic.uuid, error.localizedDescription)
    }
    guard let value = characteristic.value else {
      return print("Expected value for characteristic does not exist", peripheral.identifier, characteristic.uuid)
    }
    print("Value for characteristic", value, String(data: value, encoding: .utf8) ?? "")
  }

  func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {
    print("Periphral modified services", peripheral.identifier, invalidatedServices)
  }

}
