import { defineStore } from 'pinia'
import { ref } from 'vue'
import { recordApi } from '../api/records'
import type { ExpenseRecord, ExpenseRecordForm, PageResult } from '../types'

export const useRecordStore = defineStore('record', () => {
  const records = ref<ExpenseRecord[]>([])
  const total = ref(0)
  const pages = ref(0)

  async function fetchList(params: {
    categoryId?: number
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }) {
    const res = await recordApi.getList(params)
    const data = res.data as PageResult<ExpenseRecord>
    records.value = data.records
    total.value = data.total
    pages.value = data.pages
  }

  async function createRecord(form: ExpenseRecordForm) {
    await recordApi.create(form)
  }

  async function updateRecord(id: number, form: ExpenseRecordForm) {
    await recordApi.update(id, form)
  }

  async function deleteRecord(id: number) {
    await recordApi.delete(id)
  }

  return { records, total, pages, fetchList, createRecord, updateRecord, deleteRecord }
})
