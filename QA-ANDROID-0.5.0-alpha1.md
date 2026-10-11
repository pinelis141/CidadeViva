# QA Android — Cidade Viva 0.5.0 Alpha 1 Simulação

## Instalação e preservação

- [ ] Instalar sobre o **Cidade Viva Teste** já instalado; NÃO desinstalar.
- [ ] Abrir as três cidades e conferir caixa, dias, impostos, edifícios, níveis e ruas.
- [ ] Salvar e reabrir: todas as informações CVT3 e comportamento viário permanecem iguais.

## Teste de cidade-simulação

- [ ] Observar carros em ruas ligadas aos bairros ocupados, não em ramais sem funções.
- [ ] Em ECON, consultar empregados, viagem média (`VIAGEM`) e trechos congestionados (`TRANS`).
- [ ] Criar uma área comercial separada da cidade por rua interrompida; observar ausência de empregos até conectar a malha.
- [ ] Conectar os dois bairros com uma rua contínua; verificar que vagas são preenchidas e impostos comerciais/industriais mudam.
- [ ] Cortar a rua de conexão; observar desemprego e vagas simultaneamente, e eventual aviso de falta de ligação ao trabalho.
- [ ] Reconstruir a rua e observar recuperação sem reiniciar o aplicativo.
- [ ] Construir empregos muito distantes: não devem contratar todos os trabalhadores instantaneamente.
- [ ] Concentrar muitos moradores/trabalhos em um corredor para produzir `TRANS > 0`; avaliar qualidade urbana.
- [ ] Conferir que PAUSA interrompe simulação e animação; 1×/2×/4× funcionam.

## Interface e desempenho

- [ ] ECON, CIDADES e HUD legíveis; textos novos não sobrepõem botões.
- [ ] FPS confortável durante 5 minutos em uma cidade populosa; sem travamentos ao abrir ECON.
- [ ] Reconstrução/remoção de rua responde em tempo apropriado no celular.

## Fora do escopo deste marco

Não confundir efeito visual de carros com simulação individual de tráfego, engarrafamento dinâmico, ônibus ou serviços públicos. Estes ainda não foram implementados.

**Código, testes Java e APK Android CI não substituem o aceite físico.**
