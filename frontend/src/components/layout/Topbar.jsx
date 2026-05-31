import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Menu, LogOut, Bell, ChevronDown, Search } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotificationContext } from '../../context/NotificationProvider';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
export function Topbar({ onMenuClick }) {
  const { user, logout, isAdmin, isStudent, isFaculty } = useAuth();
  const navigate = useNavigate();
  const { notifications, unreadCount, markAsRead } = useNotificationContext();
  const [notifOpen, setNotifOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [search, setSearch] = useState('');

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const submitSearch = (e) => {
    e.preventDefault();
    const q = search.trim();
    if (!q) return;
    if (isAdmin) {
      navigate(`/admin/users?q=${encodeURIComponent(q)}`);
    } else if (isStudent) {
      navigate(`/projects?q=${encodeURIComponent(q)}`);
    } else if (isFaculty) {
      navigate(`/faculty/students?q=${encodeURIComponent(q)}`);
    }
    setSearch('');
    setNotifOpen(false);
    setUserMenuOpen(false);
  };

  return (
    <header className="sticky top-0 z-20 flex h-14 items-center gap-3 border-b border-mentor-border/80 bg-mentor-card/90 px-3 backdrop-blur-md supports-backdrop-filter:bg-mentor-card/75 md:gap-4 md:px-6">
      <div className="flex shrink-0 items-center gap-2">
        <Button
          type="button"
          variant="ghost"
          size="sm"
          className="lg:hidden -ml-1"
          onClick={onMenuClick}
          aria-label="Open menu"
        >
          <Menu className="h-5 w-5" />
        </Button>
        <Link
          to="/"
          className="hidden text-sm font-semibold text-mentor-muted hover:text-mentor-text sm:block"
        >
          Home
        </Link>
      </div>

      <form
        onSubmit={submitSearch}
        className="mx-auto hidden max-w-xl flex-1 items-center gap-2 md:flex"
      >
        <div className="relative w-full">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-mentor-muted" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder={
              isAdmin
                ? 'Search users…'
                : isStudent
                  ? 'Search projects…'
                  : isFaculty
                    ? 'Search groups…'
                    : 'Search…'
            }
            className="w-full rounded-lg border border-mentor-border bg-mentor-surface/50 py-2 pl-9 pr-3 text-sm text-mentor-text placeholder:text-mentor-muted focus:border-mentor-primary focus:outline-none focus:ring-2 focus:ring-mentor-primary/20"
          />
        </div>
        <Button type="submit" variant="secondary" size="sm" className="shrink-0">
          Go
        </Button>
      </form>

      <div className="ml-auto flex items-center gap-1 sm:gap-2">
        <div className="relative">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="relative"
            onClick={() => {
              setNotifOpen(!notifOpen);
              setUserMenuOpen(false);
            }}
            aria-label="Notifications"
          >
            <Bell className="h-5 w-5 text-mentor-text" />
            {unreadCount > 0 && (
              <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-mentor-danger px-1 text-[10px] font-bold text-white">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </Button>
          {notifOpen && (
            <div className="absolute right-0 mt-2 w-80 max-h-96 overflow-y-auto rounded-xl border border-mentor-border bg-mentor-card/95 py-2 shadow-xl backdrop-blur-md">
              <div className="flex items-center justify-between border-b border-mentor-border px-4 py-2">
                <span className="text-sm font-semibold text-mentor-text">Notifications</span>
                {unreadCount > 0 && <Badge variant="primary">{unreadCount} new</Badge>}
              </div>
              <Link
                to="/notifications"
                className="block px-4 py-2 text-xs font-medium text-mentor-primary hover:bg-mentor-surface"
                onClick={() => setNotifOpen(false)}
              >
                View all
              </Link>
              {notifications.length === 0 ? (
                <div className="px-4 py-8 text-center text-sm text-mentor-muted">No notifications</div>
              ) : (
                notifications.map((n) => (
                  <button
                    key={n.id}
                    type="button"
                    onClick={() => {
                      markAsRead(n.id);
                      setNotifOpen(false);
                    }}
                    className={`w-full border-b border-mentor-border px-4 py-3 text-left text-sm last:border-0 hover:bg-mentor-surface ${
                      !n.read ? 'bg-mentor-primary/5' : ''
                    }`}
                  >
                    {n.message}
                  </button>
                ))
              )}
            </div>
          )}
        </div>

        <div className="relative">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="max-w-[180px] gap-1 sm:max-w-[220px]"
            onClick={() => {
              setUserMenuOpen(!userMenuOpen);
              setNotifOpen(false);
            }}
          >
            <span className="truncate text-sm font-medium text-mentor-text">
              {user?.fullName || user?.email}
            </span>
            <ChevronDown className="h-4 w-4 shrink-0 text-mentor-muted" />
          </Button>
          {userMenuOpen && (
            <div className="absolute right-0 mt-2 w-48 rounded-xl border border-mentor-border bg-mentor-card/95 py-2 shadow-xl backdrop-blur-md">
              <Link
                to="/profile"
                className="block px-4 py-2 text-sm text-mentor-text hover:bg-mentor-surface"
                onClick={() => setUserMenuOpen(false)}
              >
                Profile
              </Link>
              <button
                type="button"
                onClick={() => {
                  setUserMenuOpen(false);
                  handleLogout();
                }}
                className="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-mentor-danger hover:bg-mentor-surface"
              >
                <LogOut className="h-4 w-4" />
                Log out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
