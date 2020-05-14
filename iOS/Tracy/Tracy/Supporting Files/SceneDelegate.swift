//
//  SceneDelegate.swift
//  Tracy
//
//  Created by Kane Cheshire on 12/05/2020.
//  Copyright © 2020 Kin and Carta. All rights reserved.
//

import UIKit

final class SceneDelegate: UIResponder, UIWindowSceneDelegate {

  var window: UIWindow?

  func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
    print("Scene connecting")
  }

  func sceneDidDisconnect(_ scene: UIScene) {
    print("Scene disconnected")
  }

  func sceneDidBecomeActive(_ scene: UIScene) {
    print("Scene became active")
  }

  func sceneWillResignActive(_ scene: UIScene) {
    print("Scene resigning active")
  }

  func sceneWillEnterForeground(_ scene: UIScene) {
    print("Scene entering foreground")
  }

  func sceneDidEnterBackground(_ scene: UIScene) {
    print("Scene entering background")
  }

}
