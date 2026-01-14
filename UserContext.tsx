import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { User, WakeUpRecord } from './types';

interface UserContextType {
  user: User;
  history: WakeUpRecord[];
  login: (name: string) => void;
  logout: () => void;
  recordWakeUp: () => void;
  streak: number;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

const STORAGE_KEY_USER = 'wakeguard_user';
const STORAGE_KEY_HISTORY = 'wakeguard_history';

export const UserProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User>({ name: 'Guest', isLoggedIn: false });
  const [history, setHistory] = useState<WakeUpRecord[]>([]);

  useEffect(() => {
    const savedUser = localStorage.getItem(STORAGE_KEY_USER);
    const savedHistory = localStorage.getItem(STORAGE_KEY_HISTORY);
    if (savedUser) setUser(JSON.parse(savedUser));
    if (savedHistory) setHistory(JSON.parse(savedHistory));
    else {
        // Seed some fake history for demo purposes if empty
        const fakeHistory = [];
        const today = new Date();
        for(let i=1; i<=4; i++) {
            const d = new Date(today);
            d.setDate(d.getDate() - i);
            fakeHistory.push({
                date: d.toISOString().split('T')[0],
                time: '07:30'
            });
        }
        setHistory(fakeHistory);
    }
  }, []);

  const login = (name: string) => {
    const newUser = { name, isLoggedIn: true, avatarUrl: `https://ui-avatars.com/api/?name=${name}&background=6366F1&color=fff` };
    setUser(newUser);
    localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(newUser));
  };

  const logout = () => {
    const guestUser = { name: 'Guest', isLoggedIn: false };
    setUser(guestUser);
    localStorage.removeItem(STORAGE_KEY_USER);
  };

  const recordWakeUp = () => {
    const now = new Date();
    const todayStr = now.toISOString().split('T')[0];
    
    // Check if already recorded today
    if (history.some(h => h.date === todayStr)) return;

    const newRecord = { date: todayStr, time: now.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) };
    const newHistory = [newRecord, ...history];
    setHistory(newHistory);
    localStorage.setItem(STORAGE_KEY_HISTORY, JSON.stringify(newHistory));
  };

  // Calculate streak
  const streak = React.useMemo(() => {
    if (history.length === 0) return 0;
    
    const sortedDates = (Array.from(new Set(history.map(h => h.date))) as string[]).sort((a, b) => new Date(b).getTime() - new Date(a).getTime());
    
    let currentStreak = 0;
    const today = new Date().toISOString().split('T')[0];
    const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0];

    // If latest is today or yesterday, streak is active
    if (sortedDates[0] !== today && sortedDates[0] !== yesterday) return 0;

    let checkDate = new Date(sortedDates[0]); // Start checking from latest record

    for (let i = 0; i < sortedDates.length; i++) {
        const recordDate = new Date(sortedDates[i]);
        // Compare dates ignoring time
        if (checkDate.toISOString().split('T')[0] === recordDate.toISOString().split('T')[0]) {
            currentStreak++;
            checkDate.setDate(checkDate.getDate() - 1); // Move to previous day
        } else {
            break;
        }
    }
    return currentStreak;
  }, [history]);

  return (
    <UserContext.Provider value={{ user, history, login, logout, recordWakeUp, streak }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) throw new Error("useUser must be used within UserProvider");
  return context;
};