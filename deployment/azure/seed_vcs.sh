#!/bin/bash

if [ "$#" -lt 6 ]; then
  echo "Usage: sh $0 <PARTICIPANT_NAME> <REGION> <MGMT_API_PORT> <IDENTITY_PORT> <PARTICIPANT_DID_HOST> <GAIAX_DID_HOST>"
  exit 1
fi

participant_did_host="$5"
gaiax_did_host="$6"


## Function declarations to be used later
pushCredential() {
  local participant="$1"
  local credential="$2"
  echo "Push credentials to $participant at port $identityPort"
  echo "    credential: ${credential}"
  echo
  local participant_did="did:web:$participant_did_host"
  local gaiax_did="$3"

  java -jar identity-hub-cli.jar -s="$ihUrl" vc add \
    -c="$credential" \
    -b="$participant_did" \
    -i="$gaiax_did" \
    -k="terraform/generated/dataspace/gaiaxkey.pem"
}

checkCredentials() {
  len=$(java -jar identity-hub-cli.jar -s="$ihUrl" vc list | jq -r '. | length')
  if [ "$len" -lt 2 ]; then
    echo "Wrong number of VCs, expected > 2, got ${len}"
    exit 2
  fi
}

# variables
gaiax_did="did:web:$gaiax_did_host"

participant="$1"
region="$2"
dataPort="$3"
identityPort="$4"
ihUrl="http://localhost:${identityPort}/api/v1/identity/identity-hub"

echo "### Handling participant \"$participant\" in region \"$region\""
echo "### Push seed data    "

# read the API KEY from the .env file that was generated during the resource generation phase
# cut into tokens at the "=" with cut and remove all double-quotes with tr
api_key=$(grep "EDC_API_AUTH_KEY" "docker/$participant.env" | cut -d "=" -f2 | tr -d '"')
newman run \
  --folder "Publish Master Data" \
  --env-var data_management_url="http://localhost:$dataPort/api/v1/data" \
  --env-var storage_account="${participant}assets" \
  --env-var participant_id="${participant}" \
  --env-var api_key="$api_key" \
  ../data/MVD.postman_collection.json
echo

# hack - assume all containers have sequential management api dataPort configurations, check docker/docker-compose.yml for details!!!

id=$(uuidgen)
pushCredential "$participant" '{"id": "'$id'", "credentialSubject": {"gaiaXMember": "true"}}' "$gaiax_did"
id=$(uuidgen)
pushCredential "$participant" '{"id": "'$id'", "credentialSubject": {"region": "'$region'"}}' "$gaiax_did"

checkCredentials