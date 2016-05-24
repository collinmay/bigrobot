#!/bin/bash
cd /home/robotd
echo "Robotd script launched"
sleep 10 # wait for network and stuff. this is really stupid, but I'm not good enough at wrangling systemd to get this to work properly
ruby main.rb
