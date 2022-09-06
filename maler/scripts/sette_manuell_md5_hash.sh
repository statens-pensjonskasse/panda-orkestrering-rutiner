#!/usr/bin/env bash

set -e
set -o nounset

#===========================================================
# Beskrivelse:
#===========================================================
# Sette manuell md5 hash paa en spesifikk linje i en
# spesifikk fil som er lagt paa DVH omraadet paa sharet
#===========================================================

#===========================================================
# Jira:
#===========================================================
# http://jira.spk.no/browse/SPKPRODUKT-6430
#===========================================================

#===========================================================
# Paakrevde miljovariabler maa vaere satt foer script kalles
#===========================================================
# SPK_DATA: Datapath for batch-packaging projektet.


DVH_STI="${SPK_DATA}/dvh/panda_datalevering/rettighet_ytelse/"
FILNAVN="rettighet_ytelser_for_dvh22-9f89d2c4-888a-45bd-ba6a-28236b0885b0.csv"
FILSTI="${DVH_STI}/${FILNAVN}"
MD5HASH="59a0f374b4008f3072d020a6ff45c9d8"
IDENTIFIER="12;1"
NY_MD5HASH="manuell_md5_hash_2d020a6ff45c9d8"

echo "Script starter"

echo "Konverterer ${FILSTI} til UTF-8"
iconv -f UTF-16LE -t UTF-8 -o ${FILSTI} ${FILSTI}

echo "Bytter ut md5-hash '59a0f374b4008f3072d020a6ff45c9d8' med 'manuell_md5_hash_2d020a6ff45c9d8' for linje med identifikator '12;1' i filen ${FILSTI}"
sed -i -E -e "/${MD5HASH}/s/^(.*)${MD5HASH}(.*${IDENTIFIER}.*)$/\1${NY_MD5HASH}\2/" ${FILSTI}

echo "Konverterer ${FILSTI} tilbake til UTF-16LE"
iconv -f UTF-8 -t UTF-16LE -o ${FILSTI} ${FILSTI}

echo "Script fullfoert"
