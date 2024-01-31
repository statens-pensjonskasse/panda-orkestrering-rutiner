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

* [hbf as asa](maler/fakturering/hbf_as_asa) inneholder maler for fakturering av hbf as asa fra Panda
* [hbf fls 2.0](maler/fakturering/hbf_fls_2_0) inneholder maler for fakturering av hbf fls 2.0 fra Panda
* [hbf øvrige](maler/fakturering/hbf_overige) inneholder maler for fakturering av hbf øvrige fra Panda
* [hbf stat](maler/fakturering/hbf_stat) inneholder maler for fakturering av hbf stat fra Panda
* [poa afp](maler/fakturering/poa_afp) inneholder maler for fakturering av poa afp frittstående og kjeder fra Panda
* [tjenestemannsorg](maler/fakturering/tjenestemannsorg) inneholder maler for fakturering av medlemsandel til tjenestemannsorganisasjoner og resterende kostnader som KDD skal belastes fra Panda
* [ad hoc templater](maler/fakturering/ad_hoc_templater) inneholder rutiner som bla. arkivering av termininfofil
* [premieprognoser fra panda](maler/fakturering/premieprognoser_fra_panda) inneholder rutiner for premieprognoser fra Panda 
* [rutiner for test](maler/fakturering/rutinefiler_for_test) inneholder nyttige rutiner som kan brukes i/til test 
* [deprecated rutiner](maler/fakturering/deprecated-rutiner) inneholder deprecated maler for prognosefakturering fra Panda i premieåret 2021, og panda fakturering fra Panda i 2022 og 2023.
