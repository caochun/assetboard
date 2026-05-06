import { useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  HomeIcon,
  CubeIcon,
  BellAlertIcon,
  UserGroupIcon,
  FolderIcon,
  DocumentTextIcon,
  Cog6ToothIcon,
  ArrowRightStartOnRectangleIcon,
  ChevronDownIcon,
} from '@heroicons/react/24/outline';

const navItems = [
  { to: '/', icon: HomeIcon, label: '总览' },
  { to: '/assets', icon: CubeIcon, label: '资产管理' },
  { to: '/alarms', icon: BellAlertIcon, label: '告警中心' },
  { to: '/customers', icon: UserGroupIcon, label: '客户管理' },
  { to: '/projects', icon: FolderIcon, label: '项目管理' },
  { to: '/contracts', icon: DocumentTextIcon, label: '合同管理' },
];

const settingsItems = [
  { to: '/settings/alarm-rules', label: '告警规则' },
  { to: '/settings/datasources', label: '数据源管理' },
];

export default function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const isSettingsActive = location.pathname.startsWith('/settings');
  const [settingsOpen, setSettingsOpen] = useState(isSettingsActive);

  return (
    <div className="flex h-screen bg-gray-50">
      <aside className="w-60 bg-white border-r border-gray-200 flex flex-col">
        <div className="h-16 flex items-center px-5 border-b border-gray-200">
          <span className="text-lg font-bold text-gray-900 tracking-tight">AssetBoard</span>
        </div>
        <nav className="flex-1 py-3 px-3 space-y-0.5">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-50 text-blue-700'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`
              }
            >
              <item.icon className="h-5 w-5" />
              {item.label}
            </NavLink>
          ))}

          {/* Settings group */}
          <button
            onClick={() => setSettingsOpen(!settingsOpen)}
            className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
              isSettingsActive
                ? 'bg-blue-50 text-blue-700'
                : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
            }`}
          >
            <Cog6ToothIcon className="h-5 w-5" />
            <span className="flex-1 text-left">系统设置</span>
            <ChevronDownIcon className={`h-4 w-4 transition-transform ${settingsOpen ? '' : '-rotate-90'}`} />
          </button>
          {settingsOpen && (
            <div className="ml-5 space-y-0.5">
              {settingsItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `flex items-center gap-3 pl-6 pr-3 py-1.5 rounded-lg text-sm transition-colors ${
                      isActive
                        ? 'text-blue-700 font-medium'
                        : 'text-gray-500 hover:bg-gray-100 hover:text-gray-900'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </div>
          )}
        </nav>
        <div className="border-t border-gray-200 p-4">
          <div className="text-sm font-medium text-gray-700 mb-1">{user?.name || user?.email}</div>
          <button
            onClick={logout}
            className="flex items-center gap-2 text-sm text-gray-400 hover:text-gray-600 transition-colors"
          >
            <ArrowRightStartOnRectangleIcon className="h-4 w-4" />
            退出登录
          </button>
        </div>
      </aside>
      <main className="flex-1 overflow-auto">
        <div className="max-w-7xl mx-auto px-8 py-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
