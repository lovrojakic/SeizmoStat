#!/bin/bash

curl -k -X GET -u intstv_seizmostat:N1mfSG25G4uUQIvp \
https://djx.entlab.hr/m2m/data\?resourceSpec\=SiezmoStatIntensity \
-H "Accept: application/vnd.ericsson.m2m.output+json;version=1.1"