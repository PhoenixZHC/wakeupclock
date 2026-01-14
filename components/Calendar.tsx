import React from 'react';
import { ChevronLeft, ChevronRight, Check } from 'lucide-react';
import { WakeUpRecord } from '../types';

interface Props {
  history: WakeUpRecord[];
  isDark: boolean;
}

export const Calendar: React.FC<Props> = ({ history, isDark }) => {
  const today = new Date();
  const [currentDate, setCurrentDate] = React.useState(today);

  const getDaysInMonth = (date: Date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const days = new Date(year, month + 1, 0).getDate();
    const firstDay = new Date(year, month, 1).getDay(); // 0 is Sunday
    // Adjust for Mon-start: 0->6, 1->0, ...
    const offset = firstDay === 0 ? 6 : firstDay - 1; 
    return { days, offset };
  };

  const { days, offset } = getDaysInMonth(currentDate);
  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  const isSuccess = (day: number) => {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    return history.some(h => h.date === dateStr);
  };

  const WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日'];

  return (
    <div className={`p-4 rounded-3xl shadow-sm border ${isDark ? 'bg-gray-800 border-gray-700' : 'bg-white border-white/60'}`}>
      <div className="flex justify-between items-center mb-4">
        <h3 className={`font-bold ${isDark ? 'text-white' : 'text-gray-800'}`}>
          {year}年 {month + 1}月
        </h3>
        {/* Simple prev/next visual only for now as requirement didn't specify full calendar nav */}
        <div className="flex gap-2">
           <div className={`p-1 rounded-full ${isDark ? 'hover:bg-gray-700 text-gray-400' : 'hover:bg-gray-100 text-gray-500'}`}>
             <ChevronLeft size={20} />
           </div>
           <div className={`p-1 rounded-full ${isDark ? 'hover:bg-gray-700 text-gray-400' : 'hover:bg-gray-100 text-gray-500'}`}>
             <ChevronRight size={20} />
           </div>
        </div>
      </div>
      
      <div className="grid grid-cols-7 gap-1 mb-2">
        {WEEKDAYS.map(d => (
          <div key={d} className={`text-center text-xs font-medium ${isDark ? 'text-gray-500' : 'text-gray-400'}`}>
            {d}
          </div>
        ))}
      </div>

      <div className="grid grid-cols-7 gap-1">
        {Array.from({ length: offset }).map((_, i) => (
          <div key={`empty-${i}`} />
        ))}
        {Array.from({ length: days }).map((_, i) => {
          const day = i + 1;
          const success = isSuccess(day);
          const isToday = day === today.getDate() && month === today.getMonth() && year === today.getFullYear();

          return (
            <div key={day} className="aspect-square flex items-center justify-center relative">
              <div 
                className={`
                  w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium transition-all
                  ${isToday 
                    ? (isDark ? 'border-2 border-indigo-400 text-indigo-400' : 'border-2 border-indigo-500 text-indigo-600') 
                    : (isDark ? 'text-gray-300' : 'text-gray-700')
                  }
                  ${success ? 'bg-gradient-to-br from-indigo-500 to-purple-600 text-white border-none shadow-glow' : ''}
                `}
              >
                {success ? <Check size={14} strokeWidth={3} /> : day}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
