//
//  TypingMissionView.swift
//  WakeupClock
//
//  打字任务视图：用户需要输入显示的文本
//

import SwiftUI

struct TypingMissionView: View {
    let difficulty: Difficulty
    let onComplete: () -> Void
    
    @State private var targetText = ""
    @State private var userInput = ""
    @State private var showError = false
    
    private let phrases = [
        "早起的鸟儿有虫吃",
        "新的一天开始了",
        "加油，你可以的",
        "Wake up and shine",
        "Good morning sunshine",
        "Time to start your day"
    ]
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 32) {
                Text(LocalizedString("typingInstruction"))
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(.white)
                
                // 目标文本
                Text(targetText)
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.green)
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.gray.opacity(0.2))
                    )
                
                // 输入框
                TextField(LocalizedString("typingPlaceholder"), text: $userInput)
                    .font(.system(size: 24, weight: .medium))
                    .foregroundColor(.white)
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(showError ? Color.red.opacity(0.2) : Color.gray.opacity(0.2))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(showError ? Color.red : Color.clear, lineWidth: 2)
                            )
                    )
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
                    .onChange(of: userInput) { oldValue, newValue in
                        checkInput()
                    }
                    .onSubmit {
                        checkComplete()
                    }
                
                if showError {
                    Text(LocalizedString("typingError"))
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.red)
                }
                
                // 确认按钮
                Button(action: {
                    checkComplete()
                }) {
                    Text(LocalizedString("confirm"))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color.pink)
                        )
                }
            }
            .padding()
        }
        .onAppear {
            generateText()
        }
    }
    
    private func generateText() {
        targetText = phrases.randomElement() ?? phrases[0]
        userInput = ""
        showError = false
    }
    
    private func checkInput() {
        if userInput.count > 0 && !targetText.lowercased().hasPrefix(userInput.lowercased()) {
            showError = true
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
        } else {
            showError = false
        }
    }
    
    private func checkComplete() {
        if userInput.lowercased() == targetText.lowercased() {
            onComplete()
        } else {
            showError = true
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
        }
    }
}
