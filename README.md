# Hotel Booking API & Admin Dashboard

This project is a full-stack hotel booking application featuring a robust backend built with Java and Spring Boot, and a responsive admin dashboard created with HTML, Tailwind CSS, and vanilla JavaScript.

![Admin Dashboard Screenshot](https://placehold.co/800x400/6366f1/ffffff?text=Admin+Dashboard+UI)

---

## ✨ Features

### Backend (Java & Spring Boot)
- **User Authentication**: Secure user registration with email OTP verification and JWT-based login for protected endpoints.
- **CRUD Operations**: Comprehensive REST APIs for managing Hotels, Rooms, Bookings, Users, and Reviews.
- **Payment Integration**: Integrated with ABA Pay for processing payments and generating QR codes.
- **Real-time Updates**: WebSocket integration to provide real-time payment status updates to the client.
- **Configuration-driven**: Uses `application.yml` for easy configuration of database connections, JWT secrets, and mail server settings across different environments (dev/prod).

### Frontend (HTML, JS, Tailwind CSS)
- **Admin Dashboard**: A clean, single-page application for administrators.
- **Tabbed Interface**: Easily switch between managing Hotels, Users, and Bookings.
- **Dynamic Data Handling**: All data is fetched dynamically from the backend API.
- **Interactive Modals**: Create and update records without leaving the page.
- **Responsive Design**: The UI is fully responsive and works on all screen sizes, thanks to Tailwind CSS.

---

## 🛠️ Technologies Used

- **Backend**:
  - Java 17
  - Spring Boot 3.3
  - Spring Security (with JWT)
  - Spring Data JPA (Hibernate)
  - MySQL
  - Maven
- **Frontend**:
  - HTML5
  - Tailwind CSS 3
  - Vanilla JavaScript (ES6+)
- **Deployment**:
  - Docker

---

## 🚀 Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or later.
- **Maven**: For building the Spring Boot application.
- **MySQL**: A running instance of a MySQL database.
- **Web Browser**: A modern web browser like Chrome, Firefox, or Edge.

### 1. Backend Setup

1.  **Clone the repository:**
    ```sh
    git clone <your-repository-url>
    cd hotelbooking
    ```

2.  **Configure the database:**
    - Open `src/main/resources/application.yml`.
    - Under the `dev` profile, update the `spring.datasource.url`, `username`, and `password` to match your local MySQL setup.

3.  **Run the application:**
    - You can run the application using your IDE (like IntelliJ or VS Code) or via the command line:
    ```sh
    mvn spring-boot:run
    ```
    - The server will start on `http://localhost:8080`.

### 2. Frontend Setup

1.  **Locate the frontend file:**
    - The complete frontend is a single file located at `src/main/resources/static/index.html` (or you can use the one provided in the development environment).

2.  **Open in browser:**
    - Simply open the `index.html` file in your web browser. It's a self-contained application and requires no build step.

3.  **Log in:**
    - Use the credentials of an admin user from your database to log in. The default credentials in the form are `admin@example.com` and `Sengbuntheng1`.

---

## 🔑 API Endpoints

Here are some of the main API endpoints provided by the backend:

- `POST /v1/auth/register`: Register a new user.
- `POST /v1/auth/login`: Log in and receive a JWT token.
- `POST /v1/auth/verify`: Verify a user's email with an OTP.
- `GET /v1/hotels`: Get a list of all hotels.
- `POST /v1/hotels`: Create a new hotel.
- `GET /v1/users`: Get a list of all users.
- `POST /v1/bookings/create`: Create a new booking and get a payment QR code.

For a complete list of endpoints, you can explore the Swagger UI documentation available at `http://localhost:8080/swagger-ui.html` when the backend is running.
