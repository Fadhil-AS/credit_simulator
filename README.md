# Credit Simulator

Aplikasi console untuk menghitung simulasi cicilan kredit kendaraan (Motor/Mobil).
Ditulis dengan **Java 17+** murni, **tanpa framework/library eksternal** — hanya JDK
(`java.net.http.HttpClient` untuk web service) dan JUnit 5 untuk unit test.

---

## 1. Requirement

| Kebutuhan | Versi |
|---|---|
| JDK | 17 atau lebih baru |
| Maven | 3.8+ |
| Docker | 20+ |

Aplikasi dievaluasi pada Linux/MacOS. Karena mesin pengembangan penulis adalah Windows,
seluruh build dan run juga disediakan lewat **Docker** (lihat bagian 4).

---

## 2. Build

```bash
mvn clean package
```

Artifact: `credit-simulator-cli/target/credit-simulator.jar` (executable jar, sudah di-shade).

---

## 3. Menjalankan aplikasi

Script `credit_simulator` ada di root project dan di `bin/`. Keduanya otomatis melakukan
build bila jar belum tersedia.

```bash
# Tanpa file input (mode interaktif)
$ ./credit_simulator
$ bin/credit_simulator

# Dengan file input
$ ./credit_simulator file_inputs.txt
$ bin/credit_simulator file_inputs.txt
```

Bila `credit_simulator` belum executable setelah clone:

```bash
chmod +x credit_simulator bin/credit_simulator
```

Alternatif tanpa script:

```bash
java -jar credit-simulator-cli/target/credit-simulator.jar file_inputs.txt
```

---

## 4. Menjalankan lewat Docker

Docker dipakai sebagai pengganti mesin Linux. Build image sekaligus menjalankan unit test
di dalam container:

```bash
docker build -t credit-simulator .
```

Menjalankan mode interaktif (`-it` wajib supaya bisa mengetik input):

```bash
docker run --rm -it -v credit-simulator-sheets:/data credit-simulator
```

Menjalankan dengan file input:

```bash
docker run --rm -v "$PWD/file_inputs.txt:/app/file_inputs.txt:ro" credit-simulator /app/file_inputs.txt
```

Atau lewat Docker Compose:

```bash
docker compose run --rm credit-simulator                      # interaktif
docker compose run --rm credit-simulator /app/file_inputs.txt # file input
```

Volume `/data` menyimpan sheet supaya tidak hilang saat container dihapus.

---

## 5. Daftar perintah

Ketik `show` di dalam aplikasi untuk menampilkan daftar berikut.

| Perintah | Alias | Kegunaan |
|---|---|---|
| `show` | `help`, `menu` | Menampilkan seluruh perintah yang dapat digunakan |
| `simulate` | `hitung`, `start` | Menghitung cicilan dari input baru |
| `save <nama-sheet>` | — | Menyimpan hasil simulasi aktif ke sebuah sheet |
| `load <nama-file>` | — | Mengambil simulasi dari web service lalu menghitung otomatis |
| `sheets` | — | Menampilkan seluruh sheet tersimpan (`*` = sheet aktif) |
| `switch <nama-sheet>` | `sheet` | Pindah ke sheet lain dan menampilkan ulang hasilnya |
| `delete <nama-sheet>` | — | Menghapus sheet |
| `exit` | `quit`, `keluar` | Keluar dari aplikasi |

Setiap `save` menulis dua berkas di dalam project:

| Berkas | Fungsi |
|---|---|
| `data/sheets/<nama>.sheet` | `switch <nama>` dan `sheets`, langsung dari disk |
| `data/mock/<nama>.json` | `load <nama>` lewat HTTP, dan dapat di hit Json Web Service menggunakan Postman |

Skema JSON-nya sama dengan response endpoint pada soal, sehingga `load` memperlakukannya
identik. `delete <nama>` menghapus keduanya.

Direktori keduanya dapat diganti lewat environment variable `CREDIT_SIMULATOR_SHEET_DIR` dan
`CREDIT_SIMULATOR_MOCK_DIR`. Di dalam container keduanya diarahkan ke `/data`.

---

## 6. Format file input

Satu baris = satu input. Baris kosong dan baris berawalan `#` diabaikan. Contoh lengkap
ada pada [`file_inputs.txt`](file_inputs.txt):

```text
mobil
bekas
2023
100000000
3
25000000
save simulasi-mobil
exit
```

Urutan pertanyaan simulasi: jenis kendaraan, kondisi, tahun, jumlah pinjaman, tenor, DP.

Pada mode file, input yang tidak valid **membatalkan** simulasi (tidak mengulang pertanyaan),
supaya baris berikutnya tidak salah terbaca sebagai jawaban. Pada mode interaktif pertanyaan
diulang sampai input valid.

---

## 7. Aturan bisnis

1. Jenis kendaraan `Motor` / `Mobil`, alfabet, tidak membedakan huruf besar/kecil.
2. Kondisi kendaraan `Baru` / `Bekas`, alfabet, tidak membedakan huruf besar/kecil.
3. Tahun kendaraan wajib 4 digit angka dan tidak boleh melebihi tahun berjalan.
   Untuk kondisi **Baru**, tahun minimal `tahun berjalan - 1` — pada 2026 berarti 2025 atau 2026.
   Kondisi **Bekas** tidak dibatasi tahun minimal.
4. Jumlah pinjaman numerik, maksimal 1 miliar.
5. Tenor pinjaman 1–6 tahun.
6. DP kendaraan **Baru** minimal **35%** dari jumlah pinjaman.
7. DP kendaraan **Bekas** minimal **25%** dari jumlah pinjaman.
8. Suku bunga dasar: **Mobil 8%**, **Motor 9%**.
9. Suku bunga naik **0,1% setiap 1 tahun** dan **0,5% setiap 2 tahun**.

Progresi bunga untuk Mobil: `8%`, `8,1%`, `8,6%`, `8,7%`, `9,2%`, `9,3%`.

### Rumus cicilan

Mengikuti `Rumus.xlsx` yang dilampirkan pada soal:

```text
sisaPokok(1)   = jumlahPinjaman - DP
totalTahun(n)  = sisaPokok(n) * (1 + bunga(n))
cicilanBulan(n)= totalTahun(n) / (12 * (tenor - n + 1))
cicilanTahun(n)= cicilanBulan(n) * 12
sisaPokok(n+1) = totalTahun(n) - cicilanTahun(n)
```

Contoh (Mobil, Bekas, pinjaman 100.000.000, DP 25.000.000, tenor 3):

```text
tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%
```

Seluruh perhitungan uang memakai `BigDecimal` (bukan `double`) dan dibulatkan
`HALF_UP` pada 2 desimal.

---

## 8. Unit test

```bash
# Seluruh test
mvn test

# Test + laporan coverage JaCoCo
mvn verify
# laporan: credit-simulator-core/target/site/jacoco/index.html
#          credit-simulator-cli/target/site/jacoco/index.html

# Satu kelas test saja
mvn test -Dtest=DecliningBalanceInstallmentCalculatorTest

# Menjalankan test di dalam Docker (tanpa install JDK/Maven)
docker build -t credit-simulator .
```

Cakupan test:

| Test | Yang diverifikasi |
|---|---|
| `DecliningBalanceInstallmentCalculatorTest` | angka cicilan sama persis dengan `Rumus.xlsx` |
| `InterestRateScheduleTest` | progresi bunga 8% → 9,3% untuk 6 tahun |
| `LoanInputParserTest` | seluruh aturan validasi (tahun, tenor, plafon, DP) |
| `MoneyFormatterTest` | format `Rp. 3,500,000.00` dan `8,6%` |
| `ConsoleControllerTest` | alur end-to-end console: simulasi, `show`, save/switch sheet, error handling |
| `JsonParserTest` | parser JSON (payload valid, nested, escape, dokumen rusak) |
| `LoanApiClientTest` | pemanggilan web service memakai HTTP server lokal (200, 503, field hilang, data melanggar aturan) |
| `SheetRepositoryTest` | simpan/muat/hapus sheet dan penolakan nama sheet berbahaya (`../`) |

---

## 9. CI

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) dijalankan pada setiap push dan
pull request:

1. `mvn verify` — build + seluruh unit test + laporan coverage.
2. Smoke test `./credit_simulator file_inputs.txt`.
3. `docker build` untuk memastikan image tetap dapat dibangun.
4. Upload `credit-simulator.jar` sebagai artifact.

---

## 10. Struktur project

```text
credit_simulator/
├── credit_simulator                executable utama (wajib menurut soal)
├── bin/credit_simulator            executable alternatif
├── file_inputs.txt                 contoh input untuk mode file
├── pom.xml                         parent Maven, multi-module
├── Dockerfile                      multi-stage: Maven -> JRE Alpine
├── docker-compose.yml              aplikasi + nginx penyaji data/mock
├── docker/entrypoint.sh            entrypoint image
├── .github/workflows/ci.yml        GitHub Actions
│
├── data/                           data runtime, di-gitignore kecuali loan.json
│   ├── mock/                       payload JSON, disajikan di http://localhost:8000
│   │   └── loan.json               contoh payload dari soal
│   └── sheets/                     sheet tersimpan, <nama>.sheet
│
├── credit-simulator-core/          domain murni, nol dependency runtime
│   └── src/{main,test}/java/id/co/bcadigital/credit/core/
│       ├── domain/                 Vehicle, Car, Motorcycle, VehicleFactory,
│       │                           VehicleType, VehicleCondition, LoanApplication,
│       │                           InstallmentSchedule, YearlyInstallment
│       ├── calculation/            InstallmentCalculator,
│       │                           DecliningBalanceInstallmentCalculator,
│       │                           InterestRateSchedule
│       ├── validation/             LoanInputParser, LoanRules, ValidationException
│       └── format/                 MoneyFormatter
│
└── credit-simulator-cli/           aplikasi console
    └── src/{main,test}/java/id/co/bcadigital/credit/cli/
        ├── CreditSimulatorApplication.java    composition root
        ├── command/                Command, CommandRegistry, ShowCommand,
        │                           SimulateCommand, LoadCommand, SaveSheetCommand,
        │                           SwitchSheetCommand, ListSheetsCommand,
        │                           DeleteSheetCommand, ExitCommand
        ├── presenter/              SimulationPresenter, ConsoleController
        ├── view/                   ConsoleView, SystemConsoleView
        ├── io/                     LineReader, InteractiveLineReader,
        │                           ScriptedLineReader
        ├── sheet/                  SheetRepository, PayloadRepository
        └── remote/                 LoanApiClient, JsonParser, JsonException,
                                    RemoteServiceException
```

`target/` tidak ditampilkan: seluruh isinya output build dan di-gitignore. Berkas test berada
pada `src/test/java` dengan struktur paket yang sama seperti `src/main/java`.

### Pattern MVP

**Model** — modul `credit-simulator-core/`.

| Paket | Isi |
|---|---|
| `core/domain/` | Entitas: `Vehicle`, `Car`, `Motorcycle`, `VehicleFactory`, `LoanApplication`, `InstallmentSchedule` |
| `core/calculation/` | Rumus cicilan dan progresi suku bunga |
| `core/validation/` | Aturan input dan batas produk |
| `core/format/` | Format Rupiah dan persen |

**View** — modul `credit-simulator-cli/`, digunakan untuk mencetak dan membaca satu baris.

| Paket | Kelas | Peran |
|---|---|---|
| `cli/view/` | `ConsoleView` | Kontrak tampilan |
| | `SystemConsoleView` | Mencetak ke terminal |
| `cli/io/` | `LineReader` | Kontrak sumber input |
| | `InteractiveLineReader` | Membaca ketikan dari stdin |
| | `ScriptedLineReader` | Membaca baris dari file input |

**Presenter** — modul `credit-simulator-cli/`. digunakan untuk melakukan validasi dan aksi serta menjadi penengah antara Model dan View.

| Paket | Kelas | Peran |
|---|---|---|
| `cli/presenter/` | `SimulationPresenter` | State sesi dan orkestrasi: kapan bertanya ulang, kapan membatalkan, kapan menyimpan |
| | `ConsoleController` | Merutekan satu baris masukan ke perintah atau ke alur simulasi |

### Kolaborator yang dipakai Presenter

Bukan bagian dari triad MVP, melainkan pihak yang dipanggil Presenter untuk urusan di luar alur.

| Paket | Kelas | Peran |
|---|---|---|
| `cli/command/` | `Command`, `CommandRegistry`, dan seluruh perintah | Tiap menu sebagai objek. Menu `show` dibangkitkan dari registry sehingga selalu sinkron |
| `cli/sheet/` | `SheetRepository` | Menyimpan sheet ke `data/sheets/` |
| | `PayloadRepository` | Menulis payload JSON ke `data/mock/` agar dapat di-`load` dan ditembak Postman |
| `cli/remote/` | `LoanApiClient`, `JsonParser` | Gateway ke web service beserta parser JSON-nya |
| `cli/` | `CreditSimulatorApplication` | Composition root: satu-satunya kelas yang merakit semua dependency |

---

## 11. Web service (`load`)

Perintah `load` melakukan `GET` ke endpoint pada soal:

```text
https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e212003666c
```

Apabila error dapat dilakukan dengan hit json web service di postman

```text
localhost:8000/nama-file-mock.json
```

Sample output JSON:

```json
{
  "vehicleType": "Mobil",
  "vehicleCondition": "Baru",
  "vehicleYear": 2025,
  "totalLoanAmount": 1000000000,
  "loanTenure": 6,
  "downPayment": 500000000
}
```

Payload divalidasi dengan aturan bisnis yang sama seperti input manual, lalu langsung dihitung
dan ditampilkan.

> **Catatan:** saat pengerjaan, endpoint mocky.io di atas membalas **HTTP 404** — link mocky
> tidak dapat diakses. Endpoint karena itu dibuat dapat diganti tanpa build ulang:
>
> ```bash
> CREDIT_SIMULATOR_LOAN_API="https://run.mocky.io/v3/<id>" ./credit_simulator
> ```
>
> Atau dapat di setting melalui docker-compose.yml:
>
> ```bash
> loan-api:
>    image: nginx:alpine
>    ports:
>      - "8000:80"
>    volumes:
>      - ./data/mock:/usr/share/nginx/html:ro
> ```
> Alur pemanggilan, parsing, validasi, dan penanganan error tetap diuji penuh oleh
> `LoanApiClientTest` memakai HTTP server lokal.

---

## 12. Error Handling

- Input tidak valid ditolak dengan pesan spesifik per field, mode interaktif mengulang pertanyaan.
- Kegagalan jaringan, HTTP non-200, JSON rusak, dan field hilang dilaporkan sebagai pesan, aplikasi tidak berhenti.
- Nama sheet dibatasi `[A-Za-z0-9_-]` sehingga path traversal (`../`) ditolak.
- Timeout web service: 10 detik connect, 15 detik request.
