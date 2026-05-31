import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';

const MotionDiv = motion.div;

export function DashboardLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const location = useLocation();

  return (
    <div className="flex min-h-screen bg-gradient-to-br from-mentor-surface via-white to-mentor-surface">
      <Sidebar
        mobileOpen={mobileOpen}
        onClose={() => setMobileOpen(false)}
        collapsed={sidebarCollapsed}
        onToggleCollapse={() => setSidebarCollapsed((c) => !c)}
      />
      <div
        className={`flex min-w-0 flex-1 flex-col transition-[margin] duration-200 ${
          sidebarCollapsed ? 'lg:ml-0' : 'lg:ml-0'
        }`}
      >
        <Topbar onMenuClick={() => setMobileOpen(true)} />
        <main className="flex-1 px-4 py-6 md:px-8">
          <div className="mx-auto max-w-7xl">
            <AnimatePresence mode="wait">
              <MotionDiv
                key={location.pathname}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -6 }}
                transition={{ duration: 0.2, ease: [0.22, 1, 0.36, 1] }}
              >
                <Outlet />
              </MotionDiv>
            </AnimatePresence>
          </div>
        </main>
      </div>
    </div>
  );
}
