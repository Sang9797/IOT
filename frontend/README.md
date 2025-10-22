# IoT Dashboard Frontend

A modern React.js frontend application for the IoT Microservices System, providing comprehensive dashboards for monitoring, managing, and controlling IoT devices.

## 🚀 Features

### 1. **Monitoring Dashboard**
- Real-time data visualization using WebSocket connections
- Interactive charts for temperature, pressure, vibration, and humidity
- Device status monitoring with live updates
- System health indicators
- Device distribution analytics

### 2. **Device Management Dashboard**
- Complete CRUD operations for IoT devices
- Advanced filtering and search capabilities
- Bulk operations (delete, status change)
- Device status management
- Import/Export functionality

### 3. **Control Commands Dashboard**
- Send commands to individual devices
- Broadcast commands to all devices
- Command history and status tracking
- Real-time command execution monitoring

### 4. **Alerts Dashboard**
- Real-time alert notifications
- Alert severity classification
- Alert acknowledgment system
- Historical alert data
- Alert filtering and search

### 5. **User Management**
- User creation, update, and deletion
- Role-based access control
- Permission management
- User activity tracking

## 🛠️ Technology Stack

- **React 18** - Modern React with hooks
- **React Router 6** - Client-side routing
- **Tailwind CSS** - Utility-first CSS framework
- **Recharts** - Data visualization library
- **Socket.io Client** - Real-time WebSocket communication
- **React Hook Form** - Form handling
- **Zustand** - State management
- **Axios** - HTTP client
- **React Hot Toast** - Notifications
- **Lucide React** - Icon library

## 📦 Installation

### Prerequisites
- Node.js 16+ and npm
- IoT Microservices System running (backend)

### Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Copy environment configuration
cp .env.example .env

# Edit configuration if needed
nano .env
```

### Environment Configuration
```bash
# API Configuration
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_WS_URL=ws://localhost:8080

# Optional: Custom configurations
REACT_APP_APP_NAME=IoT Dashboard
REACT_APP_VERSION=1.0.0
```

## 🚀 Development

### Start Development Server
```bash
npm start
```

The application will be available at `http://localhost:3000`

### Build for Production
```bash
npm run build
```

### Run Tests
```bash
npm test
```

## 📱 Application Structure

```
src/
├── components/          # Reusable UI components
│   ├── Layout/         # Layout components (Sidebar, Header)
│   ├── Devices/        # Device-related components
│   ├── Control/        # Control command components
│   ├── Alerts/         # Alert-related components
│   └── Users/          # User management components
├── pages/              # Page components
│   ├── Dashboard/      # Monitoring dashboard
│   ├── Devices/        # Device management
│   ├── Control/        # Control commands
│   ├── Alerts/         # Alerts dashboard
│   ├── Analytics/      # Analytics and reports
│   ├── Users/          # User management
│   └── Auth/           # Authentication pages
├── services/           # API and WebSocket services
├── store/              # State management (Zustand)
├── utils/              # Utility functions
└── styles/             # Global styles and Tailwind config
```

## 🔌 API Integration

### REST API
The frontend communicates with the IoT system through REST APIs:

- **Device Management**: `/api/devices/*`
- **Control Commands**: `/api/processor/*`
- **Analytics**: `/api/analysis/*`
- **Notifications**: `/api/notifications/*`
- **User Management**: `/api/users/*`

### WebSocket Connection
Real-time features use WebSocket connections:

- **Real-time Data**: Device sensor data updates
- **Alerts**: Instant alert notifications
- **Status Updates**: Device status changes
- **System Health**: Service health monitoring

## 🎨 UI/UX Features

### Design System
- **Consistent Color Palette**: Primary, secondary, success, warning, error colors
- **Responsive Design**: Mobile-first approach with Tailwind CSS
- **Dark Mode Support**: Built-in dark mode capabilities
- **Accessibility**: WCAG compliant components

### Components
- **Reusable Components**: Modular and maintainable
- **Form Validation**: Client-side validation with error handling
- **Loading States**: Skeleton loaders and spinners
- **Error Handling**: User-friendly error messages
- **Toast Notifications**: Non-intrusive notifications

## 📊 Real-time Features

### WebSocket Integration
```javascript
// Connect to WebSocket
webSocketService.connect(token);

// Listen for real-time updates
webSocketService.on('device_data', (data) => {
  // Update device data in real-time
});

webSocketService.on('alert', (alert) => {
  // Show alert notification
});
```

### Live Data Updates
- **Sensor Data**: Temperature, pressure, vibration, humidity
- **Device Status**: Online/offline status changes
- **Alerts**: Critical system alerts
- **System Health**: Service status updates

## 🔐 Authentication & Authorization

### Authentication Flow
1. **Login**: Email/password authentication
2. **JWT Token**: Secure token-based authentication
3. **Auto-refresh**: Automatic token refresh
4. **Logout**: Secure logout with token cleanup

### Role-based Access
- **Admin**: Full system access
- **User**: Limited access to assigned devices
- **Viewer**: Read-only access

## 📱 Responsive Design

### Breakpoints
- **Mobile**: < 768px
- **Tablet**: 768px - 1024px
- **Desktop**: > 1024px

### Mobile Features
- **Touch-friendly**: Optimized for touch interactions
- **Swipe Gestures**: Navigation and actions
- **Responsive Tables**: Horizontal scrolling for data tables
- **Mobile Menu**: Collapsible sidebar navigation

## 🧪 Testing

### Test Structure
```bash
# Unit Tests
npm test

# E2E Tests (if configured)
npm run test:e2e

# Coverage Report
npm run test:coverage
```

### Testing Libraries
- **Jest**: Testing framework
- **React Testing Library**: Component testing
- **MSW**: API mocking

## 🚀 Deployment

### Production Build
```bash
# Create production build
npm run build

# Serve static files
npx serve -s build
```

### Docker Deployment
```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Environment Variables
```bash
# Production environment
REACT_APP_API_URL=https://api.iot-system.com/api
REACT_APP_WS_URL=wss://api.iot-system.com
```

## 🔧 Configuration

### Tailwind CSS
Custom configuration in `tailwind.config.js`:
- **Color Palette**: Brand colors and semantic colors
- **Animations**: Custom animations for real-time indicators
- **Components**: Pre-built component classes

### API Configuration
Centralized API configuration in `src/services/api.js`:
- **Base URL**: Configurable API endpoint
- **Interceptors**: Request/response interceptors
- **Error Handling**: Centralized error handling

## 📈 Performance

### Optimization Features
- **Code Splitting**: Lazy loading of routes
- **Memoization**: React.memo for expensive components
- **Virtual Scrolling**: For large data sets
- **Image Optimization**: Optimized asset loading

### Bundle Analysis
```bash
# Analyze bundle size
npm run build
npx webpack-bundle-analyzer build/static/js/*.js
```

## 🐛 Troubleshooting

### Common Issues

#### WebSocket Connection Failed
```
Error: WebSocket connection failed
```
**Solution**: Ensure the IoT system is running and WebSocket endpoint is accessible.

#### API Requests Failing
```
Error: Network Error
```
**Solution**: Check API Gateway configuration and CORS settings.

#### Build Errors
```
Error: Module not found
```
**Solution**: Clear node_modules and reinstall dependencies.

### Debug Mode
```bash
# Enable debug logging
REACT_APP_DEBUG=true npm start
```

## 🤝 Contributing

### Development Workflow
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

### Code Style
- **ESLint**: Code linting
- **Prettier**: Code formatting
- **Husky**: Git hooks for quality checks

## 📚 Documentation

### API Documentation
- **Swagger/OpenAPI**: Available at `/api/docs`
- **Postman Collection**: Import for testing

### Component Documentation
- **Storybook**: Component library documentation
- **JSDoc**: Inline code documentation

## 🔗 Integration

### Backend Services
- **API Gateway**: Main entry point
- **Device Management**: Device CRUD operations
- **Device Processor**: Real-time data processing
- **Analysis Service**: Analytics and reporting
- **Notification Service**: Alert management

### External Services
- **Email Service**: SMTP configuration
- **SMS Service**: SMS provider integration
- **File Storage**: Asset management

---

**The IoT Dashboard Frontend provides a comprehensive, modern, and user-friendly interface for managing and monitoring IoT devices in real-time, with robust authentication, responsive design, and seamless integration with the IoT microservices backend.**
