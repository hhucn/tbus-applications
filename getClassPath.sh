#!/bin/bash

for f in $(for i in $(locate *.jar); do grep -Hlsi dcaiti/vsimrti/fed/app $i; done); do echo -n ${f}:; done
