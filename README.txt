M-Pesa Payment Integration Simulator

Backend (Spring Boot):
- Endpoints:
  - POST /api/mpesa/initiate
  - POST /api/mpesa/callback
  - GET  /api/mpesa/status/{id} (helper for frontend polling)
- MySQL via JPA. Configure DB via env vars:
  - DB_URL (default jdbc:mysql://localhost:3306/mpesa_sim?createDatabaseIfNotExist=true)
  - DB_USERNAME (default root)
  - DB_PASSWORD (default password)
- Build & run:
  - cd mpesa-simulator-backend
  - mvn spring-boot:run

Frontend (React + Vite):
- Run:
  - cd mpesa-simulator-frontend
  - npm install
  - npm run dev
- Backend URL:
  - Set VITE_API_BASE env var if backend not on http://localhost:8080

Simulation notes:
- Initiating creates a transaction with status PROCESSING and schedules a simulated callback in ~2s.
- The callback randomly marks the transaction SUCCESS when approved by pin or FAILED when user aborts by the cancel button.
- The frontend polls /api/mpesa/status/{id} once per second until status != PROCESSING.
