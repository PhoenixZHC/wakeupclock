import React, { useState, useEffect } from 'react';
import { Difficulty } from '../types';
import { useLanguage } from '../i18n';
import { Grid, Check } from 'lucide-react';

interface Props {
  difficulty: Difficulty;
  onComplete: () => void;
}

export const MemoryMission: React.FC<Props> = ({ difficulty, onComplete }) => {
  const { t } = useLanguage();
  
  // Difficulty Config
  const gridSize = 3; // 3x3 grid
  const tilesToMemorize = difficulty === Difficulty.EASY ? 3 : difficulty === Difficulty.MEDIUM ? 5 : 7;
  
  const [pattern, setPattern] = useState<number[]>([]);
  const [userPattern, setUserPattern] = useState<number[]>([]);
  const [gameState, setGameState] = useState<'SHOWING' | 'WAITING' | 'RECALL'>('WAITING');
  const [round, setRound] = useState(1);
  const totalRounds = 3;

  const generatePattern = () => {
    const newPattern: number[] = [];
    while (newPattern.length < tilesToMemorize) {
      const idx = Math.floor(Math.random() * (gridSize * gridSize));
      if (!newPattern.includes(idx)) {
        newPattern.push(idx);
      }
    }
    setPattern(newPattern);
    setUserPattern([]);
    setGameState('SHOWING');
  };

  useEffect(() => {
    generatePattern();
  }, [round]);

  const handleReady = () => {
      setGameState('RECALL');
  };

  const handleTileClick = (index: number) => {
    if (gameState !== 'RECALL') return;
    
    // Check if tile is in pattern
    if (pattern.includes(index)) {
      if (!userPattern.includes(index)) {
        const newUserPattern = [...userPattern, index];
        setUserPattern(newUserPattern);
        
        // Check if round complete
        if (newUserPattern.length === pattern.length) {
          if (round >= totalRounds) {
            onComplete();
          } else {
            setGameState('WAITING');
            setTimeout(() => setRound(r => r + 1), 500);
          }
        }
      }
    } else {
      // Wrong tile -> Shake effect or reset round
      if (navigator.vibrate) navigator.vibrate(200);
      setUserPattern([]);
      setGameState('WAITING');
      setTimeout(generatePattern, 500); // Restart this round
    }
  };

  return (
    <div className="w-full max-w-sm flex flex-col items-center">
      <div className="flex justify-between items-center w-full mb-8 px-2">
        <span className="text-sm font-medium text-cyan-400 flex items-center gap-2">
          <Grid size={16}/> {t('mission_MEMORY')}
        </span>
        <div className="flex gap-1">
          {Array.from({length: totalRounds}).map((_, i) => (
             <div key={i} className={`w-3 h-3 rounded-full ${i < round - 1 ? 'bg-green-500' : (i === round - 1 ? 'bg-yellow-500 animate-pulse' : 'bg-gray-700')}`}></div>
          ))}
        </div>
      </div>

      <div className="text-white font-bold text-xl mb-6 text-center h-8">
        {gameState === 'SHOWING' ? t('memoryInstruction') : t('memoryRecall')}
      </div>

      <div className="grid grid-cols-3 gap-3 p-4 bg-gray-800/50 backdrop-blur rounded-2xl shadow-xl mb-8">
        {Array.from({ length: gridSize * gridSize }).map((_, i) => {
          let isActive = false;
          let isWrong = false; // Could implement visual error state
          if (gameState === 'SHOWING') {
            isActive = pattern.includes(i);
          } else if (gameState === 'RECALL') {
            isActive = userPattern.includes(i);
          }

          return (
            <button
              key={i}
              onClick={() => handleTileClick(i)}
              disabled={gameState === 'SHOWING'}
              className={`
                w-20 h-20 rounded-xl transition-all duration-200
                ${isActive 
                  ? 'bg-cyan-400 shadow-[0_0_15px_rgba(34,211,238,0.6)] scale-105' 
                  : (gameState === 'SHOWING' ? 'bg-gray-700 opacity-80' : 'bg-gray-700 hover:bg-gray-600 active:scale-95')
                }
              `}
            />
          );
        })}
      </div>

      {gameState === 'SHOWING' && (
          <button 
            onClick={handleReady}
            className="w-full py-4 bg-cyan-600 hover:bg-cyan-500 text-white rounded-xl font-bold text-lg shadow-lg active:scale-95 transition-all flex items-center justify-center gap-2"
          >
             <Check size={20} /> {t('memoryReady')}
          </button>
      )}
    </div>
  );
};