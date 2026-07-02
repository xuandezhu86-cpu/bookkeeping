import api from './index'

export const statisticsApi = {
  getMonthlyTotal(year: string, month: string) {
    return api.get('/statistics/monthly-total', { params: { year, month } })
  },
  getYearlyTotal(year: string) {
    return api.get('/statistics/yearly-total', { params: { year } })
  },
  getCategorySummary(startDate: string, endDate: string) {
    return api.get('/statistics/category-summary', { params: { startDate, endDate } })
  },
  getDailyTrend(year: string, month: string) {
    return api.get('/statistics/daily-trend', { params: { year, month } })
  },
}
