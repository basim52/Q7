import { useEffect, useState } from 'react'
/** localStorage-backed state (on-device persistence, like DataStore in the Android app). */
export function useStored<T>(key: string, initial: T): [T, (v: T | ((p: T) => T)) => void] {
  const [v, setV] = useState<T>(() => {
    try { const s = localStorage.getItem('lf_' + key); return s ? (JSON.parse(s) as T) : initial } catch { return initial }
  })
  useEffect(() => { try { localStorage.setItem('lf_' + key, JSON.stringify(v)) } catch { /* quota */ } }, [key, v])
  return [v, setV]
}
