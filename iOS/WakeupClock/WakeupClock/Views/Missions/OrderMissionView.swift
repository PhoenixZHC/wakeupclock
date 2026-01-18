//
//  OrderMissionView.swift
//  WakeupClock
//
//  顺序任务视图：用户需要按顺序点击数字
//

import SwiftUI

struct OrderMissionView: View {
    let difficulty: Difficulty
    let onComplete: () -> Void
    
    @State private var numbers: [Int] = []
    @State private var nextNumber = 1
    @State private var showError = false
    
    private var config: (count: Int, cols: Int) {
        switch difficulty {
        case .easy: return (9, 3)
        case .medium: return (12, 3)
        case .hard: return (16, 4)
        }
    }
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 16) {
                // 标题和进度
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "list.number")
                            .foregroundColor(.orange)
                        Text(LocalizedString("mission_ORDER"))
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.orange)
                    }
                    
                    Spacer()
                    
                    Text("\(nextNumber - 1) / \(config.count)")
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.white)
                }
                .padding(.horizontal)
                .padding(.top, 60) // 增加顶部间距，避开灵动岛和状态栏
                
                // 说明
                Text(String(format: LocalizedString("orderInstruction"), config.count))
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(showError ? .red : .white)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                    .padding(.horizontal)
                    .animation(.easeInOut, value: showError)
                
                Spacer()
                
                // 数字网格
                LazyVGrid(
                    columns: Array(repeating: GridItem(.flexible(), spacing: 16), count: config.cols),
                    spacing: 16
                ) {
                    ForEach(numbers, id: \.self) { number in
                        numberButton(number: number)
                    }
                }
                .padding(.horizontal, 24)
                .frame(maxWidth: .infinity)
                
                Spacer()
                
                if showError {
                    Text(LocalizedString("orderReset"))
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.red)
                        .padding(.bottom)
                }
            }
            .padding(.vertical)
        }
        .onAppear {
            generateNumbers()
        }
    }
    
    private func numberButton(number: Int) -> some View {
        let isClicked = number < nextNumber
        
        return Button(action: {
            handleNumberClick(number)
        }) {
            ZStack {
                // 外圈 - 拨号盘主体
                Circle()
                    .fill(
                        LinearGradient(
                            colors: isClicked ? 
                                [Color.green.opacity(0.7), Color.green.opacity(0.9)] :
                                [Color(hex: "F5F5F5"), Color(hex: "E0E0E0")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .overlay(
                        // 顶部高光
                        Circle()
                            .stroke(
                                LinearGradient(
                                    colors: [Color.white.opacity(0.4), Color.clear],
                                    startPoint: .top,
                                    endPoint: .center
                                ),
                                lineWidth: 2
                            )
                    )
                    .overlay(
                        // 底部阴影
                        Circle()
                            .stroke(
                                LinearGradient(
                                    colors: [Color.clear, Color.black.opacity(0.2)],
                                    startPoint: .center,
                                    endPoint: .bottom
                                ),
                                lineWidth: 2
                            )
                    )
                    .shadow(color: isClicked ? Color.green.opacity(0.6) : Color.black.opacity(0.4), radius: 6, x: 0, y: 3)
                
                // 数字
                Text("\(number)")
                    .font(.system(size: 32, weight: .bold, design: .rounded))
                    .foregroundColor(isClicked ? .white : Color(hex: "333333"))
            }
            .frame(maxWidth: .infinity)
            .aspectRatio(1, contentMode: .fit)
        }
        .buttonStyle(DialPadButtonStyle(isClicked: isClicked))
        .disabled(isClicked)
    }
    
    private func handleNumberClick(_ number: Int) {
        if number == nextNumber {
            nextNumber += 1
            showError = false
            
            if nextNumber > config.count {
                onComplete()
            }
        } else {
            // 错误 - 重置
            showError = true
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                nextNumber = 1
                showError = false
            }
        }
    }
    
    private func generateNumbers() {
        numbers = Array(1...config.count).shuffled()
        nextNumber = 1
        showError = false
    }
}

// MARK: - 拨号盘按钮样式

struct DialPadButtonStyle: ButtonStyle {
    let isClicked: Bool
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.9 : 1.0)
            .opacity(configuration.isPressed ? 0.8 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
    }
}
