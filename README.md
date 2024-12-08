# **CalcYouLater**

**CalcYouLater** is a feature-rich Android app that combines a fully functional calculator with a hidden vault for storing and managing secret files (images and videos). Designed with user privacy and utility in mind, this app offers sleek UI, strong security, and hidden functionalities that go beyond a simple calculator.

---

## **Features**

### **1. Calculator**
- Fully functional calculator for basic and advanced calculations:
  - Addition, subtraction, multiplication, and division.
  - Scientific functions like square roots, percentages, and exponential calculations.
- Responsive, user-friendly design with circular buttons and a vibrant, intuitive layout.

---

### **2. Hidden Vault**
- **Accessing the Vault**:
  - Tap the "π" (pi) button six times to unlock the hidden vault.
  - The vault is secured with a 4-digit PIN set by the user.
- **File Management**:
  - Store images and videos privately within the vault.
  - Delete files or move them out of the vault to the public gallery.
- **Enhanced Security**:
  - Vault contents are hidden from the Recent Apps screen.
  - After switching away from the app, re-authentication via PIN is required.

---

### **3. Password Security**
- **First-Time Setup**:
  - New users are prompted to set a 4-digit PIN during the first app launch.
- **Re-Authentication**:
  - Vault access requires entering the PIN each time.
  - Multiple incorrect attempts lock the user out temporarily.

---

### **4. Fullscreen Image Viewer**
- View stored images in fullscreen mode.
- Swipe left or right to navigate through images.
- Pinch-to-zoom functionality for closer inspection of stored images.
- A sleek, translucent "Close" button enhances the viewing experience.

---

### **5. Multi-App Security**
- Prevents app contents (vault or fullscreen images) from appearing in the Recent Apps view for enhanced privacy.

---

## **Technology Stack**
- **Programming Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Image Loading Library**: Coil
- **Data Persistence**: SharedPreferences for password storage and DataStore for managing files.
- **Version Control**: Integrated with GitHub.

---

## **How to Use**

### **Calculator**
1. Perform calculations using the clean, circular button interface.
2. Use advanced features like "π" (pi) and square root for scientific calculations.

### **Accessing the Vault**
1. Tap the "π" button six times to unlock the hidden vault.
2. Enter your 4-digit PIN to gain access.

### **Managing Files in the Vault**
1. Add files using the "Add File" button (select images/videos).
2. Long-press any file to:
   - **Delete** it permanently from the vault.
   - **Move Out** to transfer it to the phone's public gallery.

### **Fullscreen Image Viewer**
1. Tap on any file in the vault to view it in fullscreen mode.
2. Swipe left or right to navigate through stored files.
3. Use pinch gestures to zoom in and out.

---

## **Setup Instructions**

### **Prerequisites**
- Android Studio installed on your system.
- Git installed and configured for version control.

### **Steps to Build the App**
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/CalcYouLater.git
2. Open the project in Android Studio.
3. Build the project and resolve any dependencies.
4. Run the app on an emulator or a connected Android device.

---

## **Security Features**
- Protects against unauthorized access using a 4-digit PIN.

---

## **Future Enhancements**
Here are some potential features planned for future releases:
- **Biometric Authentication**: Allow users to unlock the vault using fingerprint or face recognition.
- **Dark Mode Optimization**: Enhance visuals for devices running dark mode.
- **Cloud Backup**: Securely back up vault contents to the cloud.
- **Theme Customization**: Allow users to select custom themes for the app.

---
