export function toggleDark(isDark: boolean) {
  if (isDark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
  localStorage.setItem('dark-mode', String(isDark))
}

export function initTheme() {
  const isDark = localStorage.getItem('dark-mode') === 'true'
  if (isDark) {
    document.documentElement.classList.add('dark')
  }
  return isDark
}
