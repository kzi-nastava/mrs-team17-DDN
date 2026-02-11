# 🚖 Taxi App - Uber-like Transportation System

A comprehensive ride-hailing application developed as part of the Faculty of Technical Sciences, University of Novi Sad software engineering curriculum.

## 📋 Table of Contents
- [About](#about)
- [Features](#features)
- [User Roles](#user-roles)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Development Methodology](#development-methodology)
- [API Documentation](#api-documentation)
- [Contributors](#contributors)

## 🎯 About

This project is a modern ride-hailing application similar to Uber, designed to streamline urban transportation by minimizing driver-passenger interaction while ensuring a faster, more consistent, and safer experience. The system supports multiple user types with distinct functionalities and provides real-time tracking, automated driver assignment, and comprehensive ride management.

**Academic Year:** 2025/2026  
**Version:** 1.1

## ✨ Features

### Core Functionality
- 🗺️ Real-time vehicle tracking on interactive maps
- 🚗 Automated driver assignment based on proximity and availability
- 📱 Multi-platform support (Web, Android)
- 💳 Automated fare calculation
- ⭐ Driver and vehicle rating system
- 📊 Detailed ride history and analytics
- 🚨 Emergency PANIC button
- 💬 Live chat support 24/7
- 📧 Email notifications and in-app notifications
- 🔒 Secure JWT-based authentication

### Advanced Features
- Multi-stop routes with customizable waypoints
- Ride scheduling (up to 5 hours in advance)
- Favorite routes for quick booking
- Passenger linking via email
- Real-time ride tracking for linked passengers
- Driver work hour monitoring (8-hour limit)
- Price estimation before booking
- Ride cancellation with reasons tracking

## 👥 User Roles

### 1. Unregistered Users
- View active vehicles on map
- Estimate ride cost and duration
- Browse basic application information

### 2. Registered Passengers
- Book rides with multiple waypoints
- Schedule future rides
- Track vehicles in real-time
- Rate drivers and vehicles
- View complete ride history
- Generate reports for specific date ranges
- Save favorite routes
- Access PANIC button during rides
- 24/7 live chat support
- Update profile information

### 3. Drivers
- Automatic ride assignment
- View pickup and destination points
- Accept/reject ride assignments with reasons
- Manual active/inactive status toggle
- View ride history and generate reports
- Access PANIC button
- Update profile (requires admin approval)
- View active hours in last 24h

### 4. Administrators
- Create driver accounts
- Monitor all active rides
- View any user's ride history
- Generate global and individual reports
- Block/unblock users and drivers
- Respond to PANIC notifications
- Set and modify ride pricing
- Provide live chat support
- Leave notes on user accounts

## 🛠 Technology Stack

### Frontend (Web Application)
- **Framework:** Angular (TypeScript, HTML, CSS)
- **UI Library:** Angular Material / Bootstrap
- **Maps:** Leaflet with OpenStreetMap
- **Design:** Figma
- **Authentication:** JWT (JSON Web Tokens)
- **Testing:** Jasmine, Selenium (E2E)

### Backend (Server Layer)
- **Framework:** Spring Boot (Java)
- **Database:** PostgreSQL / H2
- **Email Service:** SendGrid
- **Authentication:** JWT
- **Testing:** JUnit / TestNG

### Mobile Application
- **Platform:** Android
- **Language:** Java
- **UI:** Material Design 3
- **Maps:** OpenStreetMap / Mapbox / Google Maps SDK
- **Storage:** SharedPreferences, SQLite / Firebase
- **Notifications:** Android Notification System

### Architecture
```
Mobile App
├── UI Layer (Presentation)
├── Domain Layer (Business Logic) [Optional]
└── Data Layer (Repository & Data Sources)

Web App
├── Components (Angular)
├── Services
├── Guards (AuthGuard)
├── Models/Interfaces
└── Routing

Backend
├── Controllers
├── Services
├── Repositories
├── Models/Entities
└── Security/JWT
```

## 📦 Prerequisites

### For Web Application
- Node.js (v16 or higher)
- npm or yarn
- Angular CLI

### For Backend
- Java 17 or higher
- Maven or Gradle
- PostgreSQL (or H2 for development)

### For Mobile Application
- Android Studio
- JDK 17
- Android SDK (API level 24+)

## 🚀 Installation

### Backend Setup
```bash
# Clone the repository
git clone [repository-url]
cd taxi-app/backend

# Install dependencies
mvn clean install

# Configure database in application.properties
# Set up email service credentials (SendGrid)

# Run the application
mvn spring-boot:run
```

### Frontend Setup
```bash
# Navigate to frontend directory
cd taxi-app/frontend

# Install dependencies
npm install

# Start development server
ng serve

# Open browser at http://localhost:4200
```

### Mobile App Setup
```bash
# Open Android Studio
# Import the mobile-app directory
# Sync Gradle files
# Configure API endpoints
# Run on emulator or physical device
```

## 🧪 Testing

### Backend Tests
```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

### Frontend Tests
```bash
# Run unit tests
ng test
```

### E2E Tests
```bash
# Run dedicated Selenium E2E tests
cd e2e-tests
./mvnw test -De2e.headless=true
```

### Mobile Tests
```bash
# Run in Android Studio
# Right-click on test package > Run Tests
```

## 📁 Project Structure

```
taxi-app/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/taxiapp/
│   │   │   │       ├── controllers/
│   │   │   │       ├── services/
│   │   │   │       ├── repositories/
│   │   │   │       ├── models/
│   │   │   │       └── security/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/
│   │   │   ├── services/
│   │   │   ├── guards/
│   │   │   ├── models/
│   │   │   └── interceptors/
│   │   ├── assets/
│   │   └── environments/
│   └── package.json
├── mobile-app/
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/
│   │   │   │   │   └── com/taxiapp/
│   │   │   │   │       ├── ui/
│   │   │   │   │       ├── domain/
│   │   │   │   │       └── data/
│   │   │   │   └── res/
│   │   │   └── test/
│   │   └── build.gradle
└── docs/
    ├── api/
    ├── design/
    └── reports/
```

## 📊 Development Methodology

### Agile/Scrum Approach
- **Sprint Duration:** 2 weeks
- **Tools:** Trello for task management
- **Ceremonies:** 
  - Sprint Planning
  - Daily Standups
  - Sprint Review
  - Sprint Retrospective

### Documentation Requirements
- Introductory document with weekly work plan
- Role rotation plan (Product Owner, Scrum Master)
- Sprint retrospective documents
- Sprint review documents
- Burndown charts at checkpoints
- Acceptance criteria for all tasks

### Deliverables
- Video demonstrations (5-7 minutes) at checkpoints
- Burndown charts showing team progress
- Complete API documentation

## 📖 API Documentation

API documentation is maintained separately and includes:
- All REST endpoints
- Request/Response formats
- Authentication requirements
- Error handling
- Example requests

Access the full API documentation at: `/docs/api/`

## 🎓 Academic Context

This project is developed as part of the following courses:
- Server-Side Engineering (ISS)
- Client-Side Engineering (IKS)
- Software Testing (TS)
- Mobile Applications (MA)
- Software Development Methodologies (MRS)
- Software Engineering and Information Technologies (SIIT)

### Checkpoints Schedule

| Subject | Checkpoint | Date | Deliverables |
|---------|-----------|------|--------------|
| ISS | KT1 | 29.12.2025 | All controller classes with endpoints |
| ISS | KT2 | 28.01.2026 | Assigned functional requirements |
| IKS | KT1 | 22-23.12.2025 | UI only (no backend integration) + Figma design |
| IKS | KT2 | 28.01.2026 | Full integration with backend |
| MA | KT1 | 30.12.2025 | GUI only, aligned with IKS design |
| MA | KT2 | 10.02.2026 | Full functionality |
| MRS | KT1 | 16.01.2026 | ISS + IKS KT1 requirements + methodology |
| MRS | KT2 | 11.02.2026 | ISS + IKS KT2 requirements + methodology |

## 👨‍💻 Contributors

### Student Responsibilities

**Student 1:**
- Driver registration (2.2.3)
- User profile (2.3)
- Ride ordering (2.4.1, 2.4.3)
- Ride start (2.6.1)
- Report generation (2.10)
- User blocking and notes (2.12)

**Student 2:**
- Information display (2.1.1)
- Linked passenger notifications (2.4.2)
- During ride tracking (2.6.2)
- Ride completion (2.7)
- Rating system (2.8)
- Driver ride history (2.9.2)
- Live support (2.11)
- Ride status monitoring (2.13)
- Pricing management (2.14)

**Student 3:**
- Ride estimation (2.1.2)
- Login system (2.2.1)
- User registration (2.2.2)
- Ride cancellation (2.5)
- PANIC button (2.6.3)
- Ride interruption (2.6.5)
- Passenger ride history (2.9.1)
- Admin ride history (2.9.3)

## 📝 License

This project is developed for academic purposes at the Faculty of Technical Sciences, University of Novi Sad.

## 📧 Contact

For questions and support, please contact the course assistant:
- Email: natasarajtarov@uns.ac.rs
- Trello: rajtarovnatasa@gmail.com

---

**Note:** Time values expressed in hours and minutes are shortened to minutes and seconds for demonstration purposes.

**Important:** Test data must be prepared before project defense to efficiently demonstrate all functionalities.
