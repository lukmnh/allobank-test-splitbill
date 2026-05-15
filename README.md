# Split Bill API — Allo Bank Backend Test

REST API untuk manajemen pengeluaran bersama dalam kelompok (trip, makan siang, apartemen bersama, dll).

---

## GitHub Username & Service Charge

**GitHub username:** `lukmnh`
**Service charge %:** `5` — dihitung dari `(655 % 10)` dimana 655 adalah jumlah ASCII dari `lukmnh`

```
l=108, u=117, k=107, m=109, n=110, h=104 → sum=655 → 655 % 10 = 5
```

---

## Tech Stack

- Java 21, Spring Boot 3.2, Maven
- PostgreSQL

---

## How to Build and Run

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL running

### Run lokal
```bash
./mvnw spring-boot:run
```

### Run dengan custom DB config
```bash
DB_HOST=localhost DB_PORT=5432 DB_NAME=splitbill \
DB_USERNAME=postgres DB_PASSWORD=postgres \
./mvnw spring-boot:run
```

### Run tests
```bash
./mvnw test
```

### Build & run dengan Docker
### Start PostgreSQL dengan Docker
```bash
docker run -d \
  --name splitbill-db \
  -e POSTGRES_DB=splitbill \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### Jalankan DDL
```bash
psql -U postgres -d splitbill -f src/main/resources/migration/V01__INIT_SCHEMA_TABLE.sql
```
### Build image docker dan run
```bash
docker build -t splitbill .

docker run -p 4110:4110 \
  -e DB_HOST=host.docker.internal \
  -e DB_NAME=splitbill \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  splitbill
```

API berjalan di **port 4110**.

---

## API Endpoints

### Billing
| Method | Path | Deskripsi |
|--------|------|-----------|
| POST | `/api/v1/billing` | Buat billing group |
| GET | `/api/v1/billing` | List semua billing |
| GET | `/api/v1/billing/{id}` | Detail billing + participants |

### Expense
| Method | Path | Deskripsi |
|--------|------|-----------|
| POST | `/api/v1/billing/{id}/expenses` | Tambah expense |
| GET | `/api/v1/billing/{id}/expenses` | List expense |
| GET | `/api/v1/billing/{id}/expenses/{expenseId}` | Detail expense + shares |

### Payment
| Method | Path | Deskripsi |
|--------|------|-----------|
| POST | `/api/v1/billing/{id}/payments` | Catat pembayaran |

### Settlement
| Method | Path | Deskripsi |
|--------|------|-----------|
| POST | `/api/v1/billing/{id}/settlement` | Buat settlement snapshot |
| GET | `/api/v1/billing/{id}/settlement/history` | History semua settlement |

---

## curl Commands

### 1. Buat billing group
```bash
curl -X POST http://localhost:4110/api/v1/billing \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Trip to Kuala Lumpur",
    "participants": ["Lukman", "Lia", "Budi"]
  }'
```

### 2. List billing
```bash
curl http://localhost:4110/api/v1/billing
```

### 3. Detail billing
```bash
curl http://localhost:4110/api/v1/billing/1
```

### 4. Tambah expense
```bash
curl -X POST http://localhost:4110/api/v1/billing/1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Dinner",
    "amount": "90000",
    "expenseType": "FOOD",
    "splitStrategy": "EQUAL",
    "paidById": 1,
    "splits": [
      { "participantId": 1 },
      { "participantId": 2 },
      { "participantId": 3 }
    ]
  }'
```

### 5. Buat settlement
```bash
curl -X POST http://localhost:4110/api/v1/billing/1/settlement
```

### 6. History settlement
```bash
curl http://localhost:4110/api/v1/billing/1/settlement/history
```

---

## Split Strategies

| Strategy | Penggunaan |
|----------|-----------|
| `EQUAL` | List participantId saja, share diabaikan. Sisa pembagian didistribusi dengan largest-remainder method |
| `PERCENTAGE` | Setiap entry isi share dalam persen. Total harus = 100 |
| `EXACT` | Setiap entry isi nominal. Total harus = amount expense |

## Expense Categories
`FOOD`, `TRANSPORT`, `ACCOMMODATION`, `ENTERTAINMENT`, `UTILITIES`, `SHOPPING`, `OTHER`

---

## Submission Question

> *What was the hardest design decision you made while building this, and what trade-off did you accept?*

Keputusan tersulit adalah apakah menyimpan `expense_share` saat expense dibuat (write time) atau menghitungnya ulang setiap kali settlement diminta (read time). Saya memilih materialise at write time, share dihitung dan disimpan permanen ke tabel `expense_share` saat expense dibuat. Tradeoff yang diterima adalah write path lebih kompleks dan DB menyimpan lebih banyak data. Namun keuntungannya adalah audit trail akurat dan immutable, settlement query hanya membutuhkan aggregasi sederhana, dan perubahan business logic di masa depan tidak mempengaruhi integritas data historis, hal yang krusial dalam sistem finansial.