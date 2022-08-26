#!/usr/bin/env bash

set -e
set -o nounset

#===========================================================
# Beskrivelse:
#===========================================================
# Kopierer flettede kontofoeringsfiler for 2022.08.01 fra
# arkivet for saa aa legge til Aarskull paa kolonne 22
# basert paa de fire foerste verdiene i kolonne 1.
# Deretter lagres den nye filen i arkivet under
# fakturering/pa_fak_ba_11/manuelle_tilpasninger/legg_til_aarskull/
# 2022.08.01/grunnlagsdata_2022-08-31_00-00-00-00
#===========================================================

#===========================================================
# Jira:
#===========================================================
# http://jira.spk.no/browse/SPKPRODUKT-5724
#===========================================================

#===========================================================
# Paakrevde miljovariabler maa vaere satt foer script kalles
#===========================================================
# SPK_DATA: Datapath for batch-packaging projektet.


PA_FAK_BA_11_AKRIV_STI="${SPK_DATA}/panda/arkiv/fakturering/pa_fak_ba_11"
NY_ARKIV_STI="manuelle_tilpasninger/legg_til_aarskull/2022.08.01/grunnlagsdata_2022-08-31_00-00-00-00"
GRUNNLAGSDATAMAPPE="<grunnlagsdatamappe_som_skal_kopieres>"
GRUNNLAGSDATAMAPPE_I_ARKIV="${PA_FAK_BA_11_AKRIV_STI}/flette_feilende_medlemmer/2022.08.01/${GRUNNLAGSDATAMAPPE}"
FLETTEDE_KONTOFOERINGER="flettede_kontofoeringer.csv"


mkdir -p ${PA_FAK_BA_11_AKRIV_STI}/${NY_ARKIV_STI}

echo "`date` - Starter kopiering av flettede kontofoeringer"

if [ -d ${GRUNNLAGSDATAMAPPE_I_ARKIV} ]
    then
        for fil in ${GRUNNLAGSDATAMAPPE_I_ARKIV}/flettede_kontofoeringer*; do

            zcat ${fil} \
            | awk -F';' '{print $0";"substr($1, 1, 4)}' \
            | sed 's/Pers$/Årskull/g' \
            | gzip \
            > ${PA_FAK_BA_11_AKRIV_STI}/${NY_ARKIV_STI}/$(basename $fil)
        done
    else
        echo "Finner ikke filene for flettede kontoføringer 2022.08.01 i arkivet"
fi

echo "`date` - Kopiering er ferdig"