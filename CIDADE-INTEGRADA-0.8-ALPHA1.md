# Cidade Viva 0.8 Alpha 1 — cidade integrada

Esta versão implementa as seis frentes autorizadas: edifícios 2×2, camadas do mapa, classes sociais e valorização, segurança/incêndios, saneamento e transporte público. Conserva os empréstimos e o balanceamento da 0.7.

## Construções 2×2

Quatro zonas contíguas do mesmo setor e nível podem se consolidar automaticamente, no máximo um prédio por dia. Não atravessam ruas, água, outro edifício ou tipos diferentes. Cada bloco reúne os quatro terrenos, sem criar população ou empregos extras na consolidação. Os nove desenhos novos correspondem a três setores e três níveis.

É necessário ativar energia e água e ter abastecimento real. Para residências, a avaliação prospectiva também exige satisfação de pelo menos 65 e capacidade de escola/saúde para a ocupação resultante. A mesma avaliação de capacidade ocorre antes de cada evolução: um prédio não aumenta a densidade contando com vagas ou recursos inexistentes. Demolição em qualquer parte remove todo o bloco e devolve 50% dos quatro investimentos originais; a prévia do pincel expande a área inteira e não duplica créditos.

## Camadas e classes sociais

**ECON → CAMADAS** oferece: normal, energia, água, escola/saúde, poluição, satisfação, valorização, segurança, lixo, esgoto, trânsito e ônibus. Verde indica boa condição e vermelho, deficiência; terreno sem aplicação ou rede em planejamento fica sem cor. A camada é apenas visual, não altera a simulação nem gasta dinheiro.

As residências são classificadas como populares, classe média ou alto padrão conforme nível e valorização local. Valorização considera satisfação, vegetação, poluição e segurança. As classes são derivadas dos imóveis, não moradores individuais com salários e idades. Moradias de classe média/alta ocupadas por trabalhadores geram um adicional fiscal de 10%/20% na base residencial. A soma das classes sempre coincide com a população real.

## Serviços novos

Em **SERV. → MAIS SERVIÇOS**:

| Estrutura | Construção | Manutenção diária | Capacidade |
| --- | ---: | ---: | ---: |
| Bombeiros | 1.000 | 18 | 480 unidades de atendimento |
| Polícia | 900 | 16 | 480 unidades de atendimento |
| Coleta de lixo | 800 | 16 | 600 unidades de atendimento |
| Tratamento de esgoto | 1.200 | 20 | 800 unidades de atendimento |
| Garagem de ônibus | 1.200 | 24 | 240 passageiros/dia |
| Ponto de ônibus | 80 | 1 | Acesso por caminhada de até 3 trechos |

As unidades de atendimento são população residencial e capacidade de empregos nos imóveis, permitindo que comércio e indústria também consumam saneamento e proteção. Fontes precisam de acesso por ruas, abastecimento das redes ativas e caixa municipal. Toda estrutura gera manutenção, mesmo inativa. Atendimento segue a rede e a capacidade disponível, com desempate determinístico por distância/coordenadas.

### Segurança e incêndios

Polícia reduz o risco mensurável de criminalidade, melhora a satisfação e a valorização. Não há agentes individuais ou ocorrências criminais roteirizadas nesta fase.

Incêndios são determinísticos por dia/local, raros, mais prováveis na indústria e bloqueiam a operação/crescimento do imóvel. Bombeiros com cobertura integral apagam o fogo antes do dano; sem resposta suficiente, quatro dias de incidente podem reduzir um nível do prédio. Um bloco recebe o dano como uma unidade. A perda gera o mesmo feedback vermelho de deterioração e o imóvel pode voltar a crescer depois.

Construir o primeiro quartel ativa os riscos; **ECON → CAMADAS → INCÊNDIOS** permite ligar/desligar. Desligar também apaga incidentes ativos, como opção de jogo. Riscos ficam desativados ao migrar uma cidade antiga.

### Lixo e esgoto

Cada sistema começa em planejamento e passa a ser exigido ao construir sua primeira instalação. Demolir todas as instalações não apaga a exigência. Atendimento insuficiente diminui satisfação e impede expansão de blocos 2×2. Centros de lixo/tratamento têm influência ambiental local; indústrias e tráfego intenso também poluem. Trata-se de coleta/tratamento agregado, sem caminhões individuais, volume acumulado de aterros ou tubulações editáveis.

## Ônibus e congestionamento

Uma garagem operacional e dois pontos em ruas realmente conectadas criam uma rede automática. Garagens no mesmo componente viário somam capacidade; redes desconectadas não compartilham frota. Uma garagem entre vias desconectadas vincula sua frota a uma delas, sem duplicar veículos ou transportar passageiros através do prédio.

Moradia e emprego precisam de acesso aos pontos da mesma rede. Passageiros são alocados em empregos de fato alcançáveis, dentro da capacidade da frota. Transporte coletivo permite deslocamentos de até 48 unidades de custo de viagem, contra 24 no deslocamento comum. Viagens de ônibus reduzem os veículos equivalentes nas ruas; os ônibus visuais percorrem trajetos reais entre pontos. Rotas são automáticas e a movimentação é representativa, não uma simulação de cada passageiro/horário/linha editável.

O cálculo viário faz uma primeira alocação e, quando encontra congestionamento, uma segunda busca ponderada pelos trechos carregados. Vias congestionadas encarecem o deslocamento, podem tornar empregos inalcançáveis e reduzem o alcance efetivo de bombeiros, polícia e coleta. Esgoto não é afetado pelo trânsito. Carros visuais desaceleram conforme o fluxo. Os limites de veículos/desenho protegem o desempenho Android.

## Salvamentos e atualização

Escreve **CVT7** e lê **CVT1–CVT6**, mantendo dinheiro, dívida, impostos, redes, população e os três espaços. Guarda o identificador do bloco, tempo de incêndio e ativações do saneamento. Rejeita blocos quebrados, referências fora do mapa, incêndio em lote vazio, flags inconsistentes e valores de dívida inválidos.

Identidade e chave de teste mantidas. Instalar sobre **Cidade Viva Teste**, sem desinstalar. Após salvar em CVT7, não voltar a um APK anterior.

## Verificação

Veja [auditoria da versão](AUDITORIA-0.8-ALPHA1.md). A validação em aparelho ainda é necessária: aparência, fluidez e facilidade de uso não são comprovadas pelos testes de lógica.
