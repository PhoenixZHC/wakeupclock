import React, { useEffect, useState, useRef } from 'react';
import { Alarm, VolumeLevel, MissionType } from '../types';
import { playAlarmSound, stopAlarmSound } from '../services/soundService';
import { MathMission } from './MathMission';
import { MemoryMission } from './MemoryMission';
import { OrderMission } from './OrderMission';
import { AlertTriangle, Lock, Bell, Briefcase, UserCheck, GraduationCap, Plane, Train, Users, Stethoscope, Heart, Tag } from 'lucide-react';
import { useLanguage } from '../i18n';

interface Props {
  alarm: Alarm;
  onSolved: () => void;
}

const CATEGORY_ICONS: Record<string, any> = {
    work: Briefcase,
    interview: UserCheck,
    exam: GraduationCap,
    flight: Plane,
    train: Train,
    meeting: Users,
    doctor: Stethoscope,
    date: Heart,
    other: Tag
};

// Video Assets Mapping
const CATEGORY_VIDEOS: Record<string, string> = {
    work: "https://videos.pexels.com/video-files/1183056/1183056-hd_1920_1080_25fps.mp4", 
    interview: "https://videos.pexels.com/video-files/4098366/4098366-hd_1920_1080_25fps.mp4",
    exam: "https://videos.pexels.com/video-files/6282035/6282035-hd_1920_1080_25fps.mp4",
    flight: "https://videos.pexels.com/video-files/2026883/2026883-hd_1920_1080_30fps.mp4",
    train: "https://videos.pexels.com/video-files/3028308/3028308-hd_1920_1080_24fps.mp4",
    meeting: "https://videos.pexels.com/video-files/3252011/3252011-hd_1920_1080_25fps.mp4",
    doctor: "https://videos.pexels.com/video-files/3844998/3844998-hd_1920_1080_25fps.mp4",
    date: "https://videos.pexels.com/video-files/5635368/5635368-hd_1080_1920_25fps.mp4",
    other: "https://videos.pexels.com/video-files/3121459/3121459-hd_1920_1080_25fps.mp4" 
};

export const AlarmLockdown: React.FC<Props> = ({ alarm, onSolved }) => {
  const [volumeStage, setVolumeStage] = useState<VolumeLevel>(VolumeLevel.NORMAL);
  const [showMission, setShowMission] = useState(false);
  // Random mission state
  const [activeMission, setActiveMission] = useState<MissionType>(MissionType.MATH);
  
  const { t } = useLanguage();
  const videoRef = useRef<HTMLVideoElement>(null);

  // Initialize Random Mission
  useEffect(() => {
    const missions = [MissionType.MATH, MissionType.MEMORY, MissionType.ORDER, MissionType.SHAKE];
    const randomMission = missions[Math.floor(Math.random() * missions.length)];
    setActiveMission(randomMission);
  }, []);

  // Cycle Sound Logic
  useEffect(() => {
    let stageIndex = 0;
    const stages = [VolumeLevel.NORMAL, VolumeLevel.LOUD, VolumeLevel.SUPER_LOUD];
    
    const cycleSound = () => {
        const currentStage = stages[stageIndex];
        setVolumeStage(currentStage);
        playAlarmSound(currentStage);
        stageIndex = (stageIndex + 1) % stages.length;
    };

    cycleSound();
    const interval = setInterval(cycleSound, 15000);

    return () => {
      clearInterval(interval);
      stopAlarmSound();
    };
  }, []);

  // Ensure video plays
  useEffect(() => {
    if (videoRef.current) {
        videoRef.current.playbackRate = 0.8;
        videoRef.current.play().catch(e => console.log("Auto-play prevented:", e));
    }
  }, [alarm.label]);

  const handleStartMission = () => {
    setShowMission(true);
  };

  const isCritical = volumeStage === VolumeLevel.SUPER_LOUD;
  const videoSrc = CATEGORY_VIDEOS[alarm.label] || CATEGORY_VIDEOS['other'];

  const renderMission = () => {
    switch (activeMission) {
      case MissionType.MATH:
        return <MathMission difficulty={alarm.difficulty} onComplete={onSolved} />;
      case MissionType.MEMORY:
        return <MemoryMission difficulty={alarm.difficulty} onComplete={onSolved} />;
      case MissionType.ORDER:
        return <OrderMission difficulty={alarm.difficulty} onComplete={onSolved} />;
      case MissionType.SHAKE:
      default:
        return (
          <div className="text-center animate-pulse">
              <h2 className="text-3xl font-bold mb-4 text-orange-500">{t('crazyClick')}</h2>
              <p className="mb-12 text-gray-300">{t('clickInstruction')}</p>
              <button 
                  onClick={(e) => {
                      const btn = e.currentTarget;
                      const count = parseInt(btn.dataset.count || '0') + 1;
                      btn.dataset.count = count.toString();
                      btn.innerText = t('clicksLeft', { n: 20 - count });
                      btn.style.transform = `scale(${0.9 + (count % 2) * 0.2})`;
                      if(count >= 20) onSolved();
                  }}
                  data-count="0"
                  className="w-56 h-56 rounded-full bg-gradient-to-br from-red-500 to-orange-600 text-3xl font-black shadow-[0_0_60px_rgba(239,68,68,0.5)] active:scale-90 transition-all border-4 border-white/20"
              >
                  {t('clickMe')}
              </button>
          </div>
        );
    }
  };

  if (showMission) {
    return (
      <div className="fixed inset-0 z-50 bg-gray-900 text-white flex flex-col items-center justify-center p-6">
        {renderMission()}
      </div>
    );
  }

  // Determine text based on volume stage AND category
  let stageText = '';
  let subText = '';
  const specificMsg = t(`alarm_msg_${alarm.label}` as any);
  const LabelIcon = CATEGORY_ICONS[alarm.label] || Bell;

  if (volumeStage === VolumeLevel.NORMAL) {
    stageText = specificMsg;
    subText = t('earlyBird');
  } else if (volumeStage === VolumeLevel.LOUD) {
    stageText = t('getUpNow');
    subText = specificMsg;
  } else if (volumeStage === VolumeLevel.SUPER_LOUD) {
    stageText = t('emergency');
    subText = t('noiseBombing');
  }

  return (
    <div className="fixed inset-0 z-50 flex flex-col items-center justify-center overflow-hidden bg-black">
      
      {/* 1. Background Video Layer */}
      <video
        ref={videoRef}
        key={videoSrc}
        src={videoSrc}
        autoPlay
        loop
        muted
        playsInline
        className="absolute inset-0 w-full h-full object-cover z-0"
      />

      {/* 2. Darken Overlay */}
      <div className="absolute inset-0 bg-black/40 z-10 backdrop-blur-[2px]"></div>

      {/* 3. Critical Alert Red Overlay */}
      {isCritical && (
          <div className="absolute inset-0 bg-red-600 z-10 mix-blend-overlay animate-flash-overlay pointer-events-none"></div>
      )}
      
      {/* 4. Vignette Overlay */}
      <div className="absolute inset-0 pointer-events-none z-10 bg-[radial-gradient(circle,transparent_0%,rgba(0,0,0,0.8)_100%)]"></div>

      {/* Content Layer */}
      <div className="z-20 text-center space-y-10 animate-pulse-fast relative w-full max-w-md px-6">
        <div className={`inline-flex items-center justify-center w-28 h-28 rounded-full border-4 backdrop-blur-md ${isCritical ? 'border-red-500 bg-red-900/40 text-red-500' : 'border-white/50 bg-white/10 text-white'} shadow-2xl mx-auto transition-colors duration-300`}>
            {isCritical ? (
                <AlertTriangle size={56} />
            ) : (
                <LabelIcon size={56} strokeWidth={1.5} />
            )}
        </div>
        
        <div>
          <h1 className="text-8xl font-black text-white tracking-tighter mb-4 font-mono drop-shadow-lg">
            {alarm.time}
          </h1>
          <h2 className={`text-2xl font-bold tracking-wider leading-tight px-4 drop-shadow-md ${isCritical ? 'text-red-400' : 'text-white'}`}>
            {stageText}
          </h2>
          <p className="text-gray-200 mt-3 font-medium text-lg px-4 drop-shadow-md">{subText}</p>
        </div>

        <div className="w-full pt-8">
             <button
                onClick={handleStartMission}
                className="w-full py-5 bg-white/90 backdrop-blur text-gray-900 font-bold text-xl rounded-2xl hover:bg-white transition-colors shadow-[0_10px_40px_rgba(0,0,0,0.5)] active:scale-95"
             >
                {t('startMission')}
             </button>
             <p className="text-white/70 text-xs mt-4 shadow-black drop-shadow-sm">{t('completeMission')}</p>
        </div>
      </div>
      
      <div className="absolute bottom-10 flex items-center gap-2 text-white/50 text-xs z-20">
        <Lock size={12} /> 
        <span>{t('systemLocked')}</span>
      </div>
    </div>
  );
};