import { Difficulty } from "./types";

export const APP_NAME = "醒了么";

// Updated constants matching new theme implicitly via Tailwind classes, 
// but keeping logic config here.
export const MISSION_CONFIG = {
  [Difficulty.EASY]: {
    questions: 1,
    ops: 'ADD',
    range: 20
  },
  [Difficulty.MEDIUM]: {
    questions: 3,
    ops: 'MULT_ADD',
    range: 15
  },
  [Difficulty.HARD]: {
    questions: 5,
    ops: 'COMPLEX',
    range: 50
  }
};