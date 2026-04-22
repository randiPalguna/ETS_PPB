# MyMoney Notes - Project Documentation

## Project Overview

**Project Name**: MyMoney Notes  
**Project Type**: Personal Finance Mobile Application  
**Platform**: Android  
**Development Time**: 2024  

## 1. Deskripsi Aplikasi

MyMoney Notes adalah aplikasi pencatat keuangan pribadi yang dirancang untuk membantu pengguna melacak pemasukan dan pengeluaran harian dengan mudah. Aplikasi ini menyediakan antarmuka yang sederhana namun powerful untuk manajemen keuangan personal.

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
- **Cards**: Rounded corners (12-16dp), elevated
- **Buttons**: Primary color background, rounded
- **Inputs**: Outlined text fields
- **Navigation**: Bottom navigation bar

### 2.3 Typography
- **Headline Large**: 32sp Bold
- **Title Medium**: 16sp SemiBold
- **Body Medium**: 14sp Regular
- **Body Small**: 12sp Regular

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
- Delete transaction
- Date and time display

### 3.4 Analytics (Charts)
- Pie chart untuk breakdown kategori
- Percentage per kategori
- Total per kategori
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

### 4.2 Development Steps

#### Phase 1: Setup
1. Create new Android project with Jetpack Compose
2. Configure Gradle dependencies
3. Set up color palette and theme

#### Phase 2: Data Layer
1. Create Transaction entity
2. Implement Room database
3. Create DAO for database operations
4. Build Repository pattern

#### Phase 3: ViewModels
1. Create TransactionViewModel
2. Implement business logic
3. Handle state with Flow

#### Phase 4: UI Screens
1. Build Home Screen with balance cards
2. Create Add Transaction Screen
3. Implement History Screen with list
4. Design Charts Screen

#### Phase 5: Navigation
1. Set up Navigation Compose
2. Create bottom navigation
3. Connect all screens

#### Phase 6: Testing & Polish
1. Test all features
2. Fix bugs
3. Improve UI/UX
4. Build APK

### 4.3 File Structure
```
app/src/main/java/com/mirai/mymoneynotes/
├── data/
│   ├── Transaction.kt
│   ├── TransactionDao.kt
│   ├── AppDatabase.kt
│   └── TransactionRepository.kt
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── AddTransactionScreen.kt
│   │   ├── TransactionListScreen.kt
│   │   └── ChartScreen.kt
│   ├── components/
│   │   ├── TransactionItem.kt
│   │   └── CategoryChip.kt
│   ├── navigation/
│   │   └── Navigation.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/
│   └── TransactionViewModel.kt
└── MainActivity.kt
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
