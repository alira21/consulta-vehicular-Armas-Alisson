import { useState } from 'react'
import './App.css'
import { consultarContribuyente, consultarVehiculo, consultarLicencia } from './api'

function Fuente({ valor }) {
  if (!valor) return null
  const esCache = valor === 'CACHE'
  return <span className={'fuente ' + (esCache ? 'cache' : 'vivo')}>{esCache ? 'caché' : 'en línea'}</span>
}

function Fila({ etiqueta, valor }) {
  return (
    <div className="fila">
      <span>{etiqueta}</span>
      <strong>{valor ?? '—'}</strong>
    </div>
  )
}

function Seccion({ titulo, fuente, error, children }) {
  return (
    <section className="seccion">
      <header>
        <h3>{titulo}</h3>
        <Fuente valor={fuente} />
      </header>
      {error ? <p className="error">{error}</p> : children}
    </section>
  )
}

export default function App() {
  const [form, setForm] = useState({ correo: '', ruc: '', placa: '' })
  const [res, setRes] = useState(null)
  const [error, setError] = useState('')
  const [cargando, setCargando] = useState(false)
  const [actualizando, setActualizando] = useState(false)

  const cambiar = (campo, valor) => setForm(f => ({ ...f, [campo]: valor }))

  // UN SOLO CLIC: SRI (contribuyente) -> luego vehículo + licencia en paralelo
  async function consultar(e) {
    e.preventDefault()
    setError('')
    const { correo, ruc, placa } = form

    if (!/^\S+@\S+\.\S+$/.test(correo)) return setError('Ingrese un correo válido')
    if (!/^\d{13}$/.test(ruc)) return setError('El RUC debe tener 13 dígitos')
    if (!placa.trim()) return setError('Ingrese la placa del vehículo')

    const cedula = ruc.substring(0, 10)
    const resultado = { cedula, placa, persona: null, vehiculo: null, licencia: null, errores: {} }

    setCargando(true)
    setRes(null)
    try {
      resultado.persona = await consultarContribuyente(ruc)

      const [v, l] = await Promise.allSettled([
        consultarVehiculo(placa),
        consultarLicencia(cedula, placa),
      ])
      if (v.status === 'fulfilled') resultado.vehiculo = v.value
      else resultado.errores.vehiculo = v.reason.message
      if (l.status === 'fulfilled') resultado.licencia = l.value
      else resultado.errores.licencia = l.reason.message
    } catch (err) {
      resultado.errores.persona = err.message
    }
    setRes(resultado)
    setCargando(false)
  }

  // Volver a consultar solo los puntos (para demostrar la caché)
  async function actualizarPuntos() {
    setActualizando(true)
    try {
      const l = await consultarLicencia(res.cedula, res.placa)
      setRes(r => ({ ...r, licencia: l, errores: { ...r.errores, licencia: null } }))
    } catch (err) {
      setRes(r => ({ ...r, errores: { ...r.errores, licencia: err.message } }))
    }
    setActualizando(false)
  }

  return (
    <div className="app">
      {/* -------- IZQUIERDA: consulta -------- */}
      <aside className="panel">
        <h1>Consulta vehicular</h1>
        <p className="sub">SRI · ANT</p>

        <form onSubmit={consultar}>
          <label>
            Correo electrónico
            <input type="email" value={form.correo} placeholder="nombre@correo.com"
                   onChange={e => cambiar('correo', e.target.value)} />
          </label>
          <label>
            RUC (persona natural)
            <input value={form.ruc} maxLength={13} placeholder="1712345678001"
                   onChange={e => cambiar('ruc', e.target.value.replace(/\D/g, ''))} />
          </label>
          <label>
            Placa del vehículo
            <input value={form.placa} maxLength={8} placeholder="PBX1234"
                   onChange={e => cambiar('placa', e.target.value.toUpperCase())} />
          </label>

          <button className="boton" disabled={cargando}>
            {cargando ? 'Consultando…' : 'Consultar'}
          </button>
          {error && <p className="error">{error}</p>}
        </form>
      </aside>

      {/* -------- DERECHA: resultados -------- */}
      <main className="resultados">
        {!res && !cargando && <p className="vacio">Ingrese los datos y presione Consultar.</p>}
        {cargando && <p className="vacio">Consultando SRI y ANT…</p>}

        {res && (
          <>
            <Seccion titulo="Contribuyente" fuente={res.persona?.fuente} error={res.errores.persona}>
              {res.persona && (
                <>
                  <Fila etiqueta="Nombre" valor={res.persona.razonSocial} />
                  <Fila etiqueta="RUC" valor={res.persona.ruc} />
                  <Fila etiqueta="Tipo" valor={res.persona.tipoContribuyente} />
                  <Fila etiqueta="Estado" valor={res.persona.estado} />
                  <Fila etiqueta="Actividad" valor={res.persona.actividadEconomica} />
                  <Fila etiqueta="Régimen" valor={res.persona.regimen} />
                  <Fila etiqueta="Correo" valor={form.correo} />
                </>
              )}
            </Seccion>

            {res.persona && (
              <>
                <Seccion titulo="Vehículo" fuente={res.vehiculo?.fuente} error={res.errores.vehiculo}>
                  {res.vehiculo && (
                    <>
                      <Fila etiqueta="Placa" valor={res.vehiculo.placa} />
                      <Fila etiqueta="Marca" valor={res.vehiculo.marca} />
                      <Fila etiqueta="Modelo" valor={res.vehiculo.modelo} />
                      <Fila etiqueta="Año" valor={res.vehiculo.anio} />
                      <Fila etiqueta="Clase" valor={res.vehiculo.clase} />
                      <Fila etiqueta="Color" valor={res.vehiculo.color} />
                      <Fila etiqueta="Cantón" valor={res.vehiculo.cantonMatricula} />
                    </>
                  )}
                </Seccion>

                <Seccion titulo="Licencia · ANT" fuente={res.licencia?.fuente} error={res.errores.licencia}>
                  {res.licencia && (
                    <>
                      <div className="puntos">{res.licencia.puntos}<small> / 30 puntos</small></div>
                      <Fila etiqueta="Citaciones" valor={res.licencia.totalCitaciones} />
                      <Fila etiqueta="Valor pendiente" valor={'$' + res.licencia.valorPendiente.toFixed(2)} />
                      <Fila etiqueta="Obtenido de la ANT"
                            valor={new Date(res.licencia.obtenidoEn).toLocaleString()} />
                      {res.licencia.fuente === 'CACHE' && (
                        <p className="nota">La ANT no respondió; se muestra la última información guardada.</p>
                      )}
                    </>
                  )}
                  <button className="enlace" onClick={actualizarPuntos} disabled={actualizando}>
                    {actualizando ? 'Actualizando…' : 'Actualizar puntos'}
                  </button>
                </Seccion>
              </>
            )}
          </>
        )}
      </main>
    </div>
  )
}