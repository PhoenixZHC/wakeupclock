import React, { useState, useEffect } from 'react';
import { Difficulty } from '../types';
import { useLanguage } from '../i18n';
import { Keyboard, RefreshCw } from 'lucide-react';

interface Props {
  difficulty: Difficulty;
  onComplete: () => void;
}

const PHRASES = {
  zh: [
    "早起的鸟儿有虫吃",
    "一日之计在于晨",
    "只有坚持才能胜利",
    "今天也是充满希望的一天",
    "努力不一定成功但放弃一定失败",
    "自律给我自由",
    "追逐梦想永不放弃"
  ],
  en: [
    "The early bird catches the worm",
    "Wake up and chase your dreams",
    "Success is walking from failure to failure",
    "Make today your masterpiece",
    "Discipline is freedom",
    "Stay hungry stay foolish",
    "Action speaks louder than words"
  ]
};

const RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

export const TypingMission: React.FC<Props> = ({ difficulty, onComplete }) => {
  const { t, language } = useLanguage();
  const [targetText, setTargetText] = useState('');
  const [inputText, setInputText] = useState('');
  const [error, setError] = useState(false);

  const generateText = () => {
    if (difficulty === Difficulty.HARD) {
        // Generate random string for Hard mode
        let result = '';
        for (let i = 0; i < 8; i++) {
           result += RANDOM_CHARS.charAt(Math.floor(Math.random() * RANDOM_CHARS.length));
        }
        setTargetText(result);
    } else {
        const dict = language === 'zh' ? PHRASES.zh : PHRASES.en;
        const rand = dict[Math.floor(Math.random() * dict.length)];
        setTargetText(rand);
    }
    setInputText('');
    setError(false);
  };

  useEffect(() => {
    generateText();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputText(e.target.value);
    setError(false);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (inputText.trim() === targetText) {
      onComplete();
    } else {
      setError(true);
      if (navigator.vibrate) navigator.vibrate(200);
    }
  };

  return (
    <div className="w-full max-w-sm flex flex-col items-center">
      <div className="flex items-center gap-2 text-pink-400 font-medium mb-8">
        <Keyboard size={20} />
        <span>{t('mission_TYPING')}</span>
      </div>

      <div className="w-full bg-gray-800/80 backdrop-blur rounded-2xl p-6 mb-6 shadow-xl border border-gray-700">
        <div className="text-center">
            <p className="text-gray-400 text-sm mb-2">{t('typingInstruction')}</p>
            <h3 className="text-2xl font-bold text-white tracking-wide select-none mb-4">{targetText}</h3>
        </div>

        <form onSubmit={handleSubmit}>
            <input 
                type="text" 
                value={inputText}
                onChange={handleChange}
                placeholder={t('typingPlaceholder')}
                className={`w-full bg-gray-900/50 text-white text-center text-lg p-3 rounded-xl border-2 outline-none transition-all ${error ? 'border-red-500 animate-pulse' : 'border-gray-600 focus:border-pink-500'}`}
                autoFocus
            />
            {error && <p className="text-red-500 text-xs text-center mt-2 font-bold">{t('typingError')}</p>}
            
            <button 
                type="submit"
                className="w-full mt-4 py-3 bg-pink-600 hover:bg-pink-500 text-white rounded-xl font-bold shadow-lg active:scale-95 transition-all"
            >
                {t('confirm')}
            </button>
        </form>
      </div>

      <button 
        onClick={generateText}
        className="flex items-center gap-1 text-gray-400 text-sm hover:text-white"
      >
        <RefreshCw size={14} /> {t('changeQuestion')}
      </button>
    </div>
  );
};