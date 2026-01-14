import React, { useState, useEffect } from 'react';
import { Difficulty } from '../types';
import { useLanguage } from '../i18n';
import { ListOrdered, RefreshCw } from 'lucide-react';

interface Props {
  difficulty: Difficulty;
  onComplete: () => void;
}

export const OrderMission: React.FC<Props> = ({ difficulty, onComplete }) => {
  const { t } = useLanguage();
  
  // Difficulty Config
  // Easy: 1-9 (3x3 grid)
  // Medium: 1-12 (4x3 grid)
  // Hard: 1-16 (4x4 grid)
  
  const getGridConfig = () => {
      if (difficulty === Difficulty.EASY) return { count: 9, cols: 3 };
      if (difficulty === Difficulty.MEDIUM) return { count: 12, cols: 3 }; // 4 rows
      return { count: 16, cols: 4 };
  };

  const config = getGridConfig();
  
  const [numbers, setNumbers] = useState<number[]>([]);
  const [nextNumber, setNextNumber] = useState(1);
  const [errorShake, setErrorShake] = useState(false);

  const generateNumbers = () => {
    const nums = Array.from({ length: config.count }, (_, i) => i + 1);
    // Shuffle
    for (let i = nums.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [nums[i], nums[j]] = [nums[j], nums[i]];
    }
    setNumbers(nums);
    setNextNumber(1);
    setErrorShake(false);
  };

  useEffect(() => {
    generateNumbers();
  }, [difficulty]);

  const handleNumberClick = (num: number) => {
    if (num === nextNumber) {
        const next = nextNumber + 1;
        setNextNumber(next);
        if (next > config.count) {
            onComplete();
        }
    } else {
        // Error
        if (navigator.vibrate) navigator.vibrate(200);
        setErrorShake(true);
        setTimeout(() => setErrorShake(false), 500);
        
        // Optional: Reset progress? Or just shake?
        // Let's reset progress to make it annoying (it's an alarm)
        setNextNumber(1); 
    }
  };

  return (
    <div className="w-full max-w-sm flex flex-col items-center">
      <div className="flex justify-between items-center w-full mb-6 px-2">
        <span className="text-sm font-medium text-pink-400 flex items-center gap-2">
          <ListOrdered size={16}/> {t('mission_ORDER')}
        </span>
        <span className="text-xl font-bold text-white">
             {nextNumber - 1} / {config.count}
        </span>
      </div>

      <div className="text-center mb-6">
        <p className={`text-white text-lg font-bold ${errorShake ? 'text-red-500 animate-bounce' : ''}`}>
            {errorShake ? t('orderReset') : t('orderInstruction', { n: config.count })}
        </p>
      </div>

      <div 
        className={`grid gap-3 w-full transition-all duration-200 ${errorShake ? 'animate-shake' : ''}`}
        style={{ gridTemplateColumns: `repeat(${config.cols}, minmax(0, 1fr))` }}
      >
        {numbers.map((num) => {
            const isClicked = num < nextNumber;
            return (
                <button
                    key={num}
                    onClick={() => handleNumberClick(num)}
                    disabled={isClicked}
                    className={`
                        aspect-square rounded-xl text-2xl font-bold shadow-lg transition-all duration-150
                        ${isClicked 
                            ? 'bg-transparent border-2 border-green-500/30 text-green-500/30 scale-90' 
                            : 'bg-gray-800 text-white hover:bg-gray-700 active:scale-95 active:bg-pink-600 border-b-4 border-gray-950 active:border-b-0 active:translate-y-1'
                        }
                    `}
                >
                    {num}
                </button>
            );
        })}
      </div>
      
      <style>{`
          @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-10px); }
            75% { transform: translateX(10px); }
          }
          .animate-shake {
            animation: shake 0.4s cubic-bezier(.36,.07,.19,.97) both;
          }
      `}</style>

      <button 
        onClick={generateNumbers}
        className="mt-8 flex items-center gap-1 text-gray-500 text-xs hover:text-white uppercase tracking-wider"
      >
        <RefreshCw size={12} /> {t('changeQuestion')}
      </button>
    </div>
  );
};