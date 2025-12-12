import React, { useState, useEffect } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

export default function App() {
  const [form, setForm] = useState({ phoneNumber: '', amount: '', purpose: '' })
  const [processing, setProcessing] = useState(false)
  const [message, setMessage] = useState('')
  const [txId, setTxId] = useState(null)
  const [status, setStatus] = useState(null)

  useEffect(() => {
    let interval
    if (processing && txId) {
      interval = setInterval(async () => {
        const res = await fetch(`${API_BASE}/api/mpesa/status/${txId}`)
        const data = await res.json()
        if (data.status !== 'PROCESSING') {
          setStatus(data)
          setProcessing(false)
          clearInterval(interval)
        }
      }, 1000)
    }
    return () => interval && clearInterval(interval)
  }, [processing, txId])

  const onChange = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
  }

  const onSubmit = async (e) => {
    e.preventDefault()
    setStatus(null)
    setMessage('')

    const body = {
      phoneNumber: form.phoneNumber,
      amount: parseFloat(form.amount),
      purpose: form.purpose,
    }

    try {
      const res = await fetch(`${API_BASE}/api/mpesa/initiate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        throw new Error(err.message || 'Failed to initiate payment')
      }
      const data = await res.json()
      setTxId(data.transactionId)
      setProcessing(true)
      setMessage('Processing... Please complete the STK on your phone (simulated).')
    } catch (err) {
      setMessage(err.message)
    }
  }

  return (
    <div style={{ maxWidth: 520, margin: '40px auto', fontFamily: 'system-ui, sans-serif' }}>
      <h1>M-Pesa Payment Integration Simulator</h1>
      <form onSubmit={onSubmit} style={{ display: 'grid', gap: 12 }}>
        <label>
          Phone Number
          <input
            name="phoneNumber"
            placeholder="07XXXXXXXX or +2547XXXXXXXX"
            value={form.phoneNumber}
            onChange={onChange}
            required
            style={{ width: '100%', padding: 8 }}
          />
        </label>
        <label>
          Amount
          <input
            name="amount"
            type="number"
            min="1"
            step="0.01"
            value={form.amount}
            onChange={onChange}
            required
            style={{ width: '100%', padding: 8 }}
          />
        </label>
        <label>
          Payment Purpose
          <input
            name="purpose"
            value={form.purpose}
            onChange={onChange}
            required
            style={{ width: '100%', padding: 8 }}
          />
        </label>
        <button type="submit" disabled={processing} style={{ padding: '10px 16px' }}>
          {processing ? 'Processing...' : 'Pay Now'}
        </button>
      </form>

      {message && <p style={{ marginTop: 16 }}>{message}</p>}

      {status && (
        <div style={{ marginTop: 16 }}>
          <h3>Result</h3>
          <p>Status: <strong>{status.status}</strong></p>
          <p>Result Code: {status.resultCode ?? '-'}</p>
          <p>Result Description: {status.resultDesc ?? '-'}</p>
        </div>
      )}
    </div>
  )
}
