
export enum MissionType {
  MATH = 'MATH',
  SHAKE = 'SHAKE' // Simulated by rapid clicking on web
}

export enum Difficulty {
  EASY = 1,
  MEDIUM = 2,
  HARD = 3
}

export enum RepeatMode {
  ONCE = 'ONCE',
  WORKDAYS = 'WORKDAYS',
  CUSTOM = 'CUSTOM'
}

export interface Alarm {
  id: string;
  time: string; // Format "HH:mm"
  enabled: boolean;
  label: string;
  missionType: MissionType;
  difficulty: Difficulty;
  repeatMode: RepeatMode;
  customDays: number[]; // 0=Sun, 1=Mon, etc.
  skipHolidays: boolean;
}

export enum AppState {
  DASHBOARD = 'DASHBOARD',
  RINGING = 'RINGING',
  MISSION = 'MISSION',
  PROFILE = 'PROFILE'
}

export enum VolumeLevel {
  NORMAL = 'NORMAL', // x1
  LOUD = 'LOUD', // x2
  SUPER_LOUD = 'SUPER_LOUD' // x3
}

export interface User {
  name: string;
  isLoggedIn: boolean;
  avatarUrl?: string;
}

export interface WakeUpRecord {
  date: string; // YYYY-MM-DD
  time: string;
}
