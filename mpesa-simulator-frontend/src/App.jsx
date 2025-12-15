import React, { useState, useEffect } from 'react'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

export default function App() {
  const [form, setForm] = useState({ phoneNumber: '', amount: '', purpose: '' })
  const [processing, setProcessing] = useState(false)
  const [message, setMessage] = useState('')
  const [txId, setTxId] = useState(null)
  const [status, setStatus] = useState(null)
  const [showPinModal, setShowPinModal] = useState(false)
  const [pin, setPin] = useState('')
  const [overlayVisible, setOverlayVisible] = useState(false)
  const [overlayText, setOverlayText] = useState('')

  useEffect(() => {
    let interval
    if (processing && txId) {
      interval = setInterval(async () => {
        try {
          const res = await fetch(`${API_BASE}/api/mpesa/status/${txId}`)
          const data = await res.json()
          if (data.status !== 'PROCESSING') {
            setStatus(data)
            setMessage(data.status === 'SUCCESS' ? 'Payment successful' : 'Payment failed')
            setOverlayText(data.status === 'SUCCESS' ? 'Success' : 'Failed')
            setProcessing(false)
            clearInterval(interval)
            // briefly show the result overlay
            setOverlayVisible(true)
            setTimeout(() => setOverlayVisible(false), 1500)
          }
        } catch (e) {
          // stop polling on error
          setProcessing(false)
          clearInterval(interval)
          setOverlayVisible(false)
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
    setOverlayVisible(false)
    setOverlayText('')

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
      // Open PIN modal instead of random background result
      setShowPinModal(true)
      setProcessing(false)
      setMessage('Enter your M-Pesa PIN to approve or cancel to simulate user abort.')
    } catch (err) {
      setMessage(err.message)
    }
  }

  const approve = async () => {
    if (!pin || !/^\d{4,6}$/.test(pin)) {
      alert('Enter a 4-6 digit PIN')
      return
    }
    setProcessing(true)
    setMessage('Processing...')
    setOverlayText('Processing')
    setOverlayVisible(true)
    setShowPinModal(false)
    try {
      const res = await fetch(`${API_BASE}/api/mpesa/approve/${txId}?pin=${encodeURIComponent(pin)}`, { method: 'POST' })
      if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        throw new Error(err.message || 'Approval failed. Please try again.')
      }
      const data = await res.json()
      // Immediately reflect SUCCESS in UI, and polling will confirm
      setStatus(data)
      if (data.status === 'SUCCESS') {
        setMessage('Payment successful')
        setOverlayText('Success')
        setProcessing(false)
        // keep overlay visible briefly
        setTimeout(() => setOverlayVisible(false), 1500)
      }
    } catch (e) {
      // Show error and let user try approving again without flipping to FAILED
      setProcessing(false)
      setOverlayVisible(false)
      setShowPinModal(true)
      alert(e.message)
    } finally {
      setPin('')
    }
  }

  const cancel = async () => {
    setProcessing(true)
    setMessage('Cancelling...')
    setOverlayText('Processing')
    setOverlayVisible(true)
    setShowPinModal(false)
    try {
      const res = await fetch(`${API_BASE}/api/mpesa/cancel/${txId}`, { method: 'POST' })
      const data = await res.json()
      setStatus(data)
      setMessage('Payment failed')
      setOverlayText('Failed')
      // briefly show the result overlay
      setTimeout(() => setOverlayVisible(false), 1500)
    } finally {
      setProcessing(false)
      setPin('')
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

      {overlayVisible && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.35)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', padding: 20, borderRadius: 8, minWidth: 200, textAlign: 'center', boxShadow: '0 6px 20px rgba(0,0,0,0.2)' }}>
            <div style={{ marginBottom: 8 }}>
              {overlayText === 'Processing' && (
                <span style={{ display: 'inline-block', width: 16, height: 16, border: '2px solid #999', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
              )}
            </div>
            <div style={{ fontWeight: 600, color: overlayText === 'Success' ? 'green' : overlayText === 'Failed' ? 'crimson' : '#333' }}>
              {overlayText}
            </div>
          </div>
          <style>
            {`@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }`}
          </style>
        </div>
      )}

      {status && (
        <div style={{ marginTop: 16 }}>
          <h3>Result</h3>
          <p>Status: <strong>{status.status}</strong></p>
          <p>Result Code: {status.resultCode ?? '-'}</p>
          <p>Result Description: {status.resultDesc ?? '-'}</p>
        </div>
      )}

      {showPinModal && (
        <div style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center'
        }}>
          <div style={{ background: '#fff', padding: 20, borderRadius: 8, width: 320 }}>
            <h3>Enter M-Pesa PIN</h3>
            <input
              type="password"
              value={pin}
              onChange={(e) => setPin(e.target.value)}
              placeholder="****"
              style={{ width: '100%', padding: 8, marginTop: 8 }}
            />
            <div style={{ display: 'flex', gap: 8, marginTop: 12, justifyContent: 'flex-end' }}>
              <button type="button" onClick={cancel}>Cancel</button>
              <button type="button" onClick={approve}>Approve</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
