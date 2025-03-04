# API Documentation

## Base URLs

- Local Development: `http://localhost:8222`
- Production: `https://api.yourdomain.com`

## User Service Endpoints

| Method | Endpoint                | Description         | Request Body                  | Response           |
|--------|-------------------------|---------------------|-------------------------------|--------------------|
| POST   | `/user/auth/login`      | User login          | `{LoginRequest}`              | `{LoginResponse }` |
| POST   | `/user/auth/register`   | User registration   | `{AddUserInfoRequest}`        | `{ UserResponse }` |
| POST   | `/user/auth/validate`   | Validate JWT token  | `{<?>}`                       | `{ <?> }`          |
| GET    | `/user/{userId}/email`  | Get user email      | `{@PathVariable UUID userId}` | `{ String }`       |
| GET    | `/user/{userId}/role`   | Get user role       | `{@PathVariable UUID userId}` | `{ String }`       |
| PUT    | `/user/update/{userId}` | Update user details | `{UpdateUserInfoRequest}`     | `{ UserResponse }` |

## Auth Service Endpoints

| Method | Endpoint             | Description       | Request Body           | Response           |
|--------|----------------------|-------------------|------------------------|--------------------|
| GET    | `/api/auth/login`    | User Login        | `{LoginRequest}`       | `{AuthResponse}`   |
| GET    | `/api/auth/register` | User registration | `{AddUserInfoRequest}` | `{ AuthResponse }` |

## Support Ticket Endpoints

| Method | Endpoint                                               | Description                | Request Body                                                                                                                                                               | Response                  |
|--------|--------------------------------------------------------|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|
| GET    | `/support-tickets/all`                                 | Get all tickets            | -                                                                                                                                                                          | `{SupportTicketResponse}` |
| GET    | `/support-tickets/all/redis`                           | Get all tickets from Redis | -                                                                                                                                                                          | `{SupportTicketResponse}` |
| POST   | `/support-tickets/add`                                 | Add a new ticket           | `{@RequestPart("addSupportTicketRequest") AddSupportTicketRequest addSupportTicketRequest, @RequestPart(value = "attachment", required = false) MultipartFile attachment}` | `{SupportTicketResponse}` |
| PUT    | `/support-tickets/update/{supportTicketId}`            | Update a ticket            | `{@PathVariable UUID supportTicketId, @RequestBody UpdateSupportTicketRequest updateSupportTicketRequest}`                                                                 | `{SupportTicketResponse}` |
| PUT    | `/support-tickets/update/staff/{supportTicketId}`      | Update a ticket by staff   | `{@PathVariable UUID supportTicketId, @RequestBody UpdateSupportTicketRequest updateSupportTicketRequest}`                                                                 | `{SupportTicketResponse}` |
| PUT    | `/support-tickets/update/resolution/{supportTicketId}` | Update a ticket resolution | `{@PathVariable UUID supportTicketId, @RequestBody UpdateSupportTicketRequest updateSupportTicketRequest}`                                                                 | `{SupportTicketResponse}` |

## Image Upload Endpoints

| Method | Endpoint                           | Description | Request Body                                                                      | Response            |
|--------|------------------------------------|-------------|-----------------------------------------------------------------------------------|---------------------|
| POST   | `/images/image/getImage/{id}`      | Get image   | `{@PathVariable UUID id}`                                                         | `{ImageResponse}`   |
| GET    | `/images/add`                      | Add image   | `{@RequestParam List<MultipartFile> file, @RequestHeader("userId") UUID userId }` | `{ ImageResponse }` |
| GET    | `/images/image/download/{imageId}` | Get image   | `{@PathVariable UUID imageId}`                                                    | `{ImageResponse}`   |