# Cidade Viva 0.6 Alpha 1 — serviços públicos

| Construção | Custo inicial | Manutenção por dia | Capacidade | Alcance por ruas |
| --- | ---: | ---: | ---: | ---: |
| Parque | 350 | 8 | 96 moradores | 6 passos |
| Escola | 1.100 | 24 | 180 moradores | 12 passos |
| Posto de saúde | 1.400 | 30 | 144 moradores | 12 passos |

Abra SERV. no rodapé, escolha o prédio e toque no mapa. A seleção começa em colocação de um lote. Os três serviços ocupam um terreno 1×1, podem substituir grama/mata e exigem demolição prévia de outra construção. APAGAR continua gratuito. A construção dos serviços é imediata nesta etapa.

## Atendimento e evolução

O atendimento percorre a rede de ruas: a distância é medida entre ruas adjacentes ao serviço e à residência. Edifício sem rua fica inativo. O algoritmo distribui capacidade por proximidade, desempata por coordenada e não conta o mesmo morador duas vezes em serviços do mesmo tipo. Mais unidades ampliam cobertura dentro de sua capacidade real. Uma residência pode receber atendimento parcial.

Parques acrescentam até 6 pontos de satisfação; escola e saúde acrescentam até 8 cada, proporcionais ao atendimento do lote. Casas no nível 2 só evoluem ao nível 3 quando todos os seus moradores atuais têm escola e saúde, além dos critérios existentes de demanda e satisfação. Prédios já no nível 3 não são rebaixados automaticamente pela falta do novo requisito; as regras existentes de deterioração permanecem.

Toda unidade construída gera manutenção, mesmo isolada. Com caixa zerado, os serviços suspendem atendimento; a cobrança de manutenção continua, permitindo ao jogador recuperar o orçamento removendo unidades. Serviços públicos ainda não criam empregos, não modelam alunos por idade, consultas individuais, doenças ou emergências.

ECON mostra moradores atendidos por tipo e manutenção de serviços separada de vias e edifícios. O inspetor informa capacidade, alcance e operação da unidade. Reclamações residenciais apontam falta de escola/saúde quando isso bloqueia o próximo nível. A simulação pausa enquanto o menu estiver aberto.

## Salvamentos e atualização

A versão escreve **CVT4**, que adiciona três tipos de terreno. Lê CVT1, CVT2 e CVT3, preservando dinheiro, dia, níveis, impostos e três espaços. O leitor legado não recebe tipos de serviço em arquivos CVT3. CRC32, arquivos temporários e backups permanecem.

**Instale sobre Cidade Viva Teste sem desinstalar. Depois que esta versão salvar, não volte a APKs anteriores: eles não leem CVT4.** Identidade e assinatura DEV preservadas; versionCode 18.

## Validação

6.966 verificações específicas cobrem capacidade, soma das alocações, rotas interrompidas, orçamento, caixa zerado, sobreposição, satisfação, evolução condicionada, demolição, replay e migração CVT3/CVT4. Regressões existentes e 21 verificações de controles reais permanecem no CI. O atlas anterior mantém os 125 sprites; três novos desenhos isométricos de serviços são gerados uma vez pelo renderizador, separados da simulação.

QA físico: conferir botão SERV., seleção de cada unidade, preço, aparência, inspetor, pausa do menu e leitura de ECON. Construir escola/saúde perto de casas nível 2, observar satisfação e evolução. Romper uma rua deve interromper atendimento; religar deve recuperá-lo. Salvar, reiniciar e trocar os três slots para verificar persistência. Conferir retrato e paisagem. Esta versão não foi executada em Android físico nesta entrega.
