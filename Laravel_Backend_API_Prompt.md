# Laravel Backend API Development Prompt

Create a comprehensive Laravel 10+ backend API for a vehicle management application called "SendaSnap" with the following requirements:

## **Project Setup & Configuration**
- Use Laravel 10+ with Sanctum for API authentication
- Set up CORS for mobile app integration
- Configure file storage for vehicle images and task attachments
- Use MySQL database with proper migrations
- Implement API versioning (v1)
- Add proper validation and error handling
- Include API documentation (Swagger/OpenAPI)

## **Database Schema & Models**

### **1. User Management System**
```php
// Users table with role-based access
- id, name, email, password, role, phone, avatar, email_verified_at, created_at, updated_at
- Roles: admin, manager, employee, client

// User roles and permissions system
- roles table: id, name, display_name, description
- permissions table: id, name, display_name, description  
- role_permissions table: role_id, permission_id
- user_roles table: user_id, role_id
```

### **2. Vehicle Management**
```php
// Vehicles table
- id, serial_number, make, model, chassis_model, cc, year, color
- vehicle_buy_date, auction_ship_number, net_weight, area
- length, width, height, plate_number, buying_price
- expected_yard_date, rikso_from, rikso_to, rikso_cost, rikso_company
- auction_sheet, tohon_copy, status, created_by, created_at, updated_at

// Vehicle photos table
- id, vehicle_id, photo_path, photo_type, uploaded_by, created_at

// Consignee details table  
- id, vehicle_id, name, address, phone, email, created_at, updated_at
```

### **3. Task Management System**
```php
// Tasks table
- id, title, description, work_date, work_time, status, priority
- vehicle_id, assigned_to, created_by, due_date, completed_at
- created_at, updated_at

// Task attachments table
- id, task_id, file_path, file_name, file_type, uploaded_by, created_at

// Task statuses: pending, running, completed, cancelled
// Task priorities: low, medium, high, urgent
```

### **4. Chat System**
```php
// Conversations table
- id, title, type, created_by, created_at, updated_at

// Conversation participants table
- id, conversation_id, user_id, joined_at, left_at

// Messages table
- id, conversation_id, sender_id, message, message_type, attachment_path
- is_read, created_at, updated_at

// Message types: text, image, file, system
```

## **API Endpoints Structure**

### **Authentication & User Management**
```
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/logout
POST /api/v1/auth/refresh
GET  /api/v1/auth/me
PUT  /api/v1/auth/profile
POST /api/v1/auth/change-password
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password

// User management (Admin only)
GET    /api/v1/users
POST   /api/v1/users
GET    /api/v1/users/{id}
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}
POST   /api/v1/users/{id}/assign-role
```

### **Vehicle Management**
```
GET    /api/v1/vehicles
POST   /api/v1/vehicles
GET    /api/v1/vehicles/{id}
PUT    /api/v1/vehicles/{id}
DELETE /api/v1/vehicles/{id}
POST   /api/v1/vehicles/{id}/photos
DELETE /api/v1/vehicles/{id}/photos/{photoId}
GET    /api/v1/vehicles/search
GET    /api/v1/vehicles/stats
```

### **Task Management**
```
GET    /api/v1/tasks
POST   /api/v1/tasks
GET    /api/v1/tasks/{id}
PUT    /api/v1/tasks/{id}
DELETE /api/v1/tasks/{id}
POST   /api/v1/tasks/{id}/assign
PUT    /api/v1/tasks/{id}/status
POST   /api/v1/tasks/{id}/attachments
DELETE /api/v1/tasks/{id}/attachments/{attachmentId}
GET    /api/v1/tasks/my-tasks
GET    /api/v1/tasks/assigned-to-me
```

### **Chat System**
```
GET    /api/v1/conversations
POST   /api/v1/conversations
GET    /api/v1/conversations/{id}
POST   /api/v1/conversations/{id}/messages
GET    /api/v1/conversations/{id}/messages
PUT    /api/v1/messages/{id}/read
POST   /api/v1/conversations/{id}/participants
DELETE /api/v1/conversations/{id}/participants/{userId}
```

## **Key Features to Implement**

### **1. Role-Based Access Control**
- Admin: Full access to all features
- Manager: Can manage vehicles, tasks, and users (except admins)
- Employee: Can view vehicles, manage assigned tasks, participate in chats
- Client: Can view assigned vehicles and tasks, participate in chats

### **2. File Upload System**
- Vehicle photos with multiple image support
- Task attachments (documents, images)
- Chat file attachments
- Proper file validation and storage
- Image resizing and optimization

### **3. Real-time Features**
- WebSocket integration for real-time chat
- Push notifications for task assignments
- Real-time task status updates

### **4. Search & Filtering**
- Vehicle search by make, model, chassis number
- Task filtering by status, assignee, date range
- Advanced search with multiple criteria

### **5. Reporting & Analytics**
- Vehicle statistics and reports
- Task completion analytics
- User activity reports
- Export functionality (PDF, Excel)

## **Security Requirements**
- JWT token authentication with refresh tokens
- Rate limiting on API endpoints
- Input validation and sanitization
- File upload security
- SQL injection prevention
- XSS protection
- CSRF protection for web routes

## **Additional Features**
- Email notifications for important events
- Activity logging and audit trails
- Database seeding with sample data
- API testing with PHPUnit
- Docker configuration for easy deployment
- Environment configuration for different stages

## **Response Format**
All API responses should follow this structure:
```json
{
    "success": true,
    "message": "Operation successful",
    "data": {},
    "meta": {
        "pagination": {},
        "timestamp": "2024-01-01T00:00:00Z"
    }
}
```

## **Error Handling**
- Consistent error response format
- Proper HTTP status codes
- Detailed validation error messages
- Logging for debugging

Please implement this Laravel backend with clean, maintainable code following Laravel best practices and conventions. Include proper documentation and ensure the API is ready for mobile app integration.
