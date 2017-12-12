# calendar-based-microsim
A calendar-based approach to order the various models in a microsimulation for the evolution of a population.

# authors
Dumont M., University of Namur: conversion of the initial code of TransMob to add the possibility to code with the dates

Barthélemy J., SMART, University of Wollongong: write initial code

Huynh N., SMART, University of Wollongong: write initial code and the possibility to chose the order of modules

# execution
To execute the code, run: 

java -jar syntheticPopulation.jar $seed $n_year "input tables/" "Initialize_population.csv" "yearlyImmiHholds.csv" $order $out $birthday $deathday

With :
  - $seed : the seed for the pseudo-random number generator

  - $n_year : the number of year to model

  - "input tables/" : the location of the input tables

  - "Initialize_population.csv" : population at the beginning of the simulation

  - "yearlyImmiHholds.csv" : number of immigrating households per year

  - $order : order of the simulation (0: ageing, 1: death, 2: divorce, 3: marriage, 4: birth)

  - $out : willing location for the output files

  - $birthday : boolean indicating if birthday is considered

  - $deathday : boolean indicating if deathday is considered
