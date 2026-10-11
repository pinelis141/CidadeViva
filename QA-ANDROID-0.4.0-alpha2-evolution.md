# QA Android — Cidade Viva 0.4.0-alpha2-evolution

**Automação não equivale a teste físico.** Marcar checkboxes somente após execução no aparelho.

- [ ] Instalar APK debug assinado por cima do Cidade Viva Teste sem desinstalar, preservando os três slots e os impostos de CVT3.
- [ ] Abrir uma cidade **existente** e conferir que todos os prédios continuam nos mesmos lotes e níveis, apesar das novas cores/arquiteturas.
- [ ] Verificar ao menos 4 estilos distintos em um bairro com vários lotes ocupados, com fachadas e telhados identificáveis, não apenas recoloração sutil.
- [ ] Confirmar que terreno, água, ruas e veículos permanecem visualmente alinhados ao atlas de 116 sprites, sem cortes ou texturas embaralhadas.
- [ ] Tocar CENTRAR: câmera foca a cidade construída em vez de terreno vazio, com núcleo urbano visível **entre** o HUD superior e o dock.
- [ ] Abrir outro slot salvo e voltar: câmera enquadra a área construída correspondente e não reinicia dados.
- [ ] Pinça/arrasto continuam permitindo inspeção distante; CENTRAR reenquadra quando necessário.
- [ ] Observar barras R/C/I e palavra BAIXA/MEDIA/ALTA sem sobreposição em dispositivo vertical e horizontal (se suportado).
- [ ] Construir/expandir até cruzar 100, 250 ou 500 moradores; aviso aparece uma única vez no momento da conquista e desaparece sem tap bloqueado.
- [ ] Conferir a nova meta de moradores na linha de status quando não houver advertência de zonas sem rua.
- [ ] Salvar/reabrir após um marco não repete a conquista anterior; um novo marco posterior aparece normalmente.
- [ ] Testar 1×, 2×, 4×, PAUSA, ECON, CIDADES, inspeção e construção por pincel sem regressão.
- [ ] Gravar vídeo de dois minutos, verificar fluidez dos pedestres/carro, sprite de casa subindo de nível e ausência de crash.

**Limites conhecidos:** a arte tem sete variantes por categoria/nível; o mesmo bairro pode repetir alguns estilos. Metas são informativas, sem recompensas monetárias. Sem alteração de CVT3.
