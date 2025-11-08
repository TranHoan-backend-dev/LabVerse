import { createContext, useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import type { User } from '@/types/auth.types';
import * as accountService from '@/services/account.service';
import { tokenStorage } from '@/utils/token';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  signUp: (email: string, password: string, fullName: string, username: string, roleName: 'PI' | 'RESEARCHER' | 'STUDENT') => Promise<void>;
  signIn: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    // Check for existing session on mount
    const initAuth = async () => {
      try {
        const storedUser = tokenStorage.getUser();
        const token = tokenStorage.getToken();

        if (storedUser && token) {
          // Verify token is still valid by fetching current user
          try {
            const currentUser = await accountService.getCurrentUser();
            setUser(currentUser);
          } catch (error) {
            // Token invalid, clear storage
            tokenStorage.removeToken();
            setUser(null);
          }
        } else {
          setUser(null);
        }
      } catch (error) {
        console.error('Error initializing auth:', error);
        tokenStorage.removeToken();
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  const signUp = async (
    email: string,
    password: string,
    fullName: string,
    username: string,
    roleName: 'PI' | 'RESEARCHER' | 'STUDENT'
  ) => {
    try {
      const authResponse = await accountService.register({
        email,
        password,
        fullName,
        username,
        roleName,
      });

      setUser({
        id: authResponse.userId,
        email: authResponse.email,
        username: authResponse.username,
        fullName: authResponse.fullName,
        avatarUrl: authResponse.avatarUrl,
        role: authResponse.role,
      });

      toast.success('Account created successfully!');
      navigate('/dashboard');
    } catch (error: any) {
      toast.error(error.message || 'Failed to create account');
      throw error;
    }
  };

  const signIn = async (email: string, password: string) => {
    try {
      const authResponse = await accountService.login({
        email,
        password,
      });

      setUser({
        id: authResponse.userId,
        email: authResponse.email,
        username: authResponse.username,
        fullName: authResponse.fullName,
        avatarUrl: authResponse.avatarUrl,
        role: authResponse.role,
      });

      toast.success('Signed in successfully!');
      navigate('/dashboard');
    } catch (error: any) {
      toast.error(error.message || 'Failed to sign in');
      throw error;
    }
  };

  const signOut = async () => {
    try {
      await accountService.logout();
      setUser(null);
      toast.success('Signed out successfully!');
      navigate('/');
    } catch (error: any) {
      // Even if API call fails, clear local state
      setUser(null);
      tokenStorage.removeToken();
      toast.error(error.message || 'Failed to sign out');
    }
  };

  const refreshUser = async () => {
    try {
      const currentUser = await accountService.getCurrentUser();
      setUser(currentUser);
    } catch (error: any) {
      // If refresh fails, user might be logged out
      if (error.message?.includes('expired') || error.message?.includes('401')) {
        setUser(null);
        tokenStorage.removeToken();
      }
      throw error;
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, signUp, signIn, signOut, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
