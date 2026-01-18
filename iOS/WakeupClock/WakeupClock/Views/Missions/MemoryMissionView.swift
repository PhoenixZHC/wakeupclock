//
//  MemoryMissionView.swift
//  WakeupClock
//
//  记忆任务视图：用户需要记住并点击发光的方块
//

import SwiftUI

struct MemoryMissionView: View {
    let difficulty: Difficulty
    let onComplete: () -> Void
    
    @State private var pattern: [Int] = []
    @State private var userPattern: [Int] = []
    @State private var gameState: GameState = .waiting
    @State private var round = 1
    
    private let gridSize = 3
    private let totalRounds = 3
    private var tilesToMemorize: Int {
        switch difficulty {
        case .easy: return 3
        case .medium: return 5
        case .hard: return 7
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
                
                // 状态提示
                Text(stateText)
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.white)
                    .frame(height: 24)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                
                Spacer()
                
                // 网格
                gridView
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, 32)
                
                Spacer()
                
                // 准备按钮
                if gameState == .showing {
                    readyButton
                        .padding(.horizontal, 32)
                        .padding(.bottom, 32)
                } else {
                    Spacer()
                        .frame(height: 20)
                }
            }
        }
        .onAppear {
            generatePattern()
        }
    }
    
    private var headerView: some View {
        HStack {
            HStack(spacing: 8) {
                Image(systemName: "square.grid.3x3")
                    .foregroundColor(.cyan)
                Text(LocalizedString("mission_MEMORY"))
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.cyan)
            }
            
            Spacer()
            
            // 进度点
            HStack(spacing: 8) {
                ForEach(0..<totalRounds, id: \.self) { index in
                    Circle()
                        .fill(index < round - 1 ? Color.green : (index == round - 1 ? Color.yellow : Color.gray.opacity(0.3)))
                        .frame(width: 8, height: 8)
                }
            }
        }
    }
    
    private var stateText: String {
        switch gameState {
        case .showing:
            return LocalizedString("memoryInstruction")
        case .recall:
            return LocalizedString("memoryRecall")
        case .waiting:
            return ""
        }
    }
    
    private var gridView: some View {
        LazyVGrid(
            columns: Array(repeating: GridItem(.flexible(), spacing: 16), count: gridSize),
            spacing: 16
        ) {
            ForEach(0..<(gridSize * gridSize), id: \.self) { index in
                tileButton(index: index)
            }
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color.gray.opacity(0.15))
        )
    }
    
    private func tileButton(index: Int) -> some View {
        let isActive = gameState == .showing ? pattern.contains(index) : userPattern.contains(index)
        let isClickable = gameState == .recall
        
        return Button(action: {
            if isClickable {
                handleTileClick(index)
            }
        }) {
            RoundedRectangle(cornerRadius: 16)
                .fill(isActive ? Color.cyan : Color.gray.opacity(0.3))
                .aspectRatio(1, contentMode: .fit)
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(isActive ? Color.cyan.opacity(0.8) : Color.clear, lineWidth: 3)
                )
                .shadow(
                    color: isActive ? Color.cyan.opacity(0.6) : Color.clear,
                    radius: isActive ? 15 : 0,
                    x: 0,
                    y: 0
                )
        }
        .buttonStyle(MemoryTileButtonStyle(isActive: isActive))
        .disabled(!isClickable)
    }
    
    private var readyButton: some View {
        Button(action: {
            gameState = .recall
        }) {
            HStack(spacing: 8) {
                Image(systemName: "checkmark")
                Text(LocalizedString("memoryReady"))
            }
            .font(.system(size: 18, weight: .bold))
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color.cyan)
            )
        }
    }
    
    // MARK: - 方法
    
    private func generatePattern() {
        var newPattern: [Int] = []
        while newPattern.count < tilesToMemorize {
            let index = Int.random(in: 0..<(gridSize * gridSize))
            if !newPattern.contains(index) {
                newPattern.append(index)
            }
        }
        pattern = newPattern
        userPattern = []
        gameState = .showing
    }
    
    private func handleTileClick(_ index: Int) {
        guard gameState == .recall else { return }
        
        if pattern.contains(index) {
            // 正确
            if !userPattern.contains(index) {
                userPattern.append(index)
                
                // 检查是否完成
                if userPattern.count == pattern.count {
                    if round >= totalRounds {
                        onComplete()
                    } else {
                        gameState = .waiting
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                            round += 1
                            generatePattern()
                        }
                    }
                }
            }
        } else {
            // 错误 - 重置
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
            
            userPattern = []
            gameState = .waiting
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                generatePattern()
            }
        }
    }
}

enum GameState {
    case waiting
    case showing
    case recall
}

// MARK: - 记忆方块按钮样式

struct MemoryTileButtonStyle: ButtonStyle {
    let isActive: Bool
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .opacity(configuration.isPressed ? 0.8 : 1.0)
            .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
    }
}
