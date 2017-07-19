#!/usr/bin/env bash


function turnOffWifiFor30sec {
      echo "turn of wifi"
      echo `date +%Y-%m-%d:%H:%M:%S`
      nmcli radio wifi off
      sleep 30
      echo "turn on wifi"
      echo `date +%Y-%m-%d:%H:%M:%S`
      nmcli radio wifi on
}

while true; do turnOffWifiFor30sec; sleep $(shuf -i 120-600 -n 1); done