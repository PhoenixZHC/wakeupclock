import React, { useState, useEffect } from 'react';
import { Alarm, AppState, MissionType, Difficulty, RepeatMode } from './types';
import { Dashboard } from './components/Dashboard';
import { AlarmLockdown } from './components/AlarmLockdown';
import { UserProfile } from './components/UserProfile';
import { LanguageProvider } from './i18n';
import { UserProvider, useUser } from './UserContext';

export type ThemeMode = 'auto' | 'light' | 'dark';

const AppContent: React.FC = () => {
  const [appState, setAppState] = useState<AppState>(AppState.DASHBOARD);
  const [activeAlarm, setActiveAlarm] = useState<Alarm | null>(null);
  const [themeMode, setThemeMode] = useState<ThemeMode>('auto');
  const [isDark, setIsDark] = useState(false);
  
  // Use User Context to record success
  const { recordWakeUp } = useUser();

  const [alarms, setAlarms] = useState<Alarm[]>([
    { 
      id: '1', 
      time: '07:30', 
      enabled: true, 
      label: '工作', 
      missionType: MissionType.MATH, 
      difficulty: Difficulty.MEDIUM,
      repeatMode: RepeatMode.WORKDAYS,
      customDays: [1, 2, 3, 4, 5],
      skipHolidays: true
    },
    { 
      id: '2', 
      time: '08:00', 
      enabled: false, 
      label: '备用', 
      missionType: MissionType.SHAKE, 
      difficulty: Difficulty.EASY,
      repeatMode: RepeatMode.ONCE,
      customDays: [],
      skipHolidays: false
    }
  ]);

  // Theme Logic
  useEffect(() => {
    const checkTheme = () => {
      if (themeMode === 'auto') {
          const hour = new Date().getHours();
          setIsDark(hour >= 18 || hour < 6);
      } else {
          setIsDark(themeMode === 'dark');
      }
    };
    checkTheme();
    
    // Only set interval if auto
    let interval: number | undefined;
    if (themeMode === 'auto') {
        interval = window.setInterval(checkTheme, 60000); // Check every minute
    }
    
    return () => {
        if (interval) clearInterval(interval);
    };
  }, [themeMode]);

  // Request notification permission on mount
  useEffect(() => {
    if ('Notification' in window) {
      Notification.requestPermission();
    }
  }, []);

  // Time Check Loop
  useEffect(() => {
    const interval = setInterval(() => {
      if (appState !== AppState.DASHBOARD && appState !== AppState.PROFILE) return;

      const now = new Date();
      const currentTime = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });

      const triggeringAlarm = alarms.find(a => a.enabled && a.time === currentTime);
      
      if (triggeringAlarm) {
        // Double check seconds to avoid multiple triggers within the same minute
        if (now.getSeconds() < 2) {
             triggerAlarm(triggeringAlarm);
        }
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [alarms, appState]);

  const triggerAlarm = (alarm: Alarm) => {
    setActiveAlarm(alarm);
    setAppState(AppState.RINGING);
  };

  const handleMissionSolved = () => {
    recordWakeUp(); // Record success
    setAppState(AppState.DASHBOARD);
    setActiveAlarm(null);
    if (activeAlarm) {
        if (activeAlarm.repeatMode === RepeatMode.ONCE) {
             setAlarms(prev => prev.map(a => a.id === activeAlarm.id ? { ...a, enabled: false } : a));
        }
    }
  };

  const handleAddAlarm = (newAlarm: Alarm) => {
    setAlarms(prev => [...prev, newAlarm]);
  };

  const handleToggleAlarm = (id: string) => {
    setAlarms(prev => prev.map(a => 
      a.id === id ? { ...a, enabled: !a.enabled } : a
    ));
  };

  const handleDeleteAlarm = (id: string) => {
    setAlarms(prev => prev.filter(a => a.id !== id));
  };

  return (
    <div className={`font-sans antialiased ${isDark ? 'dark bg-gray-900' : 'bg-gray-50'}`}>
      {appState === AppState.DASHBOARD && (
        <Dashboard 
          alarms={alarms} 
          onAddAlarm={handleAddAlarm} 
          onToggleAlarm={handleToggleAlarm}
          onDeleteAlarm={handleDeleteAlarm}
          onOpenProfile={() => setAppState(AppState.PROFILE)}
          isDark={isDark}
        />
      )}

      {appState === AppState.PROFILE && (
        <UserProfile 
            onBack={() => setAppState(AppState.DASHBOARD)} 
            isDark={isDark}
            themeMode={themeMode}
            setThemeMode={setThemeMode}
        />
      )}

      {(appState === AppState.RINGING || appState === AppState.MISSION) && activeAlarm && (
        <AlarmLockdown 
          alarm={activeAlarm} 
          onSolved={handleMissionSolved} 
        />
      )}
    </div>
  );
};

const App: React.FC = () => {
  return (
    <LanguageProvider>
      <UserProvider>
        <AppContent />
      </UserProvider>
    </LanguageProvider>
  );
};

export default App;