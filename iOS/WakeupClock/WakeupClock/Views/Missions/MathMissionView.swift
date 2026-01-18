//
//  MathMissionView.swift
//  WakeupClock
//
//  数学任务视图：用户需要解答数学题才能关闭闹钟
//

import SwiftUI

struct MathMissionView: View {
    let difficulty: Difficulty
    let onComplete: () -> Void
    
    @State private var problems: [MathProblem] = []
    @State private var currentProblemIndex = 0
    @State private var currentInput = ""
    @State private var showError = false
    @State private var solvedCount = 0
    
    private var config: MissionConfig {
        switch difficulty {
        case .easy:
            return MissionConfig(questions: 1, range: 20, operation: .add)
        case .medium:
            return MissionConfig(questions: 3, range: 15, operation: .multiplyAdd)
        case .hard:
            return MissionConfig(questions: 5, range: 50, operation: .complex)
        }
    }
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 16) {
                // 标题和进度
                headerView
                    .padding(.horizontal)
                    .padding(.top, 60) // 增加顶部间距，避开灵动岛和状态栏
                
                Spacer()
                
                // 题目显示
                problemView
                    .padding(.horizontal, 24)
                
                Spacer()
                
                // 数字键盘
                numberPadView
                    .padding(.horizontal, 24)
                    .padding(.bottom, 32)
            }
        }
        .onAppear {
            generateProblems()
        }
    }
    
    private var headerView: some View {
        HStack {
            HStack(spacing: 8) {
                Image(systemName: "brain.head.profile")
                    .foregroundColor(.purple)
                Text(LocalizedString("mathMission"))
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.purple)
            }
            
            Spacer()
            
            // 进度点
            HStack(spacing: 8) {
                ForEach(0..<config.questions, id: \.self) { index in
                    Circle()
                        .fill(index < solvedCount ? Color.green : Color.gray.opacity(0.3))
                        .frame(width: 8, height: 8)
                }
            }
        }
    }
    
    private var problemView: some View {
        VStack(spacing: 20) {
            if currentProblemIndex < problems.count {
                let problem = problems[currentProblemIndex]
                
                Text(problem.text)
                    .font(.system(size: 56, weight: .bold, design: .monospaced))
                    .foregroundColor(.white)
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                
                // 换题按钮
                Button(action: {
                    generateNewProblem()
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 12))
                        Text(LocalizedString("changeQuestion"))
                            .font(.system(size: 12))
                    }
                    .foregroundColor(.gray)
                }
                
                // 输入显示
                HStack {
                    Text(currentInput.isEmpty ? "0" : currentInput)
                        .font(.system(size: 42, weight: .bold, design: .monospaced))
                        .foregroundColor(.purple)
                    
                    Rectangle()
                        .fill(Color.purple)
                        .frame(width: 2, height: 36)
                        .opacity(0.8)
                }
                .frame(height: 56)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color.gray.opacity(0.2))
                )
                
                if showError {
                    Text(LocalizedString("wrongAnswer"))
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.red)
                        .transition(.opacity)
                }
            }
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(showError ? Color.red.opacity(0.2) : Color.gray.opacity(0.1))
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(showError ? Color.red : Color.clear, lineWidth: 2)
                )
        )
    }
    
    private var numberPadView: some View {
        VStack(spacing: 16) {
            ForEach(0..<3) { row in
                HStack(spacing: 16) {
                    ForEach(1..<4) { col in
                        let num = row * 3 + col
                        numberButton(num: num)
                    }
                }
            }
            
            HStack(spacing: 16) {
                // 清除按钮
                Button(action: {
                    currentInput = ""
                    showError = false
                }) {
                    Text(LocalizedString("clear"))
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .frame(height: 70)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(Color.red.opacity(0.2))
                        )
                }
                
                // 0按钮
                numberButton(num: 0)
                
                // 确认按钮
                Button(action: {
                    checkAnswer()
                }) {
                    Text(LocalizedString("confirm"))
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 70)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(Color.green)
                        )
                }
            }
        }
    }
    
    private func numberButton(num: Int) -> some View {
        Button(action: {
            if currentInput.count < 5 {
                currentInput += "\(num)"
                showError = false
            }
        }) {
            Text("\(num)")
                .font(.system(size: 28, weight: .bold))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 70)
                .background(
                    RoundedRectangle(cornerRadius: 14)
                        .fill(Color.gray.opacity(0.3))
                )
        }
    }
    
    // MARK: - 方法
    
    private func generateProblems() {
        problems = (0..<config.questions).map { _ in
            generateProblem()
        }
        currentProblemIndex = 0
        solvedCount = 0
        currentInput = ""
    }
    
    private func generateProblem() -> MathProblem {
        let range = config.range
        let a = Int.random(in: 2...range)
        let b = Int.random(in: 2...range)
        let c = Int.random(in: 1...10)
        
        switch config.operation {
        case .add:
            return MathProblem(text: "\(a) + \(b) = ?", answer: a + b)
        case .multiplyAdd:
            return MathProblem(text: "\(a) × \(b) + \(c) = ?", answer: a * b + c)
        case .complex:
            return MathProblem(text: "(\(a) + \(b)) × \(c) = ?", answer: (a + b) * c)
        }
    }
    
    private func generateNewProblem() {
        if currentProblemIndex < problems.count {
            problems[currentProblemIndex] = generateProblem()
            currentInput = ""
            showError = false
        }
    }
    
    private func checkAnswer() {
        guard currentProblemIndex < problems.count else { return }
        
        let problem = problems[currentProblemIndex]
        guard let answer = Int(currentInput), answer == problem.answer else {
            // 答案错误
            showError = true
            currentInput = ""
            
            // 震动反馈
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
            
            return
        }
        
        // 答案正确
        solvedCount += 1
        
        if solvedCount >= config.questions {
            // 所有题目完成
            onComplete()
        } else {
            // 下一题
            currentProblemIndex += 1
            currentInput = ""
            showError = false
        }
    }
}

// MARK: - 辅助结构

struct MathProblem {
    let text: String
    let answer: Int
}

enum MathOperation {
    case add
    case multiplyAdd
    case complex
}

struct MissionConfig {
    let questions: Int
    let range: Int
    let operation: MathOperation
}
