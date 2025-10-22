import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useAuthStore } from './store/authStore';
import webSocketService from './services/websocket';

// Layout Components
import Sidebar from './components/Layout/Sidebar';
import Header from './components/Layout/Header';

// Pages
import Login from './pages/Auth/Login';
import Dashboard from './pages/Dashboard/MonitoringDashboard';
import DeviceManagement from './pages/Devices/DeviceManagement';
import ControlCommands from './pages/Control/ControlCommands';
import AlertsDashboard from './pages/Alerts/AlertsDashboard';
import Analytics from './pages/Analytics/Analytics';
import UserManagement from './pages/Users/UserManagement';

// Loading Component
const LoadingSpinner = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50">
    <div className="text-center">
      <div className="loading-dots mx-auto mb-4">
        <div></div>
        <div></div>
        <div></div>
        <div></div>
      </div>
      <p className="text-gray-600">Loading...</p>
    </div>
  </div>
);

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuthStore();
  
  if (isLoading) {
    return <LoadingSpinner />;
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return children;
};

// Main App Layout
const AppLayout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="h-screen flex overflow-hidden bg-gray-100">
      <Sidebar isOpen={sidebarOpen} onToggle={() => setSidebarOpen(!sidebarOpen)} />
      <div className="flex-1 flex flex-col overflow-hidden lg:ml-0">
        <Header onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} />
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50">
          <div className="container mx-auto px-4 py-6">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
};

function App() {
  const { isAuthenticated, initializeAuth, isLoading } = useAuthStore();
  const [appLoading, setAppLoading] = useState(true);

  useEffect(() => {
    // Initialize authentication state
    initializeAuth();
    
    // Set up WebSocket connection if authenticated
    if (isAuthenticated) {
      const token = localStorage.getItem('authToken');
      if (token) {
        webSocketService.connect(token);
      }
    }
    
    setAppLoading(false);
  }, [initializeAuth, isAuthenticated]);

  // Show loading spinner while initializing
  if (appLoading || isLoading) {
    return <LoadingSpinner />;
  }

  return (
    <Router>
      <div className="App">
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#363636',
              color: '#fff',
            },
            success: {
              duration: 3000,
              iconTheme: {
                primary: '#10b981',
                secondary: '#fff',
              },
            },
            error: {
              duration: 5000,
              iconTheme: {
                primary: '#ef4444',
                secondary: '#fff',
              },
            },
          }}
        />
        
        <Routes>
          {/* Public Routes */}
          <Route 
            path="/login" 
            element={
              isAuthenticated ? <Navigate to="/dashboard" replace /> : <Login />
            } 
          />
          
          {/* Protected Routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <Dashboard />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/devices"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <DeviceManagement />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/control"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <ControlCommands />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/alerts"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <AlertsDashboard />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/analytics"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <Analytics />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/users"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <UserManagement />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          
          {/* Default redirect */}
          <Route 
            path="/" 
            element={<Navigate to="/dashboard" replace />} 
          />
          
          {/* 404 Route */}
          <Route
            path="*"
            element={
              <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="text-center">
                  <h1 className="text-4xl font-bold text-gray-900 mb-4">404</h1>
                  <p className="text-gray-600 mb-8">Page not found</p>
                  <a
                    href="/dashboard"
                    className="btn-primary"
                  >
                    Go to Dashboard
                  </a>
                </div>
              </div>
            }
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
