import React, { createContext, useState, useContext, ReactNode, useEffect } from 'react';

export type Language = 'zh' | 'en';

export const translations = {
  zh: {
    appName: "醒了么",
    slogan: "专治起床困难户",
    myAlarms: "我的闹钟",
    noAlarms: "暂无闹钟，快去添加一个吧！",
    newAlarm: "新建闹钟",
    timeLabel: "时间",
    repeatLabel: "重复",
    repeatOnce: "响一次",
    repeatWorkdays: "工作日",
    repeatCustom: "自定义",
    selectDaysLabel: "选择日期",
    skipHolidaysLabel: "法定节假日不响铃",
    skipHolidaysDesc: "智能跳过国家法定假期",
    cancel: "取消",
    saveAlarm: "保存闹钟",
    day_1: "一", day_2: "二", day_3: "三", day_4: "四", day_5: "五", day_6: "六", day_0: "日",
    everyday: "每天",
    notSet: "未设置",
    skipHolidaysTag: "节假日不响",
    remainingDays: "还剩 {d}天 {h}小时",
    remainingHours: "还剩 {h}小时 {m}分",
    remainingMinutes: "还剩 {m}分",
    wakeUp: "该起床了",
    earlyBird: "早起身体好",
    getUpNow: "立刻起床！",
    lateWarning: "再不起床就迟到了！",
    emergency: "紧急警报",
    noiseBombing: "噪音轰炸倒计时！",
    startMission: "开始解除任务",
    completeMission: "完成任务以关闭闹钟",
    systemLocked: "醒了么 WakeGuard 系统锁定中",
    mathMission: "任务: 数学解题",
    crazyClick: "疯狂点击!",
    clickInstruction: "连续点击按钮 20 次即可关闭闹钟",
    clickMe: "点我!",
    clicksLeft: "还剩 {n} 次",
    clear: "清除",
    confirm: "确认",
    wrongAnswer: "答案错误，请重试!",
    mathProblem: "数学题",
    mathEasy: "简单",
    mathMedium: "普通",
    mathHard: "困难",
    userCenter: "个人中心",
    login: "登录账户",
    loginDesc: "同步您的数据",
    guest: "访客用户",
    streakTitle: "已坚持起床",
    streakDays: "{n} 天",
    streakSentence: "已坚持准时起床 {n} 天",
    motivation: "保持这个节奏，你是最棒的！",
    motivation2: "新的一天，新的开始！",
    calendar: "打卡日历",
    settings: "设置",
    language: "语言",
    back: "返回",
    loginBtn: "登录",
    logoutBtn: "退出登录",
    username: "用户名",
    enterUsername: "请输入用户名",
    themeMode: "显示模式",
    themeAuto: "自动",
    themeLight: "日间",
    themeDark: "夜间",
    dayMode: "日间模式",
    nightMode: "夜间模式",
    autoSwitch: "随时间自动切换"
  },
  en: {
    appName: "Wakeup Clock",
    slogan: "Cure for difficulty waking up",
    myAlarms: "My Alarms",
    noAlarms: "No alarms yet, add one!",
    newAlarm: "New Alarm",
    timeLabel: "Time",
    repeatLabel: "Repeat",
    repeatOnce: "Once",
    repeatWorkdays: "Workdays",
    repeatCustom: "Custom",
    selectDaysLabel: "Select Days",
    skipHolidaysLabel: "Skip Public Holidays",
    skipHolidaysDesc: "Auto skip national holidays",
    cancel: "Cancel",
    saveAlarm: "Save Alarm",
    day_1: "M", day_2: "T", day_3: "W", day_4: "T", day_5: "F", day_6: "S", day_0: "S",
    everyday: "Everyday",
    notSet: "Not Set",
    skipHolidaysTag: "Skip Holidays",
    remainingDays: "{d}d {h}h remaining",
    remainingHours: "{h}h {m}m remaining",
    remainingMinutes: "{m}m remaining",
    wakeUp: "Time to wake up",
    earlyBird: "Early bird gets the worm",
    getUpNow: "Get up NOW!",
    lateWarning: "You're gonna be late!",
    emergency: "EMERGENCY ALERT",
    noiseBombing: "Noise bombing countdown!",
    startMission: "Start Mission",
    completeMission: "Complete mission to stop alarm",
    systemLocked: "Wakeup Clock WakeGuard Locked",
    mathMission: "Mission: Math Solver",
    crazyClick: "Crazy Click!",
    clickInstruction: "Click 20 times to stop",
    clickMe: "Click Me!",
    clicksLeft: "{n} left",
    clear: "Clear",
    confirm: "Confirm",
    wrongAnswer: "Wrong answer, try again!",
    mathProblem: "Math Problem",
    mathEasy: "Easy",
    mathMedium: "Medium",
    mathHard: "Hard",
    userCenter: "User Center",
    login: "Login",
    loginDesc: "Sync your data",
    guest: "Guest User",
    streakTitle: "Waking Streak",
    streakDays: "{n} Days",
    streakSentence: "Consistent on-time wake-up for {n} days",
    motivation: "Keep up the rhythm, you are the best!",
    motivation2: "New day, new beginning!",
    calendar: "Calendar",
    settings: "Settings",
    language: "Language",
    back: "Back",
    loginBtn: "Login",
    logoutBtn: "Logout",
    username: "Username",
    enterUsername: "Enter username",
    themeMode: "Display Mode",
    themeAuto: "Auto",
    themeLight: "Light",
    themeDark: "Dark",
    dayMode: "Day Mode",
    nightMode: "Night Mode",
    autoSwitch: "Auto-switch by time"
  }
};

interface LanguageContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: keyof typeof translations['zh'], params?: any) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const LanguageProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [language, setLanguage] = useState<Language>('zh');

  const t = (key: keyof typeof translations['zh'], params?: any) => {
    let text = translations[language][key] || translations['zh'][key] || key;
    if (params) {
      Object.keys(params).forEach(param => {
        text = text.replace(`{${param}}`, params[param]);
      });
    }
    return text;
  };

  useEffect(() => {
    document.title = t('appName');
  }, [language]);

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
};