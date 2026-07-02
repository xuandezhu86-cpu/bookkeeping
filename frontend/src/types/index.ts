export interface User {
  id: number
  username: string
  nickname: string
  email: string
  avatar: string
}

export interface Category {
  id: number
  name: string
  icon: string
  parentId: number | null
  sortOrder: number
  userId: number | null
}

export interface CategoryVo {
  id: number
  name: string
  icon: string
  sortOrder: number
  children: CategoryVo[]
}

export interface ExpenseRecord {
  id: number
  userId: number
  categoryId: number
  amount: number
  recordDate: string
  note: string
  createdAt: string
  updatedAt: string
}

export interface Budget {
  id: number
  userId: number
  categoryId: number | null
  month: string
  amount: number
}

export interface StatisticsVo {
  name: string
  amount: number
  percentage: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  pages: number
}

export interface LoginForm {
  username: string
  password: string
}

export interface RegisterForm {
  username: string
  password: string
  nickname: string
}

export interface ExpenseRecordForm {
  categoryId: number
  amount: number
  recordDate: string
  note?: string
}
