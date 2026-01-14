import { VolumeLevel } from "../types";

let audioContext: AudioContext | null = null;
let oscillator: OscillatorNode | null = null;
let gainNode: GainNode | null = null;
let loopInterval: number | null = null;

const initAudio = () => {
  if (!audioContext) {
    audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
  }
  if (audioContext.state === 'suspended') {
    audioContext.resume();
  }
};

export const playAlarmSound = (level: VolumeLevel) => {
  initAudio();
  if (!audioContext) return;

  // Stop previous sound if any
  stopAlarmSound();

  gainNode = audioContext.createGain();
  oscillator = audioContext.createOscillator();
  
  oscillator.type = 'sawtooth';
  oscillator.frequency.value = 440; // Base A4
  
  // Volume Logic based on level
  let volume = 0.3;
  if (level === VolumeLevel.LOUD) volume = 0.6;
  if (level === VolumeLevel.SUPER_LOUD) volume = 1.0;

  gainNode.gain.setValueAtTime(volume, audioContext.currentTime);
  
  oscillator.connect(gainNode);
  gainNode.connect(audioContext.destination);
  
  oscillator.start();

  // Siren effect (Warble)
  let isHigh = false;
  loopInterval = window.setInterval(() => {
    if (!oscillator || !audioContext) return;
    const now = audioContext.currentTime;
    if (isHigh) {
      oscillator.frequency.exponentialRampToValueAtTime(880, now + 0.1);
    } else {
      oscillator.frequency.exponentialRampToValueAtTime(440, now + 0.1);
    }
    isHigh = !isHigh;
  }, 500);
};

export const stopAlarmSound = () => {
  if (oscillator) {
    try {
      oscillator.stop();
      oscillator.disconnect();
    } catch (e) {
      // Ignore if already stopped
    }
    oscillator = null;
  }
  if (loopInterval) {
    clearInterval(loopInterval);
    loopInterval = null;
  }
};