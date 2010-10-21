---
Mit kell itt csinalni?

Igy tesztelem:

# Legyalulom a tablat, es beleteszek egy teszt-fajlpart:
( cat ../../../create.sql ; cat demo.sql ) | mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp

# Maga az alkalmazas:
python control_harness.py hunglish sw6x2the hunglishwebapp harness

---
Mit csinal ez?

Az upload tablabol kiszedi az ujakat.
A fajlokat bemasolja a harness adatkonyvtarba.
Elinditja a harness-t, mindegyik bidokumentumra kulon, kabe igy:
HARNESSDIR=/big1/Work/Pipeline/cvs/tcg/harness
python $HARNESSDIR/harness.py --graph=$HARNESSDIR/hunglishstategraph.txt --commands=$HARNESSDIR/hunglishcommands.txt --root=<ARGUMENTUM> --catalog catalog.tmp
Ha ez eredmenyt adott (azaz nem szallt el, es a minosegszurok nem csonkoltak nulla bajtosra),
akkor letrehoz neki egy uj doc-ot, es a mondatparokat bemasolja a bisen tablaba.
Amikor ez mind lezajlott, akkor meghivja az indexelot.


TODO:
- egy qfilter a mondatraszegmentalas utanra, ami eldobja az egeszet,
ha rosszak az aranyok mondatra vagy bajtra.

- egy qfilter, ami eldobja az egeszet, ha nagyon keves mondatpar van,
vagy nagyon rossz az aranya a parhuzamositas elotti mondatpar-szamnak.

- meghivni az indexer parancssort. Majd kiderul hogy hogy kell, windowson igy:
java -Xmx1500M -classpath lib:hunglish-0.1.0.jar:. hu.mokk.hunglish.lucene.Launcher

- harness kimenet logba. datum-uploadId.log
- indexer kimenet logba. datum.indexer.log
- maga a control_harness kimenet logba. datum.controller.log

- egy biztonsagi mentest kellene csinalni az indexrol, mielott meghivjuk az indexert.
