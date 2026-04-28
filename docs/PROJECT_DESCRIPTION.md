# MyMoney Notes - Project Documentation

## Project Overview

**Project Name**: MyMoney Notes  
**Project Type**: Personal Finance Mobile Application  
**Platform**: Android  
**Development Time**: 2024  

## 1. Deskripsi Aplikasi

MyMoney Notes adalah aplikasi pencatat keuangan pribadi untuk membantu pengguna melacak pemasukan dan pengeluaran harian secara cepat dan terstruktur.

### Target Users
- Mahasiswa yang ingin mengatur uang saku
- Pekerja yang ingin melacak pengeluaran harian
- Siapa saja yang ingin mengelola keuangan pribadi

## 2. Desain Aplikasi

### 2.1 Color Palette
Aplikasi menggunakan palet warna earth-tone:

```
Primary Colors:
- Tea Green:    #8FBC8F (143, 188, 143)
- Warm Ivory:   #FFFFF0 (255, 255, 240)
- Terracotta:   #E2725B (226, 114, 91)

Accent Colors:
- Dark Brown:   #3E2723 (62, 39, 35)
- Gold:         #FFD700 (255, 215, 0)
```

### 2.2 UI Components
- Cards: rounded corners (12-16dp), elevated
- Buttons: primary color background, rounded
- Inputs: outlined text fields
- Navigation: bottom navigation bar

### 2.3 Typography
- Headline Large: 32sp Bold
- Title Medium: 16sp SemiBold
- Body Medium: 14sp Regular
- Body Small: 12sp Regular

## 3. Fitur Utama

### 3.1 Dashboard (Home Screen)
- Balance card menampilkan total saldo
- Income card menampilkan total pemasukan
- Expense card menampilkan total pengeluaran
- Quick summary

### 3.2 Add Transaction
- Toggle antara Income/Expense
- Category selection chips
- Amount input dengan format Rupiah
- Optional description field

### 3.3 Transaction History
- List semua transaksi
- Filter by type (All/Income/Expense)
- Filter by month
- Delete transaction
- Date and time display

### 3.4 Analytics (Charts)
- Pie/donut chart untuk breakdown kategori
- Percentage per kategori
- Total per kategori
- Daily trend line chart
- Separate tabs untuk Income dan Expense

## 4. Cara Mengerjakan

### 4.1 Tech Stack
```
- Language: Kotlin
- UI Framework: Jetpack Compose
- Database: Room
- Architecture: MVVM
- Navigation: Navigation Compose
```

### 4.2 Alur Implementasi Berdasarkan Kode

#### A. Inisialisasi App dan Navigation
1. Entry point ada di `app/src/main/java/com/mirai/mymoneynotes/MainActivity.kt` pada `onCreate()`.
2. `setContent { MyMoneyNotesTheme { AppNavigation() } }` menjadi root composable app.
3. Routing didefinisikan di `ui/navigation/Navigation.kt` menggunakan `NavHost` dengan 4 route: `home`, `add`, `history`, `charts`.
4. Bottom navigation memakai `NavigationBarItem` dan `navController.navigate(...)` dengan `saveState` + `restoreState`.

#### B. Data Layer (Room)
1. Entity transaksi ada di `data/Transaction.kt`:
   - `Transaction(id, type, category, amount, date, description)`
   - `TransactionType` berisi `INCOME` dan `EXPENSE`.
2. DAO query ada di `data/TransactionDao.kt`:
   - `getAllTransactions(): Flow<List<Transaction>>`
   - `insertTransaction(...)`, `deleteTransaction(...)`
   - agregasi `getTotalIncome()`, `getTotalExpense()`, `getBalance()`.
3. Database singleton ada di `data/AppDatabase.kt` dengan nama DB `mymoney_notes_database`.
4. Repository di `data/TransactionRepository.kt` menjadi perantara ViewModel ke DAO.

#### C. ViewModel dan Business Logic
1. Semua state utama diatur di `viewmodel/TransactionViewModel.kt`:
   - `allTransactions`, `totalIncome`, `totalExpense`, `balance`, `transactionCount`
   - seluruhnya berbasis `Flow` dan dikonversi ke state via `stateIn(...)`.
2. Logika filter:
   - `filterByType(type: TransactionType?)`
   - `filterByMonth(year: Int, month: Int?)`
   - gabungan filter diproses di `filteredTransactions`.
3. Operasi tulis data:
   - `addTransaction(...)` melakukan validasi amount > 0 dan category tidak kosong
   - `deleteTransaction(transaction)` untuk hapus data.

#### D. Alur Fitur Add Transaction (UI ke DB)
1. Screen `ui/screens/AddTransactionScreen.kt` menyimpan state form lokal:
   - `selectedType`, `selectedCategory`, `amount`, `description`, `selectedDate`.
2. Tombol `Add Transaction` memanggil `viewModel.addTransaction(...)`.
3. ViewModel membuat object `Transaction`, lalu memanggil `repository.insertTransaction(...)`.
4. Setelah submit, form di-reset ke nilai default.

#### E. Alur Fitur Home dan History
1. `ui/screens/HomeScreen.kt` mengonsumsi:
   - `totalIncome`, `totalExpense`, `balance`, `transactionCount`
   - UI otomatis update saat data Room berubah.
2. `ui/screens/TransactionListScreen.kt`:
   - render data dari `filteredTransactions`
   - filter tipe via `FilterChip`
   - filter bulan via `DatePickerDialog`
   - delete item melalui `TransactionItem(onDelete = { viewModel.deleteTransaction(...) })`.

#### F. Alur Fitur Analytics
1. `ui/screens/ChartScreen.kt` membaca `allTransactions` lalu memfilter berdasarkan tab dan bulan.
2. `CategoryBreakdownCard(...)` melakukan grouping per kategori dan kalkulasi total.
3. `SimplePieChart(...)` menggambar donut chart, lalu teks total ditaruh di tengah chart.
4. `DailyTrendCard(...)` membuat agregasi nominal per hari dan menggambar line chart.
5. Penyempurnaan UI terakhir pada chart:
   - diameter donut diperkecil
   - ketebalan ring ditipiskan
   - center text disesuaikan agar tidak menabrak elemen lain.

### 4.3 Cara Build dan Uji
1. Compile cepat:
   - `./gradlew :app:compileDebugKotlin`
2. Install ke emulator/device:
   - `./gradlew installDebug`
3. Build APK debug:
   - `./gradlew assembleDebug`
4. Output APK:
   - `app/build/outputs/apk/debug/app-debug.apk`

### 4.4 Struktur Folder Inti
```
app/src/main/java/com/mirai/mymoneynotes/
|- data/
|  |- Transaction.kt
|  |- TransactionDao.kt
|  |- AppDatabase.kt
|  |- TransactionRepository.kt
|- viewmodel/
|  |- TransactionViewModel.kt
|- ui/
|  |- navigation/Navigation.kt
|  |- screens/
|  |  |- HomeScreen.kt
|  |  |- AddTransactionScreen.kt
|  |  |- TransactionListScreen.kt
|  |  |- ChartScreen.kt
|  |- components/
|     |- TransactionItem.kt
|     |- CategoryChip.kt
|- MainActivity.kt
```

### 4.5 Snippet Kode Utama

#### A. Navigation (route screen)
```kotlin
NavHost(
    navController = navController,
    startDestination = Screen.Home.route,
    modifier = Modifier.padding(innerPadding)
) {
    composable(Screen.Home.route) { HomeScreen() }
    composable(Screen.Add.route) { AddTransactionScreen() }
    composable(Screen.History.route) { TransactionListScreen() }
    composable(Screen.Charts.route) { ChartScreen() }
}
```

#### B. DAO Room (query transaksi + agregasi)
```kotlin
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double>
}
```

#### C. ViewModel (add transaction + filter)
```kotlin
fun addTransaction(
    type: TransactionType,
    category: String,
    amount: String,
    description: String,
    date: Long
) {
    viewModelScope.launch {
        val amountValue = amount.trim().toDoubleOrNull() ?: 0.0
        if (amountValue > 0 && category.isNotBlank()) {
            repository.insertTransaction(
                Transaction(
                    type = type,
                    category = category,
                    amount = amountValue,
                    date = date,
                    description = description
                )
            )
        }
    }
}

fun filterByType(type: TransactionType?) {
    _selectedTypeFilter.value = type
}
```

#### D. Submit dari AddTransactionScreen
```kotlin
Button(
    onClick = {
        viewModel.addTransaction(
            type = selectedType,
            category = selectedCategory,
            amount = amount,
            description = description,
            date = selectedDate
        )
    },
    enabled = selectedCategory.isNotBlank() && (parsedAmount ?: 0.0) > 0
) {
    Text("Add Transaction")
}
```

#### E. Chart breakdown per kategori
```kotlin
val categoryTotals = transactions
    .groupBy { it.category }
    .mapValues { entry -> entry.value.sumOf { it.amount } }
    .entries
    .sortedByDescending { it.value }
```

## 5. Screenshots

Folder `/docs/screenshots/` berisi:
1. Home Screen
2. Add Transaction Screen
3. Transaction History Screen
4. Charts Screen
5. App Thumbnail

## 6. Future Enhancements
- Export data to CSV
- Cloud backup
- Budget limits
- Recurring transactions
- Dark mode optimization
- Multiple currency support
