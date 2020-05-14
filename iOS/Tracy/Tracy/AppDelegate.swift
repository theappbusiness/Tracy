//
//  AppDelegate.swift
//  Tracy
//
//  Created by Kane Cheshire on 12/05/2020.
//  Copyright © 2020 Kin and Carta. All rights reserved.
//

import UIKit

@UIApplicationMain
final class AppDelegate: UIResponder, UIApplicationDelegate {

  private let central = Central() // On Android, this happens in the BluetoothForegroundService. Since there's no Service architecture on iOS, we'll just start everything in the AppDelegate.
  private let peripheral = Peripheral()

  func application(_ application: UIApplication, willFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    print("App will finish launching", launchOptions ?? [:])
    return true
  }

  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    print("App finished launching", launchOptions ?? [:])
    return true
  }

  // MARK: UISceneSession Lifecycle

  func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
    UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
  }

}

