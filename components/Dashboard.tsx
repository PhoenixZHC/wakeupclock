import React, { useState, useEffect, useMemo } from 'react';
import { Alarm, MissionType, Difficulty, RepeatMode } from '../types';
import { NeumorphicButton } from './NeumorphicButton';
import { Plus, Trash2, Activity, Calendar, Accessibility, User as UserIcon, Flame } from 'lucide-react';
import { useLanguage } from '../i18n';
import { useUser } from '../UserContext';

interface Props {
  alarms: Alarm[];
  onAddAlarm: (alarm: Alarm) => void;
  onToggleAlarm: (id: string) => void;
  onDeleteAlarm: (id: string) => void;
  onOpenProfile: () => void;
  isDark: boolean;
}

const AlarmItem: React.FC<{ 
  alarm: Alarm; 
  onToggle: (id: string) => void; 
  onDelete: (id: string) => void;
  t: any;
  isDark: boolean;
}> = ({ alarm, onToggle, onDelete, t, isDark }) => {
  const [offset, setOffset] = useState(0);
  const [startX, setStartX] = useState(0);
  const [startOffset, setStartOffset] = useState(0);

  const handleTouchStart = (e: React.TouchEvent) => {
    setStartX(e.touches[0].clientX);
    setStartOffset(offset);
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    const currentX = e.touches[0].clientX;
    const diff = currentX - startX;
    const proposedOffset = startOffset + diff;
    const clampedOffset = Math.min(0, Math.max(proposedOffset, -100));
    setOffset(clampedOffset);
  };

  const handleTouchEnd = () => {
    if (offset < -40) {
      setOffset(-80);
    } else {
      setOffset(0);
    }
  };

  const sortDays = (days: number[]) => {
      return [...days].sort((a,b) => (a===0?7:a) - (b===0?7:b));
  };

  const getDayLabel = (d: number) => t(`day_${d}` as any);

  return (
    <div className="relative w-full h-24 mb-4 select-none group overflow-hidden">
        {/* Background Delete Action */}
        <div 
            className="absolute inset-0 bg-red-500 rounded-2xl flex items-center justify-end px-6 cursor-pointer active:bg-red-600 transition-colors"
            onClick={() => onDelete(alarm.id)}
        >
            <Trash2 className="text-white" size={24} />
        </div>

        {/* Foreground Card */}
        <div 
            className={`absolute inset-0 rounded-2xl p-5 shadow-soft border flex justify-between items-center z-10 transition-transform duration-200 ease-out touch-pan-y
                ${isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-100'}
            `}
            style={{ transform: `translateX(${offset}px)` }}
            onTouchStart={handleTouchStart}
            onTouchMove={handleTouchMove}
            onTouchEnd={handleTouchEnd}
        >
            <div>
              <div className={`text-3xl font-light tracking-tight font-[Inter] ${isDark ? 'text-white' : 'text-text-main'}`}>{alarm.time}</div>
              <div className={`flex items-center gap-2 text-xs mt-1 ${isDark ? 'text-gray-400' : 'text-text-sub'}`}>
                {alarm.repeatMode === RepeatMode.ONCE && (
                    <span className={`px-2 py-0.5 rounded-md ${isDark ? 'bg-gray-700 text-gray-300' : 'bg-gray-100 text-gray-500'}`}>{t('repeatOnce')}</span>
                )}
                {alarm.repeatMode === RepeatMode.WORKDAYS && (
                    <span className={`px-2 py-0.5 rounded-md ${isDark ? 'bg-gray-700 text-gray-300' : 'bg-gray-100 text-gray-500'}`}>{t('repeatWorkdays')}</span>
                )}
                {alarm.repeatMode === RepeatMode.CUSTOM && (
                    <div className="flex items-center gap-2 flex-wrap">
                        {alarm.customDays.length === 7 ? (
                             <span className={`px-2 py-0.5 rounded-md ${isDark ? 'bg-gray-700 text-gray-300' : 'bg-gray-100 text-gray-500'}`}>{t('everyday')}</span>
                        ) : alarm.customDays.length === 0 ? (
                             <span className={`px-2 py-0.5 rounded-md ${isDark ? 'bg-gray-700 text-gray-300' : 'bg-gray-100 text-gray-500'}`}>{t('notSet')}</span>
                        ) : (
                             <div className="flex gap-1">
                                {sortDays(alarm.customDays).map(d => (
                                    <div key={d} className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold ${isDark ? 'bg-gray-700 text-gray-300' : 'bg-gray-100 text-gray-500'}`}>
                                        {getDayLabel(d)}
                                    </div>
                                ))}
                             </div>
                        )}
                        {alarm.skipHolidays && (
                            <span className="bg-orange-500/10 text-orange-500 px-2 py-0.5 rounded-md flex items-center gap-1 text-[10px]">
                                <Calendar size={10} /> {t('skipHolidaysTag')}
                            </span>
                        )}
                    </div>
                )}
              </div>
            </div>
            
            {/* Toggle Switch */}
            <button 
              onClick={(e) => { e.stopPropagation(); onToggle(alarm.id); }}
              className={`w-12 h-7 rounded-full relative transition-all duration-300 flex-shrink-0 ${alarm.enabled ? 'bg-primary-brand shadow-glow' : (isDark ? 'bg-gray-600 shadow-inner' : 'bg-gray-200 shadow-inner')}`}
            >
              <div className={`absolute top-1 w-5 h-5 rounded-full bg-white shadow-sm transition-all duration-300 ${alarm.enabled ? 'left-6' : 'left-1'}`}></div>
            </button>
        </div>
    </div>
  );
};

export const Dashboard: React.FC<Props> = ({ alarms, onAddAlarm, onToggleAlarm, onDeleteAlarm, onOpenProfile, isDark }) => {
  const { t, language } = useLanguage();
  const { user, streak } = useUser();
  const [time, setTime] = useState(new Date());
  const [showAddModal, setShowAddModal] = useState(false);
  
  // New Alarm Form State
  const [newTime, setNewTime] = useState('07:00');
  const [repeatMode, setRepeatMode] = useState<RepeatMode>(RepeatMode.WORKDAYS);
  const [customDays, setCustomDays] = useState<number[]>([1, 2, 3, 4, 5]);
  const [skipHolidays, setSkipHolidays] = useState(false);

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const countdownText = useMemo(() => {
    const enabledAlarms = alarms.filter(a => a.enabled);
    if (enabledAlarms.length === 0) return null;

    let minDiff = Infinity;
    const now = time;

    enabledAlarms.forEach(alarm => {
        const [h, m] = alarm.time.split(':').map(Number);
        for(let i = 0; i < 8; i++) {
            const d = new Date(now);
            d.setDate(d.getDate() + i);
            d.setHours(h, m, 0, 0);
            if (d <= now) continue;

            const day = d.getDay();
            let isActive = false;

            if (alarm.repeatMode === RepeatMode.ONCE) isActive = true;
            else if (alarm.repeatMode === RepeatMode.WORKDAYS) isActive = (day >= 1 && day <= 5);
            else if (alarm.repeatMode === RepeatMode.CUSTOM) isActive = alarm.customDays.includes(day);

            if (isActive) {
                const diff = d.getTime() - now.getTime();
                if (diff < minDiff) minDiff = diff;
                break;
            }
        }
    });

    if (minDiff === Infinity) return null;

    const totalMinutes = Math.floor(minDiff / 60000);
    const days = Math.floor(totalMinutes / (24 * 60));
    const hours = Math.floor((totalMinutes % (24 * 60)) / 60);
    const minutes = totalMinutes % 60;

    if (days > 0) return t('remainingDays', { d: days, h: hours });
    if (hours > 0) return t('remainingHours', { h: hours, m: minutes });
    return t('remainingMinutes', { m: minutes });
  }, [alarms, time, t]);

  const handleSaveAlarm = () => {
    const randomMission = Math.random() > 0.5 ? MissionType.MATH : MissionType.SHAKE;
    const alarm: Alarm = {
      id: Date.now().toString(),
      time: newTime,
      enabled: true,
      label: '起床',
      missionType: randomMission,
      difficulty: Difficulty.MEDIUM, 
      repeatMode,
      customDays: repeatMode === RepeatMode.CUSTOM ? customDays : [],
      skipHolidays: repeatMode === RepeatMode.CUSTOM ? skipHolidays : false
    };
    onAddAlarm(alarm);
    setShowAddModal(false);
  };

  const toggleDay = (dayVal: number) => {
      if (customDays.includes(dayVal)) {
          setCustomDays(customDays.filter(d => d !== dayVal));
      } else {
          setCustomDays([...customDays, dayVal]);
      }
  };

  const DAYS_LIST = [
    { val: 1, label: t('day_1') },
    { val: 2, label: t('day_2') },
    { val: 3, label: t('day_3') },
    { val: 4, label: t('day_4') },
    { val: 5, label: t('day_5') },
    { val: 6, label: t('day_6') },
    { val: 0, label: t('day_0') },
  ];

  const seconds = time.getSeconds();
  const radius = 120;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (seconds / 60) * circumference;
  const locale = language === 'zh' ? 'zh-CN' : 'en-US';

  return (
    <div className={`min-h-screen p-6 flex flex-col items-center relative overflow-hidden transition-colors duration-500 ${isDark ? 'bg-gray-900 text-white' : 'bg-page-bg text-text-main'}`}>
      
      {/* Decorative Background Blob */}
      <div className={`absolute -top-20 -right-20 w-64 h-64 rounded-full blur-3xl opacity-50 pointer-events-none ${isDark ? 'bg-purple-900/40' : 'bg-purple-200'}`}></div>
      <div className={`absolute top-40 -left-20 w-72 h-72 rounded-full blur-3xl opacity-50 pointer-events-none ${isDark ? 'bg-blue-900/40' : 'bg-blue-200'}`}></div>

      {/* Header */}
      <div className="w-full max-w-md flex justify-between items-start mb-8 z-10 pt-2">
        <div className="flex flex-col justify-center">
            <h1 className={`text-2xl font-black tracking-tight flex items-center gap-2 italic ${isDark ? 'text-white' : 'text-text-main'}`}>
              <Accessibility className="text-primary-brand" size={28} strokeWidth={2.5}/> 
              {t('appName')}
            </h1>
            <p className={`text-[10px] font-medium ml-1 mt-1 tracking-wider pl-9 opacity-80 ${isDark ? 'text-gray-400' : 'text-text-sub'}`}>
              {t('slogan')}
            </p>
        </div>
        
        {/* User Button (Date Removed, Size Increased) */}
        <div className="flex flex-col items-end gap-2">
            <button 
                onClick={onOpenProfile}
                className={`flex items-center gap-3 pr-4 pl-1.5 py-1.5 rounded-full backdrop-blur-md shadow-sm border transition-colors group ${isDark ? 'bg-gray-800/60 border-gray-700 hover:bg-gray-700' : 'bg-white/60 border-white/50 hover:bg-white/80'}`}
            >
                {user.avatarUrl ? (
                    <img src={user.avatarUrl} alt="User" className="w-8 h-8 rounded-full border border-gray-200" />
                ) : (
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center ${isDark ? 'bg-gray-700 text-gray-300' : 'bg-gray-200 text-gray-500'}`}>
                        <UserIcon size={18} />
                    </div>
                )}
                <span className={`text-xs font-bold ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>{user.name}</span>
            </button>
        </div>
      </div>

      {/* Streak Sentence (Simplified) */}
      {streak > 0 && (
          <div className={`flex items-center justify-center gap-1.5 text-xs font-bold mb-8 z-10 animate-slide-up bg-white/10 backdrop-blur-md px-4 py-1.5 rounded-full border border-white/10 ${isDark ? 'text-orange-300' : 'text-orange-600 bg-orange-50/50 border-orange-100'}`}>
               <Flame size={12} className={isDark ? 'text-orange-400' : 'text-orange-500'} fill="currentColor" />
               {t('streakSentence', { n: streak })}
          </div>
      )}

      {/* Modern Round Digital Clock */}
      <div className="relative w-64 h-64 flex items-center justify-center mb-12 z-10">
        {/* Background Circle / Card */}
        <div className={`absolute inset-0 rounded-full shadow-[0_20px_50px_-12px_rgba(0,0,0,0.1)] border ${isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-white/50'}`}></div>
        
        {/* Animated Seconds Ring */}
        <div className="absolute inset-0 rounded-full -rotate-90">
             <svg className="w-full h-full p-2" viewBox="0 0 250 250">
                 <circle 
                    cx="125" cy="125" r="120" 
                    fill="none" 
                    stroke={isDark ? '#374151' : '#F3F4F6'} 
                    strokeWidth="4" 
                 />
                 <circle 
                    cx="125" cy="125" r="120" 
                    fill="none" 
                    stroke="url(#gradient)" 
                    strokeWidth="4"
                    strokeLinecap="round"
                    style={{ strokeDasharray: circumference, strokeDashoffset, transition: 'stroke-dashoffset 0.5s linear' }}
                 />
                 <defs>
                   <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                     <stop offset="0%" stopColor="#6366F1" />
                     <stop offset="100%" stopColor="#A855F7" />
                   </linearGradient>
                 </defs>
             </svg>
        </div>

        {/* Content Container */}
        <div className="flex flex-col items-center justify-center relative z-10">
            {/* Time */}
            <div className={`text-6xl font-bold text-transparent bg-clip-text tracking-tighter font-[Inter] ${isDark ? 'bg-gradient-to-br from-white to-gray-400' : 'bg-gradient-to-br from-gray-800 to-gray-600'}`}>
                {time.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })}
            </div>
            
            {/* Countdown Subtext - Dynamic */}
            {countdownText ? (
                <div className={`mt-2 flex items-center gap-1.5 px-3 py-1 rounded-full animate-slide-up ${isDark ? 'bg-indigo-900/30' : 'bg-indigo-50'}`}>
                    <Activity size={12} className="text-primary-brand" />
                    <span className="text-xs font-semibold text-primary-brand">
                        {countdownText}
                    </span>
                </div>
            ) : (
                <div className="mt-2 flex items-center gap-1.5 px-3 py-1 h-6"></div>
            )}
        </div>
      </div>

      {/* Alarm List */}
      <div className="w-full max-w-md space-y-2 mb-24 z-10">
        <h2 className={`text-lg font-bold px-1 opacity-80 mb-2 ${isDark ? 'text-gray-300' : 'text-text-main'}`}>{t('myAlarms')}</h2>
        {alarms.map(alarm => (
          <AlarmItem 
            key={alarm.id} 
            alarm={alarm} 
            onToggle={onToggleAlarm} 
            onDelete={onDeleteAlarm} 
            t={t}
            isDark={isDark}
          />
        ))}
        {alarms.length === 0 && (
          <div className="text-center py-10 text-gray-400">
            {t('noAlarms')}
          </div>
        )}
      </div>

      {/* FAB */}
      <button 
        onClick={() => setShowAddModal(true)}
        className="fixed bottom-8 right-1/2 translate-x-1/2 sm:right-8 sm:translate-x-0 w-16 h-16 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-600 shadow-glow flex items-center justify-center text-white hover:scale-110 active:scale-95 transition-all z-20"
      >
        <Plus size={32} strokeWidth={2.5} />
      </button>

      {/* Add Alarm Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/40 backdrop-blur-sm p-4 sm:p-0">
          <div className={`w-full max-w-md rounded-3xl p-8 shadow-2xl animate-slide-up max-h-[90vh] overflow-y-auto ${isDark ? 'bg-gray-800 text-white' : 'bg-white text-text-main'}`}>
            <h2 className="text-2xl font-bold mb-6">{t('newAlarm')}</h2>
            
            {/* Time Picker */}
            <div className="mb-6">
              <label className={`block text-sm font-semibold mb-3 ${isDark ? 'text-gray-400' : 'text-text-sub'}`}>{t('timeLabel')}</label>
              <div className="relative">
                <input 
                    type="time" 
                    value={newTime} 
                    onChange={(e) => setNewTime(e.target.value)}
                    className={`w-full p-4 rounded-xl border text-5xl text-center outline-none focus:border-primary-brand focus:ring-2 focus:ring-primary-brand/20 font-mono ${isDark ? 'bg-gray-700 border-gray-600 text-white' : 'bg-gray-50 border-gray-200 text-text-main'}`}
                />
              </div>
            </div>

            {/* Repeat Mode */}
            <div className="mb-6">
              <label className={`block text-sm font-semibold mb-3 ${isDark ? 'text-gray-400' : 'text-text-sub'}`}>{t('repeatLabel')}</label>
              <div className={`grid grid-cols-3 gap-2 p-1 rounded-xl ${isDark ? 'bg-gray-700' : 'bg-gray-50'}`}>
                {[
                    { mode: RepeatMode.ONCE, label: t('repeatOnce') },
                    { mode: RepeatMode.WORKDAYS, label: t('repeatWorkdays') },
                    { mode: RepeatMode.CUSTOM, label: t('repeatCustom') }
                ].map((item) => (
                    <button
                        key={item.mode}
                        onClick={() => setRepeatMode(item.mode)}
                        className={`py-2 rounded-lg text-sm font-medium transition-all ${
                            repeatMode === item.mode 
                            ? (isDark ? 'bg-gray-600 text-white shadow-sm' : 'bg-white text-primary-brand shadow-sm') 
                            : (isDark ? 'text-gray-400 hover:text-gray-200' : 'text-gray-500 hover:text-gray-700')
                        }`}
                    >
                        {item.label}
                    </button>
                ))}
              </div>
            </div>

            {/* Custom Days Selector - Only if Custom */}
            {repeatMode === RepeatMode.CUSTOM && (
                <div className="mb-6 animate-slide-up">
                    <label className={`block text-xs font-semibold mb-3 uppercase tracking-wider ${isDark ? 'text-gray-500' : 'text-text-sub'}`}>{t('selectDaysLabel')}</label>
                    <div className="flex justify-between mb-6">
                        {DAYS_LIST.map(day => {
                            const isSelected = customDays.includes(day.val);
                            return (
                                <button
                                    key={day.val}
                                    onClick={() => toggleDay(day.val)}
                                    className={`
                                        w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold transition-all
                                        ${isSelected 
                                            ? 'bg-primary-brand text-white shadow-glow transform scale-105' 
                                            : (isDark ? 'bg-gray-700 border border-gray-600 text-gray-400' : 'bg-white border border-gray-200 text-gray-400 hover:border-gray-300')
                                        }
                                    `}
                                >
                                    {day.label}
                                </button>
                            );
                        })}
                    </div>

                    {/* Holiday Toggle */}
                    <div className={`flex items-center justify-between p-4 rounded-xl ${isDark ? 'bg-gray-700' : 'bg-gray-50'}`}>
                        <div className="flex items-center gap-3">
                            <div className={`w-8 h-8 rounded-full flex items-center justify-center ${isDark ? 'bg-orange-900/50 text-orange-400' : 'bg-orange-100 text-orange-500'}`}>
                                <Calendar size={16} />
                            </div>
                            <div>
                                <div className={`font-semibold text-sm ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>{t('skipHolidaysLabel')}</div>
                                <div className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-400'}`}>{t('skipHolidaysDesc')}</div>
                            </div>
                        </div>
                        <button 
                            onClick={() => setSkipHolidays(!skipHolidays)}
                            className={`w-12 h-7 rounded-full relative transition-all duration-300 ${skipHolidays ? 'bg-primary-brand' : (isDark ? 'bg-gray-600' : 'bg-gray-200')}`}
                        >
                            <div className={`absolute top-1 w-5 h-5 rounded-full bg-white shadow-sm transition-all duration-300 ${skipHolidays ? 'left-6' : 'left-1'}`}></div>
                        </button>
                    </div>
                </div>
            )}

            {/* Actions */}
            <div className="flex gap-4 mt-8">
              <NeumorphicButton onClick={() => setShowAddModal(false)} variant="ghost" className="flex-1">{t('cancel')}</NeumorphicButton>
              <NeumorphicButton onClick={handleSaveAlarm} variant="primary" className="flex-1">{t('saveAlarm')}</NeumorphicButton>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};