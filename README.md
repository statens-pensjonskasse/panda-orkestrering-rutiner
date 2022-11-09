# Panda orkestering rutiner

Støtte prosjekt for kjøring av [rutinefiler](http://git.spk.no/projects/PND/repos/panda-orkestrering/browse/dokumentasjon/rutiner/readme.md)
i [panda-orkestrering](http://wiki.spk.no/display/dok/SPK-Panda+Orkestrering+batch).

# Rutine

1. Opprett en branch fra master

   `git checkout -b rutine/<ITSOS>_<kort_beskrivelse>`


2. Kopier en mal fra `maler/` eller se egen rutinebeskrivelse.
    * Oppdater `variabler` seksjonen i rutinefilen med gyldige variabler.
    * Commit & Push kode til branch


3. Test kode på branch
    * Kode på branch kan kun testes fra Panda sitt DAT-miljø, logg på `dat@jee38t`
    * Kjør `runbatch.sh pa_ork_ba_01 -b <branch>`
    * Verifiser at batchen kjørte som forventet
   

4. Opprett PR til master (Team Sterope godkjenner).


5. Etter PRen er godkjent skal den merges inn i master-branchen. En cron-jobb kjører hvert 5. minutt og plukker opp rutinefiler i inn-mappa. Når rutina er
   kjørt ferdig av orkestreringsbatchen blir den automatisk flyttet til ferdig-mappa.

# Maler

Maler som brukes i ulike leveranser:

* [prognosefakturering-2021](maler/fakturering/deprecated-prognosefakturering-2021/prognosefakturering-2021) inneholder maler for prognosefakturering fra Panda i premieåret 2021
* [panda_fakturering-2022](maler/fakturering/deprecated-panda-fakturering-2022) inneholder maler for panda fakturering fra Panda i januar 2022 t.om. august 2022
* [fakturering](maler/fakturering) inneholder maler for fakturering fra Panda
