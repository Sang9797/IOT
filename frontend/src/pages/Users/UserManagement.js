import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { 
  Plus, 
  Search, 
  Filter, 
  Edit, 
  Trash2, 
  Eye,
  Shield,
  User,
  Mail,
  Phone,
  Calendar,
  CheckCircle,
  XCircle,
  AlertCircle
} from 'lucide-react';
import { userAPI } from '../../services/api';
import { toast } from 'react-hot-toast';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showEditForm, setShowEditForm] = useState(false);
  const [showDetails, setShowDetails] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    role: '',
    status: '',
    search: '',
  });

  const { register, handleSubmit, formState: { errors }, reset, setValue } = useForm();

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      // Mock data for demonstration - replace with actual API call
      const mockUsers = [
        {
          id: '1',
          name: 'John Doe',
          email: 'john.doe@iot.com',
          role: 'ADMIN',
          status: 'ACTIVE',
          phone: '+1-555-0123',
          createdAt: '2024-01-15T10:30:00Z',
          lastLogin: '2024-01-20T14:22:00Z',
          permissions: ['READ', 'WRITE', 'DELETE', 'ADMIN'],
        },
        {
          id: '2',
          name: 'Jane Smith',
          email: 'jane.smith@iot.com',
          role: 'USER',
          status: 'ACTIVE',
          phone: '+1-555-0124',
          createdAt: '2024-01-16T09:15:00Z',
          lastLogin: '2024-01-20T11:45:00Z',
          permissions: ['READ', 'WRITE'],
        },
        {
          id: '3',
          name: 'Bob Johnson',
          email: 'bob.johnson@iot.com',
          role: 'VIEWER',
          status: 'INACTIVE',
          phone: '+1-555-0125',
          createdAt: '2024-01-17T16:20:00Z',
          lastLogin: '2024-01-18T08:30:00Z',
          permissions: ['READ'],
        },
      ];
      setUsers(mockUsers);
    } catch (error) {
      console.error('Error fetching users:', error);
      toast.error('Failed to fetch users');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateUser = async (userData) => {
    try {
      // Mock API call - replace with actual API call
      const newUser = {
        id: Date.now().toString(),
        ...userData,
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
        lastLogin: null,
      };
      
      setUsers(prev => [newUser, ...prev]);
      setShowCreateForm(false);
      reset();
      toast.success('User created successfully!');
    } catch (error) {
      console.error('Error creating user:', error);
      toast.error('Failed to create user');
    }
  };

  const handleUpdateUser = async (userData) => {
    try {
      // Mock API call - replace with actual API call
      setUsers(prev => prev.map(user => 
        user.id === editingUser.id 
          ? { ...user, ...userData, updatedAt: new Date().toISOString() }
          : user
      ));
      
      setShowEditForm(false);
      setEditingUser(null);
      reset();
      toast.success('User updated successfully!');
    } catch (error) {
      console.error('Error updating user:', error);
      toast.error('Failed to update user');
    }
  };

  const handleDeleteUser = async (userId) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        // Mock API call - replace with actual API call
        setUsers(prev => prev.filter(user => user.id !== userId));
        toast.success('User deleted successfully!');
      } catch (error) {
        console.error('Error deleting user:', error);
        toast.error('Failed to delete user');
      }
    }
  };

  const handleEditUser = (user) => {
    setEditingUser(user);
    setValue('name', user.name);
    setValue('email', user.email);
    setValue('role', user.role);
    setValue('phone', user.phone);
    setValue('permissions', user.permissions);
    setShowEditForm(true);
  };

  const handleViewUser = (user) => {
    setEditingUser(user);
    setShowDetails(true);
  };

  const handleSelectUser = (userId) => {
    setSelectedUsers(prev =>
      prev.includes(userId)
        ? prev.filter(id => id !== userId)
        : [...prev, userId]
    );
  };

  const handleSelectAll = () => {
    const filteredUsers = getFilteredUsers();
    setSelectedUsers(
      selectedUsers.length === filteredUsers.length
        ? []
        : filteredUsers.map(user => user.id)
    );
  };

  const handleBulkAction = async (action) => {
    if (selectedUsers.length === 0) return;

    switch (action) {
      case 'delete':
        if (window.confirm(`Are you sure you want to delete ${selectedUsers.length} users?`)) {
          setUsers(prev => prev.filter(user => !selectedUsers.includes(user.id)));
          setSelectedUsers([]);
          toast.success(`${selectedUsers.length} users deleted successfully!`);
        }
        break;
      case 'activate':
        setUsers(prev => prev.map(user => 
          selectedUsers.includes(user.id) 
            ? { ...user, status: 'ACTIVE' }
            : user
        ));
        setSelectedUsers([]);
        toast.success('Users activated successfully!');
        break;
      case 'deactivate':
        setUsers(prev => prev.map(user => 
          selectedUsers.includes(user.id) 
            ? { ...user, status: 'INACTIVE' }
            : user
        ));
        setSelectedUsers([]);
        toast.success('Users deactivated successfully!');
        break;
      default:
        break;
    }
  };

  const getRoleColor = (role) => {
    switch (role) {
      case 'ADMIN':
        return 'badge-error';
      case 'USER':
        return 'badge-success';
      case 'VIEWER':
        return 'badge-info';
      default:
        return 'badge-gray';
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE':
        return 'badge-success';
      case 'INACTIVE':
        return 'badge-gray';
      case 'SUSPENDED':
        return 'badge-error';
      default:
        return 'badge-gray';
    }
  };

  const getFilteredUsers = () => {
    return users.filter(user => {
      if (filters.role && user.role !== filters.role) return false;
      if (filters.status && user.status !== filters.status) return false;
      if (filters.search && !user.name.toLowerCase().includes(filters.search.toLowerCase()) && 
          !user.email.toLowerCase().includes(filters.search.toLowerCase())) return false;
      return true;
    });
  };

  const filteredUsers = getFilteredUsers();

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">User Management</h1>
          <p className="text-gray-600">Manage system users and their permissions</p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => setShowCreateForm(true)}
            className="btn-primary"
          >
            <Plus className="h-4 w-4 mr-2" />
            Add User
          </button>
        </div>
      </div>

      {/* Filters and Search */}
      <div className="card p-6">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between space-y-4 lg:space-y-0">
          <div className="flex flex-col sm:flex-row sm:items-center space-y-2 sm:space-y-0 sm:space-x-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search users..."
                value={filters.search}
                onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                className="pl-10 input w-64"
              />
            </div>
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="btn-outline"
            >
              <Filter className="h-4 w-4 mr-2" />
              Filters
            </button>
            {Object.values(filters).some(f => f) && (
              <button
                onClick={() => setFilters({ role: '', status: '', search: '' })}
                className="btn-outline text-red-600 border-red-300 hover:bg-red-50"
              >
                Clear Filters
              </button>
            )}
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-sm text-gray-500">
              {filteredUsers.length} of {users.length} users
            </span>
          </div>
        </div>

        {/* Advanced Filters */}
        {showFilters && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="label">Role</label>
                <select
                  value={filters.role}
                  onChange={(e) => setFilters({ ...filters, role: e.target.value })}
                  className="input"
                >
                  <option value="">All Roles</option>
                  <option value="ADMIN">Admin</option>
                  <option value="USER">User</option>
                  <option value="VIEWER">Viewer</option>
                </select>
              </div>
              <div>
                <label className="label">Status</label>
                <select
                  value={filters.status}
                  onChange={(e) => setFilters({ ...filters, status: e.target.value })}
                  className="input"
                >
                  <option value="">All Status</option>
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                  <option value="SUSPENDED">Suspended</option>
                </select>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Bulk Actions */}
      {selectedUsers.length > 0 && (
        <div className="card p-4 bg-blue-50 border-blue-200">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-blue-900">
              {selectedUsers.length} user(s) selected
            </span>
            <div className="flex items-center space-x-2">
              <button
                onClick={() => handleBulkAction('activate')}
                className="btn-sm bg-green-600 text-white hover:bg-green-700"
              >
                <CheckCircle className="h-3 w-3 mr-1" />
                Activate
              </button>
              <button
                onClick={() => handleBulkAction('deactivate')}
                className="btn-sm bg-gray-600 text-white hover:bg-gray-700"
              >
                <XCircle className="h-3 w-3 mr-1" />
                Deactivate
              </button>
              <button
                onClick={() => handleBulkAction('delete')}
                className="btn-sm bg-red-600 text-white hover:bg-red-700"
              >
                <Trash2 className="h-3 w-3 mr-1" />
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Users Table */}
      <div className="card">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left">
                  <input
                    type="checkbox"
                    checked={selectedUsers.length === filteredUsers.length && filteredUsers.length > 0}
                    onChange={handleSelectAll}
                    className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  User
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Role
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Last Login
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {isLoading ? (
                <tr>
                  <td colSpan="6" className="px-6 py-12 text-center">
                    <div className="flex items-center justify-center">
                      <div className="loading-dots">
                        <div></div>
                        <div></div>
                        <div></div>
                        <div></div>
                      </div>
                    </div>
                  </td>
                </tr>
              ) : filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-6 py-12 text-center text-gray-500">
                    No users found
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <input
                        type="checkbox"
                        checked={selectedUsers.includes(user.id)}
                        onChange={() => handleSelectUser(user.id)}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10">
                          <div className="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                            <User className="h-5 w-5 text-gray-600" />
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{user.name}</div>
                          <div className="text-sm text-gray-500">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={getRoleColor(user.role)}>
                        {user.role}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={getStatusColor(user.status)}>
                        {user.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.lastLogin ? new Date(user.lastLogin).toLocaleString() : 'Never'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => handleViewUser(user)}
                          className="text-primary-600 hover:text-primary-900"
                          title="View Details"
                        >
                          <Eye className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleEditUser(user)}
                          className="text-gray-600 hover:text-gray-900"
                          title="Edit User"
                        >
                          <Edit className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteUser(user.id)}
                          className="text-red-600 hover:text-red-900"
                          title="Delete User"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* User Form Modal */}
      {(showCreateForm || showEditForm) && (
        <UserForm
          isOpen={showCreateForm || showEditForm}
          onClose={() => {
            setShowCreateForm(false);
            setShowEditForm(false);
            setEditingUser(null);
            reset();
          }}
          onSubmit={showCreateForm ? handleCreateUser : handleUpdateUser}
          user={editingUser}
          title={showCreateForm ? 'Create New User' : 'Edit User'}
        />
      )}

      {/* User Details Modal */}
      {showDetails && editingUser && (
        <UserDetails
          isOpen={showDetails}
          onClose={() => {
            setShowDetails(false);
            setEditingUser(null);
          }}
          user={editingUser}
        />
      )}
    </div>
  );
};

// User Form Component
const UserForm = ({ isOpen, onClose, onSubmit, user, title }) => {
  const { register, handleSubmit, formState: { errors } } = useForm();

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={onClose}></div>
        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">{title}</h3>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div>
                <label className="label">Name *</label>
                <input
                  {...register('name', { required: 'Name is required' })}
                  type="text"
                  className={`input ${errors.name ? 'border-red-300' : ''}`}
                  defaultValue={user?.name || ''}
                />
              </div>
              <div>
                <label className="label">Email *</label>
                <input
                  {...register('email', { 
                    required: 'Email is required',
                    pattern: {
                      value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                      message: 'Invalid email address'
                    }
                  })}
                  type="email"
                  className={`input ${errors.email ? 'border-red-300' : ''}`}
                  defaultValue={user?.email || ''}
                />
              </div>
              <div>
                <label className="label">Role *</label>
                <select
                  {...register('role', { required: 'Role is required' })}
                  className={`input ${errors.role ? 'border-red-300' : ''}`}
                  defaultValue={user?.role || 'USER'}
                >
                  <option value="USER">User</option>
                  <option value="ADMIN">Admin</option>
                  <option value="VIEWER">Viewer</option>
                </select>
              </div>
              <div>
                <label className="label">Phone</label>
                <input
                  {...register('phone')}
                  type="tel"
                  className="input"
                  defaultValue={user?.phone || ''}
                />
              </div>
              {!user && (
                <div>
                  <label className="label">Password *</label>
                  <input
                    {...register('password', { 
                      required: 'Password is required',
                      minLength: {
                        value: 6,
                        message: 'Password must be at least 6 characters'
                      }
                    })}
                    type="password"
                    className={`input ${errors.password ? 'border-red-300' : ''}`}
                  />
                </div>
              )}
            </form>
          </div>
          <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              onClick={handleSubmit(onSubmit)}
              className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary-600 text-base font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:ml-3 sm:w-auto sm:text-sm"
            >
              {user ? 'Update User' : 'Create User'}
            </button>
            <button
              onClick={onClose}
              className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

// User Details Component
const UserDetails = ({ isOpen, onClose, user }) => {
  if (!isOpen || !user) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={onClose}></div>
        <div className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-2xl sm:w-full">
          <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">User Details</h3>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-500">Name</label>
                  <p className="text-sm text-gray-900">{user.name}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Email</label>
                  <p className="text-sm text-gray-900">{user.email}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Role</label>
                  <p className="text-sm text-gray-900">{user.role}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Status</label>
                  <p className="text-sm text-gray-900">{user.status}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Phone</label>
                  <p className="text-sm text-gray-900">{user.phone || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Last Login</label>
                  <p className="text-sm text-gray-900">
                    {user.lastLogin ? new Date(user.lastLogin).toLocaleString() : 'Never'}
                  </p>
                </div>
              </div>
            </div>
          </div>
          <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
            <button
              onClick={onClose}
              className="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-primary-600 text-base font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 sm:ml-3 sm:w-auto sm:text-sm"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserManagement;
