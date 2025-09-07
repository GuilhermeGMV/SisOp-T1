# SisOp-T1

# English

## how to clone and run
clone: git clone https://github.com/GuilhermeGMV/SisOp-T1/edit/main/README.md

compile: javac *.java

run: java Sistema


## how to run with different starting values
in Sistema.java (line 50) change parameters as:

1st parameter: memory size

2nd parameter: frame size

3rd parameter: delta, meaning how many instructions a program will run until it is schedulled

4th parameter: continuous, true means it will run with continuous schedulling and false means programs wait the exec or execAll command to execute

to change time between instructions in continuous execution change sleep miliseconds in Scheduler.java (line 55)

# Português

## como clonar e executar
clonar: git clone https://github.com/GuilhermeGMV/SisOp-T1/edit/main/README.md

compilar: javac *.java

executar: java Sistema

## como executar com valores iniciais diferentes
em Sistema.java (linha 50) altere os parâmetros:

1º parâmetro: tamanho da memória

2º parâmetro: tamanho do quadro (frame)

3º parâmetro: delta, significa quantas instruções um programa irá executar até ser escalonado

4º parâmetro: continuous, true significa que irá rodar com escalonamento contínuo e false significa que os programas aguardam o comando exec ou execAll para executar

para alterar o tempo entre instruções na execução contínua, altere o valor do sleep em milissegundos em Scheduler.java (linha 55) 
