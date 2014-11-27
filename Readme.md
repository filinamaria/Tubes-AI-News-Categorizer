select JUDUL, FULL_TEXT, ID_KELAS from artikel natural join artikel_kategori_verified

1. connect ke db seperti biasa
2. query db "select JUDUL, FULL_TEXT, ID_KELAS from artikel natural join artikel_kategori_verified"
3. numericToNominal untuk atribut ID_KELAS (atribut no. 3/ last)
4. nominalToString untuk artibut JUDUL, FULL_TEXT (atribut no.1,2)
 