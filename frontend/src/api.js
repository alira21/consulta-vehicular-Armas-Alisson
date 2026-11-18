async function pedir(url) {
  const respuesta = await fetch(url)
  const datos = await respuesta.json().catch(() => ({}))
  if (!respuesta.ok) {
    throw new Error(datos.detail || `Error ${respuesta.status}`)
  }
  return datos
}

export const consultarContribuyente = (ruc) =>
  pedir(`/api/sri/contribuyentes/${ruc}`)

export const consultarVehiculo = (placa) =>
  pedir(`/api/sri/vehiculos/${encodeURIComponent(placa)}`)

export const consultarLicencia = (cedula, placa) =>
  pedir(`/api/ant/licencias/${cedula}?placa=${encodeURIComponent(placa)}`)