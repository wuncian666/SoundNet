# SoundNet

![License](https://img.shields.io/badge/license-MIT-blue.svg)  
**SoundNet** 是一個基於 Android 的音頻訊號處理應用程式，利用 FSK（頻移鍵控）技術實現聲音編碼與解碼。該專案旨在通過手機的麥克風和揚聲器進行短距離音頻通訊，例如傳輸簡單的文字訊息。專案使用 Goertzel 演算法檢測頻率，並支援 WAV 檔案生成與播放。

---

## 功能

- **音頻編碼**：將文字訊息轉換為 FSK 音頻訊號，並生成 WAV 檔案。
- **音頻解碼**：從麥克風錄製音頻，解調 FSK 訊號並還原為文字。
- **即時播放**：生成音頻後可直接通過揚聲器播放。
- **模式切換**：支援「Master」、「Slave」和「Point-to-Point」三種運作模式。
- **使用者界面**：提供簡單的 UI，支援訊息輸入與解調結果顯示。

---

## 技術棧

- **語言**：Java
- **平台**：Android
- **核心技術**：
  - Goertzel 演算法（頻率檢測）
  - FSK 調製與解調
  - WAV 檔案處理
- **依賴**：
  - Android SDK
  - RxJava（異步處理）
  - Material Components（UI 元件）

---

## 環境要求

- **Android Studio**：2021.3.1 或更高版本
- **最低 API 版本**：API 21 (Android 5.0 Lollipop)
- **權限**：
  - `WRITE_EXTERNAL_STORAGE`（儲存音頻檔案）
  - `RECORD_AUDIO`（錄製音頻）

---

## 安裝與設置

### 1. 克隆倉庫

```bash
git clone https://github.com/yourusername/SoundNet.git
cd SoundNet
```

### 2. 打開專案

使用 Android Studio 打開專案根目錄。
等待 Gradle 同步完成。

### 3. 配置依賴

在 app/build.gradle 中確保包含以下依賴：

```bash
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.12.0'
    implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
}
```

### 4. 添加權限

在 AndroidManifest.xml 中添加：

```bash
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### 5. 運行應用

連接 Android 設備或啟動模擬器。
在 Android Studio 中點擊「Run」按鈕。

---

## 使用方法

### 1. 啟動應用：

啟動後，應用會請求儲存和錄音權限，請授予權限。

### 2. 選擇運作模式：

- 在界面上切換模式：
  - Master：發送同步訊號並等待回應。
  - Slave：接收訊號並回應。
  - Point-to-Point：僅接收訊號。

### 3. 輸入訊息：

- 在文字框中輸入要傳輸的訊息，例如「HELLO」。

### 4. 生成並播放音頻：

- 點擊「Generate」按鈕，應用生成 FSK 音頻並播放。

### 5. 接收與解碼：

- 在接收模式下，啟用另一設備播放音頻，本應用會錄製並顯示解調結果。

## 專案結構

```bash
SoundNet/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/SoundNet/
│   │   │   │   ├── AudioPlayer/
│   │   │   │   │   └── SoundGenerator.java   # 音頻生成與播放邏輯
│   │   │   │   ├── Config/
│   │   │   │   │   └── AudioConfig.java     # 音頻相關配置常量
│   │   │   │   ├── WavFile/
│   │   │   │   │   └── WavFileHandle.java   # WAV檔案處理
│   │   │   │   ├── AudioDetect.java         # 音頻同步檢測
│   │   │   │   ├── AudioProcessingUtils.java # 音頻處理工具方法
│   │   │   │   ├── Demodulate.java          # FSK解調邏輯
│   │   │   │   ├── GoertzelDetect.java      # Goertzel演算法實現
│   │   │   │   ├── MainActivity.java        # 主活動與UI控制
│   │   │   │   ├── ReceiveProcess.java      # 音頻接收與處理
│   │   │   │   └── Record.java              # 音頻錄製功能
│   │   │   ├── res/
│   │   │   │   └── layout/activity_main.xml # 主界面布局
│   └── build.gradle                         # 模組Gradle配置
└── README.md                                # 本文件
```

---

## 已知問題

權限限制：在 Android 10 及以上版本中，儲存權限可能受到 Scoped Storage 限制。
解調準確性：在嘈雜環境下，FSK 訊號可能解調失敗，需優化閾值。
性能：長訊息生成或解調可能導致延遲，建議進一步異步處理。

---

## 貢獻指南

### 1. Fork 本倉庫。

### 2. 創建特性分支：

```bash
git checkout -b feature/YourFeature
```

### 3. 提交更改：

```bash
git commit -m "Add YourFeature"
```

### 4. 推送分支：

```bash
git push origin feature/YourFeature
```

### 5. 提交 Pull Request。

歡迎提交問題報告或功能建議！

---

## 許可證

本專案採用 MIT 許可證。詳情請見 LICENSE 檔案。

---

## 致謝

感謝所有貢獻者與測試者。
靈感來源：FSK 通訊原理與 Android 音頻處理技術。
