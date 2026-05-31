import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { cn } from '../../lib/utils';
import {
  GraduationCap,
  LayoutDashboard,
  FolderKanban,
  Users,
  Bell,
  User,
  Upload,
  Calendar,
  Sparkles,
  BarChart3,
  ChevronsLeft,
  ChevronsRight,
  UsersRound,
} from 'lucide-react';
import { Button } from '../ui/Button';

function NavItem({ to, icon, children, onNavigate, end, collapsed, label }) {
  const Icon = icon;
  const text = label || children;
  return (
    <NavLink
      to={to}
      end={end}
      onClick={onNavigate}
      title={collapsed ? text : undefined}
      className={({ isActive }) =>
        cn(
          'flex items-center gap-3 rounded-lg py-2.5 text-sm font-medium transition',
          collapsed ? 'justify-center px-2' : 'px-3',
          isActive
            ? 'bg-mentor-primary text-white shadow-md shadow-mentor-primary/25'
            : 'text-slate-300 hover:bg-white/5 hover:text-white'
        )
      }
    >
      <Icon className="h-5 w-5 shrink-0 opacity-90" />
      <span className={cn('truncate', collapsed && 'sr-only')}>{children}</span>
    </NavLink>
  );
}

function NavSection({ title, children, collapsed }) {
  return (
    <div className="mt-6 first:mt-0">
      {title && !collapsed && (
        <p className="mb-2 px-3 text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</p>
      )}
      {title && collapsed && (
        <div className="mx-auto mb-2 h-px w-8 bg-mentor-sidebar-border" aria-hidden />
      )}
      <div className="space-y-0.5">{children}</div>
    </div>
  );
}

export function Sidebar({ mobileOpen, onClose, collapsed, onToggleCollapse }) {
  const { isStudent, isFaculty, isAdmin } = useAuth();

  const closeIfMobile = () => {
    if (window.matchMedia('(max-width: 1023px)').matches) onClose?.();
  };

  const rail = (
    <>
      <div
        className={cn(
          'flex h-16 items-center border-b border-mentor-sidebar-border px-3',
          collapsed ? 'justify-center' : 'gap-2 px-4'
        )}
      >
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-mentor-primary text-white">
          <GraduationCap className="h-5 w-5" />
        </div>
        {!collapsed && (
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-bold text-white">MentorLink</p>
            <p className="truncate text-xs text-slate-500">University SaaS</p>
          </div>
        )}
      </div>

      <nav className="flex flex-1 flex-col overflow-hidden">
        <div className="flex-1 overflow-y-auto px-2 py-4">
          {isStudent && (
            <NavSection title="Student" collapsed={collapsed}>
              <NavItem to="/dashboard/student" icon={LayoutDashboard} onNavigate={closeIfMobile} end collapsed={collapsed} label="Dashboard">
                Dashboard
              </NavItem>
              <NavItem to="/student/groups" icon={UsersRound} onNavigate={closeIfMobile} collapsed={collapsed} label="Groups">
                Groups
              </NavItem>
              <NavItem to="/projects" icon={FolderKanban} onNavigate={closeIfMobile} collapsed={collapsed} label="Projects">
                Projects
              </NavItem>
              <NavItem to="/notifications" icon={Bell} onNavigate={closeIfMobile} collapsed={collapsed} label="Notifications">
                Notifications
              </NavItem>
            </NavSection>
          )}

          {isFaculty && (
            <NavSection title="Faculty" collapsed={collapsed}>
              <NavItem to="/dashboard/faculty" icon={LayoutDashboard} onNavigate={closeIfMobile} end collapsed={collapsed} label="Dashboard">
                Dashboard
              </NavItem>
              <NavItem to="/faculty/projects" icon={FolderKanban} onNavigate={closeIfMobile} collapsed={collapsed} label="Assigned projects">
                Assigned projects
              </NavItem>
              <NavItem to="/faculty/students" icon={Users} onNavigate={closeIfMobile} collapsed={collapsed} label="Students">
                Students
              </NavItem>
              <NavItem to="/faculty/recommendations" icon={Sparkles} onNavigate={closeIfMobile} collapsed={collapsed} label="Recommendations">
                Recommendations
              </NavItem>
            </NavSection>
          )}

          {isAdmin && (
            <NavSection title="Admin" collapsed={collapsed}>
              <NavItem to="/dashboard/admin" icon={LayoutDashboard} onNavigate={closeIfMobile} end collapsed={collapsed} label="Dashboard">
                Dashboard
              </NavItem>
              <NavItem to="/admin/users" icon={Users} onNavigate={closeIfMobile} collapsed={collapsed} label="Users">
                Users
              </NavItem>
              <NavItem to="/admin/analytics" icon={BarChart3} onNavigate={closeIfMobile} collapsed={collapsed} label="Analytics">
                Analytics
              </NavItem>
              <NavItem to="/admin/upload" icon={Upload} onNavigate={closeIfMobile} collapsed={collapsed} label="Upload">
                Upload
              </NavItem>
              <NavItem to="/admin/deadlines" icon={Calendar} onNavigate={closeIfMobile} collapsed={collapsed} label="Deadlines">
                Deadlines
              </NavItem>
              <NavItem to="/admin/auto-group" icon={Sparkles} onNavigate={closeIfMobile} collapsed={collapsed} label="Auto group">
                Auto group
              </NavItem>
            </NavSection>
          )}

          <NavSection title="Account" collapsed={collapsed}>
            <NavItem to="/profile" icon={User} onNavigate={closeIfMobile} collapsed={collapsed} label="Profile">
              Profile
            </NavItem>
          </NavSection>
        </div>

        <div className="hidden border-t border-mentor-sidebar-border p-2 lg:block">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="w-full justify-center text-slate-300 hover:bg-white/5 hover:text-white"
            onClick={onToggleCollapse}
            aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? <ChevronsRight className="h-5 w-5" /> : <ChevronsLeft className="h-5 w-5" />}
          </Button>
        </div>
      </nav>
    </>
  );

  return (
    <>
      {mobileOpen && (
        <button
          type="button"
          aria-label="Close menu"
          className="fixed inset-0 z-40 bg-mentor-text/40 backdrop-blur-sm lg:hidden"
          onClick={onClose}
        />
      )}
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 flex flex-col border-r border-mentor-sidebar-border bg-mentor-sidebar/95 backdrop-blur-md transition-all duration-200 lg:static lg:translate-x-0',
          collapsed ? 'w-[72px] lg:w-[72px]' : 'w-64',
          mobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        )}
      >
        {rail}
      </aside>
    </>
  );
}
