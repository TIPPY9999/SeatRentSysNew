import { ref } from 'vue'
import Swal from 'sweetalert2'

/**
 * Google Maps 邏輯封裝
 * @param {Ref} formData - 表單資料的 ref 物件
 * @param {Object} manualOverride - 手動覆蓋座標的 reactive 狀態
 */
export function useGoogleMaps(formData, manualOverride) {
  // ========== 狀態定義 ==========
  const geoLoading = ref(false)
  const geoError = ref('')
  const geoPrecision = ref('') // 儲存 Google 回傳的精確度類型
  
  // Template Refs (需在元件 setup 中 return 給 template 綁定)
  const gmapAutoRef = ref(null)
  const addrElInputRef = ref(null)

  // ========== 輔助函式 ==========
  
  // 精確度顯示文字轉換
  const formatPrecision = (type) => {
    const map = { 
      'ROOFTOP': '精確 (Rooftop)', 
      'RANGE_INTERPOLATED': '街道插值 (Interpolated)', 
      'GEOMETRIC_CENTER': '區域中心 (Center)', 
      'APPROXIMATE': '粗略 (Approximate)', 
      'PLACES_API': 'Google 地點 (Place)', 
      'MANUAL': '手動修正 (Manual)' 
    }
    return map[type] || type
  }

  const getPrecisionTagType = (type) => {
    return (type === 'ROOFTOP' || type === 'PLACES_API' || type === 'MANUAL') ? 'success' : (type === 'RANGE_INTERPOLATED' ? 'warning' : 'info')
  }

  // 把 spotAddress「強制同步」回 ElementPlus 的原生 input
  const syncAddressToNativeInput = () => {
    const updateValue = () => {
      const comp = addrElInputRef.value
      let nativeInput = null

      // 1. 嘗試從 Element Plus 元件實例獲取 input
      if (comp) {
        // Element Plus 暴露的 input 屬性 (HTMLInputElement)
        if (comp.input && comp.input instanceof HTMLInputElement) {
          nativeInput = comp.input
        } 
        // 或者透過 DOM 查找
        else if (comp.$el) {
          nativeInput = comp.$el.querySelector('input')
        }
      }

      // 2. 最後手段：透過 placeholder 查找
      if (!nativeInput) {
        nativeInput = document.querySelector('input[placeholder="請輸入完整地址（可用建議清單）"]')
      }

      if (nativeInput) {
        const newValue = formData.value.spotAddress || ''
        // 只有當值不一致時才寫入
        if (nativeInput.value !== newValue) {
          nativeInput.value = newValue
          // 重要：觸發 input 事件，確保 Vue v-model 也能收到通知
          nativeInput.dispatchEvent(new Event('input', { bubbles: true }))
        }
      }
    }
    
    // 增加多次嘗試，確保在 Google Maps 初始化或 DOM 更新後能正確寫入
    setTimeout(updateValue, 100)
    setTimeout(updateValue, 300)
    setTimeout(updateValue, 800)
  }

  // ========== 核心邏輯 ==========
  // 初始化：把 Google Places Autocomplete 綁到 Element Plus 的原生 input
const initPlacesAutocomplete = async () => {
  // 注意：要在元件 mounted 後、DOM 出來後才抓得到 input
  const comp = addrElInputRef.value
  const nativeInput =
    comp?.input instanceof HTMLInputElement
      ? comp.input
      : comp?.$el?.querySelector?.('input')

  if (!nativeInput) return

  if (!window.google?.maps?.places?.Autocomplete) {
    geoError.value = 'Google Places 尚未載入（請確認載入 places library）'
    return
  }

  const ac = new window.google.maps.places.Autocomplete(nativeInput, {
    fields: ['name', 'geometry', 'formatted_address', 'place_id'],
    componentRestrictions: { country: 'tw' }
  })

  ac.addListener('place_changed', () => {
    const place = ac.getPlace()
    onPlaceChangedForForm(place)
  })
}



  // 地址 Autocomplete：選取後回填座標
  const onPlaceChangedForForm = (placeFromEvent) => {
    const place = placeFromEvent || gmapAutoRef.value?.getPlace?.()
    if (!place?.geometry?.location) return

    // 如果 Google Place 有名稱，且我們的「據點名稱」欄位是空的，就自動帶入
    if (place.name && !formData.value.spotName) {
      formData.value.spotName = place.name
    }

    if (place.formatted_address) {
      formData.value.spotAddress = place.formatted_address
      syncAddressToNativeInput()
    }

    const loc = place.geometry.location
    formData.value.latitude = Number(loc.lat())
    formData.value.longitude = Number(loc.lng())

    geoPrecision.value = 'PLACES_API'
    manualOverride.lat = false
    manualOverride.lng = false
    geoError.value = ''
  }

  // 文字地址 → Geocoder → 回填座標
  const geocodeAddress = ({ force } = { force: false }) => {
    const address = (formData.value.spotAddress || '').trim()
    if (!address) return

    if (!force && (manualOverride.lat || manualOverride.lng)) return

    if (!window.google?.maps?.Geocoder) {
      geoError.value = 'Google Maps 尚未載入（請確認已載入 places library）'
      return
    }

    geoLoading.value = true
    geoError.value = ''

    const geocoder = new window.google.maps.Geocoder()
    geocoder.geocode({ address }, (results, status) => {
      geoLoading.value = false

      if (status === 'OK' && results?.[0]) {
        const location = results[0].geometry.location
        const lat = Number(location.lat())
        const lng = Number(location.lng())

        geoPrecision.value = results[0].geometry.location_type
        if (force || !manualOverride.lat) formData.value.latitude = lat
        if (force || !manualOverride.lng) formData.value.longitude = lng
        return
      }

      geoError.value = `找不到座標，請輸入更完整地址（狀態：${status}）`
    })
  }

  // 依據點名稱搜尋座標
  const geocodeByName = () => {
    const name = (formData.value.spotName || '').trim()
    if (!name) {
      Swal.fire({ icon: 'warning', title: '請先輸入據點名稱', timer: 1500, showConfirmButton: false })
      return
    }
    
    geoLoading.value = true
    geoError.value = ''
    
    const geocoder = new window.google.maps.Geocoder()
    geocoder.geocode({ address: name, componentRestrictions: { country: 'TW' } }, (results, status) => {
      geoLoading.value = false
      if (status === 'OK' && results?.[0]) {
        const location = results[0].geometry.location
        formData.value.latitude = Number(location.lat())
        formData.value.longitude = Number(location.lng())
        formData.value.spotAddress = results[0].formatted_address
        syncAddressToNativeInput()
        
        geoPrecision.value = results[0].geometry.location_type
        manualOverride.lat = false
        manualOverride.lng = false
      } else {
        geoError.value = `找不到該名稱的位置（狀態：${status}）`
      }
    })
  }

  // 拖曳地圖標記結束後，更新座標
  const onMarkerDragEnd = (event) => {
    const lat = event.latLng.lat()
    const lng = event.latLng.lng()
    formData.value.latitude = Number(lat.toFixed(6))
    formData.value.longitude = Number(lng.toFixed(6))
    manualOverride.lat = true
    manualOverride.lng = true
    geoPrecision.value = 'MANUAL'
  }

  return {
    // State
    geoLoading,
    geoError,
    geoPrecision,
    gmapAutoRef,
    addrElInputRef,
    
    // Methods
     initPlacesAutocomplete,
    syncAddressToNativeInput,
    onPlaceChangedForForm,
    geocodeAddress,
    geocodeByName,
    onMarkerDragEnd,
    formatPrecision,
    getPrecisionTagType
  }
}
