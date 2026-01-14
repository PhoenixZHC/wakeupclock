import React, { useState } from 'react';
import { Difficulty } from '../types';
import { MISSION_CONFIG } from '../constants';
import { Brain, RefreshCw } from 'lucide-react';
import { useLanguage } from '../i18n';

interface Props {
  difficulty: Difficulty;
  onComplete: () => void;
}

interface Problem {
  text: string;
  answer: number;
}

export const MathMission: React.FC<Props> = ({ difficulty, onComplete }) => {
  const config = MISSION_CONFIG[difficulty];
  const [solvedCount, setSolvedCount] = useState(0);
  const [currentInput, setCurrentInput] = useState('');
  const [error, setError] = useState(false);
  const { t } = useLanguage();

  const generateProblem = (): Problem => {
    const range = config.range;
    const a = Math.floor(Math.random() * range) + 2;
    const b = Math.floor(Math.random() * range) + 2;
    const c = Math.floor(Math.random() * 10) + 1;

    if (config.ops === 'ADD') {
      return { text: `${a} + ${b} = ?`, answer: a + b };
    } else if (config.ops === 'MULT_ADD') {
      return { text: `${a} × ${b} + ${c} = ?`, answer: a * b + c };
    } else {
        // Complex
      return { text: `(${a} + ${b}) × ${c} = ?`, answer: (a + b) * c };
    }
  };

  // We use a ref or state that updates only when solved to hold the current problem
  const [problem, setProblem] = useState<Problem>(generateProblem());

  const handleNumPress = (num: string) => {
    setError(false);
    if (currentInput.length < 5) {
      setCurrentInput(prev => prev + num);
    }
  };

  const handleClear = () => setCurrentInput('');
  
  const handleSkip = () => {
      setProblem(generateProblem());
      setCurrentInput('');
      setError(false);
  };
  
  const handleSubmit = () => {
    const val = parseInt(currentInput);
    if (val === problem.answer) {
      const newCount = solvedCount + 1;
      if (newCount >= config.questions) {
        onComplete();
      } else {
        setSolvedCount(newCount);
        setCurrentInput('');
        setProblem(generateProblem());
      }
    } else {
      setError(true);
      setCurrentInput('');
      // Vibrate if on mobile
      if (navigator.vibrate) navigator.vibrate(200);
    }
  };

  return (
    <div className="w-full max-w-sm">
      {/* Progress */}
      <div className="flex justify-between items-center mb-8 px-2">
        <span className="text-sm font-medium text-purple-400 flex items-center gap-2">
          <Brain size={16}/> {t('mathMission')}
        </span>
        <div className="flex gap-1">
          {Array.from({length: config.questions}).map((_, i) => (
             <div key={i} className={`w-3 h-3 rounded-full ${i < solvedCount ? 'bg-green-500' : 'bg-gray-700'}`}></div>
          ))}
        </div>
      </div>

      {/* Display */}
      <div className={`
        bg-gray-800/80 backdrop-blur rounded-2xl p-8 mb-8 text-center border-2 transition-colors duration-200 shadow-xl
        ${error ? 'border-red-500 bg-red-900/20' : 'border-gray-700'}
      `}>
        <div className="text-4xl font-bold text-white mb-2 tracking-wide font-mono">{problem.text}</div>
        
        {/* Skip Button */}
        <button 
            onClick={handleSkip}
            className="mb-6 text-xs font-medium text-gray-400 hover:text-indigo-300 transition-colors flex items-center justify-center gap-1 mx-auto active:scale-95"
        >
            <RefreshCw size={12} /> {t('changeQuestion')}
        </button>

        <div className="h-16 text-4xl font-mono text-purple-400 border-b-2 border-gray-600 flex items-center justify-center">
          {currentInput}
          <span className="animate-pulse w-1 h-8 bg-purple-400 ml-1"></span>
        </div>
        {error && <div className="text-red-500 text-sm mt-2 font-bold animate-bounce">{t('wrongAnswer')}</div>}
      </div>

      {/* Keypad */}
      <div className="grid grid-cols-3 gap-4">
        {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(num => (
          <button
            key={num}
            onClick={() => handleNumPress(num.toString())}
            className="h-16 rounded-2xl bg-gray-800 text-white text-2xl font-bold hover:bg-gray-700 active:bg-gray-600 transition-colors shadow-lg border-b-4 border-gray-900 active:border-b-0 active:translate-y-1"
          >
            {num}
          </button>
        ))}
        <button
          onClick={handleClear}
          className="h-16 rounded-2xl bg-red-900/40 text-red-500 text-xl font-bold hover:bg-red-900/60 border-b-4 border-red-900/60 active:border-b-0 active:translate-y-1"
        >
          {t('clear')}
        </button>
        <button
          onClick={() => handleNumPress('0')}
          className="h-16 rounded-2xl bg-gray-800 text-white text-2xl font-bold hover:bg-gray-700 border-b-4 border-gray-900 active:border-b-0 active:translate-y-1"
        >
          0
        </button>
        <button
          onClick={handleSubmit}
          className="h-16 rounded-2xl bg-white text-indigo-900 text-xl font-bold hover:bg-gray-100 border-b-4 border-gray-400 active:border-b-0 active:translate-y-1"
        >
          {t('confirm')}
        </button>
      </div>
    </div>
  );
};