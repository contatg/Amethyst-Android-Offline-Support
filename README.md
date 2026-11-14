<h1 align="center">Angel Aura Amethyst</h1>

<img src="https://github.com/AngelAuraMC/Amethyst-Android/blob/v3_openjdk/app_pojavlauncher/src/main/assets/amethyst.png" align="left" width="130" height="130" alt="Amethyst logo">

[![Android CI](https://github.com/AngelAuraMC/Amethyst-Android/workflows/Android%20CI/badge.svg)](https://github.com/AngelAuraMC/Amethyst-Android/actions)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/m/AngelAuraMC/Amethyst-Android)](https://github.com/AngelAuraMC/Amethyst-Android/actions)
[![Crowdin](https://badges.crowdin.net/pojavlauncher/localized.svg)](https://crowdin.com/project/pojavlauncher)
[![Discord](https://img.shields.io/discord/724163890803638273.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/5ptqkyZxEy)

*From [Boardwalk](https://github.com/zhuowei/Boardwalk)'s ashes and [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)'s ruined reputation, here comes Amethyst!*

Amethyst is a launcher that allows you to play Minecraft: Java Edition on your Android and [iOS](https://github.com/AngelAuraMC/Amethyst-iOS) devices.

For more details, check out our [wiki](https://wiki.angelauramc.dev)!

## Table of Contents

* [Introduction](#introduction)
* [Getting Amethyst](#getting-amethyst)
* [Building](#building)
* [Current Status](#current-status)
* [Known Issues](#known-issues)
* [FAQ](#faq)
* [Contributing](#contributing)
* [Support](#support)
* [License](#license)
* [Credits & Dependencies](#credits--dependencies)
* [Roadmap](#roadmap)

## Introduction

* Amethyst is a Minecraft: Java Edition launcher for Android and iOS based on [Boardwalk](https://github.com/zhuowei/Boardwalk) and [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher)
* This launcher can launch almost all available Minecraft versions ranging from rd-132211 to 1.21 snapshots (including Combat Test versions)
* Modding via Forge and Fabric are also supported.
* This repository contains source code for Android. For iOS/iPadOS, check out [Amethyst-iOS](https://github.com/AngelAuraMC/Amethyst-iOS).

## Getting Amethyst

You can get Amethyst via two methods:

1. **Releases:** Download the latest prebuilt app from [nightly.link](https://nightly.link/AngelAuraMC/Amethyst-Android/workflows/android/v3_openjdk/app-debug%20%28recommended%29.zip) or select an older version from our [automatic builds](https://github.com/AngelAuraMC/Amethyst-Android/actions).
2. **Build from Source:** Follow the [building instructions](#building) below.

## Pasta dos mods

Ao abrir o Amethyst, conceda o acesso aos arquivos para usar
`Armazenamento interno/Amethyst/`. Coloque os mods do perfil padrão em
`Amethyst/mods/` e os mapas em `Amethyst/saves/`. Para modpacks com pasta
própria, use o botão de pasta do perfil e crie ou abra a subpasta `mods`.
Na primeira abertura, o app copia os arquivos do jogo da instalação
anterior e preserva os originais. A pasta `.minecraft` dentro de `Amethyst`
contém os arquivos que os instaladores Java esperam encontrar ali.

## Building

Esta branch do fork é voltada para o Minecraft Java no Android ARM64. Inclui
conta local, controles estilo Bedrock, Holy GL4ES, Zink com Vulkan do sistema
ou Turnip e ajustes opcionais para o Adreno 610. Veja [CLAUDE.md](CLAUDE.md)
para os pontos de entrada no código e o plano de testes.

Use JDK 21, Python 3.11+, Android SDK Platform 34 extensão 12, Build Tools 34.0.0
e NDK 27.3.13750724. Configure `ANDROID_HOME` ou `sdk.dir` no `local.properties`.
No Linux ou macOS, rode na raiz do repositório.

```sh
python3 scripts/fetch-performance-assets.py
bash scripts/languagelist_updater.sh
./gradlew :app_pojavlauncher:assembleOfflineRelease :app_pojavlauncher:lintOfflineRelease
```

O APK fica em `app_pojavlauncher/build/outputs/apk/offlineRelease/`.
A variante `offlineRelease` é compilada sem depuração e usa o ID de pacote,
a autoridade de armazenamento e a chave pública de teste do projeto original
para atualizar a instalação Offline Turnip anterior. A chave de teste já
consta no repositório de origem. Use uma identidade de assinatura própria
para um pacote de distribuição independente.

O script baixa o Java 8 ARM64 do APK de referência e o Holy GL4ES do Pojav,
com verificação SHA-256. O Turnip (`libvulkan_freedreno.so`) e os componentes
do Zink são fornecidos pela própria árvore de origem.

## Current Status

* [x] OpenJDK 8 Mobile port: ARM32, ARM64, x86, x86_64
* [x] OpenJDK 17 Mobile port: ARM32, ARM64, x86, x86_64
* [x] OpenJDK 21 Mobile port: ARM32, ARM64, x86, x86_64
* [x] Headless mod installer
* [x] Mod installer with GUI
* [x] OpenGL in OpenJDK environment
* [x] OpenAL (works on most devices)
* [x] Support for Minecraft 1.12.2 and below
* [x] Support for Minecraft 1.13 and above
* [x] Support for Minecraft 1.17 (22w13a) and above
* [x] Game surface zooming
* [x] New input pipe rewritten to native code
* [x] Rewritten entire controls system
* [ ] More to come!

## Known Issues

See our [issue tracker](https://github.com/AngelAuraMC/Amethyst-Android/issues) for a list of known issues and their current status.

## FAQ

See our [wiki](https://wiki.angelauramc.dev/) for more information.

## Contributing

Contributions are welcome! We welcome any type of contribution, not only code. For example, you can help improve the [wiki](https://github.com/AngelAuraMC/angelauramc.github.io/), contribute to the [translations](https://crowdin.com/project/pojavlauncher), or submit bug reports and feature requests.

Any code change should be submitted as a pull request. The description should explain what the code does and give steps to execute it.

## Support

For support, please join our [Discord server](https://discord.gg/5ptqkyZxEy).

## License

Amethyst is licensed under [GNU LGPLv3](https://github.com/AngelAuraMC/Amethyst-Android/blob/v3_openjdk/LICENSE).

## Credits & Dependencies

* [Boardwalk](https://github.com/zhuowei/Boardwalk) (JVM Launcher): Unknown License/[Apache License 2.0](https://github.com/zhuowei/Boardwalk/blob/master/LICENSE) or GNU GPLv2.
* [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher): [GLGPL](https://github.com/PojavLauncherTeam/PojavLauncher/blob/v3_openjdk/LICENSE)
* Android Support Libraries: [Apache License 2.0](https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt).
* [GL4ES](https://github.com/AngelAuraMC/gl4es): [MIT License](https://github.com/ptitSeb/gl4es/blob/master/LICENSE).
* [MobileGlues](https://github.com/MobileGL-Dev/MobileGlues): [LGPL-2.1 License](https://github.com/MobileGL-Dev/MobileGlues/blob/dev-es/LICENSE).
* [Krypton Wrapper](https://github.com/BZLZHH/NG-GL4ES): [MIT License](https://github.com/BZLZHH/NG-GL4ES/blob/main/LICENSE)
* [ANGLE](https://chromium.googlesource.com/angle/angle): [All Rights Reserved](app_pojavlauncher/src/main/assets/licenses/ANGLE_LICENSE).
* [OpenJDK](https://github.com/AngelAuraMC/openjdk-multiarch-jdk8u): [GNU GPLv2 License](https://openjdk.java.net/legal/gplv2+ce.html).
* [LWJGL3](https://github.com/AngelAuraMC/lwjgl3): [BSD-3 License](https://github.com/LWJGL/lwjgl3/blob/master/LICENSE.md).
* [LWJGLX](https://github.com/AngelAuraMC/lwjglx) (LWJGL2 API compatibility layer for LWJGL3): unknown license.
* [Mesa 3D Graphics Library](https://gitlab.freedesktop.org/mesa/mesa): [MIT License](https://docs.mesa3d.org/license.html).
* [bhook](https://github.com/bytedance/bhook) (Used for exit code trapping): [MIT license](https://github.com/bytedance/bhook/blob/main/LICENSE).
* [libepoxy](https://github.com/anholt/libepoxy): [MIT License](https://github.com/anholt/libepoxy/blob/master/COPYING).
* [virglrenderer](https://github.com/AngelAuraMC/virglrenderer): [MIT License](https://gitlab.freedesktop.org/virgl/virglrenderer/-/blob/master/COPYING).
* [OpenAL-Soft](https://github.com/kcat/openal-soft): [GNU GPLv2](app_pojavlauncher/src/main/assets/licenses/OPENAL-SOFT_GPL2)
  * [oboe](https://github.com/google/oboe): [Apache License 2.0](app_pojavlauncher/src/main/assets/licenses/OBOE_APACHE2).
  * [pfffft](https://bitbucket.org/jpommier/pffft/src/master/): [ARR](app_pojavlauncher/src/main/assets/licenses/PFFFT_LICENSE)
* [SDL3](https://github.com/libsdl-org/SDL): [zlib License](https://github.com/libsdl-org/SDL/blob/main/LICENSE.txt)
* [sdl2-compat](https://github.com/libsdl-org/sdl2-compat): [zlib License](https://github.com/libsdl-org/sdl2-compat/blob/main/LICENSE.txt)
* Thanks to [MCHeads](https://mc-heads.net) for providing Minecraft avatars.

## Roadmap

We are currently focusing on:

* Exploring new rendering technologies.

Future plans include:

* Improving stability and performance.
* Enhancing the mod installation experience.

We welcome community feedback and suggestions for our roadmap.  Please feel free to open a feature request in our [issue tracker](https://github.com/AngelAuraMC/Amethyst-Android/issues).
