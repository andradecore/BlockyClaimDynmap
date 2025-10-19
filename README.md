# BlockyDynmap
BlockyDynmap é um plugin de integração para o servidor BlockyCRAFT que conecta os plugins [BlockyClaim](https://github.com/andradecore/BlockyClaim) e [BlockyFactions](https://github.com/andradecore/BlockyFactions) com o [Dynmap](https://dynmap.wiki.gg/wiki/Dynmap_API). Sua finalidade é gerar um mapa político e territorial dinâmico, exibindo todas as áreas protegidas e suas afiliações de facção em tempo real.

## Como Funciona
O plugin opera de forma 100% automática em segundo plano. Ele "escuta" as ações dos jogadores, como criar um terreno, entrar em uma facção ou vender uma propriedade. Com base nesses eventos, ele cria, atualiza e colore os marcadores de área no mapa do Dynmap, fornecendo uma visão estratégica e sempre atualizada do mundo do servidor.

## Funcionalidades
- **Visualização Automática de Claims**: Desenha e marca no mapa todas as áreas protegidas (`claims`) assim que são criadas.
- **Cores de Facção Dinâmicas**: Colore os marcadores dos terrenos com a cor oficial da facção do proprietário. Terrenos de jogadores sem facção são exibidos em uma cor cinza neutra.
- **Atualizações em Tempo Real**: O mapa é atualizado instantaneamente quando o status da facção de um jogador muda (ao entrar, sair, criar ou ter a facção dissolvida) ou quando um terreno muda de dono (compra, venda ou ocupação de área abandonada).
- **Marcadores Informativos**: As etiquetas dos marcadores no mapa incluem a tag da facção, e um pop-up exibe informações detalhadas como o nome do dono e o nome completo da facção.
- **Sincronização Completa**: Ao iniciar o servidor, o plugin verifica todos os terrenos existentes e os desenha no mapa, garantindo que a visualização esteja sempre consistente e correta, mesmo após uma reinicialização.

## Integração
BlockyDynmap requer que os plugins `BlockyClaim`, `BlockyFactions` e `dynmap` estejam ativos no servidor para funcionar.

- **Gatilhos do BlockyClaim**: Qualquer ação que resulte na criação (`/claim confirm`) ou na mudança de dono (`/claim buy`, `/claim occupy`) de um terreno aciona uma atualização imediata no Dynmap.
- **Gatilhos do BlockyFactions**: Ações de um jogador que alterem sua afiliação, como criar uma facção (`/fac criar`), entrar nela (`/fac entrar`) ou sair (`/fac sair`), atualizam automaticamente a cor e as informações de todos os terrenos que ele possui. Quando uma facção é dissolvida, os terrenos de todos os seus ex-membros são revertidos para a cor neutra.

## Comandos e Permissões
Este plugin funciona inteiramente em segundo plano e não adiciona novos comandos ou permissões para os jogadores. Sua operação é totalmente automática e não requer configuração.

## Reportar bugs ou requisitar features
Reporte bugs ou sugira novas funcionalidades na seção [Issues](https://github.com/andradecore/BlockyDynmap/issues) do projeto.

## Contato:
- Discord: https://discord.gg/tthPMHrP