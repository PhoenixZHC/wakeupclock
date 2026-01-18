//
//  HolidayChecker.swift
//  WakeupClock
//
//  节假日检查器：判断指定日期是否为法定节假日
//

import Foundation

/// 节假日检查器
struct HolidayChecker {
    /// 判断指定日期是否为法定节假日
    /// - Parameter date: 要检查的日期
    /// - Returns: 如果是节假日返回true，否则返回false
    static func isHoliday(_ date: Date) -> Bool {
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day, .weekday], from: date)
        
        guard let year = components.year,
              let month = components.month,
              let day = components.day else {
            return false
        }
        
        // 获取该年的节假日列表
        let holidays = getHolidaysForYear(year)
        
        // 检查是否是节假日
        return holidays.contains { holiday in
            holiday.month == month && holiday.day == day
        }
    }
    
    /// 获取指定年份的节假日列表
    /// 注意：这里使用固定的节假日列表，实际应用中可以从API获取或使用更完整的节假日数据
    private static func getHolidaysForYear(_ year: Int) -> [(month: Int, day: Int)] {
        var holidays: [(month: Int, day: Int)] = []
        
        // 固定节假日（公历）
        holidays.append((1, 1))   // 元旦
        holidays.append((5, 1))    // 劳动节
        holidays.append((10, 1))  // 国庆节（10月1-7日）
        holidays.append((10, 2))
        holidays.append((10, 3))
        holidays.append((10, 4))
        holidays.append((10, 5))
        holidays.append((10, 6))
        holidays.append((10, 7))
        
        // 春节（农历，这里简化处理，使用固定日期）
        // 实际应用中需要根据农历计算
        // 2024年春节：2月10日
        // 2025年春节：1月29日
        // 这里可以根据年份添加更多春节日期
        
        // 清明节（通常在4月4日或5日）
        holidays.append((4, 4))
        holidays.append((4, 5))
        
        // 端午节（农历，简化处理）
        // 中秋节（农历，简化处理）
        
        // TODO: 可以集成第三方API获取完整的节假日数据
        // 或者使用本地节假日数据库
        
        return holidays
    }
}
