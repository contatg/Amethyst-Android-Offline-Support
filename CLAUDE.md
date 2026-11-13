# Amethyst Offline Turnip para Android ARM64

Continue na branch `performance`. A base é a tag `v2026.07.19.0341` do
[Amethyst Android Offline Support](https://github.com/Minehacker765/Amethyst-Android-Offline-Support/tree/v2026.07.19.0341).
A prioridade é reduzir as quedas e o tempo de quadro do Minecraft Java 1.7.10
no Snapdragon 685 com Adreno 610, especialmente com Holy GL4ES.

## Código integrado

| Recurso | Pontos de entrada |
| --- | --- |
| Conta local da base Offline Support | `LocalLoginFragment.java`, `Tools.hasOnlineProfile()` |
| Seletor global e seleção por perfil | `LauncherPreferenceVideoFragment.java`, `ProfileEditorFragment.java`, `MainActivity.java`, `Tools.getCompatibleRenderers()` |
| Holy GL4ES, MobileGlues, Zink + Vulkan do sistema e Zink + Turnip | `JREUtils.java`, `egl_bridge.c`, `ctxbridges/gl_bridge.c` |
| Preset Adreno 610 | `LauncherPreferenceVideoFragment.java`, `LauncherPreferences.resolutionForShortSide()` |
| ADPF e métricas por quadro para Holy GL4ES no 1.7.10, afinidade opcional | `render_performance.c`, `bigcoreaffinity.c`, `JREUtils.java` |
| Controles estilo Bedrock e multitoque | `assets/bedrock-touch.json`, `MinecraftGLSurface.java`, `customcontrols/` |
| Compilação sem depuração com ID de atualização do APK anterior | `offlineRelease` em `app_pojavlauncher/build.gradle` |

O seletor global cobre perfis que usam a opção padrão. Para o 1.7.10 no Adreno
610, a detecção automática prefere Holy GL4ES quando disponível e respeita
a seleção explícita do perfil. O preset de vídeo ajusta o lado curto da área
renderizada para perto de 640 px e ativa SurfaceView. O layout Bedrock pode
ser alternado em Configurações → Controles.

## Desenvolvimento e validação

Siga [a seção de build do README](README.md#building). Use
`assembleOfflineRelease` para o APK de atualização. Valide que
`org.angelauramc.amethyst.debug`, a autoridade de armazenamento e a
assinatura são compatíveis com o APK Offline Turnip anterior. Instale a
nova versão por cima da antiga e verifique conta local, perfis, mundos e
controles salvos. Verifique também uma instalação limpa.

1. No Snapdragon 685, percorra a mesma área e use o mesmo mundo, distância
   de renderização e resolução em todas as comparações. Comece com
   Minecraft 1.7.10 e 2 chunks para reproduzir o teste de cerca de 70 FPS
   relatado pelo usuário. Meça durante pelo menos 10 minutos e registre
   versão do Android, renderizador, média, percentis de frame time e
   temperatura. Compare a primeira e a última janela de medição.
2. Compare os hints ADPF ligados e desligados, com afinidade de CPU
   desligada e depois ligada. Use `Render performance` no log para comparar
   tempo da thread de renderização e quadros acima de 25 ms. Um trace
   Perfetto ajuda a separar espera por CPU, GPU, apresentação e throttling.
   Se houver margem, experimente ajustar o alvo ADPF de 90 FPS ao limite
   efetivo do jogo antes de mexer na seleção de núcleos.
3. Teste joystique + câmera + pulo com três dedos, inventário, hotbar, pausa,
   retorno do segundo plano, cancelamento do toque e layouts personalizados.
   Revise os gestos conforme problemas observados no aparelho.
4. Compare Holy GL4ES, Zink com Vulkan do sistema e Zink + Turnip nas mesmas
   condições. Confirme no log a linha `AdrenoSupp: Loaded Turnip` ao
   selecionar o Turnip. O Freedreno OpenGL direto precisa de uma integração
   Gallium/EGL separada, de suporte do aparelho e de medição própria.
5. Teste a edição de perfil por renderizador, a migração das antigas opções
   Zink e o caminho de retorno ao modo automático em um aparelho sem Adreno.

A prioridade é a estabilidade do frame time. Mantenha os créditos e as
licenças distribuídas com o launcher e documente os resultados medidos.
