import React, { useEffect, useState } from 'react';
import { Alarm, VolumeLevel, MissionType } from '../types';
import { playAlarmSound, stopAlarmSound } from '../services/soundService';
import { MathMission } from './MathMission';
import { AlertTriangle, Lock, Bell } from 'lucide-react';
import { useLanguage } from '../i18n';

interface Props {
  alarm: Alarm;
  onSolved: () => void;
}

export const AlarmLockdown: React.FC<Props> = ({ alarm, onSolved }) => {
  const [volumeStage, setVolumeStage] = useState<VolumeLevel>(VolumeLevel.NORMAL);
  const [showMission, setShowMission] = useState(false);
  const { t } = useLanguage();

  // Escalation Logic
  useEffect(() => {
    // Stage 1: Normal
    playAlarmSound(VolumeLevel.NORMAL);
    
    // Stage 2: Loud after 5 seconds
    const timer1 = setTimeout(() => {
      setVolumeStage(VolumeLevel.LOUD);
      playAlarmSound(VolumeLevel.LOUD);
    }, 5000);

    // Stage 3: Super Loud + Flash after 10 seconds
    const timer2 = setTimeout(() => {
      setVolumeStage(VolumeLevel.SUPER_LOUD);
      playAlarmSound(VolumeLevel.SUPER_LOUD);
    }, 10000);

    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
      stopAlarmSound();
    };
  }, []);

  const handleStartMission = () => {
    setShowMission(true);
  };

  const isCritical = volumeStage === VolumeLevel.SUPER_LOUD;

  if (showMission) {
    return (
      <div className="fixed inset-0 z-50 bg-gray-900 text-white flex flex-col items-center justify-center p-6">
        {alarm.missionType === MissionType.MATH ? (
            <MathMission difficulty={alarm.difficulty} onComplete={onSolved} />
        ) : (
             // Simple fallback for Shake 
            <div className="text-center animate-pulse">
                <h2 className="text-3xl font-bold mb-4 text-orange-500">{t('crazyClick')}</h2>
                <p className="mb-12 text-gray-300">{t('clickInstruction')}</p>
                <button 
                    onClick={(e) => {
                        const btn = e.currentTarget;
                        const count = parseInt(btn.dataset.count || '0') + 1;
                        btn.dataset.count = count.toString();
                        btn.innerText = t('clicksLeft', { n: 20 - count });
                        // Visual feedback
                        btn.style.transform = `scale(${0.9 + (count % 2) * 0.2})`;
                        if(count >= 20) onSolved();
                    }}
                    data-count="0"
                    className="w-56 h-56 rounded-full bg-gradient-to-br from-red-500 to-orange-600 text-3xl font-black shadow-[0_0_60px_rgba(239,68,68,0.5)] active:scale-90 transition-all border-4 border-white/20"
                >
                    {t('clickMe')}
                </button>
            </div>
        )}
      </div>
    );
  }

  // Determine text based on volume stage
  let stageText = t('wakeUp');
  let subText = t('earlyBird');

  if (volumeStage === VolumeLevel.LOUD) {
    stageText = t('getUpNow');
    subText = t('lateWarning');
  } else if (volumeStage === VolumeLevel.SUPER_LOUD) {
    stageText = t('emergency');
    subText = t('noiseBombing');
  }

  return (
    <div className={`fixed inset-0 z-50 flex flex-col items-center justify-center overflow-hidden transition-colors duration-500 ${isCritical ? 'animate-flash-red' : 'bg-gray-900'}`}>
      
      {/* Background gradients */}
      {!isCritical && (
          <div className="absolute inset-0 bg-gradient-to-b from-gray-800 to-black pointer-events-none"></div>
      )}
      
      {/* Red Vignette Overlay */}
      <div className="absolute inset-0 pointer-events-none bg-[radial-gradient(circle,transparent_0%,rgba(220,38,38,0.4)_100%)]"></div>

      <div className="z-10 text-center space-y-10 animate-pulse-fast relative w-full max-w-md px-6">
        <div className={`inline-flex items-center justify-center w-24 h-24 rounded-full border-4 ${isCritical ? 'border-red-500 bg-red-900/50' : 'border-indigo-500 bg-indigo-900/50'} shadow-2xl mx-auto`}>
            {isCritical ? <AlertTriangle size={48} className="text-red-500" /> : <Bell size={48} className="text-indigo-400" />}
        </div>
        
        <div>
          <h1 className="text-7xl font-black text-white tracking-tighter mb-4 font-mono">
            {alarm.time}
          </h1>
          <h2 className={`text-3xl font-bold tracking-wider ${isCritical ? 'text-red-500' : 'text-indigo-300'}`}>
            {stageText}
          </h2>
          <p className="text-gray-400 mt-2 font-medium">{subText}</p>
        </div>

        <div className="w-full pt-8">
             {/* The "Trust Funnel" - No close button, only start mission */}
             <button
                onClick={handleStartMission}
                className="w-full py-5 bg-white text-gray-900 font-bold text-xl rounded-2xl hover:bg-gray-100 transition-colors shadow-[0_10px_40px_rgba(0,0,0,0.5)] active:scale-95"
             >
                {t('startMission')}
             </button>
             <p className="text-gray-500 text-xs mt-4">{t('completeMission')}</p>
        </div>
      </div>
      
      <div className="absolute bottom-10 flex items-center gap-2 text-gray-600 text-xs">
        <Lock size={12} /> 
        <span>{t('systemLocked')}</span>
      </div>
    </div>
  );
};
