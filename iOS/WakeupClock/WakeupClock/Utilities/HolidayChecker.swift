//
//  HolidayChecker.swift
//  WakeupClock
//
//  节假日检查器：判断指定日期是否为法定节假日
//  使用 Holiday AILCC API 获取中国法定节假日数据
//

import Foundation

/// 节假日信息模型
struct HolidayInfo: Codable {
    let holiday: Bool       // 是否为节假日
    let name: String        // 节日名称
    let wage: Int           // 工资倍数
    let date: String        // 日期字符串
    let rest: Int?          // 距离天数（可选）
}

/// API 响应模型
struct HolidayAPIResponse: Codable {
    let code: Int
    let holiday: [String: HolidayInfo]?
}

/// 单日查询 API 响应模型
struct SingleDayAPIResponse: Codable {
    let code: Int
    let type: TypeInfo?
    let holiday: HolidayInfo?
    
    struct TypeInfo: Codable {
        let type: Int       // 0: 工作日, 1: 周末, 2: 节假日, 3: 调休上班
        let name: String    // 类型名称
        let week: Int       // 星期几
    }
}

/// 节假日检查器
/// 使用 Holiday AILCC API (https://holiday.ailcc.com) 获取中国法定节假日数据
class HolidayChecker {
    
    // MARK: - 单例
    static let shared = HolidayChecker()
    
    // MARK: - 属性
    
    /// 缓存的节假日数据，按年份存储
    private var holidayCache: [Int: [String: HolidayInfo]] = [:]
    
    /// 缓存的调休上班日数据
    private var workdayCache: [Int: Set<String>] = [:]
    
    /// 缓存过期时间（7天）
    private let cacheExpiration: TimeInterval = 7 * 24 * 60 * 60
    
    /// 上次更新时间
    private var lastUpdateTime: [Int: Date] = [:]
    
    /// API 基础 URL
    private let apiBaseURL = "https://holiday.ailcc.com/api/holiday"
    
    /// UserDefaults 键
    private let holidayCacheKey = "HolidayChecker_HolidayCache"
    private let workdayCacheKey = "HolidayChecker_WorkdayCache"
    private let lastUpdateKey = "HolidayChecker_LastUpdate"
    
    // MARK: - 初始化
    
    private init() {
        loadCacheFromDisk()
    }
    
    // MARK: - 公开方法
    
    /// 判断指定日期是否为法定节假日（同步方法，使用缓存）
    /// - Parameter date: 要检查的日期
    /// - Returns: 如果是节假日返回 true，否则返回 false
    static func isHoliday(_ date: Date) -> Bool {
        return shared.checkIsHoliday(date)
    }
    
    /// 判断指定日期是否为调休上班日（同步方法，使用缓存）
    /// - Parameter date: 要检查的日期
    /// - Returns: 如果是调休上班日返回 true，否则返回 false
    static func isWorkday(_ date: Date) -> Bool {
        return shared.checkIsWorkday(date)
    }
    
    /// 判断指定日期是否应该跳过闹钟（节假日且非调休上班日）
    /// - Parameter date: 要检查的日期
    /// - Returns: 如果应该跳过返回 true
    static func shouldSkipAlarm(_ date: Date) -> Bool {
        // 如果是调休上班日，不跳过
        if isWorkday(date) {
            return false
        }
        // 如果是节假日，跳过
        return isHoliday(date)
    }
    
    /// 异步获取指定年份的节假日数据
    /// - Parameters:
    ///   - year: 年份
    ///   - completion: 完成回调
    static func fetchHolidays(for year: Int, completion: @escaping (Bool) -> Void) {
        shared.fetchHolidaysFromAPI(year: year, completion: completion)
    }
    
    /// 预加载当前年份和下一年的节假日数据
    /// - Parameter forceRefresh: 是否强制刷新（忽略缓存）
    static func preloadHolidays(forceRefresh: Bool = false) {
        let currentYear = Calendar.current.component(.year, from: Date())
        
        // 只在缓存过期或强制刷新时才请求
        if forceRefresh || shared.shouldRefreshCache(for: currentYear) {
            fetchHolidays(for: currentYear) { _ in }
        }
        if forceRefresh || shared.shouldRefreshCache(for: currentYear + 1) {
            fetchHolidays(for: currentYear + 1) { _ in }
        }
    }
    
    // MARK: - 私有方法
    
    /// 检查是否为节假日（使用缓存）
    private func checkIsHoliday(_ date: Date) -> Bool {
        let calendar = Calendar.current
        let year = calendar.component(.year, from: date)
        let dateString = formatDate(date)
        
        // 检查缓存是否需要更新
        if shouldRefreshCache(for: year) {
            // 异步更新缓存，但当前查询使用本地备用数据
            fetchHolidaysFromAPI(year: year) { _ in }
        }
        
        // 优先使用缓存数据
        if let yearCache = holidayCache[year],
           let holidayInfo = yearCache[dateString] {
            return holidayInfo.holiday
        }
        
        // 如果没有缓存，使用本地备用数据
        return checkLocalHoliday(date)
    }
    
    /// 检查是否为调休上班日
    private func checkIsWorkday(_ date: Date) -> Bool {
        let calendar = Calendar.current
        let year = calendar.component(.year, from: date)
        let dateString = formatDate(date)
        
        if let workdays = workdayCache[year] {
            return workdays.contains(dateString)
        }
        
        return false
    }
    
    /// 格式化日期为 MM-dd 格式
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MM-dd"
        return formatter.string(from: date)
    }
    
    /// 判断缓存是否需要刷新
    private func shouldRefreshCache(for year: Int) -> Bool {
        guard let lastUpdate = lastUpdateTime[year] else {
            return true
        }
        return Date().timeIntervalSince(lastUpdate) > cacheExpiration
    }
    
    /// 从 API 获取节假日数据
    private func fetchHolidaysFromAPI(year: Int, completion: @escaping (Bool) -> Void) {
        let urlString = "\(apiBaseURL)/year/\(year)"
        
        #if DEBUG
        print("📅 HolidayChecker: 开始获取 \(year) 年节假日数据...")
        print("📅 HolidayChecker: 请求 URL: \(urlString)")
        #endif
        
        guard let url = URL(string: urlString) else {
            #if DEBUG
            print("❌ HolidayChecker: URL 无效")
            #endif
            completion(false)
            return
        }
        
        let task = URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
            guard let self = self,
                  let data = data,
                  error == nil else {
                #if DEBUG
                print("❌ HolidayChecker: 网络请求失败 - \(error?.localizedDescription ?? "未知错误")")
                #endif
                DispatchQueue.main.async {
                    completion(false)
                }
                return
            }
            
            #if DEBUG
            if let httpResponse = response as? HTTPURLResponse {
                print("📅 HolidayChecker: HTTP 状态码: \(httpResponse.statusCode)")
            }
            #endif
            
            do {
                let decoder = JSONDecoder()
                let response = try decoder.decode(HolidayAPIResponse.self, from: data)
                
                if response.code == 0, let holidays = response.holiday {
                    // 更新缓存
                    self.holidayCache[year] = holidays
                    self.lastUpdateTime[year] = Date()
                    
                    // 保存到磁盘
                    self.saveCacheToDisk()
                    
                    #if DEBUG
                    print("✅ HolidayChecker: 成功获取 \(year) 年节假日数据，共 \(holidays.count) 条记录")
                    #endif
                    
                    DispatchQueue.main.async {
                        completion(true)
                    }
                } else {
                    #if DEBUG
                    print("❌ HolidayChecker: API 返回错误码: \(response.code)")
                    #endif
                    DispatchQueue.main.async {
                        completion(false)
                    }
                }
            } catch {
                #if DEBUG
                print("❌ HolidayChecker: 解析节假日数据失败 - \(error)")
                #endif
                DispatchQueue.main.async {
                    completion(false)
                }
            }
        }
        
        task.resume()
    }
    
    /// 本地备用节假日检查（当 API 不可用时使用）
    private func checkLocalHoliday(_ date: Date) -> Bool {
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day], from: date)
        
        guard let year = components.year,
              let month = components.month,
              let day = components.day else {
            return false
        }
        
        // 固定节假日
        let fixedHolidays: [(month: Int, day: Int)] = [
            (1, 1),   // 元旦
            (5, 1),   // 劳动节
            (10, 1), (10, 2), (10, 3), (10, 4), (10, 5), (10, 6), (10, 7)  // 国庆节
        ]
        
        if fixedHolidays.contains(where: { $0.month == month && $0.day == day }) {
            return true
        }
        
        // 农历节日（根据年份的大致日期）
        let lunarHolidays = getLunarHolidays(for: year)
        return lunarHolidays.contains(where: { $0.month == month && $0.day == day })
    }
    
    /// 获取指定年份的农历节日大致日期
    private func getLunarHolidays(for year: Int) -> [(month: Int, day: Int)] {
        // 这里存储已知年份的农历节日日期
        // 数据来源：国务院公告
        switch year {
        case 2024:
            return [
                // 春节 2月10-17日
                (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17),
                // 清明节 4月4-6日
                (4, 4), (4, 5), (4, 6),
                // 端午节 6月8-10日
                (6, 8), (6, 9), (6, 10),
                // 中秋节 9月15-17日
                (9, 15), (9, 16), (9, 17)
            ]
        case 2025:
            return [
                // 春节 1月28日-2月4日
                (1, 28), (1, 29), (1, 30), (1, 31), (2, 1), (2, 2), (2, 3), (2, 4),
                // 清明节 4月4-6日
                (4, 4), (4, 5), (4, 6),
                // 端午节 5月31日-6月2日
                (5, 31), (6, 1), (6, 2),
                // 中秋节+国庆节 10月1-8日（已包含在固定节假日）
                (9, 29), (9, 30)  // 中秋节部分
            ]
        case 2026:
            return [
                // 春节 2月15-23日
                (2, 15), (2, 16), (2, 17), (2, 18), (2, 19), (2, 20), (2, 21), (2, 22), (2, 23),
                // 清明节 4月4-6日
                (4, 4), (4, 5), (4, 6),
                // 端午节 6月19-21日
                (6, 19), (6, 20), (6, 21),
                // 中秋节 9月25-27日
                (9, 25), (9, 26), (9, 27)
            ]
        default:
            // 默认清明节日期
            return [(4, 4), (4, 5)]
        }
    }
    
    // MARK: - 缓存持久化
    
    /// 保存缓存到磁盘
    private func saveCacheToDisk() {
        let defaults = UserDefaults.standard
        
        // 保存节假日缓存
        if let encoded = try? JSONEncoder().encode(holidayCache) {
            defaults.set(encoded, forKey: holidayCacheKey)
        }
        
        // 保存上次更新时间（将 Int key 转为 String，因为 UserDefaults 不支持 Int key）
        var updateTimeDict: [String: TimeInterval] = [:]
        for (year, date) in lastUpdateTime {
            updateTimeDict[String(year)] = date.timeIntervalSince1970
        }
        defaults.set(updateTimeDict, forKey: lastUpdateKey)
    }
    
    /// 从磁盘加载缓存
    private func loadCacheFromDisk() {
        let defaults = UserDefaults.standard
        
        // 加载节假日缓存
        if let data = defaults.data(forKey: holidayCacheKey),
           let decoded = try? JSONDecoder().decode([Int: [String: HolidayInfo]].self, from: data) {
            holidayCache = decoded
        }
        
        // 加载上次更新时间
        if let updateTimeDict = defaults.dictionary(forKey: lastUpdateKey) as? [String: TimeInterval] {
            lastUpdateTime = updateTimeDict.reduce(into: [:]) { result, pair in
                if let year = Int(pair.key) {
                    result[year] = Date(timeIntervalSince1970: pair.value)
                }
            }
        }
    }
    
    /// 清除缓存
    static func clearCache() {
        shared.holidayCache.removeAll()
        shared.workdayCache.removeAll()
        shared.lastUpdateTime.removeAll()
        
        let defaults = UserDefaults.standard
        defaults.removeObject(forKey: shared.holidayCacheKey)
        defaults.removeObject(forKey: shared.workdayCacheKey)
        defaults.removeObject(forKey: shared.lastUpdateKey)
    }
}
