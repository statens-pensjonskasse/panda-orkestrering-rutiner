import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

/*

#===========================================================
# Beskrivelse:
#===========================================================
# Leser gjennom eksisterende CSV filer for rettighet og ytelse
# og beregner ny MD5 sum. Produserer CSV fil med to kolloner,
# forrige og neste MD5 sum.
#
# Leser: $SPK_DATA/panda/arkiv/reserveberegning/pa_res_ba_01/panda-testdata-rettighet-ytelser/2022.09.01/grunnlagsdata_2022-09-02_04-25-05-35
# Skriver: $SPK_DATA/dvh/panda_datalevering/rettighet_ytelse/oppdaterteMD5Verdier.csv
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

#===========================================================
# Kjøring
#===========================================================
# Program kjøres som `java DVHMd5Oversetter.java`
# Krever java 17+

 */

class DVHMd5Oversetter {


    record PartsForrigeNeste(String[] parts, String forrige, String neste) {
    }

    record ForrigeNeste(String forrige, String neste) {
    }

    class Md5Checksum {
        /*
         * Vi ønsker ikke å dra inn større avhengighet og løser
         * https://www.geeksforgeeks.org/md5-hash-in-java/
         */
        public static String createMd5Of(final String input) {
            try {
                final MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(input.getBytes());

                // Convert byte array into signum representation
                final BigInteger no = new BigInteger(1, messageDigest);

                // Convert message digest into hex value
                final StringBuilder hashtext = new StringBuilder(no.toString(16));
                while (hashtext.length() < 32) {
                    hashtext.insert(0, "0");
                }
                return hashtext.toString();
            } catch (final NoSuchAlgorithmException e) {
                throw new RuntimeException("Klarte ikke å slå opp algoritme for MD5", e);
            }
        }
    }

    class OversettMD5 {
        public static List<ForrigeNeste> oversettMD5ForFil(final File csvFilTilDVH) throws IOException {
            final List<ForrigeNeste> forrigeNeste = new ArrayList<>();
            try (final FileInputStream fileStream = new FileInputStream(csvFilTilDVH)) {
                final InputStream inputStream = csvFilTilDVH.getName().endsWith(".gz") ? new GZIPInputStream(fileStream) : fileStream;
                final BufferedReader buffered = new BufferedReader(new InputStreamReader(inputStream, UTF_8));

                final List<ForrigeNeste> forrigeNestes = buffered
                        .lines()
                        .map(s -> s.split(";", -1))
                        .map(p -> new PartsForrigeNeste(p, "", ""))
                        .map(forrige -> new PartsForrigeNeste(forrige.parts(), forrige.parts()[3], forrige.neste()))
                        .map(neste -> {
                            final List<String> felterSomInngårIMd5 = List.of(
                                    neste.parts()[4],
                                    neste.parts()[5],
                                    neste.parts()[6],
                                    neste.parts()[7],
                                    neste.parts()[8],
                                    neste.parts()[9],
                                    neste.parts()[10],
                                    neste.parts()[11],
                                    neste.parts()[12],
                                    neste.parts()[13],
                                    neste.parts()[14],
                                    neste.parts()[15],
                                    neste.parts()[16],
                                    neste.parts()[17],
                                    neste.parts()[18],
                                    neste.parts()[19],
                                    neste.parts()[20],
                                    neste.parts()[21],
                                    neste.parts()[22],
                                    neste.parts()[23],
                                    neste.parts()[24],
                                    neste.parts()[25],
                                    neste.parts()[26],
                                    neste.parts()[27],
                                    neste.parts()[28],
                                    neste.parts()[29],
                                    neste.parts()[30],
                                    neste.parts()[31],
                                    neste.parts()[32],
                                    neste.parts()[33],
                                    neste.parts()[34],
                                    neste.parts()[35],
                                    neste.parts()[36]

                            );
                            final String nesteMD5 = Md5Checksum.createMd5Of(join(";", felterSomInngårIMd5));

                            // Må ta hennsyn til at en MD5 sum allerede er endret manuelt (http://jira.spk.no/browse/SPKPRODUKT-6430)
                            if (neste.forrige.equals("59a0f374b4008f3072d020a6ff45c9d8") && nesteMD5.equals("bfbaf6648318a027aea4ac678ab2a8b2")) {
                                return new PartsForrigeNeste(neste.parts(), "manuell_md5_hash_2d020a6ff45c9d8", nesteMD5);
                            }

                            return new PartsForrigeNeste(neste.parts(), neste.forrige(), nesteMD5);
                        })
                        .filter(f -> !f.parts()[0].equals("KjoeringsId"))
                        .map(f -> new ForrigeNeste(f.forrige(), f.neste()))
                        .toList();
                forrigeNeste.addAll(forrigeNestes);
            }

            return forrigeNeste;
        }

    }

    public static void main(String[] args) {

        final File målkatalog = new File(System.getenv("SPK_DATA"), "panda/arkiv/reserveberegning/pa_res_ba_01/panda-testdata-rettighet-ytelser/2022.09.01/grunnlagsdata_2022-09-02_04-25-05-35");

        final List<File> rettighet_ytelser_for_dvh = Arrays.stream(målkatalog.listFiles())
                .filter(f -> f.getName().startsWith("rettighet_ytelser_for_dvh"))
                .toList();

        if (rettighet_ytelser_for_dvh.isEmpty()) {
            System.err.println("Fant ingen filer som starter med rettighet_ytelser_for_dvh*");
            System.exit(2);
        }

        final File midlertidigFilTilDVH = new File(System.getenv("SPK_DATA"), "panda/midlertidig-md5oppslag.csv");

        try {

            final PrintWriter printWriter = new PrintWriter(midlertidigFilTilDVH);
            printWriter.println("Forrige MD5;Neste MD5");

            rettighet_ytelser_for_dvh
                    .stream()
                    .map(f -> {
                        try {
                            return OversettMD5.oversettMD5ForFil(f);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(res -> {
                        res.forEach(fn -> {
                            printWriter.println(String.format("%s;%s", fn.forrige(), fn.neste()));
                        });
                    });
            printWriter.close();
        } catch (IOException ex) {
            System.err.println("Klarte ikke å skrive til midlertidig fil. " + ex);
        }
        final File filTilDVH = new File(System.getenv("SPK_DATA"), "dvh/panda_datalevering/rettighet_ytelse_md5_oppdatering/oppdaterteMD5Verdier.csv");
        final File filTilArkiv = new File(System.getenv("SPK_DATA"), "panda/arkiv/reserveberegning/pa_res_ba_01/panda-testdata-rettighet-ytelser/2022.09.01/md5_mapping_gammel_ny/oppdaterteMD5Verdier.csv");
        try {
            if (!filTilArkiv.getParentFile().exists()) {
                if (!filTilArkiv.getParentFile().mkdirs()) {
                    throw new RuntimeException(String.format("Klarte ikke kopiere midlertidig fil %s til arkivet %s. Klarte ikke opprette mapper i arkivet", midlertidigFilTilDVH, filTilArkiv));
                }
            }
            Files.copy(midlertidigFilTilDVH.toPath(), filTilArkiv.toPath());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Klarte ikke kopiere midlertidig fil %s til arkivet %s", midlertidigFilTilDVH, filTilArkiv), e);
        }
        if (!filTilDVH.getParentFile().exists()) {
            if (!filTilDVH.getParentFile().mkdirs()) {
                throw new RuntimeException(String.format("Klarte ikke å flytte midlertidig fil %s til DVH %s. Klarte ikke opprette mapper hos DVH", midlertidigFilTilDVH, filTilDVH));
            }
        }
        if(midlertidigFilTilDVH.renameTo(filTilDVH)) {
            System.out.println("Ferdig! Produserte filen " + filTilDVH.getAbsolutePath());
        } else {
            System.err.println("Klarte ikke å flytte midlertidig fil til DVH. Se " + midlertidigFilTilDVH.getAbsolutePath() + " Klarte ikke å flytte til: " + filTilDVH.getAbsolutePath());
        }

    }

}