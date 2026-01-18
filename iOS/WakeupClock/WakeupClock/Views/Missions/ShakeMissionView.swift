//
//  ShakeMissionView.swift
//  WakeupClock
//
//  摇晃任务视图：用户需要快速点击按钮多次
//

import SwiftUI

struct ShakeMissionView: View {
    let difficulty: Difficulty
    let onComplete: () -> Void
    
    @State private var clickCount = 0
    
    private var targetClicks: Int {
        switch difficulty {
        case .easy: return 15
        case .medium: return 25
        case .hard: return 40
        }
    }
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 32) {
                Text(LocalizedString("crazyClick"))
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.orange)
                
                Text(LocalizedString("clickInstruction"))
                    .font(.system(size: 16))
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
                
                // 点击按钮
                Button(action: {
                    handleClick()
                }) {
                    Text(LocalizedString("clickMe"))
                        .font(.system(size: 24, weight: .black))
                        .foregroundColor(.white)
                        .frame(width: 200, height: 200)
                        .background(
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: [Color.red, Color.orange],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .shadow(color: .red.opacity(0.5), radius: 20)
                        )
                        .scaleEffect(clickCount % 2 == 0 ? 1.0 : 0.95)
                        .animation(.easeInOut(duration: 0.1), value: clickCount)
                }
                
                Text(String(format: LocalizedString("clicksLeft"), targetClicks - clickCount))
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.orange)
            }
            .padding()
        }
    }
    
    private func handleClick() {
        clickCount += 1
        
        // 震动反馈
        let generator = UIImpactFeedbackGenerator(style: .light)
        generator.impactOccurred()
        
        if clickCount >= targetClicks {
            onComplete()
        }
    }
}
