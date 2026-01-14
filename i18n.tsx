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
    labelLabel: "标签",
    repeatLabel: "重复",
    repeatOnce: "响一次",
    repeatWorkdays: "工作日",
    repeatCustom: "自定义",
    selectDaysLabel: "选择日期",
    skipHolidaysLabel: "法定节假日不响铃",
    skipHolidaysDesc: "智能跳过国家法定假期",
    cancel: "取消",
    saveAlarm: "保存闹钟",
    delete: "删除",
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
    changeQuestion: "换一题",
    mathProblem: "数学题",
    mathEasy: "简单",
    mathMedium: "普通",
    mathHard: "困难",
    streakTitle: "已坚持起床",
    streakDays: "{n} 天",
    streakSentence: "已坚持准时起床 {n} 天，坚持下去！",
    motivation: "保持这个节奏，你是最棒的！",
    motivation2: "新的一天，新的开始！",
    calendar: "打卡日历",
    settings: "设置",
    language: "语言",
    back: "返回",
    themeMode: "显示模式",
    themeAuto: "自动",
    themeLight: "日间",
    themeDark: "夜间",
    dayMode: "日间模式",
    nightMode: "夜间模式",
    autoSwitch: "随时间自动切换",
    resetData: "重置数据",
    resetConfirm: "确定要清空所有历史数据吗？",
    about: "关于",
    version: "版本 1.2.0",
    // Categories
    label_work: "上班",
    label_date: "约会",
    label_flight: "赶飞机",
    label_train: "赶火车",
    label_meeting: "会议",
    label_doctor: "看病",
    label_interview: "面试",
    label_exam: "考试",
    label_other: "其他",
    // Category Specific Ringing Messages
    alarm_msg_work: "起床搬砖啦！迟到要扣钱的！",
    alarm_msg_date: "约会别迟到，印象分很重要！",
    alarm_msg_flight: "赶飞机啦！误机就麻烦了！",
    alarm_msg_train: "火车不等人，快点起床！",
    alarm_msg_meeting: "会议要开始了，精神点！",
    alarm_msg_doctor: "预约了医生，健康第一！",
    alarm_msg_interview: "面试加油！穿得精神点！",
    alarm_msg_exam: "考试顺利！起来复习了！",
    alarm_msg_other: "新的一天，该起床了！",
    // New Missions
    missionLabel: "解除任务",
    mission_MATH: "数学解题",
    mission_SHAKE: "疯狂点击",
    mission_MEMORY: "记忆方块",
    mission_ORDER: "数字顺序",
    mission_TYPING: "文字拼写",
    memoryInstruction: "记住亮起的方块",
    memoryRecall: "请点击刚才发光的方块",
    memoryReady: "我记住了",
    orderInstruction: "请按顺序点击数字 (1-{n})",
    orderReset: "顺序错误，重置!",
    typingInstruction: "请输入上方显示的文字",
    typingPlaceholder: "在此输入...",
    typingError: "输入错误，请重新检查！",
  },
  en: {
    appName: "Wakeup Clock",
    slogan: "Cure for difficulty waking up",
    myAlarms: "My Alarms",
    noAlarms: "No alarms yet, add one!",
    newAlarm: "New Alarm",
    timeLabel: "Time",
    labelLabel: "Label",
    repeatLabel: "Repeat",
    repeatOnce: "Once",
    repeatWorkdays: "Workdays",
    repeatCustom: "Custom",
    selectDaysLabel: "Select Days",
    skipHolidaysLabel: "Skip Public Holidays",
    skipHolidaysDesc: "Auto skip national holidays",
    cancel: "Cancel",
    saveAlarm: "Save Alarm",
    delete: "Delete",
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
    changeQuestion: "Skip",
    mathProblem: "Math Problem",
    mathEasy: "Easy",
    mathMedium: "Medium",
    mathHard: "Hard",
    streakTitle: "Waking Streak",
    streakDays: "{n} Days",
    streakSentence: "Consistent on-time wake-up for {n} days. Keep going!",
    motivation: "Keep up the rhythm, you are the best!",
    motivation2: "New day, new beginning!",
    calendar: "Calendar",
    settings: "Settings",
    language: "Language",
    back: "Back",
    themeMode: "Display Mode",
    themeAuto: "Auto",
    themeLight: "Light",
    themeDark: "Dark",
    dayMode: "Day Mode",
    nightMode: "Night Mode",
    autoSwitch: "Auto-switch by time",
    resetData: "Reset Data",
    resetConfirm: "Are you sure to clear all history?",
    about: "About",
    version: "Version 1.2.0",
    // Categories
    label_work: "Work",
    label_date: "Date",
    label_flight: "Flight",
    label_train: "Train",
    label_meeting: "Meeting",
    label_doctor: "Doctor",
    label_interview: "Interview",
    label_exam: "Exam",
    label_other: "Other",
    // Category Specific Ringing Messages
    alarm_msg_work: "Time to make money! Don't be late!",
    alarm_msg_date: "Don't be late for your date!",
    alarm_msg_flight: "Don't miss your flight! Fly away!",
    alarm_msg_train: "The train is leaving! Hurry!",
    alarm_msg_meeting: "Meeting starts soon! Wake up!",
    alarm_msg_doctor: "Doctor's appointment today!",
    alarm_msg_interview: "Good luck on the interview! Get up!",
    alarm_msg_exam: "Ace that exam! Time to study!",
    alarm_msg_other: "Time to wake up! Fresh start!",
    // New Missions
    missionLabel: "Wake-up Mission",
    mission_MATH: "Math Solver",
    mission_SHAKE: "Crazy Click",
    mission_MEMORY: "Memory Matrix",
    mission_ORDER: "Order Tap",
    mission_TYPING: "Typing Drill",
    memoryInstruction: "Memorize the pattern",
    memoryRecall: "Tap the tiles that glowed",
    memoryReady: "I'm Ready",
    orderInstruction: "Tap numbers in order (1-{n})",
    orderReset: "Wrong order, reset!",
    typingInstruction: "Type the text shown above",
    typingPlaceholder: "Type here...",
    typingError: "Incorrect, please check again!",
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