#!/bin/bash

SERVICES=(
    "AccountService" 
    "EurekaServer" 
    "GatewayService"
    "GroupService"
    "paper-service" 
    "ReadingService"
)

for SERVICE in "${SERVICES[@]}"; do
    echo "==============================="
    echo "Deploying $SERVICE ..."
    echo "==============================="

    cd services/$SERVICE || exit

    if ! flyctl apps list | grep -q "$SERVICE"; then
        flyctl launch --name "$SERVICE" --region sin --now --ha=false --copy-config
    fi

    flyctl deploy --remote-only --detach

    cd ../../
done