import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';

const mockUseAuth = vi.fn();

vi.mock('../context/AuthContext', () => ({
  useAuth: () => mockUseAuth(),
}));

function renderGuardedRoute(authValue, roles = ['ADMIN']) {
  mockUseAuth.mockReturnValue(authValue);

  return render(
    <MemoryRouter initialEntries={['/secure']}>
      <Routes>
        <Route
          path="/secure"
          element={
            <ProtectedRoute roles={roles}>
              <div>Secure Content</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div>Login Page</div>} />
        <Route path="/" element={<div>Home Page</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    mockUseAuth.mockReset();
  });

  it('redirects unauthenticated users to login', () => {
    renderGuardedRoute({ user: null, loading: false });
    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('renders children for allowed roles', () => {
    renderGuardedRoute({ user: { role: 'ROLE_ADMIN' }, loading: false });
    expect(screen.getByText('Secure Content')).toBeInTheDocument();
  });

  it('redirects authenticated users without required role to home', () => {
    renderGuardedRoute({ user: { role: 'ROLE_STUDENT' }, loading: false });
    expect(screen.getByText('Home Page')).toBeInTheDocument();
  });
});
