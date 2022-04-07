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


5. Etter godkjenning, bestill kjøring av pa_ork_ba_01.
   * Se [Rutine - Orkestrering - Bestille kjøring av pa_ork_ba_01](http://wiki.spk.no/pages/viewpage.action?pageId=350323695)
   * Slett branch når PR merges
   
   
   
# Maler
Maler som brukes i ulike leveranser
* [prognosefakturering-2021](maler/fakturering/2021/prognosefakturering-2021) inneholder maler for prognosefakturering fra Panda i premieåret 2021
* [fakturering](maler/fakturering) inneholder maler for fakturering fra Panda


   
