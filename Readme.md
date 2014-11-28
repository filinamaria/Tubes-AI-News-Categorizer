
Finalisasi kerjaan hari ini (27 November 2014):

1. bikin kelas java, nama terserah
2. bikin method buat connect ke database mysql
3. query db "select JUDUL, FULL_TEXT, ID_KELAS from artikel natural join artikel_kategori_verified"
4. Filter" yang digunakan (urutan penting):
    1. numericToNominal untuk atribut ID_KELAS (atribut no. 3/ last)
    2. nominalToString untuk artibut JUDUL, FULL_TEXT (atribut no.1,2)
    3. Filter stringToWordVector dengan properti default untuk atribut JUDUL dan FULL_TEXT
    4. Set atribut ID_KELAS sebagai label
5. bikin stub buat demo besok yang isinya:
    1. Classifier naiveBayesMultinomial dengan properti default

 
