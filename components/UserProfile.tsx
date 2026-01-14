import React, { useState } from 'react';
import { useUser } from '../UserContext';
import { useLanguage } from '../i18n';
import { Calendar } from './Calendar';
import { NeumorphicButton } from './NeumorphicButton';
import { User, Globe, ChevronLeft, Moon, Sun, Award, LogOut, Camera, Smartphone } from 'lucide-react';
import { ThemeMode } from '../App';

interface Props {
  onBack: () => void;
  isDark: boolean;
  themeMode: ThemeMode;
  setThemeMode: (mode: ThemeMode) => void;
}

export const UserProfile: React.FC<Props> = ({ onBack, isDark, themeMode, setThemeMode }) => {
  const { user, login, logout, streak, history } = useUser();
  const { t, language, setLanguage } = useLanguage();
  const [usernameInput, setUsernameInput] = useState('');

  const handleLogin = () => {
    if (usernameInput.trim()) {
      login(usernameInput.trim());
    }
  };

  return (
    <div className={`min-h-screen p-6 flex flex-col items-center relative overflow-y-auto ${isDark ? 'bg-gray-900 text-white' : 'bg-page-bg text-text-main'}`}>
      
      {/* Header */}
      <div className="w-full max-w-md flex items-center justify-between mb-8 z-10 pt-2">
        <button 
          onClick={onBack}
          className={`p-2 rounded-full backdrop-blur-md border shadow-sm transition-all ${isDark ? 'bg-gray-800 border-gray-700 text-gray-300 hover:bg-gray-700' : 'bg-white/60 border-white/50 text-gray-600 hover:bg-white/80'}`}
        >
          <ChevronLeft size={24} />
        </button>
        <h1 className="text-xl font-bold tracking-tight opacity-90">{t('userCenter')}</h1>
        <div className="w-10"></div> {/* Spacer */}
      </div>

      {/* User Card */}
      <div className={`w-full max-w-md rounded-3xl p-6 mb-6 shadow-xl border relative overflow-hidden ${isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-white/60'}`}>
        {!user.isLoggedIn ? (
          <div className="flex flex-col items-center gap-4">
            <div className="w-20 h-20 rounded-full bg-gray-200 flex items-center justify-center text-gray-400 mb-2">
               <User size={40} />
            </div>
            <h2 className="text-xl font-bold">{t('guest')}</h2>
            <div className="w-full space-y-3">
              <input 
                type="text" 
                placeholder={t('enterUsername')}
                value={usernameInput}
                onChange={(e) => setUsernameInput(e.target.value)}
                className={`w-full px-4 py-3 rounded-xl outline-none focus:ring-2 focus:ring-indigo-500 transition-all text-center ${isDark ? 'bg-gray-700 text-white placeholder-gray-500' : 'bg-gray-50 text-gray-900 placeholder-gray-400'}`}
              />
              <NeumorphicButton onClick={handleLogin} variant="primary" className="w-full !py-3">
                {t('loginBtn')}
              </NeumorphicButton>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-4">
            <div className="relative">
                <img src={user.avatarUrl} alt="Avatar" className="w-20 h-20 rounded-full border-4 border-indigo-100 shadow-sm" />
                <div className="absolute bottom-0 right-0 bg-green-500 w-5 h-5 rounded-full border-2 border-white"></div>
            </div>
            <div className="flex-1">
              <h2 className={`text-2xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>{user.name}</h2>
              <div className="text-xs text-indigo-500 font-medium bg-indigo-500/10 px-2 py-0.5 rounded-full inline-block mt-1">
                WakeGuard ID: #{Math.floor(Math.random()*10000)}
              </div>
            </div>
            <button onClick={logout} className={`p-2 rounded-full ${isDark ? 'hover:bg-gray-700 text-gray-400' : 'hover:bg-gray-100 text-gray-400'}`}>
               <LogOut size={20} />
            </button>
          </div>
        )}
      </div>

      {/* Stats / Streak */}
      {user.isLoggedIn && (
         <div className="w-full max-w-md mb-6 animate-slide-up">
            <div className={`rounded-3xl p-6 flex items-center justify-between border shadow-lg bg-gradient-to-r ${isDark ? 'from-indigo-900 to-purple-900 border-indigo-800' : 'from-indigo-500 to-purple-600 text-white border-transparent'}`}>
                <div>
                    <div className={`text-sm font-medium opacity-80 ${isDark ? 'text-indigo-200' : 'text-indigo-100'}`}>{t('streakTitle')}</div>
                    <div className="text-4xl font-black mt-1 flex items-baseline gap-2">
                        {streak} <span className="text-lg font-bold opacity-60">{t('streakDays').replace('{n}', '')}</span>
                    </div>
                </div>
                <div className="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center backdrop-blur-sm">
                    <Award size={32} className="text-yellow-300" strokeWidth={3} />
                </div>
            </div>
            <p className={`text-center text-xs mt-3 font-medium ${isDark ? 'text-gray-500' : 'text-gray-500'}`}>
                "{t('motivation')}"
            </p>
         </div>
      )}

      {/* Calendar */}
      <div className="w-full max-w-md mb-6 animate-slide-up" style={{ animationDelay: '0.1s' }}>
          <h3 className={`text-sm font-bold mb-3 px-2 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>{t('calendar')}</h3>
          <Calendar history={history} isDark={isDark} />
      </div>

      {/* Settings Group */}
      <div className="w-full max-w-md animate-slide-up" style={{ animationDelay: '0.2s' }}>
          <h3 className={`text-sm font-bold mb-3 px-2 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>{t('settings')}</h3>
          <div className={`rounded-3xl shadow-sm border overflow-hidden ${isDark ? 'bg-gray-800 border-gray-700 divide-gray-700' : 'bg-white border-white/60 divide-gray-100'}`}>
              
              {/* Language */}
              <div className="p-4 flex justify-between items-center border-b-[1px] border-inherit">
                  <div className="flex items-center gap-3">
                      <div className={`w-8 h-8 rounded-full flex items-center justify-center ${isDark ? 'bg-blue-900/30 text-blue-400' : 'bg-blue-50 text-blue-500'}`}>
                          <Globe size={16} />
                      </div>
                      <span className={`font-medium ${isDark ? 'text-gray-200' : 'text-gray-700'}`}>{t('language')}</span>
                  </div>
                  <div className="flex bg-gray-100/10 p-1 rounded-lg border border-gray-200/20">
                      <button 
                        onClick={() => setLanguage('zh')}
                        className={`px-3 py-1 text-xs font-bold rounded-md transition-all ${language === 'zh' ? (isDark ? 'bg-gray-600 text-white' : 'bg-white text-indigo-600 shadow-sm') : (isDark ? 'text-gray-400' : 'text-gray-500')}`}
                      >
                        中文
                      </button>
                      <button 
                        onClick={() => setLanguage('en')}
                        className={`px-3 py-1 text-xs font-bold rounded-md transition-all ${language === 'en' ? (isDark ? 'bg-gray-600 text-white' : 'bg-white text-indigo-600 shadow-sm') : (isDark ? 'text-gray-400' : 'text-gray-500')}`}
                      >
                        English
                      </button>
                  </div>
              </div>

              {/* Theme Mode */}
              <div className="p-4 flex justify-between items-center">
                  <div className="flex items-center gap-3">
                      <div className={`w-8 h-8 rounded-full flex items-center justify-center ${isDark ? 'bg-purple-900/30 text-purple-400' : 'bg-purple-50 text-purple-500'}`}>
                          {themeMode === 'auto' ? <Smartphone size={16} /> : (themeMode === 'dark' ? <Moon size={16} /> : <Sun size={16} />)}
                      </div>
                      <span className={`font-medium ${isDark ? 'text-gray-200' : 'text-gray-700'}`}>{t('themeMode')}</span>
                  </div>
                  <div className="flex bg-gray-100/10 p-1 rounded-lg border border-gray-200/20">
                      <button 
                        onClick={() => setThemeMode('light')}
                        className={`px-3 py-1 text-xs font-bold rounded-md transition-all ${themeMode === 'light' ? (isDark ? 'bg-gray-600 text-white' : 'bg-white text-indigo-600 shadow-sm') : (isDark ? 'text-gray-400' : 'text-gray-500')}`}
                      >
                        {t('themeLight')}
                      </button>
                      <button 
                        onClick={() => setThemeMode('dark')}
                        className={`px-3 py-1 text-xs font-bold rounded-md transition-all ${themeMode === 'dark' ? (isDark ? 'bg-gray-600 text-white' : 'bg-white text-indigo-600 shadow-sm') : (isDark ? 'text-gray-400' : 'text-gray-500')}`}
                      >
                        {t('themeDark')}
                      </button>
                      <button 
                        onClick={() => setThemeMode('auto')}
                        className={`px-3 py-1 text-xs font-bold rounded-md transition-all ${themeMode === 'auto' ? (isDark ? 'bg-gray-600 text-white' : 'bg-white text-indigo-600 shadow-sm') : (isDark ? 'text-gray-400' : 'text-gray-500')}`}
                      >
                        {t('themeAuto')}
                      </button>
                  </div>
              </div>

          </div>
      </div>

    </div>
  );
};